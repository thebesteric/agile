package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.history;

import cn.hutool.core.text.CharSequenceUtil;
import io.github.thebesteric.framework.agile.commons.exception.InvalidParamsException;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.DMLOperator;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowDefinition;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowDefinitionHistory;

/**
 * WorkflowDefinitionHistoryBuilder
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-09-30 18:04:35
 */
public class WorkflowDefinitionHistoryBuilder extends AbstractBuilder<WorkflowDefinitionHistory> {

    private final WorkflowDefinitionHistory workflowDefinitionHistory;

    private WorkflowDefinitionHistoryBuilder(WorkflowDefinitionHistory workflowDefinitionHistory) {
        this.workflowDefinitionHistory = workflowDefinitionHistory;
    }

    public static WorkflowDefinitionHistoryBuilder builder() {
        return new WorkflowDefinitionHistoryBuilder(new WorkflowDefinitionHistory());
    }

    public WorkflowDefinitionHistoryBuilder tenantId(String tenantId) {
        this.workflowDefinitionHistory.setTenantId(tenantId);
        return this;
    }

    public WorkflowDefinitionHistoryBuilder workflowDefinitionId(Integer workflowDefinitionId) {
        this.workflowDefinitionHistory.setWorkflowDefinitionId(workflowDefinitionId);
        return this;
    }

    public WorkflowDefinitionHistoryBuilder dmlOperator(DMLOperator dmlOperator) {
        this.workflowDefinitionHistory.setDmlOperator(dmlOperator);
        return this;
    }

    public WorkflowDefinitionHistoryBuilder beforeObj(WorkflowDefinition before) {
        this.workflowDefinitionHistory.setBeforeObj(before);
        return this;
    }

    public WorkflowDefinitionHistoryBuilder currentObj(WorkflowDefinition current) {
        this.workflowDefinitionHistory.setCurrentObj(current);
        return this;
    }

    public WorkflowDefinitionHistoryBuilder desc(String desc) {
        this.workflowDefinitionHistory.setDesc(desc);
        return this;
    }

    public WorkflowDefinitionHistory build() {
        String tenantId = this.workflowDefinitionHistory.getTenantId();
        Integer workflowDefinitionId = this.workflowDefinitionHistory.getWorkflowDefinitionId();
        DMLOperator dmlOperator = this.workflowDefinitionHistory.getDmlOperator();
        if (CharSequenceUtil.isEmpty(tenantId) || workflowDefinitionId == null || dmlOperator == null) {
            throw new InvalidParamsException("tenantId, workflowDefinitionId, dmlOperator cannot be empty");
        }
        return super.build(this.workflowDefinitionHistory);
    }
}
