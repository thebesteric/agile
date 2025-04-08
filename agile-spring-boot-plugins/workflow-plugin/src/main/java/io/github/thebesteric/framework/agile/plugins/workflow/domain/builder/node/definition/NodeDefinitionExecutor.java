package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.definition;

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
            throw new WorkflowException("已存在相同的节点定义");
        }
        // 创建节点定义
        nodeDefinition = super.save(nodeDefinition);

        WorkflowDefinitionExecutor workflowDefinitionExecutor = new WorkflowDefinitionExecutor(jdbcTemplate);
        Integer workflowDefinitionId = nodeDefinition.getWorkflowDefinitionId();
        WorkflowDefinition workflowDefinition = workflowDefinitionExecutor.getById(workflowDefinitionId);

        // 获取节点审批人
        Set<Approver> approvers = nodeDefinition.getApprovers();
        Set<RoleApprover> roleApprovers = nodeDefinition.getRoleApprovers();
        NodeType nodeType = nodeDefinition.getNodeType();

        // 节点定义不是开始或结束节点，非自动审批条件下，审批人为空的情况
        if (nodeType != NodeType.START && nodeType != NodeType.END && !workflowDefinition.isAllowEmptyAutoApprove()) {
            // 默认审批人为空
            if (workflowDefinition.getWhenEmptyApprover() == null) {
                if ((nodeDefinition.isUserApprove() && approvers.isEmpty()) || (nodeDefinition.isRoleApprove() && roleApprovers.isEmpty())) {
                    throw new WorkflowException("非自动审批条件下，审批人不能为空");
                }
            }

            // 用户审批节点，设置审批人为流程定义的默认审批人
            if (nodeDefinition.isUserApprove() && approvers.isEmpty()) {
                approvers = Set.of(workflowDefinition.getWhenEmptyApprover());
            }
        }

        // 角色审批
        if (nodeDefinition.isRoleApprove()) {
            // 保存角色审批人
            this.saveRoleApprovers(nodeDefinition, roleApprovers);
            // 将 approvers 设置为角色用户
            for (RoleApprover roleApprover : roleApprovers) {
                approvers.add(Approver.of(roleApprover.getRoleId(), roleApprover.getRoleName(), roleApprover.getRoleDesc(), true));
            }
        }

        // 保存节点审批人
        ApproverIdType approverIdType = nodeDefinition.isRoleApprove() ? ApproverIdType.ROLE : ApproverIdType.USER;
        ApproveType approveType = nodeDefinition.getApproveType();
        RoleApproveType roleApproveType = nodeDefinition.getRoleApproveType();
        this.saveApprovers(nodeDefinition, approverIdType, approveType, roleApproveType, approvers);

        return nodeDefinition;
    }

    /**
     * 保存节点审批人
     *
     * @param nodeDefinition  节点定义
     * @param approverIdType  审批人 ID 类型
     * @param approveType     审批类型
     * @param roleApproveType 角色审批类型
     * @param approvers       审批人
     *
     * @author wangweijun
     * @since 2024/11/28 15:24
     */
    public void saveApprovers(NodeDefinition nodeDefinition, ApproverIdType approverIdType, ApproveType approveType, RoleApproveType roleApproveType, Set<Approver> approvers) {
        NodeAssignmentBuilder nodeAssignmentBuilder = NodeAssignmentBuilder.builder(nodeDefinition.getTenantId(), nodeDefinition.getId());
        NodeAssignmentExecutorBuilder assignmentExecutorBuilder = NodeAssignmentExecutorBuilder.builder(jdbcTemplate);
        NodeAssignmentExecutor assignmentExecutor = assignmentExecutorBuilder.build();
        if (approvers == null || approvers.isEmpty()) {
            // 是否是动态审批节点
            if (nodeDefinition.isDynamic()) {
                String dynamicApproverId = WorkflowConstants.DYNAMIC_ASSIGNMENT_APPROVER_VALUE.formatted(nodeDefinition.getDynamicAssignmentNum());
                NodeAssignment dynamicNodeAssignment = nodeAssignmentBuilder.approverInfo(approverIdType, approveType, roleApproveType, dynamicApproverId, null, null).build();
                assignmentExecutor.saveNodeAssignment(dynamicNodeAssignment);
            }
            return;
        }
        for (Approver approver : approvers) {
            NodeAssignment nodeAssignment = nodeAssignmentBuilder.approverInfo(approverIdType, approveType, roleApproveType, approver.getId(), approver.getName(), approver.getDesc()).build();
            assignmentExecutor.saveNodeAssignment(nodeAssignment);
        }
        nodeAssignmentBuilder.resetSeq();
    }

    /**
     * 保存角色审批人
     *
     * @param nodeDefinition 节点定义
     * @param roleApprovers  角色审批人
     *
     * @author wangweijun
     * @since 2024/11/28 15:25
     */
    public void saveRoleApprovers(NodeDefinition nodeDefinition, Set<RoleApprover> roleApprovers) {
        if (roleApprovers == null || roleApprovers.isEmpty()) {
            return;
        }
        // 记录角色用户对应关系
        NodeRoleAssignmentExecutorBuilder nodeRoleAssignmentExecutorBuilder = NodeRoleAssignmentExecutorBuilder.builder(jdbcTemplate);
        NodeRoleAssignmentExecutor nodeRoleUserAssignmentExecutor = nodeRoleAssignmentExecutorBuilder.build();

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
                        .userInfo(userSeqValue, roleApprover.getUserId(), roleApprover.getUserName(), roleApprover.getUserDesc())
                        .roleInfo(roleSeqValue, roleApprover.getRoleId(), roleApprover.getRoleName(), roleApprover.getRoleDesc())
                        .build();
                nodeRoleUserAssignmentExecutor.saveNodeRoleAssignment(nodeRoleAssignment);
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
            NodeDefinition nodeDef = NodeDefinition.of(rs);
            setApproverAndRoleApprovers(tenantId, nodeDef);
            return nodeDef;
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
            NodeDefinition nodeDef = NodeDefinition.of(rs);
            setApproverAndRoleApprovers(tenantId, nodeDef);
            return nodeDef;
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
        setApproverAndRoleApprovers(tenantId, nodeDefinitions);
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
            NodeDefinition nodeDef = NodeDefinition.of(rs);
            setApproverAndRoleApprovers(tenantId, nodeDef);
            return nodeDef;
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
            NodeDefinition nodeDef = NodeDefinition.of(rs);
            setApproverAndRoleApprovers(tenantId, nodeDef);
            return nodeDef;
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
        setApproverAndRoleApprovers(tenantId, nodeDefinitions);
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
        setApproverAndRoleApprovers(tenantId, toNodeDefinitions);
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
        NodeDefinition curNodeDefinition = this.getById(nodeDefinitionId);
        boolean roleApprove = curNodeDefinition.isRoleApprove();
        return nodeAssignmentExecutor.findByNodeDefinitionId(tenantId, nodeDefinitionId)
                .stream().map(nodeAssignment -> Approver.of(nodeAssignment, roleApprove)).collect(Collectors.toSet());
    }

    /**
     * 根据节点定义 ID 查找节点角色审批人
     *
     * @param tenantId         租户 ID
     * @param nodeDefinitionId 节点定义 ID
     *
     * @return Set<RoleApprover>
     *
     * @author wangweijun
     * @since 2024/10/12 13:49
     */
    public Set<RoleApprover> findRoleApproversByNodeDefinitionId(String tenantId, Integer nodeDefinitionId) {
        NodeRoleAssignmentExecutor nodeRoleAssignmentExecutor = new NodeRoleAssignmentExecutor(jdbcTemplate);
        return nodeRoleAssignmentExecutor.findByNodeDefinitionId(tenantId, nodeDefinitionId, NodeRoleAssignmentType.NORMAL)
                .stream().map(RoleApprover::of).collect(Collectors.toSet());
    }

    /**
     * 根据流程定义 ID 和排序顺序查找节点定义集合
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     * @param sequence             排序顺序
     *
     * @return List<NodeDefinition>
     *
     * @author wangweijun
     * @since 2024/10/24 21:14
     */
    public List<NodeDefinition> findBySequence(String tenantId, Integer workflowDefinitionId, Double sequence) {
        final String selectSql = """
                SELECT * FROM awf_node_definition WHERE `state` = 1 AND `tenant_id` = ? AND `wf_def_id` = ? AND `sequence` = ?
                """;
        RowMapper<NodeDefinition> rowMapper = (ResultSet rs, int rowNum) -> NodeDefinition.of(rs);
        List<NodeDefinition> nodeDefinitions = jdbcTemplate.query(selectSql, rowMapper, tenantId, workflowDefinitionId, sequence).stream().toList();
        // 设置审批人集合
        setApproverAndRoleApprovers(tenantId, nodeDefinitions);
        return nodeDefinitions;
    }

    /**
     * 设置审批人集合
     *
     * @param tenantId        租户 ID
     * @param nodeDefinitions 节点定义集合
     *
     * @author wangweijun
     * @since 2024/10/24 21:10
     */
    private void setApproverAndRoleApprovers(String tenantId, List<NodeDefinition> nodeDefinitions) {
        for (NodeDefinition nodeDef : nodeDefinitions) {
            this.setApproverAndRoleApprovers(tenantId, nodeDef);
        }
    }

    /**
     * 设置审批人集合
     *
     * @param tenantId       租户 ID
     * @param nodeDefinition 节点定义
     *
     * @author wangweijun
     * @since 2024/10/24 21:10
     */
    private void setApproverAndRoleApprovers(String tenantId, NodeDefinition nodeDefinition) {
        Set<Approver> approvers = findApproversByNodeDefinitionId(tenantId, nodeDefinition.getId());
        nodeDefinition.setApprovers(approvers);
        Set<RoleApprover> roleApprovers = findRoleApproversByNodeDefinitionId(tenantId, nodeDefinition.getId());
        nodeDefinition.setRoleApprovers(roleApprovers);
    }
}
