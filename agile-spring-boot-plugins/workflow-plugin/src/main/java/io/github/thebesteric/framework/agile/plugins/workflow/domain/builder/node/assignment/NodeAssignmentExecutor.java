package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.assignment;

import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.NodeAssignment;
import io.github.thebesteric.framework.agile.plugins.workflow.exception.WorkflowException;
import io.vavr.control.Try;
import lombok.Getter;
import lombok.Setter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.util.List;

/**
 * NodeAssignmentExecutor
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-20 09:39:00
 */
@Getter
@Setter
public class NodeAssignmentExecutor extends AbstractExecutor<NodeAssignment> {

    private NodeAssignment nodeAssignment;

    public NodeAssignmentExecutor(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
        this.nodeAssignment = new NodeAssignment();
    }

    /**
     * 保存节点定义
     *
     * @author wangweijun
     * @since 2024/6/18 16:25
     */
    public NodeAssignment saveNodeAssignment(NodeAssignment nodeAssignment) {
        // 检查是否有已存在的节点定义
        NodeAssignment existsNodeAssignment = this.getByNodeDefinitionIdAndApproverId();
        if (existsNodeAssignment != null) {
            throw new WorkflowException("已存在相同的节点审批人");
        }
        return super.save(nodeAssignment);
    }

    /**
     * 根据节点定义 ID 和用户 ID 获取审批人
     *
     * @return NodeAssignment
     *
     * @author wangweijun
     * @since 2024/6/20 09:56
     */
    public NodeAssignment getByNodeDefinitionIdAndApproverId() {
        return this.getByNodeDefinitionIdAndApproverId(nodeAssignment.getTenantId(), nodeAssignment.getNodeDefinitionId(), nodeAssignment.getApproverId());
    }

    /**
     * 根据节点定义 ID 和用户 ID 获取审批人
     *
     * @param tenantId         租户 ID
     * @param nodeDefinitionId 节点定义 ID
     * @param approverId       审批人 ID
     *
     * @return NodeAssignment
     *
     * @author wangweijun
     * @since 2024/6/20 09:58
     */
    public NodeAssignment getByNodeDefinitionIdAndApproverId(String tenantId, Integer nodeDefinitionId, String approverId) {
        final String selectSql = """
                SELECT * FROM awf_node_assignment WHERE `tenant_id` = ? AND `node_def_id` = ? AND `approver_id` = ? AND `state` = 1
                """;
        return Try.of(() -> this.jdbcTemplate.queryForObject(selectSql, (rs, rowNum) -> NodeAssignment.of(rs), tenantId, nodeDefinitionId, approverId)).getOrNull();
    }

    /**
     * 根据节点定义 ID 查找所有审批人
     *
     * @return List<NodeAssignment>
     *
     * @author wangweijun
     * @since 2024/6/20 10:14
     */
    public List<NodeAssignment> findByNodeDefinitionId() {
        return this.findByNodeDefinitionId(nodeAssignment.getTenantId(), nodeAssignment.getNodeDefinitionId());
    }

    /**
     * 根据节点定义 ID 查找所有审批人
     *
     * @param tenantId         租户 ID
     * @param nodeDefinitionId 节点定义 ID
     *
     * @return List<NodeAssignment>
     *
     * @author wangweijun
     * @since 2024/6/20 10:14
     */
    public List<NodeAssignment> findByNodeDefinitionId(String tenantId, Integer nodeDefinitionId) {
        final String selectSql = """
                SELECT * FROM awf_node_assignment WHERE `tenant_id` = ? AND `node_def_id` = ? AND `state` = 1 ORDER BY `approver_seq` ASC
                """;
        RowMapper<NodeAssignment> rowMapper = (ResultSet rs, int rowNum) -> NodeAssignment.of(rs);
        return jdbcTemplate.query(selectSql, rowMapper, tenantId, nodeDefinitionId).stream().toList();
    }

    /**
     * 根据流程实例 ID 查找所有审批人
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     *
     * @return List<NodeAssignment>
     *
     * @author wangweijun
     * @since 2024/9/12 15:15
     */
    public List<NodeAssignment> findByWorkflowInstanceId(String tenantId, Integer workflowInstanceId) {
        final String selectSql = """
                SELECT na.* FROM awf_node_assignment na
                    LEFT JOIN awf_node_definition nd ON nd.`id` = na.`node_def_id`
                    LEFT JOIN awf_wf_definition wd ON wd.`id` = nd.`wf_def_id`
                    LEFT JOIN awf_wf_instance wi ON wi.`wf_def_id` = wd.`id`
                WHERE wi.`tenant_id` = ? AND wi.`id` = ?
                """;
        RowMapper<NodeAssignment> rowMapper = (ResultSet rs, int rowNum) -> NodeAssignment.of(rs);
        return jdbcTemplate.query(selectSql, rowMapper, tenantId, workflowInstanceId).stream().toList();
    }

    /**
     * 根据流程定义 ID 查找所有审批人
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     *
     * @return List<NodeAssignment>
     *
     * @author wangweijun
     * @since 2024/9/30 15:43
     */
    public List<NodeAssignment> findByWorkflowDefinitionId(String tenantId, Integer workflowDefinitionId) {
        final String selectSql = """
                SELECT na.* FROM awf_node_assignment na
                    LEFT JOIN awf_node_definition nd ON nd.`id` = na.`node_def_id`
                    LEFT JOIN awf_wf_definition wd ON wd.`id` = nd.`wf_def_id`
                WHERE wd.`tenant_id` = ? AND wd.`id` = ?
                """;
        RowMapper<NodeAssignment> rowMapper = (ResultSet rs, int rowNum) -> NodeAssignment.of(rs);
        return jdbcTemplate.query(selectSql, rowMapper, tenantId, workflowDefinitionId).stream().toList();
    }


    /**
     * 根据节点定义 ID 删除所有审批人
     *
     * @param tenantId         租户 ID
     * @param nodeDefinitionId 节点定义 ID
     *
     * @author wangweijun
     * @since 2024/7/3 11:19
     */
    public void deleteByNodeDefinitionId(String tenantId, Integer nodeDefinitionId) {
        final String deleteSql = """
                DELETE FROM awf_node_assignment WHERE `tenant_id` = ? AND `node_def_id` = ?
                """;
        jdbcTemplate.update(deleteSql, tenantId, nodeDefinitionId);
    }
}
