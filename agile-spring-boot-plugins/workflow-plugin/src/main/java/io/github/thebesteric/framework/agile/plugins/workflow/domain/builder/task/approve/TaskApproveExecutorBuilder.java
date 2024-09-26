package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.task.approve;

import io.github.thebesteric.framework.agile.plugins.workflow.constant.ActiveStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.ApproveStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.ApproverIdType;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.TaskApprove;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * TaskApproveExecutorBuilder
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-24 20:06:19
 */
public class TaskApproveExecutorBuilder extends AbstractExecutorBuilder<TaskApprove> {

    private final TaskApproveExecutor taskApproveExecutor;

    private TaskApproveExecutorBuilder(JdbcTemplate jdbcTemplate) {
        this.taskApproveExecutor = new TaskApproveExecutor(jdbcTemplate);
    }

    public static TaskApproveExecutorBuilder builder(JdbcTemplate jdbcTemplate) {
        return new TaskApproveExecutorBuilder(jdbcTemplate);
    }

    public TaskApproveExecutorBuilder newEntity() {
        this.taskApproveExecutor.setTaskApprove(new TaskApprove());
        return this;
    }

    public TaskApproveExecutorBuilder tenantId(String tenantId) {
        this.taskApproveExecutor.getTaskApprove().setTenantId(tenantId);
        return this;
    }

    public TaskApproveExecutorBuilder workflowInstanceId(Integer workflowInstanceId) {
        this.taskApproveExecutor.getTaskApprove().setWorkflowInstanceId(workflowInstanceId);
        return this;
    }

    public TaskApproveExecutorBuilder taskInstanceId(Integer taskInstanceId) {
        this.taskApproveExecutor.getTaskApprove().setTaskInstanceId(taskInstanceId);
        return this;
    }

    public TaskApproveExecutorBuilder approverId(String approverId) {
        this.taskApproveExecutor.getTaskApprove().setApproverId(approverId);
        return this;
    }

    public TaskApproveExecutorBuilder approveSeq(Integer approveSeq) {
        this.taskApproveExecutor.getTaskApprove().setApproverSeq(approveSeq);
        return this;
    }

    public TaskApproveExecutorBuilder approverIdType(ApproverIdType approverIdType) {
        this.taskApproveExecutor.getTaskApprove().setApproverIdType(approverIdType);
        return this;
    }

    public TaskApproveExecutorBuilder status(ApproveStatus approveStatus) {
        this.taskApproveExecutor.getTaskApprove().setStatus(approveStatus);
        return this;
    }

    public TaskApproveExecutorBuilder active(ActiveStatus approveStatus) {
        this.taskApproveExecutor.getTaskApprove().setActive(approveStatus);
        return this;
    }

    public TaskApproveExecutorBuilder comment(String comment) {
        this.taskApproveExecutor.getTaskApprove().setComment(comment);
        return this;
    }

    public TaskApproveExecutor build() {
        super.setDefaultEntityAttrs(taskApproveExecutor.getTaskApprove());
        return taskApproveExecutor;
    }
}
