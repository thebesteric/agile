package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.instance;

import io.github.thebesteric.framework.agile.plugins.workflow.constant.WorkflowStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.RequestConditions;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowInstance;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * WorkflowInstanceExecutorBuilder
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-24 10:11:48
 */
public class WorkflowInstanceExecutorBuilder extends AbstractExecutorBuilder<WorkflowInstance> {

    private final WorkflowInstanceExecutor workflowDefinitionExecutor;

    private WorkflowInstanceExecutorBuilder(JdbcTemplate jdbcTemplate) {
        this.workflowDefinitionExecutor = new WorkflowInstanceExecutor(jdbcTemplate);
    }

    public WorkflowInstanceExecutorBuilder tenantId(String tenantId) {
        this.workflowDefinitionExecutor.getWorkflowInstance().setTenantId(tenantId);
        return this;
    }

    public WorkflowInstanceExecutorBuilder workflowDefinitionId(Integer workflowDefinitionId) {
        this.workflowDefinitionExecutor.getWorkflowInstance().setWorkflowDefinitionId(workflowDefinitionId);
        return this;
    }

    public WorkflowInstanceExecutorBuilder requesterId(String requesterId) {
        this.workflowDefinitionExecutor.getWorkflowInstance().setRequesterId(requesterId);
        return this;
    }

    public WorkflowInstanceExecutorBuilder requestConditions(RequestConditions requestConditions) {
        this.workflowDefinitionExecutor.getWorkflowInstance().setRequestConditions(requestConditions);
        return this;
    }

    public WorkflowInstanceExecutorBuilder businessId(String businessId) {
        this.workflowDefinitionExecutor.getWorkflowInstance().setBusinessId(businessId);
        return this;
    }

    public WorkflowInstanceExecutorBuilder businessType(String businessType) {
        this.workflowDefinitionExecutor.getWorkflowInstance().setBusinessType(businessType);
        return this;
    }

    public WorkflowInstanceExecutorBuilder status(WorkflowStatus status) {
        this.workflowDefinitionExecutor.getWorkflowInstance().setStatus(status);
        return this;
    }

    public WorkflowInstanceExecutorBuilder desc(String desc) {
        this.workflowDefinitionExecutor.getWorkflowInstance().setDesc(desc);
        return this;
    }

    public static WorkflowInstanceExecutorBuilder builder(JdbcTemplate jdbcTemplate) {
        return new WorkflowInstanceExecutorBuilder(jdbcTemplate);
    }

    public WorkflowInstanceExecutor build() {
        super.setDefaultEntityAttrs(workflowDefinitionExecutor.getWorkflowInstance());
        return this.workflowDefinitionExecutor;
    }
}
