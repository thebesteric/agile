package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.task.approve;

import io.github.thebesteric.framework.agile.commons.exception.InvalidParamsException;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.RoleApproveStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.TaskRoleApproveRecord;

/**
 * TaskRoleApproveRecordBuilder
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-09-14 11:51:04
 */
public class TaskRoleApproveRecordBuilder extends AbstractBuilder<TaskRoleApproveRecord> {

    private final TaskRoleApproveRecord taskRoleApproveRecord;

    private TaskRoleApproveRecordBuilder(TaskRoleApproveRecord taskRoleApproveRecord) {
        this.taskRoleApproveRecord = taskRoleApproveRecord;
    }

    public static TaskRoleApproveRecordBuilder builder() {
        return new TaskRoleApproveRecordBuilder(new TaskRoleApproveRecord());
    }

    public TaskRoleApproveRecordBuilder tenantId(String tenantId) {
        this.taskRoleApproveRecord.setTenantId(tenantId);
        return this;
    }

    public TaskRoleApproveRecordBuilder workflowInstanceId(Integer workflowInstanceId) {
        this.taskRoleApproveRecord.setWorkflowInstanceId(workflowInstanceId);
        return this;
    }

    public TaskRoleApproveRecordBuilder taskInstanceId(Integer taskInstanceId) {
        this.taskRoleApproveRecord.setTaskInstanceId(taskInstanceId);
        return this;
    }

    public TaskRoleApproveRecordBuilder taskApproveId(Integer taskApproveId) {
        this.taskRoleApproveRecord.setTaskApproveId(taskApproveId);
        return this;
    }

    public TaskRoleApproveRecordBuilder nodeRoleAssignmentId(Integer nodeRoleAssignmentId) {
        this.taskRoleApproveRecord.setNodeRoleAssignmentId(nodeRoleAssignmentId);
        return this;
    }

    public TaskRoleApproveRecordBuilder status(RoleApproveStatus roleApproveStatus) {
        this.taskRoleApproveRecord.setStatus(roleApproveStatus);
        return this;
    }

    public TaskRoleApproveRecordBuilder comment(String comment) {
        this.taskRoleApproveRecord.setComment(comment);
        return this;
    }

    public TaskRoleApproveRecord build() {
        String tenantId = this.taskRoleApproveRecord.getTenantId();
        Integer workflowInstanceId = this.taskRoleApproveRecord.getWorkflowInstanceId();
        Integer taskInstanceId = this.taskRoleApproveRecord.getTaskInstanceId();
        Integer taskApproveId = this.taskRoleApproveRecord.getTaskApproveId();
        Integer nodeRoleAssignmentId = this.taskRoleApproveRecord.getNodeRoleAssignmentId();
        RoleApproveStatus status = this.taskRoleApproveRecord.getStatus();
        if (tenantId == null || workflowInstanceId == null || taskInstanceId == null || taskApproveId == null || nodeRoleAssignmentId == null || status == null) {
            throw new InvalidParamsException("tenantId, workflowInstanceId, taskInstanceId, taskApproveId, nodeRoleAssignmentId, status cannot be null or empty");
        }
        return super.build(this.taskRoleApproveRecord);
    }
}
