package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.definition;

import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowDefinition;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 流程定义创建者
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-14 15:47:20
 */
public class WorkflowDefinitionExecutorBuilder extends AbstractExecutorBuilder<WorkflowDefinition> {

    private final WorkflowDefinitionExecutor workflowDefinitionExecutor;

    private WorkflowDefinitionExecutorBuilder(JdbcTemplate jdbcTemplate) {
        this.workflowDefinitionExecutor = new WorkflowDefinitionExecutor(jdbcTemplate);
    }

    public static WorkflowDefinitionExecutorBuilder builder(JdbcTemplate jdbcTemplate) {
        return new WorkflowDefinitionExecutorBuilder(jdbcTemplate);
    }

    public WorkflowDefinitionExecutorBuilder tenantId(String tenantId) {
        this.workflowDefinitionExecutor.getWorkflowDefinition().setTenantId(tenantId);
        return this;
    }

    public WorkflowDefinitionExecutorBuilder key(String key) {
        this.workflowDefinitionExecutor.getWorkflowDefinition().setKey(key);
        this.workflowDefinitionExecutor.getWorkflowDefinition().setName(key);
        return this;
    }

    public WorkflowDefinitionExecutorBuilder name(String name) {
        this.workflowDefinitionExecutor.getWorkflowDefinition().setName(name);
        return this;
    }

    public WorkflowDefinitionExecutorBuilder type(String type) {
        this.workflowDefinitionExecutor.getWorkflowDefinition().setType(type);
        return this;
    }

    public WorkflowDefinitionExecutorBuilder desc(String desc) {
        this.workflowDefinitionExecutor.getWorkflowDefinition().setDesc(desc);
        return this;
    }

    public WorkflowDefinitionExecutorBuilder workflowDefinition(WorkflowDefinition workflowDefinition) {
        workflowDefinitionExecutor.setWorkflowDefinition(workflowDefinition);
        return this;
    }

    public WorkflowDefinitionExecutor build() {
        super.setDefaultEntityAttrs(workflowDefinitionExecutor.getWorkflowDefinition());
        return this.workflowDefinitionExecutor;
    }

}
