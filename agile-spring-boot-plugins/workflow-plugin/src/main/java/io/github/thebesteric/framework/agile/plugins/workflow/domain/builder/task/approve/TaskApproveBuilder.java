package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.task.approve;

import io.github.thebesteric.framework.agile.commons.exception.InvalidParamsException;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.ActiveStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.ApproveStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.TaskApprove;

/**
 * TaskApproveBuilder
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-24 19:46:19
 */
public class TaskApproveBuilder extends AbstractBuilder<TaskApprove> {

    private final TaskApprove taskApprove;

    private TaskApproveBuilder(TaskApprove taskApprove) {
        this.taskApprove = taskApprove;
    }

    public static TaskApproveBuilder builder() {
        return new TaskApproveBuilder(new TaskApprove());
    }

    public TaskApproveBuilder tenantId(String tenantId) {
        this.taskApprove.setTenantId(tenantId);
        return this;
    }

    public TaskApproveBuilder workflowInstanceId(Integer workflowInstanceId) {
        this.taskApprove.setWorkflowInstanceId(workflowInstanceId);
        return this;
    }

    public TaskApproveBuilder taskInstanceId(Integer taskInstanceId) {
        this.taskApprove.setTaskInstanceId(taskInstanceId);
        return this;
    }

    public TaskApproveBuilder approverId(String approverId) {
        this.taskApprove.setApproverId(approverId);
        return this;
    }

    public TaskApproveBuilder active(ActiveStatus activeStatus) {
        this.taskApprove.setActive(activeStatus);
        return this;
    }

    public TaskApproveBuilder status(ApproveStatus approveStatus) {
        this.taskApprove.setStatus(approveStatus);
        return this;
    }

    public TaskApproveBuilder comment(String comment) {
        this.taskApprove.setComment(comment);
        return this;
    }

    public TaskApprove build() {
        String tenantId = this.taskApprove.getTenantId();
        Integer taskInstanceId = this.taskApprove.getTaskInstanceId();
        String approverId = this.taskApprove.getApproverId();
        ApproveStatus status = this.taskApprove.getStatus();
        if (tenantId == null || taskInstanceId == null || approverId == null || status == null) {
            throw new InvalidParamsException("tenantId, taskInstanceId, approverId, status cannot be null or empty");
        }
        return super.build(this.taskApprove);
    }

}
