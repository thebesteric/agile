package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.task.approve;

import io.github.thebesteric.framework.agile.plugins.workflow.constant.RoleApproveStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.TaskRoleApproveRecord;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * TaskRoleApproveRecordExecutorBuilder
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-09-14 11:59:15
 */
public class TaskRoleApproveRecordExecutorBuilder extends AbstractExecutorBuilder<TaskRoleApproveRecord> {

    private final TaskRoleApproveRecordExecutor taskRoleApproveRecordExecutor;

    private TaskRoleApproveRecordExecutorBuilder(JdbcTemplate jdbcTemplate) {
        this.taskRoleApproveRecordExecutor = new TaskRoleApproveRecordExecutor(jdbcTemplate);
    }

    public static TaskRoleApproveRecordExecutorBuilder builder(JdbcTemplate jdbcTemplate) {
        return new TaskRoleApproveRecordExecutorBuilder(jdbcTemplate);
    }

    public TaskRoleApproveRecordExecutorBuilder tenantId(String tenantId) {
        this.taskRoleApproveRecordExecutor.getTaskRoleApproveRecord().setTenantId(tenantId);
        return this;
    }

    public TaskRoleApproveRecordExecutorBuilder workflowInstanceId(Integer workflowInstanceId) {
        this.taskRoleApproveRecordExecutor.getTaskRoleApproveRecord().setWorkflowInstanceId(workflowInstanceId);
        return this;
    }

    public TaskRoleApproveRecordExecutorBuilder taskInstanceId(Integer taskInstanceId) {
        this.taskRoleApproveRecordExecutor.getTaskRoleApproveRecord().setTaskInstanceId(taskInstanceId);
        return this;
    }

    public TaskRoleApproveRecordExecutorBuilder nodeRoleAssignmentId(Integer nodeRoleAssignmentId) {
        this.taskRoleApproveRecordExecutor.getTaskRoleApproveRecord().setNodeRoleAssignmentId(nodeRoleAssignmentId);
        return this;
    }

    public TaskRoleApproveRecordExecutorBuilder status(RoleApproveStatus roleApproveStatus) {
        this.taskRoleApproveRecordExecutor.getTaskRoleApproveRecord().setStatus(roleApproveStatus);
        return this;
    }

    public TaskRoleApproveRecordExecutor build() {
        super.setDefaultEntityAttrs(this.taskRoleApproveRecordExecutor.getTaskRoleApproveRecord());
        return this.taskRoleApproveRecordExecutor;
    }

}
