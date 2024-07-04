package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.task.history;

import io.github.thebesteric.framework.agile.commons.exception.InvalidParamsException;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.TaskHistoryMessage;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.TaskHistory;

/**
 * TaskHistoryBuilder
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-28 13:39:37
 */
public class TaskHistoryBuilder extends AbstractBuilder<TaskHistory> {

    private final TaskHistory taskHistory;

    private TaskHistoryBuilder(TaskHistory taskHistory) {
        this.taskHistory = taskHistory;
    }

    public static TaskHistoryBuilder builder() {
        return new TaskHistoryBuilder(new TaskHistory());
    }

    public TaskHistoryBuilder tenantId(String tenantId) {
        this.taskHistory.setTenantId(tenantId);
        return this;
    }

    public TaskHistoryBuilder workflowInstanceId(Integer workflowInstanceId) {
        this.taskHistory.setWorkflowInstanceId(workflowInstanceId);
        return this;
    }

    public TaskHistoryBuilder taskInstanceId(Integer taskInstanceId) {
        this.taskHistory.setTaskInstanceId(taskInstanceId);
        return this;
    }

    public TaskHistoryBuilder message(TaskHistoryMessage message) {
        this.taskHistory.setMessage(message);
        return this;
    }

    public TaskHistoryBuilder message(String message) {
        return message(TaskHistoryMessage.custom(message));
    }

    public TaskHistory build() {
        String tenantId = this.taskHistory.getTenantId();
        Integer workflowInstanceId = this.taskHistory.getWorkflowInstanceId();
        Integer taskInstanceId = this.taskHistory.getTaskInstanceId();
        TaskHistoryMessage message = this.taskHistory.getMessage();
        if (tenantId == null || workflowInstanceId == null || taskInstanceId == null || message == null) {
            throw new InvalidParamsException("tenantId, workflowInstanceId, taskInstanceId, message cannot be null or empty");
        }
        return super.build(this.taskHistory);
    }
}
