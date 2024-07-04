package io.github.thebesteric.framework.agile.plugins.workflow.service;

import io.github.thebesteric.framework.agile.plugins.workflow.config.AgileWorkflowContext;

/**
 * AbstractWorkflowService
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-17 13:55:43
 */
public abstract class AbstractWorkflowService implements WorkflowService {

    protected AgileWorkflowContext context;

    protected AbstractWorkflowService(AgileWorkflowContext context) {
        this.context = context;
    }
}
