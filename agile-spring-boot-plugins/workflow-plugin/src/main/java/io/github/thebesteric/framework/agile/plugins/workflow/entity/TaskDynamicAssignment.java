package io.github.thebesteric.framework.agile.plugins.workflow.entity;

import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityClass;
import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityColumn;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serial;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 任务动态用户定义表：针对于动态审批节点
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-10-17 17:09:31
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@EntityClass(value = "awf_task_dynamic_assignment", comment = "任务动态用户定义表")
public class TaskDynamicAssignment extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 5558922352732644764L;

    @EntityColumn(name = "tenant_id", length = 32, nullable = false, comment = "租户 ID")
    private String tenantId;

    @EntityColumn(name = "node_def_id", nullable = false, comment = "节点定义 ID")
    private Integer nodeDefinitionId;

    @EntityColumn(name = "task_inst_id", comment = "任务实例 ID")
    private Integer taskInstanceId;

    @EntityColumn(name = "approver_id", length = 32, nullable = false, comment = "审批人 ID")
    private String approverId;

    @EntityColumn(name = "approver_name", length = 64, comment = "审批人名称")
    private String approverName;

    @EntityColumn(name = "approver_seq", type = EntityColumn.Type.SMALL_INT, comment = "审批顺序")
    private Integer approverSeq;

    @EntityColumn(name = "approver_desc", comment = "审批人名称")
    private String approverDesc;

    public static TaskDynamicAssignment of(ResultSet rs) throws SQLException {
        TaskDynamicAssignment taskDynamicAssignment = new TaskDynamicAssignment();
        taskDynamicAssignment.setTenantId(rs.getString("tenant_id"));
        taskDynamicAssignment.setNodeDefinitionId(rs.getInt("node_def_id"));
        taskDynamicAssignment.setTaskInstanceId(rs.getInt("task_inst_id"));
        taskDynamicAssignment.setApproverId(rs.getString("approver_id"));
        taskDynamicAssignment.setApproverName(rs.getString("approver_name"));
        taskDynamicAssignment.setApproverDesc(rs.getString("approver_desc"));
        // 解决 rs.getInt("xxx") null 值会返回 0 的问题
        Object approverSeqObject = rs.getObject("approver_seq");
        if (approverSeqObject != null) {
            taskDynamicAssignment.setApproverSeq((Integer) approverSeqObject);
        }
        return of(taskDynamicAssignment, rs);
    }
}
