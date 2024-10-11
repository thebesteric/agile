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
 * 流程用户任务关联表
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-09-10 11:04:53
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@EntityClass(value = "awf_wf_assignment", comment = "流程用户任务关联表")
public class WorkflowAssignment extends BaseEntity {
    @Serial
    private static final long serialVersionUID = -4526605448360470960L;

    @EntityColumn(name = "tenant_id", length = 32, nullable = false, comment = "租户 ID")
    private String tenantId;

    @EntityColumn(name = "wf_def_id", nullable = false, comment = "流程定义 ID")
    private Integer workflowDefinitionId;

    @EntityColumn(name = "approver_id", length = 32, nullable = false, comment = "用户 ID")
    private String approverId;

    @EntityColumn(name = "approver_name", length = 64, comment = "用户名称")
    private String approverName;

    @EntityColumn(name = "approver_seq", type = EntityColumn.Type.SMALL_INT, comment = "审批顺序")
    private Integer approverSeq;

    @EntityColumn(name = "approver_desc", comment = "用户备注")
    private String approverDesc;

    public static WorkflowAssignment of(ResultSet rs) throws SQLException {
        WorkflowAssignment nodeAssignment = new WorkflowAssignment();
        nodeAssignment.setTenantId(rs.getString("tenant_id"));
        nodeAssignment.setWorkflowDefinitionId(rs.getInt("wf_def_id"));
        nodeAssignment.setApproverId(rs.getString("approver_id"));
        nodeAssignment.setApproverName(rs.getString("approver_name"));
        nodeAssignment.setApproverDesc(rs.getString("approver_desc"));
        // 解决 rs.getInt("xxx") null 值会返回 0 的问题
        Object userSeqObject = rs.getObject("approver_seq");
        if (userSeqObject != null) {
            nodeAssignment.setApproverSeq((Integer) userSeqObject);
        }
        return of(nodeAssignment, rs);
    }
}
