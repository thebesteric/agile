package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.instance;

import cn.hutool.core.text.CharSequenceUtil;
import io.github.thebesteric.framework.agile.commons.exception.InvalidParamsException;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.BusinessInfo;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.RequestConditions;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowInstance;

/**
 * WorkflowInstanceBuilder
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-24 10:02:17
 */
public class WorkflowInstanceBuilder extends AbstractBuilder<WorkflowInstance> {

    private final WorkflowInstance workflowInstance;

    private WorkflowInstanceBuilder(WorkflowInstance workflowInstance) {
        this.workflowInstance = workflowInstance;
    }

    public static WorkflowInstanceBuilder builder() {
        return new WorkflowInstanceBuilder(new WorkflowInstance());
    }

    public WorkflowInstanceBuilder tenantId(String tenantId) {
        this.workflowInstance.setTenantId(tenantId);
        return this;
    }

    public WorkflowInstanceBuilder workflowDefinitionId(Integer workflowDefinitionId) {
        this.workflowInstance.setWorkflowDefinitionId(workflowDefinitionId);
        return this;
    }

    public WorkflowInstanceBuilder requesterId(String requesterId) {
        this.workflowInstance.setRequesterId(requesterId);
        return this;
    }

    public WorkflowInstanceBuilder requestConditions(RequestConditions requestConditions) {
        this.workflowInstance.setRequestConditions(requestConditions);
        return this;
    }

    public WorkflowInstanceBuilder businessType(BusinessInfo businessInfo) {
        this.workflowInstance.setBusinessInfo(businessInfo);
        return this;
    }

    public WorkflowInstanceBuilder desc(String desc) {
        this.workflowInstance.setDesc(desc);
        return this;
    }

    public WorkflowInstance build() {
        String tenantId = this.workflowInstance.getTenantId();
        Integer workflowDefinitionId = this.workflowInstance.getWorkflowDefinitionId();
        String requesterId = this.workflowInstance.getRequesterId();
        if (tenantId == null || workflowDefinitionId == null || CharSequenceUtil.isEmpty(requesterId)) {
            throw new InvalidParamsException("tenantId, workflowDefinitionId, requesterId cannot be empty");
        }
        return super.build(this.workflowInstance);
    }

}
