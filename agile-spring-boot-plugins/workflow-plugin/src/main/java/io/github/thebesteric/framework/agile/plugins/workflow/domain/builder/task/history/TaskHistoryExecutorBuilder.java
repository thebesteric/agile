package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.task.history;

import io.github.thebesteric.framework.agile.plugins.workflow.constant.TaskHistoryMessage;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.TaskHistory;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * TaskHistoryExecutorBuilder
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-24 20:06:19
 */
public class TaskHistoryExecutorBuilder extends AbstractExecutorBuilder<TaskHistory> {

    private final TaskHistoryExecutor taskHistoryExecutor;

    private TaskHistoryExecutorBuilder(JdbcTemplate jdbcTemplate) {
        this.taskHistoryExecutor = new TaskHistoryExecutor(jdbcTemplate);
    }

    public static TaskHistoryExecutorBuilder builder(JdbcTemplate jdbcTemplate) {
        return new TaskHistoryExecutorBuilder(jdbcTemplate);
    }

    public TaskHistoryExecutorBuilder tenantId(String tenantId) {
        this.taskHistoryExecutor.getTaskHistory().setTenantId(tenantId);
        return this;
    }

    public TaskHistoryExecutorBuilder workflowInstanceId(Integer workflowInstanceId) {
        this.taskHistoryExecutor.getTaskHistory().setWorkflowInstanceId(workflowInstanceId);
        return this;
    }

    public TaskHistoryExecutorBuilder taskInstanceId(Integer taskInstanceId) {
        this.taskHistoryExecutor.getTaskHistory().setTaskInstanceId(taskInstanceId);
        return this;
    }

    public TaskHistoryExecutorBuilder title(String title) {
        this.taskHistoryExecutor.getTaskHistory().setTitle(title);
        return this;
    }

    public TaskHistoryExecutorBuilder message(TaskHistoryMessage message) {
        this.taskHistoryExecutor.getTaskHistory().setMessage(message);
        return this;
    }

    public TaskHistoryExecutorBuilder message(String message) {
        return message(TaskHistoryMessage.custom(message));
    }

    public TaskHistoryExecutor build() {
        super.setDefaultEntityAttrs(taskHistoryExecutor.getTaskHistory());
        return taskHistoryExecutor;
    }
}
