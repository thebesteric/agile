package io.github.thebesteric.framework.agile.plugins.workflow.entity;

import io.github.thebesteric.framework.agile.commons.util.DateUtils;
import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityClass;
import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityColumn;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.PublishStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * 流程定义表
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-11 20:43:34
 */
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Data
@EntityClass(value = "awf_wf_definition", comment = "流程定义表")
public class WorkflowDefinition extends BaseEntity {
    @Serial
    private static final long serialVersionUID = -3771186800016440103L;

    @EntityColumn(length = 32, uniqueGroup = "tenant_key", nullable = false, comment = "租户 ID")
    private String tenantId;

    @EntityColumn(length = 32, uniqueGroup = "tenant_key", nullable = false, comment = "流程标识")
    private String key = "default";

    @EntityColumn(length = 32, nullable = false, comment = "流程名称")
    private String name;

    @EntityColumn(length = 32, comment = "流程类型（用于类型分类）")
    private String type = "default";

    @EntityColumn(type = EntityColumn.Type.TINY_INT, nullable = false, comment = "发布状态")
    private PublishStatus publish = PublishStatus.UNPUBLISHED;

    @EntityColumn(comment = "发布日期")
    protected Date publishedAt;

    public void setPublish(PublishStatus publish) {
        this.publish = publish;
        this.publishedAt = new Date();
    }

    public boolean isPublished() {
        return PublishStatus.PUBLISHED == this.publish;
    }

    public static WorkflowDefinition of(ResultSet rs) throws SQLException {
        WorkflowDefinition workflowDefinition = new WorkflowDefinition();
        workflowDefinition.setTenantId(rs.getString("tenant_id"));
        workflowDefinition.setKey(rs.getString("key"));
        workflowDefinition.setName(rs.getString("name"));
        workflowDefinition.setType(rs.getString("type"));
        workflowDefinition.setPublish(PublishStatus.of(rs.getInt("publish")));
        workflowDefinition.setCreatedAt(DateUtils.parseToDateTime(rs.getString("published_at")));
        return of(workflowDefinition, rs);
    }
}
