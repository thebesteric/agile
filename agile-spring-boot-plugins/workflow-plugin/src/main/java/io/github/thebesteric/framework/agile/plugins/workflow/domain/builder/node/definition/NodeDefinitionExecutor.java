package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.definition;

import io.github.thebesteric.framework.agile.commons.exception.DataExistsException;
import io.github.thebesteric.framework.agile.commons.util.JsonUtils;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.ApproveType;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.NodeType;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.Approver;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.assignment.NodeAssignmentBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.assignment.NodeAssignmentExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.assignment.NodeAssignmentExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.NodeAssignment;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.NodeDefinition;
import io.vavr.control.Try;
import lombok.Getter;
import lombok.Setter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
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
    private final NodeAssignmentExecutor nodeAssignmentExecutor;

    public NodeDefinitionExecutor(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
        this.nodeDefinition = new NodeDefinition();
        nodeAssignmentExecutor = new NodeAssignmentExecutor(jdbcTemplate);
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
        String insertSql = """
                INSERT INTO awf_node_definition (`tenant_id`, `wf_def_id`, `name`, `node_type`, `approve_type`, `conditions`, `sequence`, `created_at`, `created_by`, `desc`, `state`, `version`)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        // 审批类型
        ApproveType approveType = nodeDefinition.getApproveType();

        KeyHolder keyHolder = new GeneratedKeyHolder();
        int rowsAffected = this.jdbcTemplate.update(
                conn -> {
                    AtomicInteger index = new AtomicInteger(0);
                    PreparedStatement ps = conn.prepareStatement(insertSql, new String[]{"id"});
                    ps.setString(index.incrementAndGet(), nodeDefinition.getTenantId());
                    ps.setInt(index.incrementAndGet(), nodeDefinition.getWorkflowDefinitionId());
                    ps.setString(index.incrementAndGet(), nodeDefinition.getName());
                    ps.setInt(index.incrementAndGet(), nodeDefinition.getNodeType().getCode());
                    ps.setInt(index.incrementAndGet(), approveType.getCode());
                    ps.setString(index.incrementAndGet(), JsonUtils.toJson(nodeDefinition.getConditions()));
                    ps.setInt(index.incrementAndGet(), nodeDefinition.getSequence());
                    ps.setString(index.incrementAndGet(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(nodeDefinition.getCreatedAt()));
                    ps.setString(index.incrementAndGet(), nodeDefinition.getCreatedBy());
                    ps.setString(index.incrementAndGet(), nodeDefinition.getDesc());
                    ps.setInt(index.incrementAndGet(), 1);
                    ps.setInt(index.incrementAndGet(), nodeDefinition.getVersion());
                    return ps;
                }, keyHolder);

        if (rowsAffected > 0) {
            nodeDefinition.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());

            // 设置节点审批人
            NodeAssignmentBuilder nodeAssignmentBuilder = NodeAssignmentBuilder.builder(nodeDefinition.getTenantId(), nodeDefinition.getId());
            NodeAssignmentExecutorBuilder assignmentExecutorBuilder = NodeAssignmentExecutorBuilder.builder(jdbcTemplate);
            for (Approver approver : nodeDefinition.getApprovers()) {
                NodeAssignment nodeAssignment = nodeAssignmentBuilder.userId(approveType, approver.getId(), approver.getDesc()).build();
                NodeAssignmentExecutor assignmentExecutor = assignmentExecutorBuilder.nodeAssignment(nodeAssignment).build();
                assignmentExecutor.save();
            }
            nodeAssignmentBuilder.resetSeq();
        } else {
            throw new DataIntegrityViolationException("Failed to insert data, no rows affected.");
        }
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
        return Try.of(() -> this.jdbcTemplate.queryForObject(selectSql, (rs, rowNum) -> NodeDefinition.of(rs), workflowDefinitionId, name, tenantId)).getOrNull();
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
        return getById(nodeDefinition.getTenantId(), nodeDefinition.getId());
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
        return Try.of(() -> this.jdbcTemplate.queryForObject(selectSql, (rs, rowNum) -> NodeDefinition.of(rs), id, tenantId)).getOrNull();
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
                SELECT * FROM awf_node_definition WHERE `wf_def_id` = ? AND `state` = 1 AND `tenant_id` = ?
                """;
        RowMapper<NodeDefinition> rowMapper = (ResultSet rs, int rowNum) -> NodeDefinition.of(rs);
        return jdbcTemplate.query(selectSql, rowMapper, workflowDefinitionId, tenantId).stream().toList();
    }

    /**
     * 根据流程定义 ID 和 sequence 获取节点定义集合
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 节点定义 ID
     * @param sequence             排序值
     *
     * @return List<NodeDefinition>
     *
     * @author wangweijun
     * @since 2024/7/4 13:24
     */
    public List<NodeDefinition> findByWorkflowDefinitionIdAndSequence(String tenantId, Integer workflowDefinitionId, Integer sequence) {
        final String selectSql = """
                SELECT * FROM awf_node_definition WHERE `wf_def_id` = ? AND `state` = 1 AND `tenant_id` = ? AND `sequence` = ?
                """;
        RowMapper<NodeDefinition> rowMapper = (ResultSet rs, int rowNum) -> NodeDefinition.of(rs);
        return jdbcTemplate.query(selectSql, rowMapper, workflowDefinitionId, tenantId, sequence).stream().toList();
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
        return Try.of(() -> this.jdbcTemplate.queryForObject(selectSql, (rs, rowNum) -> NodeDefinition.of(rs), workflowDefinitionId, tenantId)).getOrNull();
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
        return Try.of(() -> this.jdbcTemplate.queryForObject(selectSql, (rs, rowNum) -> NodeDefinition.of(rs), workflowDefinitionId, tenantId)).getOrNull();
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
                SELECT * FROM awf_node_definition WHERE `wf_def_id` = ? AND `node_type` = %s AND `tenant_id` = ?
                """.formatted(NodeType.TASK.getCode());
        RowMapper<NodeDefinition> rowMapper = (ResultSet rs, int rowNum) -> NodeDefinition.of(rs);
        return jdbcTemplate.query(selectSql, rowMapper, workflowDefinitionId, tenantId).stream().toList();
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
            List<NodeAssignment> nodeAssignments = nodeAssignmentExecutor.findByNodeDefinitionId(tenantId, toNodeDefinition.getId());
            Set<Approver> approvers = nodeAssignments.stream().map(assignment -> Approver.of(assignment.getUserId(), assignment.getDesc())).collect(Collectors.toSet());
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
}
