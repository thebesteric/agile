package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.task.approve;

import io.github.thebesteric.framework.agile.plugins.workflow.constant.ActiveStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.ApproveStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.assignment.NodeRoleAssignmentExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.definition.NodeDefinitionExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.task.instance.TaskInstanceExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.NodeDefinition;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.NodeRoleAssignment;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.TaskApprove;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.TaskInstance;
import io.github.thebesteric.framework.agile.plugins.workflow.exception.WorkflowException;
import io.vavr.control.Try;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.util.List;

/**
 * TaskApproveExecutor
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-24 19:53:12
 */
@Getter
@Setter
public class TaskApproveExecutor extends AbstractExecutor<TaskApprove> {

    private TaskApprove taskApprove;

    protected TaskApproveExecutor(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
        this.taskApprove = new TaskApprove();
    }

    /**
     * 保存审批信息
     *
     * @return TaskApprove
     *
     * @author wangweijun
     * @since 2024/6/24 20:02
     */
    public TaskApprove save() {
        // 检查是否有已存在的节点定义
        TaskApprove existsTaskApprove = this.getByTaskInstanceIdAndApproverId();
        if (existsTaskApprove != null) {
            throw new WorkflowException("已存在相同的节点审批人");
        }
        return super.save(taskApprove);
    }

    /**
     * 根据任务实例 ID 和审批人 ID 查询审批信息
     *
     * @return TaskApprove
     *
     * @author wangweijun
     * @since 2024/6/24 20:03
     */
    public TaskApprove getByTaskInstanceIdAndApproverId() {
        return this.getByTaskInstanceIdAndApproverId(taskApprove.getTenantId(), taskApprove.getTaskInstanceId(), taskApprove.getApproverId());
    }

    /**
     * 根据任务实例 ID 和审批人 ID 查询审批信息
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     * @param activeStatus   生效状态
     * @param roleId         角色 ID
     * @param userId         用户 ID
     *
     * @return TaskApprove
     *
     * @author wangweijun
     * @since 2024/6/24 20:03
     */
    public TaskApprove getByTaskInstanceIdAndRoleIdAndApproverId(String tenantId, Integer taskInstanceId, ActiveStatus activeStatus, String roleId, String userId) {
        TaskInstanceExecutor taskInstanceExecutor = new TaskInstanceExecutor(jdbcTemplate);
        NodeDefinitionExecutor nodeDefinitionExecutor = new NodeDefinitionExecutor(jdbcTemplate);
        TaskInstance taskInstance = taskInstanceExecutor.getById(taskInstanceId);
        NodeDefinition nodeDefinition = nodeDefinitionExecutor.getById(taskInstance.getNodeDefinitionId());

        // 判断是否属于角色审批
        if (nodeDefinition.isRoleApprove() && StringUtils.isNotEmpty(roleId)) {
            NodeRoleAssignmentExecutor nodeRoleAssignmentExecutor = new NodeRoleAssignmentExecutor(jdbcTemplate);
            NodeRoleAssignment nodeRoleAssignment = nodeRoleAssignmentExecutor.getByNodeDefinitionIdAndRoleIdAndApproverId(tenantId, nodeDefinition.getId(), roleId, userId, null);
            if (nodeRoleAssignment == null) {
                throw new WorkflowException("角色审批人未定义");
            }
            userId = nodeRoleAssignment.getRoleId();
        }

        if (activeStatus == null) {
            activeStatus = ActiveStatus.ACTIVE;
        }

        final String selectSql = """
                SELECT * FROM awf_task_approve 
                WHERE `state` = 1 AND `active` = %s AND `tenant_id` = ? AND `task_inst_id` = ? AND `approver_id` = '%s' 
                """.formatted(activeStatus.getCode(), userId);
        RowMapper<TaskApprove> rowMapper = (ResultSet rs, int rowNum) -> TaskApprove.of(rs);
        return Try.of(() -> this.jdbcTemplate.queryForObject(selectSql, rowMapper, tenantId, taskInstanceId)).getOrNull();
    }

