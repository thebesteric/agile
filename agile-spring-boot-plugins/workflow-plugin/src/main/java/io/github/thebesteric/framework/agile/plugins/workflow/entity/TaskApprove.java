package io.github.thebesteric.framework.agile.plugins.workflow.entity;

import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityClass;
import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityColumn;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.ActiveStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.ApproveStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serial;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 任务实例审批人表
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-12 17:45:35
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@EntityClass(value = "awf_task_approve", comment = "任务实例审批表")
public class TaskApprove extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 2797791705099661803L;

    @EntityColumn(name = "tenant_id", length = 32, nullable = false, comment = "租户 ID")
    private String tenantId;

    @EntityColumn(name = "wf_inst_id", nullable = false, comment = "流程实例 ID")
    private Integer workflowInstanceId;

    @EntityColumn(name = "task_inst_id", nullable = false, comment = "任务实例 ID")
    private Integer taskInstanceId;

    @EntityColumn(name = "approver_id", length = 32, nullable = false, comment = "审批人")
    private String approverId;

    @EntityColumn(name = "approver_seq", type = EntityColumn.Type.SMALL_INT, comment = "审批顺序")
    private Integer approverSeq;

    @EntityColumn(type = EntityColumn.Type.TINY_INT, nullable = false, comment = "审批状态")
    private ApproveStatus status = ApproveStatus.IN_PROGRESS;

    @EntityColumn(type = EntityColumn.Type.TINY_INT, nullable = false, comment = "活动状态")
    private ActiveStatus active = ActiveStatus.ACTIVE;

    @EntityColumn(length = 255, comment = "审批意见")
    private String comment;

    public static TaskApprove of(ResultSet rs) throws SQLException {
        TaskApprove taskApprove = new TaskApprove();
        taskApprove.setTenantId(rs.getString("tenant_id"));
        taskApprove.setWorkflowInstanceId(rs.getInt("wf_inst_id"));
        taskApprove.setTaskInstanceId(rs.getInt("task_inst_id"));
        taskApprove.setApproverId(rs.getString("approver_id"));
        // 解决 rs.getInt("xxx") null 值会返回 0 的问题
        Object approverSeqObject = rs.getObject("approver_seq");
        if (approverSeqObject != null) {
            taskApprove.setApproverSeq((Integer) approverSeqObject);
        }
        taskApprove.setStatus(ApproveStatus.of(rs.getInt("status")));
        taskApprove.setActive(ActiveStatus.of(rs.getInt("active")));
        taskApprove.setComment(rs.getString("comment"));
        return of(taskApprove, rs);
    }
}
