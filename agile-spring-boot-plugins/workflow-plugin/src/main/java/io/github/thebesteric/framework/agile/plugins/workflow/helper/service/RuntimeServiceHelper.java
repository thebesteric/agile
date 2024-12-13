package io.github.thebesteric.framework.agile.plugins.workflow.helper.service;

import io.github.thebesteric.framework.agile.plugins.database.core.domain.Page;
import io.github.thebesteric.framework.agile.plugins.workflow.WorkflowEngine;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.ApproveStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.NodeStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.WorkflowStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.*;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.response.WorkflowDefinitionFlowSchema;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.response.WorkflowInstanceApproveRecords;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.*;
import io.github.thebesteric.framework.agile.plugins.workflow.helper.AbstractServiceHelper;
import io.github.thebesteric.framework.agile.plugins.workflow.service.RuntimeService;

import java.util.ArrayList;
import java.util.Collections;
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
     * @param desc               申请内容
     *
     * @return 流程实例
     *
     * @author wangweijun
     * @since 2024/7/9 14:02
     */
    public WorkflowInstance start(WorkflowDefinition workflowDefinition, Requester requester, String desc) {
        return this.start(workflowDefinition, requester, desc, null);
    }

    /**
     * 启动流程
     *
     * @param workflowDefinition 流程定义
     * @param desc               申请内容
     *
     * @return 流程实例
     *
     * @author wangweijun
     * @since 2024/7/9 14:02
     */
    public WorkflowInstance start(WorkflowDefinition workflowDefinition, Requester requester, BusinessInfo businessInfo, String desc) {
        return this.start(workflowDefinition, requester, businessInfo, desc, new ArrayList<>());
    }

    /**
     * 启动流程
     *
     * @param workflowDefinition 流程定义
     * @param requester          申请人
     * @param desc               申请内容
     * @param dynamicApprovers   动态审批人
     *
     * @return 流程实例
     *
     * @author wangweijun
     * @since 2024/7/9 14:02
     */
    public WorkflowInstance start(WorkflowDefinition workflowDefinition, Requester requester, String desc, List<Approver> dynamicApprovers) {
        return this.start(workflowDefinition, requester, null, desc, null, dynamicApprovers);
    }

    /**
     * 启动流程
     *
     * @param workflowDefinition 流程定义
     * @param requester          申请人
     * @param businessInfo       业务信息
     * @param desc               申请内容
     * @param requestConditions  申请条件
     *
     * @return 流程实例
     *
     * @author wangweijun
     * @since 2024/7/9 14:02
     */
    public WorkflowInstance start(WorkflowDefinition workflowDefinition, Requester requester, BusinessInfo businessInfo, String desc, RequestConditions requestConditions) {
        return this.start(workflowDefinition, requester, businessInfo, desc, requestConditions, null);
    }

    /**
     * 启动流程
     *
     * @param workflowDefinition 流程定义
     * @param requester          申请人
     * @param businessInfo       业务信息
     * @param desc               申请内容
     * @param dynamicApprovers   动态审批人
     *
     * @return 流程实例
     *
     * @author wangweijun
     * @since 2024/7/9 14:02
     */
    public WorkflowInstance start(WorkflowDefinition workflowDefinition, Requester requester, BusinessInfo businessInfo, String desc, List<Approver> dynamicApprovers) {
        String tenantId = workflowDefinition.getTenantId();
        String key = workflowDefinition.getKey();
        return this.start(tenantId, key, requester, businessInfo, desc, null, dynamicApprovers);
    }

    /**
     * 启动流程
     *
     * @param workflowDefinition 流程定义
     * @param requester          申请人
     * @param businessInfo       业务信息
     * @param desc               申请内容
     * @param requestConditions  申请条件
     * @param dynamicApprovers   动态审批人
     *
     * @return 流程实例
     *
     * @author wangweijun
     * @since 2024/7/9 14:02
     */
    public WorkflowInstance start(WorkflowDefinition workflowDefinition, Requester requester, BusinessInfo businessInfo, String desc, RequestConditions requestConditions, List<Approver> dynamicApprovers) {
        String tenantId = workflowDefinition.getTenantId();
        String key = workflowDefinition.getKey();
        return this.start(tenantId, key, requester, businessInfo, desc, requestConditions, dynamicApprovers);
    }

    /**
     * 启动流程
     *
     * @param tenantId              租户 ID
     * @param workflowDefinitionKey 流程定义 Key
     * @param requester             申请人
     * @param businessInfo          业务信息
     * @param desc                  申请内容
     * @param requestConditions     申请条件
     * @param dynamicApprovers      动态审批人
     *
     * @return 流程实例
     *
     * @author wangweijun
     * @since 2024/7/9 14:02
     */
    public WorkflowInstance start(String tenantId, String workflowDefinitionKey, Requester requester, BusinessInfo businessInfo, String desc, RequestConditions requestConditions, List<Approver> dynamicApprovers) {
        return this.runtimeService.start(tenantId, workflowDefinitionKey, requester, businessInfo, desc, requestConditions, dynamicApprovers);
    }

    /**
     * 锁定流程定义（不会改变流程发布状态）
     *
     * @param tenantId              租户 ID
     * @param workflowDefinitionKey 流程唯一标识
     */
    public void lock(String tenantId, String workflowDefinitionKey) {
        this.runtimeService.lock(tenantId, workflowDefinitionKey);
    }

    /**
     * 锁定流程定义（不会改变流程发布状态）
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     */
    public void lock(String tenantId, Integer workflowDefinitionId) {
        this.runtimeService.lock(tenantId, workflowDefinitionId);
    }

    /**
     * 解锁流程定义（不会更新流程定义）
     *
     * @param tenantId              租户 ID
     * @param workflowDefinitionKey 流程唯一标识
     */
    public void unlock(String tenantId, String workflowDefinitionKey) {
        this.runtimeService.unlock(tenantId, workflowDefinitionKey);
    }

    /**
     * 解锁流程定义（不会更新流程定义）
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     */
    public void unlock(String tenantId, Integer workflowDefinitionId) {
        this.runtimeService.unlock(tenantId, workflowDefinitionId);
    }

    /**
     * 查找审批实例
     *
     * @param tenantId                     租户 ID
     * @param workflowInstanceId           流程实例 ID
     * @param roleId                       审批人角色 ID
     * @param approverId                   审批人
     * @param nodeStatus                   节点状态
     * @param approveStatus                审批状态
     * @param approveDatesSegmentCondition 审批时间段查询条件
     *
     * @return List<TaskInstance>
     *
     * @author wangweijun
     * @since 2024/7/9 16:06
     */
    public List<TaskInstance> findTaskInstances(String tenantId, Integer workflowInstanceId, String roleId, String approverId,
                                                NodeStatus nodeStatus, ApproveStatus approveStatus, ApproveDatesSegmentCondition approveDatesSegmentCondition) {
        return this.findTaskInstances(tenantId, workflowInstanceId, roleId, approverId, nodeStatus, approveStatus, approveDatesSegmentCondition, 1, Integer.MAX_VALUE).getRecords();
    }

    /**
     * 查找审批实例
     *
     * @param tenantId                     租户 ID
     * @param workflowInstanceId           流程实例 ID
     * @param roleId                       审批人角色 ID
     * @param approverId                   审批人 ID
     * @param nodeStatus                   节点状态
     * @param approveStatus                审批状态
     * @param approveDatesSegmentCondition 审批时间段查询条件
     * @param page                         当前页
     * @param pageSize                     每页显示数量
     *
     * @return List<TaskInstance>
     *
     * @author wangweijun
     * @since 2024/7/9 16:06
     */
    public Page<TaskInstance> findTaskInstances(String tenantId, Integer workflowInstanceId, String roleId, String approverId,
                                                NodeStatus nodeStatus, ApproveStatus approveStatus, ApproveDatesSegmentCondition approveDatesSegmentCondition, Integer page, Integer pageSize) {
        return this.runtimeService.findTaskInstances(tenantId, workflowInstanceId, roleId, approverId, nodeStatus, approveStatus, approveDatesSegmentCondition, page, pageSize);
    }


    /**
     * 查找审批实例
     *
     * @param tenantId                     租户 ID
     * @param workflowInstanceId           流程实例 ID
     * @param roleId                       审批人角色 ID
     * @param approverId                   审批人 ID
     * @param nodeStatuses                 节点状态
     * @param approveStatus                审批人审批状态
     * @param approveDatesSegmentCondition 审批时间段查询条件
     * @param page                         页码
     * @param pageSize                     每页大小
     *
     * @return Page<TaskInstance>
     *
     * @author wangweijun
     * @since 2024/9/23 19:49
     */
    public Page<TaskInstance> findTaskInstances(String tenantId, Integer workflowInstanceId, String roleId, String approverId,
                                                List<NodeStatus> nodeStatuses, ApproveStatus approveStatus, ApproveDatesSegmentCondition approveDatesSegmentCondition, Integer page, Integer pageSize) {
        return this.runtimeService.findTaskInstances(tenantId, workflowInstanceId, roleId, approverId, nodeStatuses, approveStatus, approveDatesSegmentCondition, page, pageSize);
    }

    /**
     * 查找审批实例
     *
     * @param tenantId                     租户 ID
     * @param workflowInstanceId           流程实例 ID
     * @param roleId                       审批人角色 ID
     * @param approverId                   审批人 ID
     * @param nodeStatus                   节点状态
     * @param approveStatuses              审批人审批状态
     * @param approveDatesSegmentCondition 审批时间段查询条件
     * @param page                         页码
     * @param pageSize                     每页大小
     *
     * @return Page<TaskInstance>
     *
     * @author wangweijun
     * @since 2024/9/23 19:49
     */
    public Page<TaskInstance> findTaskInstances(String tenantId, Integer workflowInstanceId, String roleId, String approverId,
                                                NodeStatus nodeStatus, List<ApproveStatus> approveStatuses, ApproveDatesSegmentCondition approveDatesSegmentCondition, Integer page, Integer pageSize) {
        return this.runtimeService.findTaskInstances(tenantId, workflowInstanceId, roleId, approverId, nodeStatus, approveStatuses, approveDatesSegmentCondition, page, pageSize);
    }

    /**
     * 查找审批实例
     *
     * @param tenantId                     租户 ID
     * @param workflowInstanceId           流程实例 ID
     * @param roleIds                      审批人 ID
     * @param approverId                   审批人 ID
     * @param nodeStatuses                 节点状态
     * @param approveStatuses              审批人审批状态
     * @param approveDatesSegmentCondition 审批时间段查询条件
     * @param page                         页码
     * @param pageSize                     每页大小
     *
     * @return Page<TaskInstance>
     *
     * @author wangweijun
     * @since 2024/9/23 19:49
     */
    public Page<TaskInstance> findTaskInstances(String tenantId, Integer workflowInstanceId, List<String> roleIds, String approverId,
                                                List<NodeStatus> nodeStatuses, List<ApproveStatus> approveStatuses, ApproveDatesSegmentCondition approveDatesSegmentCondition, Integer page, Integer pageSize) {
        return this.runtimeService.findTaskInstances(tenantId, workflowInstanceId, roleIds, approverId, nodeStatuses, approveStatuses, approveDatesSegmentCondition, page, pageSize);
    }

    /**
     * 是否是角色审批实例
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     *
     * @return boolean
     *
     * @author wangweijun
     * @since 2024/10/10 18:24
     */
    public boolean isRoleTaskInstance(String tenantId, Integer taskInstanceId) {
        return this.runtimeService.isRoleTaskInstance(tenantId, taskInstanceId);
    }

    /**
     * 根据任务实例 ID：获取角色 ID
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     * @param approverId     用户 ID
     *
     * @return String
     *
     * @author wangweijun
     * @since 2024/10/10 18:24
     */
    public String getRoleIdByTaskInstanceId(String tenantId, Integer taskInstanceId, String approverId) {
        return this.runtimeService.getRoleIdByTaskInstanceId(tenantId, taskInstanceId, approverId);
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
     * 获取当前流程实例下正在进行的节点定义
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     *
     * @return NodeDefinition
     *
     * @author wangweijun
     * @since 2024/9/27 15:16
     */
    public NodeDefinition getInCurrentlyEffectNodeDefinition(String tenantId, Integer workflowInstanceId) {
        return this.runtimeService.getInCurrentlyEffectNodeDefinition(tenantId, workflowInstanceId);
    }

    /**
     * 获取当前流程实例下正在进行的审批用户
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     *
     * @return List<Approver>
     *
     * @author wangweijun
     * @since 2024/10/11 10:57
     */
    public List<Approver> findInCurrentlyEffectApprovers(String tenantId, Integer workflowInstanceId) {
        return this.runtimeService.findInCurrentlyEffectApprovers(tenantId, workflowInstanceId);
    }

    /**
     * 获取当前流程实例下正在进行的角色审批用户
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     *
     * @return List<Approver>
     *
     * @author wangweijun
     * @since 2024/10/11 10:57
     */
    public List<RoleApprover> findInCurrentlyEffectRoleApprovers(String tenantId, Integer workflowInstanceId) {
        return this.runtimeService.findInCurrentlyEffectRoleApprovers(tenantId, workflowInstanceId);
    }

    /**
     * 根据任务实例 ID 查找任务实例
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     *
     * @return TaskInstance
     *
     * @author wangweijun
     * @since 2024/9/27 14:55
     */
    public TaskInstance getTaskInstance(String tenantId, Integer taskInstanceId) {
        return this.runtimeService.getTaskInstance(tenantId, taskInstanceId);
    }

    /**
     * 根据流程实例 ID 获取流程实例
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     *
     * @return WorkflowInstance
     *
     * @author wangweijun
     * @since 2024/10/12 15:15
     */
    public WorkflowInstance getWorkflowInstanceById(String tenantId, Integer workflowInstanceId) {
        return this.runtimeService.getWorkflowInstanceById(tenantId, workflowInstanceId);
    }

    /**
     * 根据任务实例 ID 获取流程实例
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     *
     * @return WorkflowInstance
     *
     * @author wangweijun
     * @since 2024/10/12 15:15
     */
    public WorkflowInstance getWorkflowInstanceByTaskInstanceId(String tenantId, Integer taskInstanceId) {
        return this.runtimeService.getWorkflowInstanceByTaskInstanceId(tenantId, taskInstanceId);
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
     * 查找流程实例：根据发起人
     *
     * @param tenantId    租户 ID
     * @param requesterId 发起人
     *
     * @return List<WorkflowInstance>
     *
     * @author wangweijun
     * @since 2024/7/10 18:24
     */
    public List<WorkflowInstance> findWorkflowInstancesByRequestId(String tenantId, String requesterId) {
        return this.findWorkflowInstancesByRequestId(tenantId, requesterId, List.of());
    }

    /**
     * 查找流程实例：根据发起人
     *
     * @param tenantId       租户 ID
     * @param requesterId    发起人
     * @param workflowStatus 流程状态
     *
     * @return List<WorkflowInstance>
     *
     * @author wangweijun
     * @since 2024/7/10 18:24
     */
    public List<WorkflowInstance> findWorkflowInstancesByRequestId(String tenantId, String requesterId, WorkflowStatus workflowStatus) {
        return this.findWorkflowInstancesByRequestId(tenantId, requesterId, List.of(workflowStatus));
    }

    /**
     * 查找流程实例：根据发起人
     *
     * @param tenantId         租户 ID
     * @param requesterId      发起人
     * @param workflowStatuses 流程状态
     *
     * @return List<WorkflowInstance>
     *
     * @author wangweijun
     * @since 2024/7/10 18:24
     */
    public List<WorkflowInstance> findWorkflowInstancesByRequestId(String tenantId, String requesterId, List<WorkflowStatus> workflowStatuses) {
        return this.findWorkflowInstancesByRequestId(tenantId, requesterId, workflowStatuses, 1, Integer.MAX_VALUE).getRecords();
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
     * 查找流程实例：根据流程定义 key
     *
     * @param tenantId         租户 ID
     * @param key              流程定义 key
     * @param workflowStatuses 流程状态
     * @param page             当前页
     * @param pageSize         每页显示数量
     *
     * @return Page<WorkflowInstance>
     *
     * @author wangweijun
     * @since 2024/10/14 10:10
     */
    public Page<WorkflowInstance> findWorkflowInstancesByKey(String tenantId, String key, List<WorkflowStatus> workflowStatuses, Integer page, Integer pageSize) {
        return this.runtimeService.findWorkflowInstancesByKey(tenantId, key, workflowStatuses, page, pageSize);
    }

    /**
     * 查找流程实例：根据流程定义 key
     *
     * @param tenantId       租户 ID
     * @param key            流程定义 key
     * @param workflowStatus 流程状态
     * @param page           当前页
     * @param pageSize       每页显示数量
     *
     * @return Page<WorkflowInstance>
     *
     * @author wangweijun
     * @since 2024/10/14 10:10
     */
    public Page<WorkflowInstance> findWorkflowInstancesByKey(String tenantId, String key, WorkflowStatus workflowStatus, Integer page, Integer pageSize) {
        return this.findWorkflowInstancesByKey(tenantId, key, List.of(workflowStatus), page, pageSize);
    }

    /**
     * 查找流程实例：根据流程定义 key
     *
     * @param tenantId 租户 ID
     * @param key      流程定义 key
     * @param page     当前页
     * @param pageSize 每页显示数量
     *
     * @return Page<WorkflowInstance>
     *
     * @author wangweijun
     * @since 2024/10/14 10:10
     */
    public Page<WorkflowInstance> findWorkflowInstancesByKey(String tenantId, String key, Integer page, Integer pageSize) {
        return this.findWorkflowInstancesByKey(tenantId, key, Collections.emptyList(), page, pageSize);
    }

    /**
     * 动态设置审批人
     *
     * @param tenantId         租户 ID
     * @param nodeDefinitionId 节点定义 ID
     * @param taskInstanceId   任务实例 ID
     * @param approvers        审批人列表
     *
     * @author wangweijun
     * @since 2024/9/9 13:58
     */
    public void dynamicAssignmentApprovers(String tenantId, Integer nodeDefinitionId, Integer taskInstanceId, List<Approver> approvers) {
        this.runtimeService.dynamicAssignmentApprovers(tenantId, nodeDefinitionId, taskInstanceId, approvers);
    }

    /**
     * 审批-同意
     *
     * @param taskInstance 任务实例
     * @param userId       用户 ID
     * @param comment      审批意见
     *
     * @author wangweijun
     * @since 2024/7/9 16:31
     */
    public void approve(TaskInstance taskInstance, String userId, String comment) {
        this.approve(taskInstance.getTenantId(), taskInstance.getId(), null, userId, comment);
    }

    /**
     * 审批-同意
     *
     * @param taskInstance 任务实例
     * @param roleId       角色 ID
     * @param userId       用户 ID
     * @param comment      审批意见
     *
     * @author wangweijun
     * @since 2024/7/9 16:31
     */
    public void approve(TaskInstance taskInstance, String roleId, String userId, String comment) {
        this.approve(taskInstance.getTenantId(), taskInstance.getId(), roleId, userId, comment);
    }

    /**
     * 审批-同意
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     * @param roleId         角色 ID
     * @param userId         用户 ID
     * @param comment        审批意见
     *
     * @author wangweijun
     * @since 2024/7/9 16:31
     */
    public void approve(String tenantId, Integer taskInstanceId, String roleId, String userId, String comment) {
        this.runtimeService.approve(tenantId, taskInstanceId, roleId, userId, comment);
    }

    /**
     * 审批-转派
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     * @param userId         用户 ID
     * @param invitee        被转派人
     * @param comment        转派意见
     *
     * @author wangweijun
     * @since 2024/11/7 15:15
     */
    public void reassign(String tenantId, Integer taskInstanceId, String userId, Invitee invitee, String comment) {
        this.reassign(tenantId, taskInstanceId, null, userId, invitee, comment);
    }

    /**
     * 审批-转派
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     * @param roleId         角色 ID
     * @param userId         用户 ID
     * @param invitee        被转派人
     * @param comment        转派意见
     *
     * @author wangweijun
     * @since 2024/11/7 15:15
     */
    public void reassign(String tenantId, Integer taskInstanceId, String roleId, String userId, Invitee invitee, String comment) {
        this.runtimeService.reassign(tenantId, taskInstanceId, roleId, userId, invitee, comment);
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
     * @param taskInstance 任务实例
     * @param roleId       角色 ID
     * @param userId       用户 ID
     * @param comment      审批意见
     *
     * @author wangweijun
     * @since 2024/9/6 10:18
     */
    public void redo(TaskInstance taskInstance, String roleId, String userId, String comment) {
        this.redo(taskInstance.getTenantId(), taskInstance.getId(), roleId, userId, comment);
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
        this.redo(tenantId, taskInstanceId, null, approverId, comment);
    }

    /**
     * 审批-撤回
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     * @param roleId         角色 ID
     * @param userId         用户 ID
     * @param comment        审批意见
     *
     * @author wangweijun
     * @since 2024/9/6 10:18
     */
    public void redo(String tenantId, Integer taskInstanceId, String roleId, String userId, String comment) {
        this.runtimeService.redo(tenantId, taskInstanceId, roleId, userId, comment);
    }

    /**
     * 审批-拒绝
     *
     * @param taskInstance 任务实例
     * @param userId       用户 ID
     * @param comment      审批意见
     *
     * @author wangweijun
     * @since 2024/7/10 13:37
     */
    public void reject(TaskInstance taskInstance, String userId, String comment) {
        this.reject(taskInstance.getTenantId(), taskInstance.getId(), null, userId, comment);
    }

    /**
     * 审批-拒绝
     *
     * @param taskInstance 任务实例
     * @param roleId       角色 ID
     * @param userId       用户 ID
     * @param comment      审批意见
     *
     * @author wangweijun
     * @since 2024/7/10 13:37
     */
    public void reject(TaskInstance taskInstance, String roleId, String userId, String comment) {
        this.reject(taskInstance.getTenantId(), taskInstance.getId(), roleId, userId, comment);
    }

    /**
     * 审批-拒绝
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     * @param userId         用户 ID
     * @param comment        审批意见
     *
     * @author wangweijun
     * @since 2024/7/10 13:37
     */
    public void reject(String tenantId, Integer taskInstanceId, String userId, String comment) {
        this.reject(tenantId, taskInstanceId, null, userId, comment);
    }

    /**
     * 审批-拒绝
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     * @param roleId         角色 ID
     * @param userId         用户 ID
     * @param comment        审批意见
     *
     * @author wangweijun
     * @since 2024/7/10 13:37
     */
    public void reject(String tenantId, Integer taskInstanceId, String roleId, String userId, String comment) {
        this.runtimeService.reject(tenantId, taskInstanceId, roleId, userId, comment);
    }

    /**
     * 审批-放弃
     *
     * @param taskInstance 任务实例
     * @param userId       用户 ID
     * @param comment      审批意见
     *
     * @author wangweijun
     * @since 2024/7/10 17:40
     */
    public void abandon(TaskInstance taskInstance, String userId, String comment) {
        this.abandon(taskInstance.getTenantId(), taskInstance.getId(), null, userId, comment);
    }

    /**
     * 审批-放弃
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     * @param userId         用户 ID
     * @param comment        审批意见
     *
     * @author wangweijun
     * @since 2024/7/10 17:40
     */
    public void abandon(String tenantId, Integer taskInstanceId, String userId, String comment) {
        this.abandon(tenantId, taskInstanceId, null, userId, comment);
    }

    /**
     * 审批-放弃
     *
     * @param taskInstance 任务实例
     * @param roleId       角色 ID
     * @param userId       用户 ID
     * @param comment      审批意见
     *
     * @author wangweijun
     * @since 2024/7/10 17:40
     */
    public void abandon(TaskInstance taskInstance, String roleId, String userId, String comment) {
        this.abandon(taskInstance.getTenantId(), taskInstance.getId(), roleId, userId, comment);
    }

    /**
     * 审批-放弃
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     * @param roleId         角色 ID
     * @param userId         用户 ID
     * @param comment        审批意见
     *
     * @author wangweijun
     * @since 2024/7/10 17:40
     */
    public void abandon(String tenantId, Integer taskInstanceId, String roleId, String userId, String comment) {
        this.runtimeService.abandon(tenantId, taskInstanceId, roleId, userId, comment);
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
     * 审批-中断
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     * @param comment            审批意见
     *
     * @author wangweijun
     * @since 2024/11/5 21:35
     */
    public void interrupt(String tenantId, Integer workflowInstanceId, String comment) {
        this.runtimeService.interrupt(tenantId, workflowInstanceId, comment);
    }

    /**
     * 替换审批人（未审批状态下）
     *
     * @param taskInstance     任务实例
     * @param sourceApproverId 原审批人
     * @param targetApprover   新审批人
     *
     * @author wangweijun
     * @since 2024/9/10 19:36
     */
    public void replaceApprover(TaskInstance taskInstance, String sourceApproverId, Approver targetApprover) {
        this.runtimeService.replaceApprover(taskInstance, sourceApproverId, targetApprover);
    }

    /**
     * 替换审批人（未审批状态下）
     *
     * @param workflowInstance 流程实例
     * @param sourceApproverId 原审批人
     * @param targetApprover   新审批人
     *
     * @author wangweijun
     * @since 2024/9/10 19:36
     */
    public void replaceApprover(WorkflowInstance workflowInstance, String sourceApproverId, Approver targetApprover) {
        this.runtimeService.replaceApprover(workflowInstance, sourceApproverId, targetApprover);
    }

    /**
     * 替换审批人（未审批状态下）
     *
     * @param tenantId         租户 ID
     * @param sourceApproverId 原审批人
     * @param targetApprover   新审批人
     *
     * @author wangweijun
     * @since 2024/9/10 19:36
     */
    public void replaceApprover(String tenantId, String sourceApproverId, Approver targetApprover) {
        this.runtimeService.replaceApprover(tenantId, sourceApproverId, targetApprover);
    }

    /**
     * 替换审批人（未审批状态下）
     *
     * @param taskInstance         任务实例
     * @param sourceApproverRoleId 原审批角色
     * @param sourceApproverId     原审批人
     * @param targetRoleApprover   新审批人
     *
     * @author wangweijun
     * @since 2024/12/3 17:23
     */
    public void replaceRoleApprover(TaskInstance taskInstance, String sourceApproverRoleId, String sourceApproverId, RoleApprover targetRoleApprover) {
        this.runtimeService.replaceRoleApprover(taskInstance, sourceApproverRoleId, sourceApproverId, targetRoleApprover);
    }

    /**
     * 替换审批人（未审批状态下）
     *
     * @param workflowInstance     流程实例
     * @param sourceApproverRoleId 原审批角色
     * @param sourceApproverId     原审批人
     * @param targetRoleApprover   新审批人
     *
     * @author wangweijun
     * @since 2024/9/10 19:36
     */
    public void replaceRoleApprover(WorkflowInstance workflowInstance, String sourceApproverRoleId, String sourceApproverId, RoleApprover targetRoleApprover) {
        this.runtimeService.replaceRoleApprover(workflowInstance, sourceApproverRoleId, sourceApproverId, targetRoleApprover);
    }

    /**
     * 替换审批人（未审批状态下）
     *
     * @param tenantId             租户 ID
     * @param sourceApproverRoleId 原审批角色
     * @param sourceApproverId     原审批人
     * @param targetRoleApprover   新审批人
     *
     * @author wangweijun
     * @since 2024/9/10 19:36
     */
    public void replaceRoleApprover(String tenantId, String sourceApproverRoleId, String sourceApproverId, RoleApprover targetRoleApprover) {
        this.runtimeService.replaceRoleApprover(tenantId, sourceApproverRoleId, sourceApproverId, targetRoleApprover);
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
     * @param curRoleId          当前角色 ID
     * @param curUserId          当前用户 ID
     *
     * @return WorkflowInstanceApproveRecords
     *
     * @author wangweijun
     * @since 2024/9/12 13:42
     */
    public WorkflowInstanceApproveRecords getWorkflowInstanceApproveRecords(String tenantId, Integer workflowInstanceId, String curRoleId, String curUserId) {
        return this.runtimeService.getWorkflowInstanceApproveRecords(tenantId, workflowInstanceId, List.of(curRoleId), curUserId);
    }

    /**
     * 获取流程审批记录
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     * @param curRoleIds         当前角色 IDs
     * @param curUserId          当前用户 ID
     *
     * @return WorkflowInstanceApproveRecords
     *
     * @author wangweijun
     * @since 2024/9/12 13:42
     */
    public WorkflowInstanceApproveRecords getWorkflowInstanceApproveRecords(String tenantId, Integer workflowInstanceId, List<String> curRoleIds, String curUserId) {
        return this.runtimeService.getWorkflowInstanceApproveRecords(tenantId, workflowInstanceId, curRoleIds, curUserId);
    }

    /**
     * 获取流程审批记录
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     * @param curRoleId            当前角色 ID
     * @param curUserId            当前用户 ID
     *
     * @return List<WorkflowInstanceApproveRecords>
     *
     * @author wangweijun
     * @since 2024/9/12 13:42
     */
    public List<WorkflowInstanceApproveRecords> findWorkflowInstanceApproveRecords(String tenantId, Integer workflowDefinitionId, String curRoleId, String curUserId) {
        return this.runtimeService.findWorkflowInstanceApproveRecords(tenantId, workflowDefinitionId, List.of(curRoleId), curUserId);
    }

    /**
     * 获取流程审批记录
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     * @param curRoleIds           当前角色 IDs
     * @param curUserId            当前用户 ID
     *
     * @return List<WorkflowInstanceApproveRecords>
     *
     * @author wangweijun
     * @since 2024/10/14 16:37
     */
    public List<WorkflowInstanceApproveRecords> findWorkflowInstanceApproveRecords(String tenantId, Integer workflowDefinitionId, List<String> curRoleIds, String curUserId) {
        return this.runtimeService.findWorkflowInstanceApproveRecords(tenantId, workflowDefinitionId, curRoleIds, curUserId);
    }

    /**
     * 获取流程定义纲要
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     *
     * @return WorkflowDefinitionFlowSchema
     *
     * @author wangweijun
     * @since 2024/11/4 11:19
     */
    public WorkflowDefinitionFlowSchema schema(String tenantId, Integer workflowInstanceId) {
        return this.runtimeService.getWorkflowDefinitionFlowSchema(tenantId, workflowInstanceId);
    }

    /**
     * 获取审批记录
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     * @param approverId     用户 ID
     *
     * @return TaskApprove
     *
     * @author wangweijun
     * @since 2024/10/11 13:51
     */
    public TaskApprove getTaskApprove(String tenantId, Integer taskInstanceId, String approverId) {
        return this.runtimeService.getTaskApprove(tenantId, taskInstanceId, approverId);
    }

    /**
     * 获取角色审批记录
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     * @param roleId         角色 ID
     * @param approverId     用户 ID
     *
     * @return TaskRoleApproveRecord
     *
     * @author wangweijun
     * @since 2024/10/11 13:51
     */
    public TaskRoleApprove getTaskRoleApprove(String tenantId, Integer taskInstanceId, String roleId, String approverId) {
        return this.runtimeService.getTaskRoleApprove(tenantId, taskInstanceId, roleId, approverId);
    }

    /**
     * 获取审批记录列表
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     *
     * @return List<TaskApprove>
     *
     * @author wangweijun
     * @since 2024/10/11 13:52
     */
    public List<TaskApprove> findTaskApproves(String tenantId, Integer workflowInstanceId) {
        return this.runtimeService.findTaskApproves(tenantId, workflowInstanceId);
    }

    /**
     * 获取角色审批记录列表
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     *
     * @return List<TaskRoleApprove>
     *
     * @author wangweijun
     * @since 2024/10/11 13:52
     */
    public List<TaskRoleApprove> findTaskRoleApproves(String tenantId, Integer workflowInstanceId) {
        return this.runtimeService.findTaskRoleApproves(tenantId, workflowInstanceId);
    }

    /**
     * 查找动态审批人
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     *
     * @return List<TaskDynamicAssignment>
     *
     * @author wangweijun
     * @since 2024/10/18 10:35
     */
    public List<TaskDynamicAssignment> findTaskDynamicAssignments(String tenantId, Integer taskInstanceId) {
        return this.runtimeService.findTaskDynamicAssignments(tenantId, taskInstanceId);
    }

    /**
     * 是否是动态审批节点，且没有设置动态审批人
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     *
     * @return boolean
     *
     * @author wangweijun
     * @since 2024/10/18 10:54
     */
    public boolean isDynamicNodeAndUnSettingApprovers(String tenantId, Integer taskInstanceId) {
        return this.runtimeService.isDynamicNodeAndUnSettingApprovers(tenantId, taskInstanceId);
    }

    /**
     * 查询正在进行的流程实例
     *
     * @param tenantId             租户 ID
     * @param workflowStatus       流程状态
     * @param workflowDefinitionId 流程定义 ID
     *
     * @return List<WorkflowInstance>
     *
     * @author wangweijun
     * @since 2024/10/22 11:50
     */
    public List<WorkflowInstance> findWorkflowInstances(String tenantId, WorkflowStatus workflowStatus, Integer workflowDefinitionId) {
        return this.runtimeService.findWorkflowInstances(tenantId, workflowStatus, workflowDefinitionId);
    }

    /**
     * 查询正在进行的流程实例
     *
     * @param tenantId              租户 ID
     * @param workflowStatus        流程状态
     * @param workflowDefinitionKey 流程定义 KEY
     *
     * @return List<WorkflowInstance>
     *
     * @author wangweijun
     * @since 2024/10/22 11:50
     */
    public List<WorkflowInstance> findWorkflowInstances(String tenantId, WorkflowStatus workflowStatus, String workflowDefinitionKey) {
        return this.runtimeService.findWorkflowInstances(tenantId, workflowStatus, workflowDefinitionKey);
    }

    /**
     * 获取业务信息
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     *
     * @return BusinessInfo
     *
     * @author wangweijun
     * @since 2024/12/9 11:34
     */
    public BusinessInfo getBusinessInfo(String tenantId, Integer workflowInstanceId) {
        WorkflowInstance workflowInstance = getWorkflowInstanceById(tenantId, workflowInstanceId);
        return workflowInstance.getBusinessInfo();
    }

    /**
     * 获取业务信息
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     *
     * @return BusinessInfo
     *
     * @author wangweijun
     * @since 2024/12/9 11:34
     */
    public void setBusinessInfo(String tenantId, Integer workflowInstanceId, BusinessInfo businessInfo) {
        WorkflowInstance workflowInstance = getWorkflowInstanceById(tenantId, workflowInstanceId);
        workflowInstance.setBusinessInfo(businessInfo);
        this.runtimeService.setBusinessInfo(tenantId, workflowInstanceId, businessInfo);
    }
}
