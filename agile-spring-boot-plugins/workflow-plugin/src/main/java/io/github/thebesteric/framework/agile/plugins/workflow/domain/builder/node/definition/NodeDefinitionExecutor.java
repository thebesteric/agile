package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.definition;

import io.github.thebesteric.framework.agile.commons.exception.DataExistsException;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.*;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.Approver;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.RoleApprover;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.assignment.*;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.definition.WorkflowDefinitionExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.NodeAssignment;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.NodeDefinition;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.NodeRoleAssignment;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowDefinition;
import io.github.thebesteric.framework.agile.plugins.workflow.exception.WorkflowException;
import io.vavr.control.Try;
import lombok.Getter;
import lombok.Setter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.sql.ResultSet;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * NodeDefinitionExecutor
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-18 15:59:00
 */
@Getter
@Setter
public class NodeDefinitionExecutor extends AbstractExecutor<NodeDefinition> {

    private NodeDefinition nodeDefinition;

    public NodeDefinitionExecutor(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
        this.nodeDefinition = new NodeDefinition();
    }

    /**
     * 保存节点定义
     *
     * @author wangweijun
     * @since 2024/6/18 16:25
     */
    public NodeDefinition save() {
        // 检查是否有已存在的节点定义
        NodeDefinition existsNodeDefinition = this.getByWorkflowDefinitionIdAndName();
        if (existsNodeDefinition != null) {
            throw new DataExistsException("已存在相同的节点定义");
        }
        // 创建节点定义
        nodeDefinition = super.save(nodeDefinition);

        WorkflowDefinitionExecutor workflowDefinitionExecutor = new WorkflowDefinitionExecutor(jdbcTemplate);
        Integer workflowDefinitionId = nodeDefinition.getWorkflowDefinitionId();
        WorkflowDefinition workflowDefinition = workflowDefinitionExecutor.getById(workflowDefinitionId);

        // 获取节点审批人
        Set<Approver> approvers = nodeDefinition.getApprovers();
        NodeType nodeType = nodeDefinition.getNodeType();

        // 节点定义不是开始或结束节点，非自动审批条件下，审批人为空的情况
        if (nodeType != NodeType.START && nodeType != NodeType.END && !workflowDefinition.isAllowEmptyAutoApprove() && approvers.isEmpty()) {
            // 默认审批人为空
            if (workflowDefinition.getWhenEmptyApprovers().isEmpty()) {
                throw new WorkflowException("非自动审批条件下，审批人不能为空");
            }
            // 不是角色审批节点，设置审批人为流程定义的默认审批人
            if (!nodeDefinition.isRoleApprove()) {
                approvers = workflowDefinition.getWhenEmptyApprovers();
            }
        }

        // 角色审批
        if (nodeDefinition.isRoleApprove()) {
            // 记录角色用户对应关系
            Set<RoleApprover> roleApprovers = nodeDefinition.getRoleApprovers();
            NodeRoleAssignmentExecutorBuilder nodeRoleAssignmentExecutorBuilder = NodeRoleAssignmentExecutorBuilder.builder(jdbcTemplate);

            // 按角色分组并保存
            Map<String, List<RoleApprover>> approversByRoleId = roleApprovers.stream().collect(Collectors.groupingBy(RoleApprover::getRoleId));

            for (Map.Entry<String, List<RoleApprover>> entry : approversByRoleId.entrySet()) {
                List<RoleApprover> list = entry.getValue();
                NodeRoleAssignmentBuilder nodeRoleAssignmentBuilder = NodeRoleAssignmentBuilder.builder(nodeDefinition.getTenantId(), nodeDefinition.getId());
                AtomicInteger userSeq = NodeRoleAssignmentBuilder.userSeq;
                AtomicInteger roleSeq = NodeRoleAssignmentBuilder.roleSeq;
                // 角色的顺序
                int roleSeqValue = roleSeq.getAndIncrement();
                for (RoleApprover roleApprover : list) {
                    // 角色用户的顺序
                    int userSeqValue = userSeq.getAndIncrement();
                    NodeRoleAssignment nodeRoleAssignment = nodeRoleAssignmentBuilder
                            .userId(userSeqValue, roleApprover.getUserId(), roleApprover.getUserDesc())
                            .roleId(roleSeqValue, roleApprover.getRoleId(), roleApprover.getRoleDesc())
                            .build();
                    NodeRoleAssignmentExecutor nodeRoleUserAssignmentExecutor = nodeRoleAssignmentExecutorBuilder.nodeRoleUserAssignment(nodeRoleAssignment).build();
                    nodeRoleUserAssignmentExecutor.save();
                }
                // 如果角色审批是：ANY，则需要保证每个角色用户都有自己的顺序
                if (RoleApproveType.ANY == nodeDefinition.getRoleApproveType()) {
                    NodeRoleAssignmentBuilder.resetUserSeq();
                    NodeRoleAssignmentBuilder.resetRoleSeq();
                }
                // 如果角色审批是：ALL，则需要保证所有角色用户都有统一的顺序
                else if (RoleApproveType.ALL == nodeDefinition.getRoleApproveType()) {
                    NodeRoleAssignmentBuilder.resetRoleSeq();
                }
                // 如果角色审批是：SEQ，则需要保证每个角色和每个角色用户都有自己的顺序
                else if (RoleApproveType.SEQ == nodeDefinition.getRoleApproveType()) {
                    NodeRoleAssignmentBuilder.resetUserSeq();
                }
            }
            NodeRoleAssignmentBuilder.resetSeq();


            // 将 approvers 设置为角色用户
            approvers = roleApprovers.stream().map(roleApprover -> Approver.of(roleApprover.getRoleId(), roleApprover.getRoleDesc())).collect(Collectors.toSet());
        }

        // 设置节点审批人
        ApproverIdType approverIdType = nodeDefinition.isRoleApprove() ? ApproverIdType.ROLE : ApproverIdType.USER;
        ApproveType approveType = nodeDefinition.getApproveType();
        NodeAssignmentBuilder nodeAssignmentBuilder = NodeAssignmentBuilder.builder(nodeDefinition.getTenantId(), nodeDefinition.getId());
        NodeAssignmentExecutorBuilder assignmentExecutorBuilder = NodeAssignmentExecutorBuilder.builder(jdbcTemplate);
        for (Approver approver : approvers) {
            NodeAssignment nodeAssignment = nodeAssignmentBuilder.approverId(approverIdType, approveType, approver.getId(), approver.getDesc()).build();
            NodeAssignmentExecutor assignmentExecutor = assignmentExecutorBuilder.nodeAssignment(nodeAssignment).build();
            assignmentExecutor.save();
        }
        nodeAssignmentBuilder.resetSeq();

        return nodeDefinition;
    }

