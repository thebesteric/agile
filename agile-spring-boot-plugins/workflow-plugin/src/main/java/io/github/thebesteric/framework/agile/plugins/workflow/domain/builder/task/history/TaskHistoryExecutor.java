package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.task.history;

import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.TaskHistory;
import lombok.Getter;
import lombok.Setter;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * TaskHistoryExecutor
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-28 13:50:12
 */
@Getter
@Setter
public class TaskHistoryExecutor extends AbstractExecutor<TaskHistory> {

    private TaskHistory taskHistory;

    protected TaskHistoryExecutor(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
        this.taskHistory = new TaskHistory();
    }

    /**
     * 保存审批信息
     *
     * @return TaskApprove
     *
     * @author wangweijun
     * @since 2024/6/24 20:02
     */
    public TaskHistory save() {
        return super.save(taskHistory);
    }

}
