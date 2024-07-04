package io.github.thebesteric.framework.agile.plugins.workflow.service;

import io.github.thebesteric.framework.agile.plugins.workflow.config.AgileWorkflowContext;

/**
 * AbstractRuntimeService
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-24 09:56:46
 */
public abstract class AbstractRuntimeService implements RuntimeService {

    protected AgileWorkflowContext context;

    protected AbstractRuntimeService(AgileWorkflowContext context) {
        this.context = context;
    }

}