    /**
     * 更新节点定义
     *
     * @author wangweijun
     * @since 2024/6/18 17:34
     */
    public int update() {
        String updateSql = """
                UPDATE awf_node_definition 
                SET `name` = :name, `type` = %s, `desc` = :desc, `updated_at` = :updatedAt, `updated_by` = :updatedBy, `version` = `version` + 1 
                WHERE `id` = :id and `tenant_id` = :tenant_id
                """.formatted(nodeDefinition.getNodeType().getCode());
        nodeDefinition.setUpdatedAt(new Date());
        nodeDefinition.setUpdatedBy(nodeDefinition.getUpdatedBy());
        SqlParameterSource parameters = new BeanPropertySqlParameterSource(nodeDefinition);
        return new NamedParameterJdbcTemplate(jdbcTemplate).update(updateSql, parameters);
    }

    /**
     * 根据流程定义 ID 和 节点定义 name 获取节点定义
     *
     * @return WorkflowDefinition
     *
     * @author wangweijun
     * @since 2024/6/17 15:23
     */
    public NodeDefinition getByWorkflowDefinitionIdAndName() {
        return getByWorkflowDefinitionIdAndName(nodeDefinition.getTenantId(), nodeDefinition.getWorkflowDefinitionId(), nodeDefinition.getName());
    }

    /**
     * 根据流程定义 ID 和 节点定义 name 获取节点定义
     *
     * @return WorkflowDefinition
     *
     * @author wangweijun
     * @since 2024/6/17 15:23
     */
    public NodeDefinition getByWorkflowDefinitionIdAndName(String tenantId, Integer workflowDefinitionId, String name) {
        final String selectSql = """
                SELECT * FROM awf_node_definition WHERE `wf_def_id` = ? AND `name` = ? AND `state` = 1 AND `tenant_id` = ?
                """;
        return Try.of(() -> this.jdbcTemplate.queryForObject(selectSql, (rs, rowNum) -> {
            NodeDefinition nd = NodeDefinition.of(rs);
            Set<Approver> approvers = findApproversByNodeDefinitionId(tenantId, nd.getId());
            nd.setApprovers(approvers);
            return nd;
        }, workflowDefinitionId, name, tenantId)).getOrNull();
    }

