package io.github.thebesteric.framework.agile.plugins.workflow.entity;

import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityClass;
import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityColumn;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.RoleApproveStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

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

    @EntityColumn(name = "status", type = EntityColumn.Type.TINY_INT, nullable = false, comment = "审批状态")
    private RoleApproveStatus status = RoleApproveStatus.IN_PROGRESS;

    @EntityColumn(name = "reassigned_task_role_approve_record_id", comment = "被委派的角色任务实例审批记录 ID")
    private Integer reassignedTaskRoleApproveRecordId;

    @EntityColumn(name = "comment", length = 255, comment = "审批意见")
    private String comment;

    /**
     * 创建委派记录
     *
     * @param from 被委派的审批记录
     *
     * @return TaskRoleApproveRecord
     *
     * @author wangweijun
     * @since 2024/11/8 15:17
     */
    public static TaskRoleApproveRecord reassignFrom(TaskRoleApproveRecord from) {
        TaskRoleApproveRecord reassignTo = new TaskRoleApproveRecord();
        BeanUtils.copyProperties(from, reassignTo, "id", "status", "comment", "createdAt", "updatedAt", "createdBy", "updatedBy", "version");
        reassignTo.setStatus(RoleApproveStatus.IN_PROGRESS);
        return reassignTo;
    }

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
        this.setStatus(RoleApproveStatus.IN_PROGRESS);
        this.setComment(comment);
    }

    /**
     * 将审批实例设置为：挂起
     *
     * @author wangweijun
     * @since 2024/9/26 10:06
     */
    public void convertToApproveStatusSuspended() {
        this.setStatus(RoleApproveStatus.SUSPEND);
        this.setComment(null);
    }

    /**
     * 将审批实例设置为：拒绝
     *
     * @author wangweijun
     * @since 2024/9/26 10:06
     */
    public void convertToApproveStatusRejected(String comment) {
        this.setStatus(RoleApproveStatus.REJECTED);
        this.setComment(comment);
    }

    /**
     * 将审批实例设置为：跳过
     *
     * @author wangweijun
     * @since 2024/11/6 09:51
     */
    public void convertToApproveStatusSkipped() {
        this.convertToApproveStatusSkipped(null);
    }

    /**
     * 将审批实例设置为：跳过
     *
     * @author wangweijun
     * @since 2024/11/6 09:51
     */
    public void convertToApproveStatusSkipped(String comment) {
        this.setStatus(RoleApproveStatus.SKIPPED);
        this.setComment(comment);
    }

    /**
     * 将审批实例设置为：中断
     *
     * @author wangweijun
     * @since 2024/11/6 09:51
     */
    public void convertToApproveStatusInterrupted() {
        this.convertToApproveStatusInterrupted(null);
    }

    /**
     * 将审批实例设置为：中断
     *
     * @author wangweijun
     * @since 2024/11/6 09:51
     */
    public void convertToApproveStatusInterrupted(String comment) {
        this.setStatus(RoleApproveStatus.INTERRUPTED);
        this.setComment(comment);
    }

    public static TaskRoleApproveRecord of(ResultSet rs) throws SQLException {
        TaskRoleApproveRecord taskRoleApproveRecord = new TaskRoleApproveRecord();
        taskRoleApproveRecord.setTenantId(rs.getString("tenant_id"));
        taskRoleApproveRecord.setWorkflowInstanceId(rs.getInt("wf_inst_id"));
        taskRoleApproveRecord.setTaskInstanceId(rs.getInt("task_inst_id"));
        taskRoleApproveRecord.setTaskApproveId(rs.getInt("task_approve_id"));
        taskRoleApproveRecord.setNodeRoleAssignmentId(rs.getInt("node_role_assignment_id"));
        taskRoleApproveRecord.setStatus(RoleApproveStatus.of(rs.getInt("status")));
        // 解决 rs.getInt("xxx") null 值会返回 0 的问题
        Object reassignedTaskRoleApproveRecordIdObject = rs.getObject("reassigned_task_role_approve_record_id");
        if (reassignedTaskRoleApproveRecordIdObject != null) {
            taskRoleApproveRecord.setReassignedTaskRoleApproveRecordId((Integer) reassignedTaskRoleApproveRecordIdObject);
        }
        taskRoleApproveRecord.setComment(rs.getString("comment"));
        return of(taskRoleApproveRecord, rs);
    }
}
