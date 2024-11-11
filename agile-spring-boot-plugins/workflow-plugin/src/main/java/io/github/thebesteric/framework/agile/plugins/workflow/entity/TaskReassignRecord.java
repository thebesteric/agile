package io.github.thebesteric.framework.agile.plugins.workflow.entity;

import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityClass;
import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityColumn;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.ApproverIdType;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serial;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 任务转派记录
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-11-07 15:56:21
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@EntityClass(value = "awf_task_reassign_record", comment = "任务转派记录")
public class TaskReassignRecord extends BaseEntity {
    @Serial
    private static final long serialVersionUID = -4918386470004199682L;

    @EntityColumn(name = "tenant_id", length = 32, nullable = false, comment = "租户 ID")
    private String tenantId;

    @EntityColumn(name = "wf_inst_id", nullable = false, comment = "流程实例 ID")
    private Integer workflowInstanceId;

    @EntityColumn(name = "task_inst_id", nullable = false, comment = "任务实例 ID")
    private Integer taskInstanceId;

    @EntityColumn(name = "task_approve_id", nullable = false, comment = "任务审批 ID")
    private Integer taskApproveId;

    @EntityColumn(name = "from_role_id", length = 32, comment = "from 角色 ID")
    private String fromRoleId;

    @EntityColumn(name = "from_role_name", length = 64, comment = "from 角色名称")
    private String fromRoleName;

    @EntityColumn(name = "from_role_seq", type = EntityColumn.Type.SMALL_INT, comment = "from 角色审批顺序")
    private Integer fromRoleSeq;

    @EntityColumn(name = "from_role_desc", comment = "from 角色描述")
    private String fromRoleDesc;

    @EntityColumn(name = "from_user_id", length = 32, nullable = false, comment = "from 用户 ID")
    private String fromUserId;

    @EntityColumn(name = "from_user_name", length = 64, comment = "from 用户名称")
    private String fromUserName;

    @EntityColumn(name = "from_user_seq", type = EntityColumn.Type.SMALL_INT, comment = "from 用户审批顺序")
    private Integer fromUserSeq;

    @EntityColumn(name = "from_user_desc", comment = "from 用户描述")
    private String fromUserDesc;

    @EntityColumn(name = "to_role_id", length = 32, comment = "to 角色 ID")
    private String toRoleId;

    @EntityColumn(name = "to_role_name", length = 64, comment = "to 角色名称")
    private String toRoleName;

    @EntityColumn(name = "to_role_seq", type = EntityColumn.Type.SMALL_INT, comment = "to 角色审批顺序")
    private Integer toRoleSeq;

    @EntityColumn(name = "to_role_desc", comment = "to 角色描述")
    private String toRoleDesc;

    @EntityColumn(name = "to_user_id", length = 32, nullable = false, comment = "to 用户 ID")
    private String toUserId;

    @EntityColumn(name = "to_user_name", length = 64, comment = "to 用户名称")
    private String toUserName;

    @EntityColumn(name = "to_user_seq", type = EntityColumn.Type.SMALL_INT, comment = "to 用户审批顺序")
    private Integer toUserSeq;

    @EntityColumn(name = "to_user_desc", comment = "to 用户描述")
    private String toUserDesc;

    @EntityColumn(name = "approver_id_type", type = EntityColumn.Type.TINY_INT, nullable = false, comment = "审核人 ID 类型")
    private ApproverIdType approverIdType = ApproverIdType.USER;

    @EntityColumn(name = "comment", length = 255, comment = "转派意见")
    private String comment;

    public static TaskReassignRecord of(ResultSet rs) throws SQLException {
        TaskReassignRecord taskReassignRecord = new TaskReassignRecord();
        taskReassignRecord.setTenantId(rs.getString("tenant_id"));
        taskReassignRecord.setWorkflowInstanceId(rs.getInt("wf_inst_id"));
        taskReassignRecord.setTaskInstanceId(rs.getInt("task_inst_id"));
        taskReassignRecord.setTaskApproveId(rs.getInt("task_approve_id"));

        taskReassignRecord.setFromRoleId(rs.getString("from_role_id"));
        taskReassignRecord.setFromRoleName(rs.getString("from_role_name"));
        taskReassignRecord.setFromRoleDesc(rs.getString("from_role_desc"));
        // 解决 rs.getInt("xxx") null 值会返回 0 的问题
        Object fromRoleSeqObject = rs.getObject("from_role_seq");
        if (fromRoleSeqObject != null) {
            taskReassignRecord.setFromRoleSeq((Integer) fromRoleSeqObject);
        }
        taskReassignRecord.setFromUserId(rs.getString("from_user_id"));
        taskReassignRecord.setFromUserName(rs.getString("from_user_name"));
        taskReassignRecord.setFromUserDesc(rs.getString("from_user_desc"));
        // 解决 rs.getInt("xxx") null 值会返回 0 的问题
        Object fromUserSeqObject = rs.getObject("from_user_seq");
        if (fromUserSeqObject != null) {
            taskReassignRecord.setFromUserSeq((Integer) fromUserSeqObject);
        }

        taskReassignRecord.setToRoleId(rs.getString("to_role_id"));
        taskReassignRecord.setToRoleName(rs.getString("to_role_name"));
        taskReassignRecord.setToRoleDesc(rs.getString("to_role_desc"));
        // 解决 rs.getInt("xxx") null 值会返回 0 的问题
        Object toRoleSeqObject = rs.getObject("to_role_seq");
        if (toRoleSeqObject != null) {
            taskReassignRecord.setToRoleSeq((Integer) toRoleSeqObject);
        }
        taskReassignRecord.setToUserId(rs.getString("to_user_id"));
        taskReassignRecord.setToUserName(rs.getString("to_user_name"));
        taskReassignRecord.setToUserDesc(rs.getString("to_user_desc"));
        // 解决 rs.getInt("xxx") null 值会返回 0 的问题
        Object toUserSeqObject = rs.getObject("to_user_seq");
        if (toUserSeqObject != null) {
            taskReassignRecord.setToUserSeq((Integer) toUserSeqObject);
        }

        taskReassignRecord.setApproverIdType(ApproverIdType.of(rs.getInt("approver_id_type")));
        taskReassignRecord.setComment(rs.getString("comment"));
        return of(taskReassignRecord, rs);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskReassignRecord that = (TaskReassignRecord) o;
        return new EqualsBuilder().appendSuper(super.equals(o)).append(id, that.id).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).appendSuper(super.hashCode()).append(id).toHashCode();
    }
}
