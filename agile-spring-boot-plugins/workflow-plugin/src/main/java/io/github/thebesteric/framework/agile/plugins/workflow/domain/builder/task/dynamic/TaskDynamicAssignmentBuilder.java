package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.task.dynamic;

import io.github.thebesteric.framework.agile.plugins.workflow.constant.ApproveType;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.TaskDynamicAssignment;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * TaskDynamicAssignmentBuilder
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-10-17 17:21:24
 */
public class TaskDynamicAssignmentBuilder extends AbstractBuilder<TaskDynamicAssignment> {

    private final TaskDynamicAssignment taskDynamicAssignment;

    private final AtomicInteger seq = new AtomicInteger(1);

    private TaskDynamicAssignmentBuilder(TaskDynamicAssignment taskDynamicAssignment) {
        this.taskDynamicAssignment = taskDynamicAssignment;
    }

    public static TaskDynamicAssignmentBuilder builder(String tenantId, Integer nodeDefinitionId, Integer taskInstanceId) {
        TaskDynamicAssignmentBuilder builder = new TaskDynamicAssignmentBuilder(new TaskDynamicAssignment());
        builder.taskDynamicAssignment.setTenantId(tenantId);
        builder.taskDynamicAssignment.setNodeDefinitionId(nodeDefinitionId);
        builder.taskDynamicAssignment.setTaskInstanceId(taskInstanceId);
        return builder;
    }

    public TaskDynamicAssignmentBuilder approverInfo(ApproveType approveType, String approverId, String approverName, String approverDesc) {
        this.taskDynamicAssignment.setApproverId(approverId);
        this.taskDynamicAssignment.setApproverName(approverName);
        this.taskDynamicAssignment.setApproverDesc(approverDesc);
        if (ApproveType.SEQ == approveType) {
            this.taskDynamicAssignment.setApproverSeq(seq.getAndIncrement());
        }
        return this;
    }

    public TaskDynamicAssignment build() {
        return super.build(this.taskDynamicAssignment);
    }

    public void resetSeq() {
        this.seq.set(1);
    }
}
