package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.assignment;

import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowAssignment;

/**
 * WorkflowAssignmentBuilder
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-09-10 12:02:08
 */
public class WorkflowAssignmentBuilder extends AbstractBuilder<WorkflowAssignment> {

    private final WorkflowAssignment workflowAssignment;

    private WorkflowAssignmentBuilder(WorkflowAssignment workflowAssignment) {
        this.workflowAssignment = workflowAssignment;
    }

    public static WorkflowAssignmentBuilder builder(String tenantId, Integer workflowDefinitionId) {
        WorkflowAssignmentBuilder builder = new WorkflowAssignmentBuilder(new WorkflowAssignment());
        builder.workflowAssignment.setTenantId(tenantId);
        builder.workflowAssignment.setWorkflowDefinitionId(workflowDefinitionId);
        return builder;
    }

    public WorkflowAssignmentBuilder approverInfo(String approverId, String approverName, String approverDesc) {
        this.workflowAssignment.setApproverId(approverId);
        this.workflowAssignment.setApproverName(approverName);
        this.workflowAssignment.setApproverDesc(approverDesc);
        return this;
    }

    public WorkflowAssignment build() {
        return super.build(this.workflowAssignment);
    }
}
