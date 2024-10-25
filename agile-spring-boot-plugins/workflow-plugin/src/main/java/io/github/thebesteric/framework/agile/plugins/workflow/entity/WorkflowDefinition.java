package io.github.thebesteric.framework.agile.plugins.workflow.entity;

import io.github.thebesteric.framework.agile.commons.util.DateUtils;
import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityClass;
import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityColumn;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.ConditionNotMatchedAnyStrategy;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.ContinuousApproveMode;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.PublishStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.Approver;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Transient;

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

    @EntityColumn(length = 64, uniqueGroup = "tenant_key", nullable = false, comment = "流程标识")
    private String key = "default";

    @EntityColumn(length = 64, nullable = false, comment = "流程名称")
    private String name;

    @EntityColumn(length = 64, comment = "流程类型（用于类型分类）")
    private String type = "default";

    @EntityColumn(type = EntityColumn.Type.TINY_INT, nullable = false, comment = "连续审批方式：默认每个节点都需要审批")
    private ContinuousApproveMode continuousApproveMode = ContinuousApproveMode.APPROVE_ALL;

    @EntityColumn(type = EntityColumn.Type.TINY_INT, nullable = false, comment = "没有条件节点符合时的处理策略: 默认抛出异常")
    private ConditionNotMatchedAnyStrategy conditionNotMatchedAnyStrategy = ConditionNotMatchedAnyStrategy.PROCESS_THROW_EXCEPTION;

    @EntityColumn(nullable = false, defaultExpression = "0", comment = "审批人为空时，是否允许自动审批")
    private boolean allowEmptyAutoApprove = false;

    @EntityColumn(nullable = false, defaultExpression = "1", comment = "是否允许撤回")
    private boolean allowRedo = true;

    @EntityColumn(nullable = false, defaultExpression = "0", comment = "是否必须填写审批意见")
    private boolean requiredComment = false;

    @EntityColumn(type = EntityColumn.Type.TINY_INT, nullable = false, comment = "发布状态")
    private PublishStatus publish = PublishStatus.UNPUBLISHED;

    @EntityColumn(comment = "发布日期")
    private Date publishedAt;

    /** 审批人（当节点审批人为空，且 allowEmptyAutoApprove 为 false 时生效），存储在 WorkflowAssignment 表中 */
    @Transient
    private Approver whenEmptyApprover;

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
        workflowDefinition.setContinuousApproveMode(ContinuousApproveMode.of(rs.getInt("continuous_approve_mode")));
        workflowDefinition.setConditionNotMatchedAnyStrategy(ConditionNotMatchedAnyStrategy.of(rs.getInt("condition_not_matched_any_strategy")));
        workflowDefinition.setAllowEmptyAutoApprove(rs.getInt("allow_empty_auto_approve") == 1);
        workflowDefinition.setAllowRedo(rs.getInt("allow_redo") == 1);
        workflowDefinition.setRequiredComment(rs.getInt("required_comment") == 1);
        workflowDefinition.setPublish(PublishStatus.of(rs.getInt("publish")));
        workflowDefinition.setCreatedAt(DateUtils.parseToDateTime(rs.getString("published_at")));
        return of(workflowDefinition, rs);
    }
}
