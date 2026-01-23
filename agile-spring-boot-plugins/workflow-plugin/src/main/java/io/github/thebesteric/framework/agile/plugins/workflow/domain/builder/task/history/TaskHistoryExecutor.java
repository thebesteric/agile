package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.task.history;

import io.github.thebesteric.framework.agile.core.domain.page.PagingResponse;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.TaskHistory;
import lombok.Getter;
import lombok.Setter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

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

    /**
     * 查询审批日志
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     * @param workflowInstanceId   流程实例 ID
     * @param requesterId          发起人 ID
     * @param page                 当前页
     * @param pageSize             每页显示数量
     *
     * @return Page<TaskHistory>
     *
     * @author wangweijun
     * @since 2024/7/11 15:06
     */
    public PagingResponse<TaskHistory> findTaskHistories(String tenantId, Integer workflowDefinitionId, Integer workflowInstanceId, String requesterId, Integer page, Integer pageSize) {
        String selectSql = """
                SELECT h.* FROM awf_task_history h 
                LEFT JOIN awf_wf_instance wi ON wi.id = h.wf_inst_id 
                WHERE h.tenant_id = ? AND h.`state` = 1
                """;
        if (workflowDefinitionId != null) {
            selectSql += " AND wi.wf_def_id = %s".formatted(workflowDefinitionId);
        }
        if (workflowInstanceId != null) {
            selectSql += " AND h.wf_inst_id = %s".formatted(workflowInstanceId);
        }
        if (requesterId != null) {
            selectSql += " AND wi.requester_id = %s".formatted(requesterId);
        }

        String countSql = "SELECT COUNT(*) FROM (" + selectSql + ") AS t";
        Integer count = this.jdbcTemplate.queryForObject(countSql, Integer.class, tenantId);

        selectSql += " ORDER BY h.`id` DESC, h.`created_at` DESC LIMIT ? OFFSET ?";
        Integer offset = (page - 1) * pageSize;
        List<TaskHistory> records = this.jdbcTemplate.query(selectSql, (rs, rowNum) -> TaskHistory.of(rs), tenantId, pageSize, offset);

        return PagingResponse.of(page, pageSize, count, records);
    }
}
