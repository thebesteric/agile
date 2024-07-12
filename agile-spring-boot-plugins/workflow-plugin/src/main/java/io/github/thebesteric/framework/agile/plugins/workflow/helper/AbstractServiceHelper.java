package io.github.thebesteric.framework.agile.plugins.workflow.helper;

import io.github.thebesteric.framework.agile.plugins.workflow.WorkflowEngine;
import lombok.Getter;

/**
 * AbstractWorkflowHelper
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-08 14:50:17
 */
@Getter
public abstract class AbstractServiceHelper {

    protected final WorkflowEngine workflowEngine;

    protected AbstractServiceHelper(WorkflowEngine workflowEngine) {
        this.workflowEngine = workflowEngine;
    }

    /**
     * 设置当前用户
     *
     * @param user 用户
     *
     * @author wangweijun
     * @since 2024/7/9 10:18
     */
    public void setCurrentUser(String user) {
        workflowEngine.setCurrentUser(user);
    }

}
