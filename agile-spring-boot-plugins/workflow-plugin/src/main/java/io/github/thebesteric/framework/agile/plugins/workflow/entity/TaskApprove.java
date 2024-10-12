package io.github.thebesteric.framework.agile.plugins.workflow.entity;

import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityClass;
import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityColumn;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.ActiveStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.ApproveStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.ApproverIdType;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.WorkflowConstants;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.base.BaseEntity;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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

    @EntityColumn(name = "approver_id", length = 32, nullable = false, comment = "审批人 ID")
    private String approverId;

    @EntityColumn(name = "approver_seq", type = EntityColumn.Type.SMALL_INT, comment = "审批顺序")
    private Integer approverSeq;

    @EntityColumn(type = EntityColumn.Type.TINY_INT, nullable = false, comment = "审批人 ID 类型")
    private ApproverIdType approverIdType = ApproverIdType.USER;

    @EntityColumn(type = EntityColumn.Type.TINY_INT, nullable = false, comment = "审批状态")
    private ApproveStatus status = ApproveStatus.IN_PROGRESS;

    @EntityColumn(type = EntityColumn.Type.TINY_INT, nullable = false, comment = "活动状态")
    private ActiveStatus active = ActiveStatus.ACTIVE;

    @EntityColumn(length = 255, comment = "审批意见")
    private String comment;

    /**
     * 是否未设置指定审批人
     *
     * @return boolean
     *
     * @author wangweijun
     * @since 2024/9/9 16:19
     */
    public boolean isUnSettingApprover() {
        return this.approverId.startsWith(WorkflowConstants.DYNAMIC_ASSIGNMENT_APPROVER_VALUE_PREFIX) && this.approverId.endsWith(WorkflowConstants.DYNAMIC_ASSIGNMENT_APPROVER_VALUE_SUFFIX);
    }

    /**
     * 将审批实例设置为：进行中
     *
     * @author wangweijun
     * @since 2024/9/11 18:36
     */
    public void convertToApproveStatusInProgress() {
        this.convertToApproveStatusInProgress(null);
    }

    /**
     * 将审批实例设置为：进行中
     *
     * @param comment 审批意见
     *
     * @author wangweijun
     * @since 2024/9/11 18:39
     */
    public void convertToApproveStatusInProgress(String comment) {
        this.setStatus(ApproveStatus.IN_PROGRESS);
        this.setActive(ActiveStatus.ACTIVE);
        this.setComment(comment);
    }

    /**
     * 将审批实例设置为：跳过
     *
     * @author wangweijun
     * @since 2024/9/11 18:31
     */
    public void convertToApproveStatusSkipped() {
        this.setStatus(ApproveStatus.SKIPPED);
        this.setActive(ActiveStatus.INACTIVE);
        this.setComment(null);
    }

    /**
     * 将审批实例设置为：已同意
     *
     * @author wangweijun
     * @since 2024/9/18 16:49
     */
    public void convertToApproveStatusApproved() {
        this.convertToApproveStatusApproved(null);
    }

    /**
     * 将审批实例设置为：已同意
     *
     * @author wangweijun
     * @since 2024/9/18 16:49
     */
    public void convertToApproveStatusApproved(String comment) {
        this.setStatus(ApproveStatus.APPROVED);
        this.setActive(ActiveStatus.INACTIVE);
        this.setComment(comment);
    }

    /**
     * 将审批实例设置为：已弃权
     *
     * @author wangweijun
     * @since 2024/9/25 12:08
     */
    public void convertToApproveStatusAbandoned(String comment) {
        this.setStatus(ApproveStatus.ABANDONED);
        this.setActive(ActiveStatus.INACTIVE);
        this.setComment(comment);
    }

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
        taskApprove.setApproverIdType(ApproverIdType.of(rs.getInt("approver_id_type")));
        taskApprove.setStatus(ApproveStatus.of(rs.getInt("status")));
        taskApprove.setActive(ActiveStatus.of(rs.getInt("active")));
        taskApprove.setComment(rs.getString("comment"));
        return of(taskApprove, rs);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskApprove that = (TaskApprove) o;
        return new EqualsBuilder().appendSuper(super.equals(o)).append(tenantId, that.tenantId).append(workflowInstanceId, that.workflowInstanceId).append(taskInstanceId, that.taskInstanceId).append(approverId, that.approverId).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).appendSuper(super.hashCode()).append(tenantId).append(workflowInstanceId).append(taskInstanceId).append(approverId).toHashCode();
    }
}
