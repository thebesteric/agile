package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.task.instance;

import io.github.thebesteric.framework.agile.plugins.workflow.constant.NodeStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.TaskInstance;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * TaskInstanceExecutorBuilder
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-24 13:50:27
 */
public class TaskInstanceExecutorBuilder extends AbstractExecutorBuilder<TaskInstance> {

    private final TaskInstanceExecutor taskInstanceExecutor;

    private TaskInstanceExecutorBuilder(JdbcTemplate jdbcTemplate) {
        this.taskInstanceExecutor = new TaskInstanceExecutor(jdbcTemplate);
    }

    public static TaskInstanceExecutorBuilder builder(JdbcTemplate jdbcTemplate) {
        return new TaskInstanceExecutorBuilder(jdbcTemplate);
    }

    public TaskInstanceExecutorBuilder newInstance() {
        this.taskInstanceExecutor.setTaskInstance(new TaskInstance());
        return this;
    }

    public TaskInstanceExecutorBuilder tenantId(String tenantId) {
        this.taskInstanceExecutor.getTaskInstance().setTenantId(tenantId);
        return this;
    }

    public TaskInstanceExecutorBuilder workflowInstanceId(Integer workflowInstanceId) {
        this.taskInstanceExecutor.getTaskInstance().setWorkflowInstanceId(workflowInstanceId);
        return this;
    }

    public TaskInstanceExecutorBuilder nodeDefinitionId(Integer nodeDefinitionId) {
        this.taskInstanceExecutor.getTaskInstance().setNodeDefinitionId(nodeDefinitionId);
        return this;
    }

    public TaskInstanceExecutorBuilder status(NodeStatus status) {
        this.taskInstanceExecutor.getTaskInstance().setStatus(status);
        return this;
    }

    public TaskInstanceExecutorBuilder roleApprove(boolean roleApprove) {
        this.taskInstanceExecutor.getTaskInstance().setRoleApprove(roleApprove);
        return this;
    }

    public TaskInstanceExecutorBuilder approvedCount(Integer approvedCount) {
        this.taskInstanceExecutor.getTaskInstance().setApprovedCount(approvedCount);
        return this;
    }

    public TaskInstanceExecutorBuilder totalCount(Integer totalCount) {
        this.taskInstanceExecutor.getTaskInstance().setTotalCount(totalCount);
        return this;
    }

    public TaskInstanceExecutor build() {
        super.setDefaultEntityAttrs(taskInstanceExecutor.getTaskInstance());
        return taskInstanceExecutor;
    }
}
