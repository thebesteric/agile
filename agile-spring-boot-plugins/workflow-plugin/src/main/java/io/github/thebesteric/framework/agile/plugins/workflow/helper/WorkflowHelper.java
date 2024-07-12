package io.github.thebesteric.framework.agile.plugins.workflow.helper;

import io.github.thebesteric.framework.agile.plugins.workflow.WorkflowEngine;
import io.github.thebesteric.framework.agile.plugins.workflow.helper.service.DeploymentServiceHelper;
import io.github.thebesteric.framework.agile.plugins.workflow.helper.service.RuntimeServiceHelper;
import io.github.thebesteric.framework.agile.plugins.workflow.helper.service.TaskHistoryServiceHelper;
import io.github.thebesteric.framework.agile.plugins.workflow.helper.service.WorkflowServiceHelper;
import lombok.Getter;

/**
 * WorkflowHelper
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-08 14:24:11
 */
@Getter
public class WorkflowHelper {

    // 处理流程定义相关
    private final DeploymentServiceHelper deploymentServiceHelper;
    // 处理流程实例相关
    private final WorkflowServiceHelper workflowServiceHelper;
    // 处理流程运行时相关
    private final RuntimeServiceHelper runtimeServiceHelper;
    // 处理审批日志相关
    private final TaskHistoryServiceHelper taskHistoryServiceHelper;

    public WorkflowHelper(WorkflowEngine workflowEngine) {
        this.deploymentServiceHelper = new DeploymentServiceHelper(workflowEngine);
        this.workflowServiceHelper = new WorkflowServiceHelper(workflowEngine);
        this.runtimeServiceHelper = new RuntimeServiceHelper(workflowEngine);
        this.taskHistoryServiceHelper = new TaskHistoryServiceHelper(workflowEngine);
    }

}
