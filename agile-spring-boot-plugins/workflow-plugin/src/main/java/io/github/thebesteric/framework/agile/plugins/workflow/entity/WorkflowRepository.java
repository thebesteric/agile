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
 * Repository
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-14 10:08:46
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@EntityClass(value = "awf_wf_repository", comment = "流程附件表")
public class WorkflowRepository extends BaseEntity {
    @Serial
    private static final long serialVersionUID = -1178875905573963929L;

    @EntityColumn(name = "tenant_id", length = 32, nullable = false, comment = "租户 ID")
    private String tenantId;

    @EntityColumn(name = "wf_inst_id", nullable = false, comment = "流程实例 ID")
    private Integer workflowInstanceId;

    @EntityColumn(name = "attachment_type", length = 64, comment = "附件类型")
    private String attachmentType;

    @EntityColumn(name = "attachment_id", length = 64, nullable = false, comment = "附件 ID")
    private String attachmentId;

    @EntityColumn(name = "attachment_name", length = 64, comment = "附件名称")
    private String attachmentName;

    @EntityColumn(name = "attachment_suffix", length = 64, comment = "附件后缀")
    private String attachmentSuffix;

    @EntityColumn(name = "attachment_uri", comment = "附件 URI")
    private String attachmentUri;

    @EntityColumn(name = "attachment_content", type = EntityColumn.Type.MEDIUM_BLOB, comment = "附件内容")
    private byte[] attachmentContent;

    public static WorkflowRepository of(ResultSet rs) throws SQLException {
        WorkflowRepository workflowInstance = new WorkflowRepository();
        workflowInstance.setTenantId(rs.getString("tenant_id"));
        workflowInstance.setWorkflowInstanceId(rs.getInt("wf_inst_id"));
        workflowInstance.setAttachmentType(rs.getString("attachment_type"));
        workflowInstance.setAttachmentId(rs.getString("attachment_id"));
        workflowInstance.setAttachmentName(rs.getString("attachment_name"));
        workflowInstance.setAttachmentSuffix(rs.getString("attachment_suffix"));
        workflowInstance.setAttachmentUri(rs.getString("attachment_uri"));
        workflowInstance.setAttachmentContent(rs.getBytes("attachment_content"));
        return of(workflowInstance, rs);
    }
}
