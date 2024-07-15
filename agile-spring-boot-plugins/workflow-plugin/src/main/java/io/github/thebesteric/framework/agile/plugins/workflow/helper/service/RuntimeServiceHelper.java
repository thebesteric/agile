package io.github.thebesteric.framework.agile.plugins.workflow.helper.service;

import io.github.thebesteric.framework.agile.plugins.database.core.domain.Page;
import io.github.thebesteric.framework.agile.plugins.workflow.WorkflowEngine;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.ApproveStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.NodeStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.WorkflowStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.RequestConditions;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.TaskInstance;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowDefinition;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowInstance;
import io.github.thebesteric.framework.agile.plugins.workflow.helper.AbstractServiceHelper;
import io.github.thebesteric.framework.agile.plugins.workflow.service.RuntimeService;

import java.util.List;

/**
 * 流程运行时帮助类
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-09 11:52:01
 */
public class RuntimeServiceHelper extends AbstractServiceHelper {

    private final RuntimeService runtimeService;

    public RuntimeServiceHelper(WorkflowEngine workflowEngine) {
        super(workflowEngine);
        this.runtimeService = workflowEngine.getRuntimeService();
    }

    /**
     * 启动流程
     *
     * @param workflowDefinition 流程定义
     * @param requesterId        申请人
     * @param desc               申请内容
     *
     * @author wangweijun
     * @since 2024/7/9 14:02
     */
    public void start(WorkflowDefinition workflowDefinition, String requesterId, String desc) {
        this.start(workflowDefinition, requesterId, null, null, desc);
    }

    /**
     * 启动流程
     *
     * @param workflowDefinition 流程定义
     * @param requesterId        申请人
     * @param businessId         业务 ID
     * @param businessType       业务类型
     * @param desc               申请内容
     *
     * @author wangweijun
     * @since 2024/7/9 14:02
     */
    public void start(WorkflowDefinition workflowDefinition, String requesterId, String businessId, String businessType, String desc) {
        this.start(workflowDefinition, requesterId, businessId, businessType, desc, null);
    }

    /**
     * 启动流程
     *
     * @param workflowDefinition 流程定义
     * @param requesterId        申请人
     * @param businessId         业务 ID
     * @param businessType       业务类型
     * @param desc               申请内容
     * @param requestConditions  申请条件
     *
     * @author wangweijun
     * @since 2024/7/9 14:02
     */
    public void start(WorkflowDefinition workflowDefinition, String requesterId, String businessId, String businessType, String desc, RequestConditions requestConditions) {
        String tenantId = workflowDefinition.getTenantId();
        String key = workflowDefinition.getKey();
        this.start(tenantId, key, requesterId, businessId, businessType, desc, requestConditions);
    }

    /**
     * 启动流程
     *
     * @param tenantId              租户 ID
     * @param workflowDefinitionKey 流程定义 Key
     * @param requesterId           申请人
     * @param businessId            业务 ID
     * @param businessType          业务类型
     * @param desc                  申请内容
     * @param requestConditions     申请条件
     *
     * @author wangweijun
     * @since 2024/7/9 14:02
     */
    public void start(String tenantId, String workflowDefinitionKey, String requesterId, String businessId, String businessType, String desc, RequestConditions requestConditions) {
        this.runtimeService.start(tenantId, workflowDefinitionKey, requesterId, businessId, businessType, desc, requestConditions);
    }

    /**
     * 查找审批实例
     *
     * @param tenantId      租户 ID
     * @param approverId    审批人
     * @param nodeStatus    节点状态
     * @param approveStatus 审批状态
     *
     * @return List<TaskInstance>
     *
     * @author wangweijun
     * @since 2024/7/9 16:06
     */
    public List<TaskInstance> findTaskInstances(String tenantId, String approverId, NodeStatus nodeStatus, ApproveStatus approveStatus) {
        return this.findTaskInstances(tenantId, approverId, nodeStatus, approveStatus, 1, Integer.MAX_VALUE).getRecords();
    }

    /**
     * 查找审批实例
     *
     * @param tenantId      租户 ID
     * @param approverId    审批人
     * @param nodeStatus    节点状态
     * @param approveStatus 审批状态
     * @param page          当前页
     * @param pageSize      每页显示数量
     *
     * @return List<TaskInstance>
     *
     * @author wangweijun
     * @since 2024/7/9 16:06
     */
    public Page<TaskInstance> findTaskInstances(String tenantId, String approverId, NodeStatus nodeStatus, ApproveStatus approveStatus, Integer page, Integer pageSize) {
        return this.runtimeService.findTaskInstances(tenantId, approverId, nodeStatus, approveStatus, page, pageSize);
    }

