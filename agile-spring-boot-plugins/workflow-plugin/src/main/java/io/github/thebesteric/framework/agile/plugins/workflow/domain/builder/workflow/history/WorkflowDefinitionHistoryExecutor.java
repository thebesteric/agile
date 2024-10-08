package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.history;

import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowDefinitionHistory;
import lombok.Getter;
import lombok.Setter;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * WorkflowDefinitionHistoryExecutor
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-09-30 18:12:43
 */
@Getter
@Setter
public class WorkflowDefinitionHistoryExecutor extends AbstractExecutor<WorkflowDefinitionHistory> {

    private WorkflowDefinitionHistory workflowDefinitionHistory;

    public WorkflowDefinitionHistoryExecutor(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
        this.workflowDefinitionHistory = new WorkflowDefinitionHistory();
    }
}
