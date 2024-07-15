package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.repository;

import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowRepository;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * WorkflowRepositoryExecutorBuilder
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-15 13:34:51
 */
public class WorkflowRepositoryExecutorBuilder extends AbstractExecutorBuilder<WorkflowRepository> {

    private final WorkflowRepositoryExecutor workflowRepositoryExecutor;

    private WorkflowRepositoryExecutorBuilder(JdbcTemplate jdbcTemplate) {
        this.workflowRepositoryExecutor = new WorkflowRepositoryExecutor(jdbcTemplate);
    }

    public static WorkflowRepositoryExecutorBuilder builder(JdbcTemplate jdbcTemplate) {
        return new WorkflowRepositoryExecutorBuilder(jdbcTemplate);
    }

    public WorkflowRepositoryExecutor build() {
        super.setDefaultEntityAttrs(workflowRepositoryExecutor.getWorkflowRepository());
        return this.workflowRepositoryExecutor;
    }

}