    /**
     * 根据节点定义 ID 获取节点定义
     *
     * @return WorkflowDefinition
     *
     * @author wangweijun
     * @since 2024/6/18 17:44
     */
    public NodeDefinition getById() {
        return this.getById(nodeDefinition.getTenantId(), nodeDefinition.getId());
    }

    /**
     * 根据节点定义 ID 获取节点定义
     *
     * @return WorkflowDefinition
     *
     * @author wangweijun
     * @since 2024/6/18 17:44
     */
    public NodeDefinition getById(String tenantId, Integer id) {
        final String selectSql = """
                SELECT * FROM awf_node_definition WHERE `id` = ? AND `tenant_id` = ?
                """;
        return Try.of(() -> this.jdbcTemplate.queryForObject(selectSql, (rs, rowNum) -> {
            NodeDefinition nd = NodeDefinition.of(rs);
            Set<Approver> approvers = findApproversByNodeDefinitionId(tenantId, nd.getId());
            nd.setApprovers(approvers);
            return nd;
        }, id, tenantId)).getOrNull();
    }

    /**
     * 根据流程定义 ID 获取节点定义集合
     *
     * @return List<NodeDefinition>
     *
     * @author wangweijun
     * @since 2024/6/19 15:55
     */
    public List<NodeDefinition> findByWorkflowDefinitionId() {
        return findByWorkflowDefinitionId(nodeDefinition.getTenantId(), nodeDefinition.getWorkflowDefinitionId());
    }

    /**
     * 根据流程定义 ID 获取节点定义集合
     *
     * @return List<NodeDefinition>
     *
     * @author wangweijun
     * @since 2024/6/19 15:55
     */
    public List<NodeDefinition> findByWorkflowDefinitionId(String tenantId, Integer workflowDefinitionId) {
        final String selectSql = """
                SELECT * FROM awf_node_definition WHERE `wf_def_id` = ? AND `state` = 1 AND `tenant_id` = ? order by `sequence` ASC
                """;
        RowMapper<NodeDefinition> rowMapper = (ResultSet rs, int rowNum) -> NodeDefinition.of(rs);
        List<NodeDefinition> nodeDefinitions = jdbcTemplate.query(selectSql, rowMapper, workflowDefinitionId, tenantId).stream().toList();
        nodeDefinitions.forEach(nd -> {
            Set<Approver> approvers = findApproversByNodeDefinitionId(tenantId, nd.getId());
            nd.setApprovers(approvers);
        });
        return nodeDefinitions;
    }

    /**
     * 获取指定流程定义下的开始节点
     *
     * @return NodeDefinition
     *
     * @author wangweijun
     * @since 2024/6/19 16:02
     */
    public NodeDefinition getStartNode() {
        return getStartNode(nodeDefinition.getTenantId(), nodeDefinition.getWorkflowDefinitionId());
    }

    /**
     * 获取指定流程定义下的开始节点
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     *
     * @return NodeDefinition
     *
     * @author wangweijun
     * @since 2024/6/19 16:02
     */
    public NodeDefinition getStartNode(String tenantId, Integer workflowDefinitionId) {
        final String selectSql = """
                SELECT * FROM awf_node_definition WHERE `wf_def_id` = ? AND `node_type` = %s AND `tenant_id` = ?
                """.formatted(NodeType.START.getCode());
        return Try.of(() -> this.jdbcTemplate.queryForObject(selectSql, (rs, rowNum) -> {
            NodeDefinition nd = NodeDefinition.of(rs);
            Set<Approver> approvers = findApproversByNodeDefinitionId(tenantId, nd.getId());
            nd.setApprovers(approvers);
            return nd;
        }, workflowDefinitionId, tenantId)).getOrNull();
    }

    /**
     * 获取指定流程定义下的结束节点
     *
     * @return NodeDefinition
     *
     * @author wangweijun
     * @since 2024/6/19 16:02
     */
    public NodeDefinition getEndNode() {
        return getEndNode(nodeDefinition.getTenantId(), nodeDefinition.getWorkflowDefinitionId());
    }

    /**
     * 获取指定流程定义下的结束节点
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     *
     * @return NodeDefinition
     *
     * @author wangweijun
     * @since 2024/6/19 16:02
     */
    public NodeDefinition getEndNode(String tenantId, Integer workflowDefinitionId) {
        final String selectSql = """
                SELECT * FROM awf_node_definition WHERE `wf_def_id` = ? AND `node_type` = %s AND `tenant_id` = ?
                """.formatted(NodeType.END.getCode());
        return Try.of(() -> this.jdbcTemplate.queryForObject(selectSql, (rs, rowNum) -> {
            NodeDefinition nd = NodeDefinition.of(rs);
            Set<Approver> approvers = findApproversByNodeDefinitionId(tenantId, nd.getId());
            nd.setApprovers(approvers);
            return nd;
        }, workflowDefinitionId, tenantId)).getOrNull();
    }

