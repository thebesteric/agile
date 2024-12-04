package io.github.thebesteric.framework.agile.plugins.workflow.entity;

import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityClass;
import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityColumn;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.TaskHistoryMessage;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serial;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 实例审批记录
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-11 21:36:07
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@EntityClass(value = "awf_task_history", comment = "实例审批记录")
public class TaskHistory extends BaseEntity {
    @Serial
    private static final long serialVersionUID = -6499457922655850862L;

    @EntityColumn(name = "tenant_id", length = 32, nullable = false, comment = "租户 ID")
    private String tenantId;

    @EntityColumn(name = "wf_inst_id", nullable = false, comment = "流程实例 ID")
    private Integer workflowInstanceId;

    @EntityColumn(name = "task_inst_id", comment = "任务实例 ID")
    private Integer taskInstanceId;

    @EntityColumn(name = "title", length = 64, comment = "日志标题")
    private String title;

    @EntityColumn(type = EntityColumn.Type.VARCHAR, length = 2048, nullable = false, comment = "日志信息")
    private TaskHistoryMessage message;

    public static TaskHistory of(ResultSet rs) throws SQLException {
        TaskHistory taskApprove = new TaskHistory();
        taskApprove.setTenantId(rs.getString("tenant_id"));
        taskApprove.setWorkflowInstanceId(rs.getInt("wf_inst_id"));
        taskApprove.setTaskInstanceId(rs.getInt("task_inst_id"));
        taskApprove.setTitle(rs.getString("title"));
        taskApprove.setMessage(TaskHistoryMessage.of(rs.getString("message")));
        return of(taskApprove, rs);
    }
}
