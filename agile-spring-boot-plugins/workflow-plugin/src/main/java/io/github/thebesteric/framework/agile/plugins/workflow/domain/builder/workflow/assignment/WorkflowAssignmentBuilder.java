package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.assignment;

import io.github.thebesteric.framework.agile.plugins.workflow.constant.ApproveType;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowAssignment;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * WorkflowAssignmentBuilder
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-09-10 12:02:08
 */
public class WorkflowAssignmentBuilder extends AbstractBuilder<WorkflowAssignment> {

    private final WorkflowAssignment workflowAssignment;

    private final AtomicInteger seq = new AtomicInteger(1);

    private WorkflowAssignmentBuilder(WorkflowAssignment workflowAssignment) {
        this.workflowAssignment = workflowAssignment;
    }

    public static WorkflowAssignmentBuilder builder(String tenantId, Integer workflowDefinitionId) {
        WorkflowAssignmentBuilder builder = new WorkflowAssignmentBuilder(new WorkflowAssignment());
        builder.workflowAssignment.setTenantId(tenantId);
        builder.workflowAssignment.setWorkflowDefinitionId(workflowDefinitionId);
        return builder;
    }

    public WorkflowAssignmentBuilder approverId(String userId, String desc) {
        return this.approverId(null, userId, desc);
    }

    public WorkflowAssignmentBuilder approverId(ApproveType approveType, String approverId, String desc) {
        this.workflowAssignment.setApproverId(approverId);
        this.workflowAssignment.setDesc(desc);
        if (ApproveType.SEQ == approveType) {
            this.workflowAssignment.setApproverSeq(seq.getAndIncrement());
        }
        return this;
    }

    public WorkflowAssignment build() {
        return super.build(this.workflowAssignment);
    }

    public void resetSeq() {
        this.seq.set(1);
    }
}