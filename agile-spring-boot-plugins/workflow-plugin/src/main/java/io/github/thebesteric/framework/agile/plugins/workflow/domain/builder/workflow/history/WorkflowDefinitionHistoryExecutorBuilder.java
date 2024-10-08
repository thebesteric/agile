package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.history;

import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowDefinitionHistory;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * WorkflowDefinitionHistoryExecutorBuilder
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-09-30 18:15:39
 */
public class WorkflowDefinitionHistoryExecutorBuilder extends AbstractExecutorBuilder<WorkflowDefinitionHistory> {

    private final WorkflowDefinitionHistoryExecutor workflowDefinitionHistoryExecutor;

    public WorkflowDefinitionHistoryExecutorBuilder(JdbcTemplate jdbcTemplate) {
        this.workflowDefinitionHistoryExecutor = new WorkflowDefinitionHistoryExecutor(jdbcTemplate);
    }

    public static WorkflowDefinitionHistoryExecutorBuilder builder(JdbcTemplate jdbcTemplate) {
        return new WorkflowDefinitionHistoryExecutorBuilder(jdbcTemplate);
    }

    public WorkflowDefinitionHistoryExecutor build() {
        super.setDefaultEntityAttrs(this.workflowDefinitionHistoryExecutor.getWorkflowDefinitionHistory());
        return this.workflowDefinitionHistoryExecutor;
    }
}