    /**
     * 查找流程实例
     *
     * @param tenantId    租户 ID
     * @param requesterId 发起人
     * @param statuses    流程状态
     * @param page        当前页
     * @param pageSize    每页显示数量
     *
     * @return List<WorkflowInstance>
     *
     * @author wangweijun
     * @since 2024/7/10 18:24
     */
    public Page<WorkflowInstance> findWorkflowInstances(String tenantId, String requesterId, List<WorkflowStatus> statuses, Integer page, Integer pageSize) {
        return this.runtimeService.findWorkflowInstances(tenantId, requesterId, statuses, page, pageSize);
    }

    /**
     * 查找流程实例
     *
     * @param tenantId    租户 ID
     * @param requesterId 发起人
     * @param status      流程状态
     * @param page        当前页
     * @param pageSize    每页显示数量
     *
     * @return List<WorkflowInstance>
     *
     * @author wangweijun
     * @since 2024/7/10 18:24
     */
    public Page<WorkflowInstance> findWorkflowInstances(String tenantId, String requesterId, WorkflowStatus status, Integer page, Integer pageSize) {
        return this.findWorkflowInstances(tenantId, requesterId, List.of(status), page, pageSize);
    }

    /**
     * 查找流程实例
     *
     * @param tenantId    租户 ID
     * @param requesterId 发起人
     * @param page        当前页
     * @param pageSize    每页显示数量
     *
     * @return List<WorkflowInstance>
     *
     * @author wangweijun
     * @since 2024/7/10 18:24
     */
    public Page<WorkflowInstance> findWorkflowInstances(String tenantId, String requesterId, Integer page, Integer pageSize) {
        return this.findWorkflowInstances(tenantId, requesterId, List.of(), page, pageSize);
    }

    /**
     * 审批-同意
     *
     * @param taskInstance 任务实例
     * @param approverId   审批人
     * @param comment      审批意见
     *
     * @author wangweijun
     * @since 2024/7/9 16:31
     */
    public void approve(TaskInstance taskInstance, String approverId, String comment) {
        this.approve(taskInstance.getTenantId(), taskInstance.getId(), approverId, comment);
    }

    /**
     * 审批-同意
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     * @param approverId     审批人
     * @param comment        审批意见
     *
     * @author wangweijun
     * @since 2024/7/9 16:31
     */
    public void approve(String tenantId, Integer taskInstanceId, String approverId, String comment) {
        this.runtimeService.approve(tenantId, taskInstanceId, approverId, comment);
    }

    /**
     * 审批-拒绝
     *
     * @param taskInstance 任务实例
     * @param approverId   审批人
     * @param comment      审批意见
     *
     * @author wangweijun
     * @since 2024/7/10 13:37
     */
    public void reject(TaskInstance taskInstance, String approverId, String comment) {
        this.runtimeService.reject(taskInstance.getTenantId(), taskInstance.getId(), approverId, comment);
    }

    /**
     * 审批-拒绝
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     * @param approverId     审批人
     * @param comment        审批意见
     *
     * @author wangweijun
     * @since 2024/7/10 13:37
     */
    public void reject(String tenantId, Integer taskInstanceId, String approverId, String comment) {
        this.runtimeService.reject(tenantId, taskInstanceId, approverId, comment);
    }

    /**
     * 审批-放弃
     *
     * @param taskInstance 任务实例
     * @param approverId   审批人
     * @param comment      审批意见
     *
     * @author wangweijun
     * @since 2024/7/10 17:40
     */
    public void abandon(TaskInstance taskInstance, String approverId, String comment) {
        this.abandon(taskInstance.getTenantId(), taskInstance.getId(), approverId, comment);
    }

    /**
     * 审批-放弃
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     * @param approverId     审批人
     * @param comment        审批意见
     *
     * @author wangweijun
     * @since 2024/7/10 17:40
     */
    public void abandon(String tenantId, Integer taskInstanceId, String approverId, String comment) {
        this.runtimeService.abandon(tenantId, taskInstanceId, approverId, comment);
    }

    /**
     * 审批-取消
     *
     * @param workerInstance 流程实例
     *
     * @author wangweijun
     * @since 2024/7/10 17:55
     */
    public void cancel(WorkflowInstance workerInstance) {
        this.cancel(workerInstance.getTenantId(), workerInstance.getId());
    }

    /**
     * 审批-取消
     *
     * @param tenantId         租户 ID
     * @param workerInstanceId 流程实例 ID
     *
     * @author wangweijun
     * @since 2024/7/10 17:55
     */
    public void cancel(String tenantId, Integer workerInstanceId) {
        this.runtimeService.cancel(tenantId, workerInstanceId);
    }
}
