package io.github.thebesteric.framework.agile.plugins.workflow.helper;

import io.github.thebesteric.framework.agile.plugins.workflow.WorkflowEngine;
import io.github.thebesteric.framework.agile.plugins.workflow.helper.service.*;
import lombok.Getter;

/**
 * 工作流帮助类
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-08 14:24:11
 */
@Getter
public class WorkflowHelper {

    private final WorkflowEngine workflowEngine;

    /** 处理流程定义相关 */
    private final DeploymentServiceHelper deploymentServiceHelper;
    /** 处理流程实例相关 */
    private final WorkflowServiceHelper workflowServiceHelper;
    /** 处理流程运行时相关 */
    private final RuntimeServiceHelper runtimeServiceHelper;
    /** 处理审批日志相关 */
    private final TaskHistoryServiceHelper taskHistoryServiceHelper;
    /** 处理流程附件相关 */
    private final RepositoryServiceHelper repositoryServiceHelper;

    public WorkflowHelper(WorkflowEngine workflowEngine) {
        this(workflowEngine, null);
    }

    public WorkflowHelper(WorkflowEngine workflowEngine, String currentUser) {
        this.workflowEngine = workflowEngine;
        this.deploymentServiceHelper = new DeploymentServiceHelper(workflowEngine);
        this.workflowServiceHelper = new WorkflowServiceHelper(workflowEngine);
        this.runtimeServiceHelper = new RuntimeServiceHelper(workflowEngine);
        this.taskHistoryServiceHelper = new TaskHistoryServiceHelper(workflowEngine);
        this.repositoryServiceHelper = new RepositoryServiceHelper(workflowEngine);
        this.setCurrentUser(currentUser);
    }

    /**
     * 设置当前操作人
     *
     * @param user 当前操作人
     *
     * @author wangweijun
     * @since 2024/7/15 15:11
     */
    public void setCurrentUser(String user) {
        if (user != null) {
            this.workflowEngine.setCurrentUser(user);
        }
    }

}