    /**
     * 根据任务实例 ID 和审批人 ID 查询审批信息
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     * @param approverId     用户 ID
     *
     * @return TaskApprove
     *
     * @author wangweijun
     * @since 2024/10/11 14:09
     */
    public TaskApprove getByTaskInstanceIdAndApproverId(String tenantId, Integer taskInstanceId, String approverId) {
        final String selectSql = """
                SELECT * FROM awf_task_approve 
                WHERE `state` = 1 AND `tenant_id` = ? AND `task_inst_id` = ? AND `approver_id` = ?
                """;
        RowMapper<TaskApprove> rowMapper = (ResultSet rs, int rowNum) -> TaskApprove.of(rs);
        return Try.of(() -> this.jdbcTemplate.queryForObject(selectSql, rowMapper, tenantId, taskInstanceId, approverId)).getOrNull();
    }

    /**
     * 根据任务实例 ID 查询审批信息
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     *
     * @return List<TaskApprove>
     *
     * @author wangweijun
     * @since 2024/6/24 20:12
     */
    public List<TaskApprove> findByTaskInstanceId(String tenantId, Integer taskInstanceId) {
        return this.findByTaskInstanceId(tenantId, taskInstanceId, null);
    }

    /**
     * 根据任务实例 ID 和 ActiveStatus 查询审批信息
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     *
     * @return List<TaskApprove>
     *
     * @author wangweijun
     * @since 2024/6/24 20:12
     */
    public List<TaskApprove> findByTaskInstanceId(String tenantId, Integer taskInstanceId, ActiveStatus activeStatus) {
        String selectSql = """
                SELECT * FROM awf_task_approve WHERE `tenant_id` = ? AND `task_inst_id` = ? AND `state` = 1
                """;
        if (activeStatus != null) {
            selectSql += " AND `active` = ?";
        }
        RowMapper<TaskApprove> rowMapper = (ResultSet rs, int rowNum) -> TaskApprove.of(rs);
        if (activeStatus == null) {
            return jdbcTemplate.query(selectSql, rowMapper, tenantId, taskInstanceId).stream().toList();
        }
        return jdbcTemplate.query(selectSql, rowMapper, tenantId, taskInstanceId, activeStatus.getCode()).stream().toList();
    }

    /**
     * 根据任务实例 ID 查询审批信息
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     * @param activeStatus       活动状态
     * @param approveStatus      审核状态
     *
     * @return List<TaskApprove>
     *
     * @author wangweijun
     * @since 2024/6/27 13:46
     */
    public List<TaskApprove> findByTWorkflowInstanceId(String tenantId, Integer workflowInstanceId, ActiveStatus activeStatus, ApproveStatus approveStatus) {
        String selectSql = """
                SELECT a.* FROM awf_task_approve a 
                LEFT JOIN awf_task_instance i ON a.`task_inst_id` = i.`id`
                WHERE a.`tenant_id` = ? AND i.`wf_inst_id` = ? AND a.`state` = 1
                """;
        if (activeStatus != null) {
            selectSql += " AND a.`active` = '" + activeStatus.getCode() + "'";
        }
        if (approveStatus != null) {
            selectSql += " AND a.`status` = '" + approveStatus.getCode() + "'";
        }
        RowMapper<TaskApprove> rowMapper = (ResultSet rs, int rowNum) -> TaskApprove.of(rs);
        return jdbcTemplate.query(selectSql, rowMapper, tenantId, workflowInstanceId).stream().toList();
    }

    /**
     * 根据任务实例 ID 查询审批信息
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     *
     * @return List<TaskApprove>
     *
     * @author wangweijun
     * @since 2024/6/27 13:46
     */
    public List<TaskApprove> findByTWorkflowInstanceId(String tenantId, Integer workflowInstanceId) {
        return findByTWorkflowInstanceId(tenantId, workflowInstanceId, null, null);
    }

}
