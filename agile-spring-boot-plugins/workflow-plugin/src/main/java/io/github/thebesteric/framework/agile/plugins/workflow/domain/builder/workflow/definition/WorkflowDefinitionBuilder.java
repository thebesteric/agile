package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.definition;

import cn.hutool.core.text.CharSequenceUtil;
import io.github.thebesteric.framework.agile.commons.exception.InvalidParamsException;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowDefinition;

/**
 * WorkflowDefinitionBuilder
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-19 14:25:08
 */
public class WorkflowDefinitionBuilder extends AbstractBuilder<WorkflowDefinition> {

    private final WorkflowDefinition workflowDefinition;

    private WorkflowDefinitionBuilder(WorkflowDefinition workflowDefinition) {
        this.workflowDefinition = workflowDefinition;
    }

    public static WorkflowDefinitionBuilder builder() {
        return new WorkflowDefinitionBuilder(new WorkflowDefinition());
    }

    public WorkflowDefinitionBuilder tenantId(String tenantId) {
        this.workflowDefinition.setTenantId(tenantId);
        return this;
    }

    public WorkflowDefinitionBuilder key(String key) {
        this.workflowDefinition.setKey(key);
        if (CharSequenceUtil.isEmpty(this.workflowDefinition.getName())) {
            this.workflowDefinition.setName(key);
        }
        return this;
    }

    public WorkflowDefinitionBuilder name(String name) {
        this.workflowDefinition.setName(name);
        return this;
    }

    public WorkflowDefinitionBuilder type(String type) {
        if (CharSequenceUtil.isNotEmpty(type)) {
            this.workflowDefinition.setType(type);
        }
        return this;
    }

    public WorkflowDefinitionBuilder desc(String desc) {
        this.workflowDefinition.setDesc(desc);
        return this;
    }

    public WorkflowDefinition build() {
        String tenantId = this.workflowDefinition.getTenantId();
        String key = this.workflowDefinition.getKey();
        String name = this.workflowDefinition.getName();
        if (CharSequenceUtil.isEmpty(tenantId) || CharSequenceUtil.isEmpty(key) || CharSequenceUtil.isEmpty(name)) {
            throw new InvalidParamsException("tenantId, key, name cannot be empty");
        }
        return super.build(this.workflowDefinition);
    }
}
