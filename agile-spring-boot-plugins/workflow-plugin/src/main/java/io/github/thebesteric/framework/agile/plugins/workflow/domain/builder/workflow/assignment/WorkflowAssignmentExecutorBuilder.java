package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.assignment;

import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowAssignment;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * WorkflowAssignmentExecutorBuilder
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-9-10 12:13:40
 */
public class WorkflowAssignmentExecutorBuilder extends AbstractExecutorBuilder<WorkflowAssignment> {

    private final WorkflowAssignmentExecutor workflowAssignmentExecutor;

    private WorkflowAssignmentExecutorBuilder(JdbcTemplate jdbcTemplate) {
        this.workflowAssignmentExecutor = new WorkflowAssignmentExecutor(jdbcTemplate);
    }

    public static WorkflowAssignmentExecutorBuilder builder(JdbcTemplate jdbcTemplate) {
        return new WorkflowAssignmentExecutorBuilder(jdbcTemplate);
    }

    public WorkflowAssignmentExecutorBuilder workflowAssignment(WorkflowAssignment workflowAssignment) {
        workflowAssignmentExecutor.setWorkflowAssignment(workflowAssignment);
        return this;
    }

    public WorkflowAssignmentExecutorBuilder tenantId(String tenantId) {
        workflowAssignmentExecutor.getWorkflowAssignment().setTenantId(tenantId);
        return this;
    }

    public WorkflowAssignmentExecutorBuilder approverId(String approverId) {
        workflowAssignmentExecutor.getWorkflowAssignment().setApproverId(approverId);
        return this;
    }

    public WorkflowAssignmentExecutorBuilder workflowDefinitionId(Integer workflowDefinitionId) {
        workflowAssignmentExecutor.getWorkflowAssignment().setWorkflowDefinitionId(workflowDefinitionId);
        return this;
    }

    public WorkflowAssignmentExecutor build() {
        super.setDefaultEntityAttrs(workflowAssignmentExecutor.getWorkflowAssignment());
        return workflowAssignmentExecutor;
    }
}
