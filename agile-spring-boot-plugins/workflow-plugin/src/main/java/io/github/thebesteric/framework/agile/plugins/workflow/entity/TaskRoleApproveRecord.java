package io.github.thebesteric.framework.agile.plugins.workflow.entity;

import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityClass;
import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityColumn;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.ApproveStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 角色任务实例审批记录表
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-09-14 11:40:39
 */
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@EntityClass(value = "awf_task_role_approve_record", comment = "角色任务实例审批记录表")
public class TaskRoleApproveRecord extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 2145825445484000896L;

    @EntityColumn(name = "tenant_id", length = 32, nullable = false, comment = "租户 ID")
    private String tenantId;

    @EntityColumn(name = "wf_inst_id", nullable = false, comment = "流程实例 ID")
    private Integer workflowInstanceId;

    @EntityColumn(name = "task_inst_id", nullable = false, comment = "任务实例 ID")
    private Integer taskInstanceId;

    @EntityColumn(name = "task_approve_id", nullable = false, comment = "任务审批 ID")
    private Integer taskApproveId;

    @EntityColumn(name = "node_role_assignment_id", nullable = false, comment = "角色用户 ID")
    private Integer nodeRoleAssignmentId;

    @EntityColumn(type = EntityColumn.Type.TINY_INT, nullable = false, comment = "审批状态")
    private ApproveStatus status = ApproveStatus.IN_PROGRESS;

    @EntityColumn(length = 255, comment = "审批意见")
    private String comment;

    /**
     * 将审批实例设置为：进行中
     *
     * @author wangweijun
     * @since 2024/9/25 16:23
     */
    public void convertToApproveStatusInProgress() {
        this.convertToApproveStatusInProgress(null);
    }

    /**
     * 将审批实例设置为：进行中
     *
     * @author wangweijun
     * @since 2024/9/25 16:23
     */
    public void convertToApproveStatusInProgress(String comment) {
        this.setStatus(ApproveStatus.IN_PROGRESS);
        this.setComment(comment);
    }

    /**
     * 将审批实例设置为：挂起
     *
     * @author wangweijun
     * @since 2024/9/26 10:06
     */
    public void convertToApproveStatusSuspended() {
        this.setStatus(ApproveStatus.SUSPEND);
        this.setComment(null);
    }

    /**
     * 将审批实例设置为：拒绝
     *
     * @author wangweijun
     * @since 2024/9/26 10:06
     */
    public void convertToApproveStatusRejected(String comment) {
        this.setStatus(ApproveStatus.REJECTED);
        this.setComment(comment);
    }

    public static TaskRoleApproveRecord of(ResultSet rs) throws SQLException {
        TaskRoleApproveRecord taskRoleApproveRecord = new TaskRoleApproveRecord();
        taskRoleApproveRecord.setTenantId(rs.getString("tenant_id"));
        taskRoleApproveRecord.setWorkflowInstanceId(rs.getInt("wf_inst_id"));
        taskRoleApproveRecord.setTaskInstanceId(rs.getInt("task_inst_id"));
        taskRoleApproveRecord.setTaskApproveId(rs.getInt("task_approve_id"));
        taskRoleApproveRecord.setNodeRoleAssignmentId(rs.getInt("node_role_assignment_id"));
        taskRoleApproveRecord.setStatus(ApproveStatus.of(rs.getInt("status")));
        taskRoleApproveRecord.setComment(rs.getString("comment"));
        return of(taskRoleApproveRecord, rs);
    }
}
