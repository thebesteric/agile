package io.github.thebesteric.framework.agile.plugins.workflow.helper.service;

import io.github.thebesteric.framework.agile.plugins.database.core.domain.Page;
import io.github.thebesteric.framework.agile.plugins.workflow.WorkflowEngine;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.response.TaskHistoryResponse;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowDefinition;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowInstance;
import io.github.thebesteric.framework.agile.plugins.workflow.helper.AbstractServiceHelper;
import io.github.thebesteric.framework.agile.plugins.workflow.service.RuntimeService;

/**
 * 任务日志帮助类
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-11 14:59:19
 */
public class TaskHistoryServiceHelper extends AbstractServiceHelper {

    private final RuntimeService runtimeService;

    public TaskHistoryServiceHelper(WorkflowEngine workflowEngine) {
        super(workflowEngine);
        this.runtimeService = workflowEngine.getRuntimeService();
    }

    /**
     * 查询任务审批记录
     *
     * @param tenantId 租户 ID
     * @param page     当前页
     * @param pageSize 每页显示数量
     *
     * @return Page<TaskHistory>
     */
    public Page<TaskHistoryResponse> findTaskHistories(String tenantId, Integer page, Integer pageSize) {
        return runtimeService.findTaskHistories(tenantId, page, pageSize);
    }

    /**
     * 查询任务审批记录
     *
     * @param workflowDefinition 流程定义
     * @param page               当前页
     * @param pageSize           每页显示数量
     *
     * @return Page<TaskHistory>
     */
    public Page<TaskHistoryResponse> findTaskHistories(WorkflowDefinition workflowDefinition, Integer page, Integer pageSize) {
        return this.findTaskHistories(workflowDefinition.getTenantId(), workflowDefinition.getId(), null, null, page, pageSize);
    }

    /**
     * 查询任务审批记录
     *
     * @param workflowInstance 流程实例
     * @param page             当前页
     * @param pageSize         每页显示数量
     *
     * @return Page<TaskHistory>
     */
    public Page<TaskHistoryResponse> findTaskHistories(WorkflowInstance workflowInstance, Integer page, Integer pageSize) {
        return this.findTaskHistories(workflowInstance.getTenantId(), workflowInstance.getWorkflowDefinitionId(), workflowInstance.getId(), workflowInstance.getRequesterId(), page, pageSize);
    }

    /**
     * 查询任务审批记录
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     * @param requesterId          请求人 ID
     * @param page                 当前页
     * @param pageSize             每页显示数量
     *
     * @return Page<TaskHistory>
     */
    public Page<TaskHistoryResponse> findTaskHistories(String tenantId, Integer workflowDefinitionId, String requesterId, Integer page, Integer pageSize) {
        return this.findTaskHistories(tenantId, workflowDefinitionId, null, requesterId, page, pageSize);
    }

    /**
     * 查询任务审批记录
     *
     * @param tenantId    租户 ID
     * @param requesterId 请求人 ID
     * @param page        当前页
     * @param pageSize    每页显示数量
     *
     * @return Page<TaskHistory>
     */
    public Page<TaskHistoryResponse> findTaskHistories(String tenantId, String requesterId, Integer page, Integer pageSize) {
        return this.findTaskHistories(tenantId, null, null, requesterId, page, pageSize);
    }

    /**
     * 查询任务审批记录
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     * @param page               当前页
     * @param pageSize           每页显示数量
     *
     * @return Page<TaskHistory>
     */
    public Page<TaskHistoryResponse> findTaskHistories(String tenantId, Integer workflowInstanceId, Integer page, Integer pageSize) {
        return this.findTaskHistories(tenantId, null, workflowInstanceId, null, page, pageSize);
    }

    /**
     * 查询任务审批记录
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     * @param workflowInstanceId   流程实例 ID
     * @param page                 当前页
     * @param pageSize             每页显示数量
     *
     * @return Page<TaskHistory>
     */
    public Page<TaskHistoryResponse> findTaskHistories(String tenantId, Integer workflowDefinitionId, Integer workflowInstanceId, Integer page, Integer pageSize) {
        return this.findTaskHistories(tenantId, workflowDefinitionId, workflowInstanceId, null, page, pageSize);
    }

    /**
     * 查询任务审批记录
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     * @param workflowInstanceId   流程实例 ID
     * @param requesterId          请求人 ID
     * @param page                 当前页
     * @param pageSize             每页显示数量
     *
     * @return Page<TaskHistory>
     */
    public Page<TaskHistoryResponse> findTaskHistories(String tenantId, Integer workflowDefinitionId, Integer workflowInstanceId, String requesterId, Integer page, Integer pageSize) {
        return runtimeService.findTaskHistories(tenantId, workflowDefinitionId, workflowInstanceId, requesterId, page, pageSize);
    }

}
