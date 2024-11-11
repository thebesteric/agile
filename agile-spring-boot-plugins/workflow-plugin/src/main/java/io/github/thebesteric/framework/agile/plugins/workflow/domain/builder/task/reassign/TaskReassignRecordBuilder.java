package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.task.reassign;

import io.github.thebesteric.framework.agile.plugins.workflow.constant.ApproverIdType;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.Invitee;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.Inviter;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.TaskApprove;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.TaskReassignRecord;

/**
 * TaskReassignRecordBuilder
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-11-07 16:15:05
 */
public class TaskReassignRecordBuilder extends AbstractBuilder<TaskReassignRecord> {
    private final TaskReassignRecord taskReassignRecord;

    private TaskReassignRecordBuilder(TaskReassignRecord taskReassignRecord) {
        this.taskReassignRecord = taskReassignRecord;
    }

    public static TaskReassignRecordBuilder builder() {
        return new TaskReassignRecordBuilder(new TaskReassignRecord());
    }

    public TaskReassignRecordBuilder ofUserIdType(TaskApprove taskApprove, Inviter fromUser, Invitee toUser, String comment) {
        this.taskReassignRecord.setTenantId(taskApprove.getTenantId());
        this.taskReassignRecord.setWorkflowInstanceId(taskApprove.getWorkflowInstanceId());
        this.taskReassignRecord.setTaskInstanceId(taskApprove.getTaskInstanceId());
        this.taskReassignRecord.setTaskApproveId(taskApprove.getId());

        this.taskReassignRecord.setFromUserId(fromUser.getUserId());
        this.taskReassignRecord.setFromUserName(fromUser.getUserName());
        this.taskReassignRecord.setFromUserDesc(fromUser.getUserDesc());
        this.taskReassignRecord.setFromUserSeq(fromUser.getUserSeq());

        this.taskReassignRecord.setToUserId(toUser.getUserId());
        this.taskReassignRecord.setToUserName(toUser.getUserName());
        this.taskReassignRecord.setToUserDesc(toUser.getUserDesc());
        this.taskReassignRecord.setToUserSeq(fromUser.getUserSeq());

        this.taskReassignRecord.setApproverIdType(ApproverIdType.USER);
        this.taskReassignRecord.setComment(comment);
        return this;
    }

    public TaskReassignRecordBuilder ofRoleIdType(TaskApprove taskApprove, Inviter fromUser, Invitee toUser, String comment) {
        this.taskReassignRecord.setTenantId(taskApprove.getTenantId());
        this.taskReassignRecord.setWorkflowInstanceId(taskApprove.getWorkflowInstanceId());
        this.taskReassignRecord.setTaskInstanceId(taskApprove.getTaskInstanceId());
        this.taskReassignRecord.setTaskApproveId(taskApprove.getId());

        this.taskReassignRecord.setFromRoleId(fromUser.getRoleId());
        this.taskReassignRecord.setFromRoleName(fromUser.getRoleName());
        this.taskReassignRecord.setFromRoleDesc(fromUser.getRoleDesc());
        this.taskReassignRecord.setFromRoleSeq(fromUser.getRoleSeq());

        this.taskReassignRecord.setFromUserId(fromUser.getUserId());
        this.taskReassignRecord.setFromUserName(fromUser.getUserName());
        this.taskReassignRecord.setFromUserDesc(fromUser.getUserDesc());
        this.taskReassignRecord.setFromUserSeq(fromUser.getUserSeq());

        this.taskReassignRecord.setToRoleId(toUser.getRoleId());
        this.taskReassignRecord.setToRoleName(toUser.getRoleName());
        this.taskReassignRecord.setToRoleDesc(toUser.getRoleDesc());
        this.taskReassignRecord.setToRoleSeq(fromUser.getRoleSeq());

        this.taskReassignRecord.setToUserId(toUser.getUserId());
        this.taskReassignRecord.setToUserName(toUser.getUserName());
        this.taskReassignRecord.setToUserDesc(toUser.getUserDesc());
        this.taskReassignRecord.setToUserSeq(fromUser.getUserSeq());

        this.taskReassignRecord.setApproverIdType(ApproverIdType.ROLE);
        this.taskReassignRecord.setComment(comment);
        return this;
    }

    public TaskReassignRecord build() {
        return super.build(this.taskReassignRecord);
    }
}