    /**
     * 获取指定流程定义下的结束节点结合
     *
     * @return List<NodeDefinition>
     *
     * @author wangweijun
     * @since 2024/6/19 16:02
     */
    public List<NodeDefinition> findTaskNodes() {
        return findTaskNodes(nodeDefinition.getTenantId(), nodeDefinition.getWorkflowDefinitionId());
    }

    /**
     * 获取指定流程定义下的结束节点集合
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     *
     * @return List<NodeDefinition>
     *
     * @author wangweijun
     * @since 2024/6/19 16:02
     */
    public List<NodeDefinition> findTaskNodes(String tenantId, Integer workflowDefinitionId) {
        final String selectSql = """
                SELECT * FROM awf_node_definition WHERE `wf_def_id` = ? AND `node_type` = %s AND `tenant_id` = ? ORDER BY `sequence` ASC
                """.formatted(NodeType.TASK.getCode());
        RowMapper<NodeDefinition> rowMapper = (ResultSet rs, int rowNum) -> NodeDefinition.of(rs);
        List<NodeDefinition> nodeDefinitions = jdbcTemplate.query(selectSql, rowMapper, workflowDefinitionId, tenantId).stream().toList();
        nodeDefinitions.forEach(nd -> {
            Set<Approver> approvers = findApproversByNodeDefinitionId(tenantId, nd.getId());
            nd.setApprovers(approvers);
        });
        return nodeDefinitions;
    }

    /**
     * 根据来自节点 ID 查找目标节点集合
     *
     * @param tenantId             租户 ID
     * @param fromNodeDefinitionId 来自节点 ID
     *
     * @return List<NodeDefinition>
     *
     * @author wangweijun
     * @since 2024/6/24 16:00
     */
    public List<NodeDefinition> findToTaskNodesByFromNodeId(String tenantId, Integer fromNodeDefinitionId) {
        final String selectSql = """
                SELECT * FROM awf_node_definition WHERE `state` = 1 AND `id` IN (
                    SELECT `to_node_id` FROM awf_node_relation WHERE `active` = 1 AND `from_node_id` = ? AND `tenant_id` = ?
                )
                """;
        RowMapper<NodeDefinition> rowMapper = (ResultSet rs, int rowNum) -> NodeDefinition.of(rs);
        List<NodeDefinition> toNodeDefinitions = jdbcTemplate.query(selectSql, rowMapper, fromNodeDefinitionId, tenantId).stream().toList();
        // 设置审批人集合
        for (NodeDefinition toNodeDefinition : toNodeDefinitions) {
            Set<Approver> approvers = findApproversByNodeDefinitionId(tenantId, toNodeDefinition.getId());
            toNodeDefinition.setApprovers(approvers);
        }
        return toNodeDefinitions;
    }

    /**
     * 删除节点定义
     *
     * @author wangweijun
     * @since 2024/6/19 09:52
     */
    public boolean deleteById() {
        return deleteById(nodeDefinition.getTenantId(), nodeDefinition.getId());
    }

    /**
     * 删除节点定义
     *
     * @author wangweijun
     * @since 2024/6/19 09:52
     */
    public boolean deleteById(String tenantId, Integer id) {
        String deleteSql = """
                DELETE FROM awf_node_definition WHERE `id` = ? AND `tenant_id` = ?
                """;
        return this.jdbcTemplate.update(deleteSql, id, tenantId) > 0;
    }

    /**
     * 根据节点定义 ID 查找节点审批人
     *
     * @param tenantId         租户 ID
     * @param nodeDefinitionId 节点定义 ID
     *
     * @return Set<Approver>
     *
     * @author wangweijun
     * @since 2024/9/6 14:45
     */
    public Set<Approver> findApproversByNodeDefinitionId(String tenantId, Integer nodeDefinitionId) {
        NodeAssignmentExecutor nodeAssignmentExecutor = new NodeAssignmentExecutor(jdbcTemplate);
        return nodeAssignmentExecutor.findByNodeDefinitionId(tenantId, nodeDefinitionId)
                .stream().map(assignment -> Approver.of(assignment.getApproverId(), assignment.getDesc())).collect(Collectors.toSet());
    }
}
