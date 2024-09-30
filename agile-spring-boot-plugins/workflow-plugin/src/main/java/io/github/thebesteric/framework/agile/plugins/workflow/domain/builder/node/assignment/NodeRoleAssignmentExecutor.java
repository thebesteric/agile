package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.assignment;

import io.github.thebesteric.framework.agile.commons.exception.DataExistsException;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.NodeRoleAssignment;
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
public class NodeRoleAssignmentExecutor extends AbstractExecutor<NodeRoleAssignment> {

    private NodeRoleAssignment nodeRoleAssignment;

    public NodeRoleAssignmentExecutor(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
        this.nodeRoleAssignment = new NodeRoleAssignment();
    }

    /**
     * 保存节点定义
     *
     * @author wangweijun
     * @since 2024/6/18 16:25
     */
    public NodeRoleAssignment save() {
        // 检查是否有已存在的节点定义
        String tenantId = nodeRoleAssignment.getTenantId();
        Integer nodeDefinitionId = nodeRoleAssignment.getNodeDefinitionId();
        String roleId = nodeRoleAssignment.getRoleId();
        String userId = nodeRoleAssignment.getUserId();
        NodeRoleAssignment existsNodeRoleAssignment = this.getByNodeDefinitionIdAndApproverId(tenantId, nodeDefinitionId, roleId, userId);
        if (existsNodeRoleAssignment != null) {
            throw new DataExistsException("已存在相同的节点审批人");
        }
        return super.save(nodeRoleAssignment);
    }

    /**
     * 根据节点定义 ID 和角色 ID 获取审批人
     *
     * @param tenantId         租户 ID
     * @param nodeDefinitionId 节点定义 ID
     * @param roleId           角色 ID
     *
     * @return List<NodeRoleAssignment>
     *
     * @author wangweijun
     * @since 2024/9/14 13:38
     */
    public List<NodeRoleAssignment> findByNodeDefinitionIdRoleId(String tenantId, Integer nodeDefinitionId, String roleId) {
        final String selectSql = """
                SELECT * FROM awf_node_role_assignment 
                WHERE `tenant_id` = ? AND `node_def_id` = ? AND `role_id` = ? AND `state` = 1
                ORDER BY `role_seq` ASC, `user_seq` ASC
                """;
        RowMapper<NodeRoleAssignment> rowMapper = (ResultSet rs, int rowNum) -> NodeRoleAssignment.of(rs);
        return this.jdbcTemplate.query(selectSql, rowMapper, tenantId, nodeDefinitionId, roleId).stream().toList();
    }

    /**
     * 根据节点定义 ID 和用户 ID 获取审批人
     *
     * @param tenantId         租户 ID
     * @param nodeDefinitionId 节点定义 ID
     * @param roleId           角色 ID
     * @param userId           审批人 ID
     *
     * @return NodeRoleUserAssignment
     *
     * @author wangweijun
     * @since 2024/9/13 09:58
     */
    public NodeRoleAssignment getByNodeDefinitionIdAndApproverId(String tenantId, Integer nodeDefinitionId, String roleId, String userId) {
        final String selectSql = """
                SELECT * FROM awf_node_role_assignment 
                WHERE `tenant_id` = ? AND `node_def_id` = ? AND `role_id` = ? AND `user_id` = ? AND `state` = 1
                """;
        RowMapper<NodeRoleAssignment> rowMapper = (ResultSet rs, int rowNum) -> NodeRoleAssignment.of(rs);
        return Try.of(() -> this.jdbcTemplate.queryForObject(selectSql, rowMapper, tenantId, nodeDefinitionId, roleId, userId)).getOrNull();
    }

    /**
     * 根据节点定义 ID 查找所有审批人
     *
     * @param tenantId         租户 ID
     * @param nodeDefinitionId 节点定义 ID
     *
     * @return List<NodeRoleUserAssignment>
     *
     * @author wangweijun
     * @since 2024/9/13 09:59
     */
    public List<NodeRoleAssignment> findByNodeDefinitionId(String tenantId, Integer nodeDefinitionId) {
        final String selectSql = """
                SELECT * FROM awf_node_role_assignment 
                WHERE `tenant_id` = ? AND `node_def_id` = ? AND `state` = 1 
                ORDER BY `role_seq` ASC, `user_seq` ASC
                """;
        RowMapper<NodeRoleAssignment> rowMapper = (ResultSet rs, int rowNum) -> NodeRoleAssignment.of(rs);
        return jdbcTemplate.query(selectSql, rowMapper, tenantId, nodeDefinitionId).stream().toList();
    }

    /**
     * 根据流程实例 ID 查找所有审批人
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     *
     * @return List<NodeRoleUserAssignment>
     *
     * @author wangweijun
     * @since 2024/9/13 09:59
     */
    public List<NodeRoleAssignment> findByWorkflowInstanceId(String tenantId, Integer workflowInstanceId) {
        final String selectSql = """
                SELECT na.* FROM awf_node_role_assignment na
                    LEFT JOIN awf_node_definition nd ON nd.`id` = na.`node_def_id`
                    LEFT JOIN awf_wf_definition wd ON wd.`id` = nd.`wf_def_id`
                    LEFT JOIN awf_wf_instance wi ON wi.`wf_def_id` = wd.`id`
                WHERE wi.`tenant_id` = ? AND wi.`id` = ?
                """;
        RowMapper<NodeRoleAssignment> rowMapper = (ResultSet rs, int rowNum) -> NodeRoleAssignment.of(rs);
        return jdbcTemplate.query(selectSql, rowMapper, tenantId, workflowInstanceId).stream().toList();
    }

    /**
     * 根据流程定义 ID 查找所有审批人
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     *
     * @return List<NodeRoleUserAssignment>
     *
     * @author wangweijun
     * @since 2024/9/13 09:59
     */
    public List<NodeRoleAssignment> findByWorkflowDefinitionId(String tenantId, Integer workflowDefinitionId) {
        final String selectSql = """
                SELECT na.* FROM awf_node_role_assignment na
                    LEFT JOIN awf_node_definition nd ON nd.`id` = na.`node_def_id`
                    LEFT JOIN awf_wf_definition wd ON wd.`id` = nd.`wf_def_id`
                WHERE wd.`tenant_id` = ? AND wd.`id` = ?
                """;
        RowMapper<NodeRoleAssignment> rowMapper = (ResultSet rs, int rowNum) -> NodeRoleAssignment.of(rs);
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
                DELETE FROM awf_node_role_assignment WHERE `tenant_id` = ? AND `node_def_id` = ?
                """;
        jdbcTemplate.update(deleteSql, tenantId, nodeDefinitionId);
    }
}
