package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.task.reassign;

import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.TaskReassignRecord;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * TaskReassignRecordExecutorBuilder
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-11-07 16:23:53
 */
public class TaskReassignRecordExecutorBuilder extends AbstractExecutorBuilder<TaskReassignRecord> {

    private final TaskReassignRecordExecutor taskReassignRecordExecutor;

    private TaskReassignRecordExecutorBuilder(JdbcTemplate jdbcTemplate) {
        this.taskReassignRecordExecutor = new TaskReassignRecordExecutor(jdbcTemplate);
    }

    public static TaskReassignRecordExecutorBuilder builder(JdbcTemplate jdbcTemplate) {
        return new TaskReassignRecordExecutorBuilder(jdbcTemplate);
    }

    public TaskReassignRecordExecutorBuilder newInstance() {
        this.taskReassignRecordExecutor.setTaskReassignRecord(new TaskReassignRecord());
        return this;
    }

    public TaskReassignRecordExecutor build() {
        super.setDefaultEntityAttrs(taskReassignRecordExecutor.getTaskReassignRecord());
        return taskReassignRecordExecutor;
    }
}
