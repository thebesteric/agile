package io.github.thebesteric.framework.agile.plugins.workflow.entity;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityClass;
import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityColumn;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.WorkflowStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.BusinessInfo;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.RequestConditions;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.response.WorkflowDefinitionFlowSchema;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.response.WorkflowInstanceApproveRecords;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.beans.Transient;
import java.io.Serial;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 流程实例表
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-11 21:27:39
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@EntityClass(value = "awf_wf_instance", comment = "流程实例表")
public class WorkflowInstance extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 6601397279728054369L;

    @EntityColumn(name = "tenant_id", length = 32, nullable = false, comment = "租户 ID")
    private String tenantId;

    @EntityColumn(name = "wf_def_id", nullable = false, comment = "流程定义 ID")
    private Integer workflowDefinitionId;

    @EntityColumn(name = "requester_id", length = 32, nullable = false, comment = "流程发起人 ID")
    private String requesterId;

    @EntityColumn(name = "requester_name", length = 64, comment = "流程发起人名称")
    private String requesterName;

    @EntityColumn(name = "requester_desc", comment = "流程发起人描述")
    private String requesterDesc;

    @EntityColumn(name = "business_info", type = EntityColumn.Type.JSON, comment = "业务信息")
    private BusinessInfo businessInfo;

    @EntityColumn(name = "req_conditions", type = EntityColumn.Type.JSON, comment = "请求条件")
    private RequestConditions requestConditions;

    @EntityColumn(name = "status", type = EntityColumn.Type.TINY_INT, nullable = false, comment = "流程状态")
    private WorkflowStatus status = WorkflowStatus.WAITING;

    @EntityColumn(name = "flow_schema", type = EntityColumn.Type.JSON, comment = "流程定义")
    private WorkflowDefinitionFlowSchema flowSchema;

    @EntityColumn(name = "approve_records", type = EntityColumn.Type.JSON, comment = "流程实例审批记录")
    private WorkflowInstanceApproveRecords approveRecords;

    /**
     * 流程是否已经完成
     *
     * @return boolean
     * @author wangweijun
     * @since 2024/11/7 19:09
     */
    @Transient
    public boolean isFinished() {
        return WorkflowStatus.isFinished(this.status);
    }

    public static WorkflowInstance of(ResultSet rs) throws SQLException {
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setTenantId(rs.getString("tenant_id"));
        workflowInstance.setWorkflowDefinitionId(rs.getInt("wf_def_id"));
        workflowInstance.setRequesterId(rs.getString("requester_id"));
        workflowInstance.setRequesterName(rs.getString("requester_name"));
        workflowInstance.setRequesterDesc(rs.getString("requester_desc"));
        String businessInfoStr = rs.getString("business_info");
        if (CharSequenceUtil.isNotEmpty(businessInfoStr)) {
            workflowInstance.setBusinessInfo(JSONUtil.toBean(businessInfoStr, BusinessInfo.class));
        }
        String reqConditionsStr = rs.getString("req_conditions");
        if (CharSequenceUtil.isNotEmpty(reqConditionsStr)) {
            workflowInstance.setRequestConditions(JSONUtil.toBean(reqConditionsStr, RequestConditions.class));
        }
        String approveRecordsStr = rs.getString("approve_records");
        if (CharSequenceUtil.isNotEmpty(approveRecordsStr)) {
            workflowInstance.setApproveRecords(JSONUtil.toBean(approveRecordsStr, WorkflowInstanceApproveRecords.class));
        }
        String flowSchemaStr = rs.getString("flow_schema");
        if (CharSequenceUtil.isNotEmpty(flowSchemaStr)) {
            workflowInstance.setFlowSchema(JSONUtil.toBean(flowSchemaStr, WorkflowDefinitionFlowSchema.class));
        }
        workflowInstance.setStatus(WorkflowStatus.of(rs.getInt("status")));
        return of(workflowInstance, rs);
    }
}
