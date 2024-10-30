package io.github.thebesteric.framework.agile.plugins.workflow.entity;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityClass;
import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityColumn;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.WorkflowStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.RequestConditions;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

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

    @EntityColumn(length = 255, comment = "业务类型")
    private String businessType;

    @EntityColumn(length = 32, comment = "业务标识")
    private String businessId;

    @EntityColumn(name = "req_conditions", type = EntityColumn.Type.JSON, comment = "请求条件")
    private RequestConditions requestConditions;

    @EntityColumn(type = EntityColumn.Type.TINY_INT, nullable = false, comment = "流程状态")
    private WorkflowStatus status;

    public static WorkflowInstance of(ResultSet rs) throws SQLException {
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setTenantId(rs.getString("tenant_id"));
        workflowInstance.setWorkflowDefinitionId(rs.getInt("wf_def_id"));
        workflowInstance.setRequesterId(rs.getString("requester_id"));
        workflowInstance.setRequesterName(rs.getString("requester_name"));
        workflowInstance.setRequesterDesc(rs.getString("requester_desc"));
        workflowInstance.setBusinessType(rs.getString("business_type"));
        workflowInstance.setBusinessId(rs.getString("business_id"));
        String reqConditionsStr = rs.getString("req_conditions");
        if (CharSequenceUtil.isNotEmpty(reqConditionsStr)) {
            workflowInstance.setRequestConditions(JSONUtil.toBean(reqConditionsStr, RequestConditions.class));
        }
        workflowInstance.setStatus(WorkflowStatus.of(rs.getInt("status")));
        return of(workflowInstance, rs);
    }
}
