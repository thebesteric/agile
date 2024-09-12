package io.github.thebesteric.framework.agile.plugins.workflow.helper.service;

import io.github.thebesteric.framework.agile.plugins.database.core.domain.Page;
import io.github.thebesteric.framework.agile.plugins.workflow.WorkflowEngine;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.ApproveStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.NodeStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.WorkflowStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.Approver;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.RequestConditions;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.response.WorkflowInstanceApproveRecords;
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
     * @return 流程实例
     *
     * @author wangweijun
     * @since 2024/7/9 14:02
     */
    public WorkflowInstance start(WorkflowDefinition workflowDefinition, String requesterId, String desc) {
        return this.start(workflowDefinition, requesterId, null, null, desc);
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
     * @return 流程实例
     *
     * @author wangweijun
     * @since 2024/7/9 14:02
     */
    public WorkflowInstance start(WorkflowDefinition workflowDefinition, String requesterId, String businessId, String businessType, String desc) {
        return this.start(workflowDefinition, requesterId, businessId, businessType, desc, null);
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
     * @return 流程实例
     *
     * @author wangweijun
     * @since 2024/7/9 14:02
     */
    public WorkflowInstance start(WorkflowDefinition workflowDefinition, String requesterId, String businessId, String businessType, String desc, RequestConditions requestConditions) {
        String tenantId = workflowDefinition.getTenantId();
        String key = workflowDefinition.getKey();
        return this.start(tenantId, key, requesterId, businessId, businessType, desc, requestConditions);
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
     * @return 流程实例
     *
     * @author wangweijun
     * @since 2024/7/9 14:02
     */
    public WorkflowInstance start(String tenantId, String workflowDefinitionKey, String requesterId, String businessId, String businessType, String desc, RequestConditions requestConditions) {
        return this.runtimeService.start(tenantId, workflowDefinitionKey, requesterId, businessId, businessType, desc, requestConditions);
    }

    /**
     * 查找审批实例
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     * @param approverId         审批人
     * @param nodeStatus         节点状态
     * @param approveStatus      审批状态
     *
     * @return List<TaskInstance>
     *
     * @author wangweijun
     * @since 2024/7/9 16:06
     */
    public List<TaskInstance> findTaskInstances(String tenantId, Integer workflowInstanceId, String approverId, NodeStatus nodeStatus, ApproveStatus approveStatus) {
        return this.findTaskInstances(tenantId, workflowInstanceId, approverId, nodeStatus, approveStatus, 1, Integer.MAX_VALUE).getRecords();
    }

    /**
     * 查找审批实例
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     * @param approverId         审批人
     * @param nodeStatus         节点状态
     * @param approveStatus      审批状态
     * @param page               当前页
     * @param pageSize           每页显示数量
     *
     * @return List<TaskInstance>
     *
     * @author wangweijun
     * @since 2024/7/9 16:06
     */
    public Page<TaskInstance> findTaskInstances(String tenantId, Integer workflowInstanceId, String approverId, NodeStatus nodeStatus, ApproveStatus approveStatus, Integer page, Integer pageSize) {
        return this.runtimeService.findTaskInstances(tenantId, workflowInstanceId, approverId, nodeStatus, approveStatus, page, pageSize);
    }

    /**
     * 获取当前流程实例下正在进行的审批节点
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     *
     * @return TaskInstance
     *
     * @author wangweijun
     * @since 2024/9/9 15:06
     */
    public TaskInstance getInCurrentlyEffectTaskInstance(String tenantId, Integer workflowInstanceId) {
        return this.runtimeService.getInCurrentlyEffectTaskInstance(tenantId, workflowInstanceId);
    }

    /**
     * 查找流程实例：根据发起人
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
    public Page<WorkflowInstance> findWorkflowInstancesByRequestId(String tenantId, String requesterId, List<WorkflowStatus> statuses, Integer page, Integer pageSize) {
        return this.runtimeService.findWorkflowInstancesByRequestId(tenantId, requesterId, statuses, page, pageSize);
    }

    /**
     * 查找流程实例：根据发起人
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
    public Page<WorkflowInstance> findWorkflowInstancesByRequestId(String tenantId, String requesterId, WorkflowStatus status, Integer page, Integer pageSize) {
        return this.findWorkflowInstancesByRequestId(tenantId, requesterId, List.of(status), page, pageSize);
    }

    /**
     * 查找流程实例：根据发起人
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
    public Page<WorkflowInstance> findWorkflowInstancesByRequestId(String tenantId, String requesterId, Integer page, Integer pageSize) {
        return this.findWorkflowInstancesByRequestId(tenantId, requesterId, List.of(), page, pageSize);
    }

    /**
     * 查找流程实例：根据审批人
     *
     * @param tenantId         租户 ID
     * @param approverId       审批人 ID
     * @param workflowStatuses 流程状态
     * @param approveStatuses  审批状态
     * @param page             当前页
     * @param pageSize         每页显示数量
     *
     * @return Page<WorkflowInstance>
     *
     * @author wangweijun
     * @since 2024/9/10 17:40
     */
    public Page<WorkflowInstance> findWorkflowInstancesByApproverId(String tenantId, String approverId, List<WorkflowStatus> workflowStatuses, List<ApproveStatus> approveStatuses, Integer page, Integer pageSize) {
        return this.runtimeService.findWorkflowInstancesByApproverId(tenantId, approverId, workflowStatuses, approveStatuses, page, pageSize);
    }

    /**
     * 查找流程实例：根据审批人
     *
     * @param tenantId       租户 ID
     * @param approverId     审批人 ID
     * @param workflowStatus 流程状态
     * @param approveStatus  审批状态
     * @param page           当前页
     * @param pageSize       每页显示数量
     *
     * @return Page<WorkflowInstance>
     *
     * @author wangweijun
     * @since 2024/9/10 17:40
     */
    public Page<WorkflowInstance> findWorkflowInstancesByApproverId(String tenantId, String approverId, WorkflowStatus workflowStatus, ApproveStatus approveStatus, Integer page, Integer pageSize) {
        List<WorkflowStatus> workflowStatuses = workflowStatus == null ? null : List.of(workflowStatus);
        List<ApproveStatus> approveStatuses = approveStatus == null ? null : List.of(approveStatus);
        return this.findWorkflowInstancesByApproverId(tenantId, approverId, workflowStatuses, approveStatuses, page, pageSize);
    }

    /**
     * 查找流程实例：根据审批人
     *
     * @param tenantId   租户 ID
     * @param approverId 审批人 ID
     * @param page       当前页
     * @param pageSize   每页显示数量
     *
     * @return Page<WorkflowInstance>
     *
     * @author wangweijun
     * @since 2024/9/10 17:40
     */
    public Page<WorkflowInstance> findWorkflowInstancesByApproverId(String tenantId, String approverId, Integer page, Integer pageSize) {
        return this.findWorkflowInstancesByApproverId(tenantId, approverId, List.of(), List.of(), page, pageSize);
    }

    /**
     * 动态设置审批人
     *
     * @param tenantId         租户 ID
     * @param nodeDefinitionId 节点定义 ID
     * @param approver         审批人
     *
     * @author wangweijun
     * @since 2024/9/9 13:58
     */
    public void dynamicAssignmentApprovers(String tenantId, Integer nodeDefinitionId, Approver approver) {
        this.dynamicAssignmentApprovers(tenantId, nodeDefinitionId, List.of(approver));
    }

    /**
     * 动态设置审批人
     *
     * @param tenantId         租户 ID
     * @param nodeDefinitionId 节点定义 ID
     * @param approvers        审批人列表
     *
     * @author wangweijun
     * @since 2024/9/9 13:58
     */
    public void dynamicAssignmentApprovers(String tenantId, Integer nodeDefinitionId, List<Approver> approvers) {
        this.runtimeService.dynamicAssignmentApprovers(tenantId, nodeDefinitionId, approvers);
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
     * 审批-撤回
     *
     * @param taskInstance 任务实例
     * @param approverId   审批人 ID
     * @param comment      审批意见
     *
     * @author wangweijun
     * @since 2024/9/6 10:18
     */
    public void redo(TaskInstance taskInstance, String approverId, String comment) {
        this.redo(taskInstance.getTenantId(), taskInstance.getId(), approverId, comment);
    }

    /**
     * 审批-撤回
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     * @param approverId     审批人 ID
     * @param comment        审批意见
     *
     * @author wangweijun
     * @since 2024/9/6 10:18
     */
    public void redo(String tenantId, Integer taskInstanceId, String approverId, String comment) {
        this.runtimeService.redo(tenantId, taskInstanceId, approverId, comment);
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

    /**
     * 更新审批人（未审批状态下）
     *
     * @param taskInstance     任务实例
     * @param sourceApproverId 原审批人
     * @param targetApproverId 新审批人
     *
     * @author wangweijun
     * @since 2024/9/10 19:36
     */
    public void updateApprover(TaskInstance taskInstance, String sourceApproverId, String targetApproverId) {
        this.runtimeService.updateApprover(taskInstance, sourceApproverId, targetApproverId);
    }

    /**
     * 更新审批人（未审批状态下）
     *
     * @param workflowInstance 流程实例
     * @param sourceApproverId 原审批人
     * @param targetApproverId 新审批人
     *
     * @author wangweijun
     * @since 2024/9/10 19:36
     */
    public void updateApprover(WorkflowInstance workflowInstance, String sourceApproverId, String targetApproverId) {
        this.runtimeService.updateApprover(workflowInstance, sourceApproverId, targetApproverId);
    }

    /**
     * 更新审批人（未审批状态下）
     *
     * @param tenantId         租户 ID
     * @param sourceApproverId 原审批人
     * @param targetApproverId 新审批人
     *
     * @author wangweijun
     * @since 2024/9/10 19:36
     */
    public void updateApprover(String tenantId, String sourceApproverId, String targetApproverId) {
        this.runtimeService.updateApprover(tenantId, sourceApproverId, targetApproverId);
    }

    /**
     * 获取流程审批记录
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     *
     * @return WorkflowInstanceApproveRecords
     *
     * @author wangweijun
     * @since 2024/9/12 13:42
     */
    public WorkflowInstanceApproveRecords getWorkflowInstanceApproveRecords(String tenantId, Integer workflowInstanceId) {
        return this.runtimeService.getWorkflowInstanceApproveRecords(tenantId, workflowInstanceId);
    }

    /**
     * 获取流程审批记录
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     *
     * @return List<WorkflowInstanceApproveRecords>
     *
     * @author wangweijun
     * @since 2024/9/12 13:42
     */
    public List<WorkflowInstanceApproveRecords> findWorkflowInstanceApproveRecords(String tenantId, Integer workflowDefinitionId) {
        return this.runtimeService.findWorkflowInstanceApproveRecords(tenantId, workflowDefinitionId);
    }

    /**
     * 获取流程审批记录
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     * @param currentUserId      当前用户 ID
     *
     * @return WorkflowInstanceApproveRecords
     *
     * @author wangweijun
     * @since 2024/9/12 13:42
     */
    public WorkflowInstanceApproveRecords getWorkflowInstanceApproveRecords(String tenantId, Integer workflowInstanceId, String currentUserId) {
        return this.runtimeService.getWorkflowInstanceApproveRecords(tenantId, workflowInstanceId, currentUserId);
    }

    /**
     * 获取流程审批记录
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     * @param currentUserId        当前用户 ID
     *
     * @return List<WorkflowInstanceApproveRecords>
     *
     * @author wangweijun
     * @since 2024/9/12 13:42
     */
    public List<WorkflowInstanceApproveRecords> findWorkflowInstanceApproveRecords(String tenantId, Integer workflowDefinitionId, String currentUserId) {
        return this.runtimeService.findWorkflowInstanceApproveRecords(tenantId, workflowDefinitionId, currentUserId);
    }
}
