package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.task.dynamic;

import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.TaskDynamicAssignment;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * TaskDynamicAssignmentExecutorBuilder
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-10-17 17:30:19
 */
public class TaskDynamicAssignmentExecutorBuilder extends AbstractExecutorBuilder<TaskDynamicAssignment> {

    private final TaskDynamicAssignmentExecutor taskDynamicAssignmentExecutor;

    private TaskDynamicAssignmentExecutorBuilder(JdbcTemplate jdbcTemplate) {
        this.taskDynamicAssignmentExecutor = new TaskDynamicAssignmentExecutor(jdbcTemplate);
    }

    public static TaskDynamicAssignmentExecutorBuilder builder(JdbcTemplate jdbcTemplate) {
        return new TaskDynamicAssignmentExecutorBuilder(jdbcTemplate);
    }

    public TaskDynamicAssignmentExecutorBuilder newInstance() {
        this.taskDynamicAssignmentExecutor.setTaskDynamicAssignment(new TaskDynamicAssignment());
        return this;
    }

    public TaskDynamicAssignmentExecutorBuilder tenantId(String tenantId) {
        this.taskDynamicAssignmentExecutor.getTaskDynamicAssignment().setTenantId(tenantId);
        return this;
    }

    public TaskDynamicAssignmentExecutorBuilder nodeDefinitionId(Integer nodeDefinitionId) {
        this.taskDynamicAssignmentExecutor.getTaskDynamicAssignment().setNodeDefinitionId(nodeDefinitionId);
        return this;
    }

    public TaskDynamicAssignmentExecutorBuilder taskInstanceId(Integer taskInstanceId) {
        this.taskDynamicAssignmentExecutor.getTaskDynamicAssignment().setTaskInstanceId(taskInstanceId);
        return this;
    }

    public TaskDynamicAssignmentExecutorBuilder approverId(String approverId) {
        this.taskDynamicAssignmentExecutor.getTaskDynamicAssignment().setApproverId(approverId);
        return this;
    }

    public TaskDynamicAssignmentExecutorBuilder approverName(String approverName) {
        this.taskDynamicAssignmentExecutor.getTaskDynamicAssignment().setApproverName(approverName);
        return this;
    }

    public TaskDynamicAssignmentExecutorBuilder approverDesc(String approverDesc) {
        this.taskDynamicAssignmentExecutor.getTaskDynamicAssignment().setApproverDesc(approverDesc);
        return this;
    }

    public TaskDynamicAssignmentExecutorBuilder approverSeq(Integer approverSeq) {
        this.taskDynamicAssignmentExecutor.getTaskDynamicAssignment().setApproverSeq(approverSeq);
        return this;
    }

    public TaskDynamicAssignmentExecutor build() {
        super.setDefaultEntityAttrs(taskDynamicAssignmentExecutor.getTaskDynamicAssignment());
        return taskDynamicAssignmentExecutor;
    }
}
