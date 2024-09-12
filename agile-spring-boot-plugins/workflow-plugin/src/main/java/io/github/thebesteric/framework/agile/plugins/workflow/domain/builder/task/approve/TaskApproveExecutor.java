package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.task.approve;

import io.github.thebesteric.framework.agile.commons.exception.DataExistsException;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.ActiveStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.TaskApprove;
import io.vavr.control.Try;
import lombok.Getter;
import lombok.Setter;
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
            throw new DataExistsException("已存在相同的节点审批人");
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
     * @param approverId     审批人 ID
     *
     * @return TaskApprove
     *
     * @author wangweijun
     * @since 2024/6/24 20:03
     */
    public TaskApprove getByTaskInstanceIdAndApproverId(String tenantId, Integer taskInstanceId, String approverId) {
        final String selectSql = """
                SELECT * FROM awf_task_approve WHERE `tenant_id` = ? AND `task_inst_id` = ? AND `approver_id` = ? AND `state` = 1 AND `active` = 1
                """;
        return Try.of(() -> this.jdbcTemplate.queryForObject(selectSql, (rs, rowNum) -> TaskApprove.of(rs), tenantId, taskInstanceId, approverId)).getOrNull();
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
     *
     * @return List<TaskApprove>
     *
     * @author wangweijun
     * @since 2024/6/27 13:46
     */
    public List<TaskApprove> findByTWorkflowInstanceId(String tenantId, Integer workflowInstanceId, ActiveStatus activeStatus) {
        String selectSql = """
                SELECT a.* FROM awf_task_approve a 
                LEFT JOIN awf_task_instance i ON a.`task_inst_id` = i.`id`
                WHERE a.`tenant_id` = ? AND i.`wf_inst_id` = ? AND a.`state` = 1
                """;
        if (activeStatus != null) {
            selectSql += " AND a.`active` = '" + activeStatus.getCode() + "'";
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
        return findByTWorkflowInstanceId(tenantId, workflowInstanceId, null);
    }

}
