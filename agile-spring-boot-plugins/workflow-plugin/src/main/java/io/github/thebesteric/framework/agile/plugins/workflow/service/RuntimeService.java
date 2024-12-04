package io.github.thebesteric.framework.agile.plugins.workflow.service;

import io.github.thebesteric.framework.agile.plugins.database.core.domain.Page;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.ApproveStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.NodeStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.WorkflowStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.*;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.response.TaskHistoryResponse;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.response.WorkflowDefinitionFlowSchema;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.response.WorkflowInstanceApproveRecords;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.*;

import java.util.List;

/**
 * 运行时 Service
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-14 10:40:53
 */
public interface RuntimeService {

    /**
     * 启动流程
     *
     * @param tenantId              租户 ID
     * @param workflowDefinitionKey 流程定义 Key
     * @param requester             申请人信息
     * @param businessInfo          业务信息
     * @param desc                  描述
     * @param requestConditions     申请条件
     * @param dynamicApprovers      动态审批人
     *
     * @return WorkflowInstanceBuilder
     *
     * @author wangweijun
     * @since 2024/6/14 11:02
     */
    WorkflowInstance start(String tenantId, String workflowDefinitionKey, Requester requester, BusinessInfo businessInfo, String desc, RequestConditions requestConditions, List<Approver> dynamicApprovers);

    /**
     * 启动流程
     *
     * @param tenantId              租户 ID
     * @param workflowDefinitionKey 流程定义 Key
     * @param requester             申请人
     * @param businessInfo          业务信息
     * @param desc                  描述
     *
     * @return WorkflowInstanceBuilder
     *
     * @author wangweijun
     * @since 2024/6/14 11:02
     */
    WorkflowInstance start(String tenantId, String workflowDefinitionKey, Requester requester, BusinessInfo businessInfo, String desc);

    /**
     * 启动流程
     *
     * @param tenantId              租户 ID
     * @param workflowDefinitionKey 流程定义 Key
     * @param requester             申请人
     * @param desc                  描述
     * @param requestConditions     申请条件
     *
     * @return WorkflowInstanceBuilder
     *
     * @author wangweijun
     * @since 2024/6/14 11:02
     */
    WorkflowInstance start(String tenantId, String workflowDefinitionKey, Requester requester, String desc, RequestConditions requestConditions);


    /**
     * 启动流程
     *
     * @param tenantId              租户 ID
     * @param workflowDefinitionKey 流程定义 Key
     * @param requester             申请人
     * @param desc                  描述
     *
     * @return WorkflowInstanceBuilder
     *
     * @author wangweijun
     * @since 2024/6/14 11:02
     */
    WorkflowInstance start(String tenantId, String workflowDefinitionKey, Requester requester, String desc);

    /**
     * 获取下一节点
     *
     * @param tenantId           租户 ID
     * @param fromTaskInstanceId 当前任务实例 ID
     * @param roleId             角色 ID
     * @param userId             用户 ID
     *
     * @return List<TaskInstance>
     *
     * @author wangweijun
     * @since 2024/6/24 19:43
     */
    List<TaskInstance> next(String tenantId, Integer fromTaskInstanceId, String roleId, String userId);

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
     * @since 2024/6/24 19:43
     */
    void approve(String tenantId, Integer taskInstanceId, String roleId, String userId, String comment);

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
    void reassign(String tenantId, Integer taskInstanceId, String roleId, String userId, Invitee invitee, String comment);

    /**
     * 审批-撤销
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
    void redo(String tenantId, Integer taskInstanceId, String roleId, String userId, String comment);

    /**
     * 审批-驳回
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     * @param roleId         角色 ID
     * @param userId         用户 ID
     * @param comment        审批意见
     *
     * @author wangweijun
     * @since 2024/6/24 19:43
     */
    void reject(String tenantId, Integer taskInstanceId, String roleId, String userId, String comment);

    /**
     * 审批-弃权
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     * @param roleId         角色 ID
     * @param userId         用户 ID
     * @param comment        审批意见
     *
     * @author wangweijun
     * @since 2024/6/24 19:43
     */
    void abandon(String tenantId, Integer taskInstanceId, String roleId, String userId, String comment);

    /**
     * 审批-取消流程
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     *
     * @author wangweijun
     * @since 2024/6/27 12:43
     */
    void cancel(String tenantId, Integer workflowInstanceId);

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
    void interrupt(String tenantId, Integer workflowInstanceId, String comment);

    /**
     * 查询审批任务
     *
     * @param tenantId                     租户 ID
     * @param workflowInstanceId           流程实例 ID
     * @param roleId                       审批人角色 ID
     * @param approverId                   审批人 ID
     * @param nodeStatus                   节点状态
     * @param approveStatus                审批人审批状态
     * @param approveDatesSegmentCondition 审批时间段查询条件
     * @param page                         页码
     * @param pageSize                     每页大小
     *
     * @return List<TaskInstance>
     *
     * @author wangweijun
     * @since 2024/6/25 10:17
     */
    Page<TaskInstance> findTaskInstances(String tenantId, Integer workflowInstanceId, String roleId, String approverId, NodeStatus nodeStatus, ApproveStatus approveStatus, ApproveDatesSegmentCondition approveDatesSegmentCondition, Integer page, Integer pageSize);

    /**
     * 查询审批任务
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
    Page<TaskInstance> findTaskInstances(String tenantId, Integer workflowInstanceId, String roleId, String approverId, List<NodeStatus> nodeStatuses, ApproveStatus approveStatus, ApproveDatesSegmentCondition approveDatesSegmentCondition, Integer page, Integer pageSize);

    /**
     * 查询审批任务
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
    Page<TaskInstance> findTaskInstances(String tenantId, Integer workflowInstanceId, String roleId, String approverId, NodeStatus nodeStatus, List<ApproveStatus> approveStatuses, ApproveDatesSegmentCondition approveDatesSegmentCondition, Integer page, Integer pageSize);

    /**
     * 查询审批任务
     *
     * @param tenantId                     租户 ID
     * @param workflowInstanceId           流程实例 ID
     * @param roleIds                      审批人角色 ID
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
    Page<TaskInstance> findTaskInstances(String tenantId, Integer workflowInstanceId, List<String> roleIds, String approverId, List<NodeStatus> nodeStatuses, List<ApproveStatus> approveStatuses, ApproveDatesSegmentCondition approveDatesSegmentCondition, Integer page, Integer pageSize);

    /**
     * 查询审批任务
     *
     * @param tenantId                     租户 ID
     * @param workflowInstanceId           流程实例 ID
     * @param roleId                       审批人角色 ID
     * @param approverId                   审批人 ID
     * @param nodeStatus                   节点状态
     * @param approveStatus                审批人审批状态
     * @param approveDatesSegmentCondition 审批时间段查询条件
     *
     * @return List<TaskInstance>
     *
     * @author wangweijun
     * @since 2024/6/25 10:17
     */
    List<TaskInstance> findTaskInstances(String tenantId, Integer workflowInstanceId, String roleId, String approverId, NodeStatus nodeStatus, ApproveStatus approveStatus, ApproveDatesSegmentCondition approveDatesSegmentCondition);

    /**
     * 查询审批任务
     *
     * @param tenantId                     租户 ID
     * @param workflowInstanceId           流程实例 ID
     * @param approveDatesSegmentCondition 审批时间段查询条件
     * @param page                         当前页
     * @param pageSize                     每页显示数量
     *
     * @return List<TaskInstance>
     *
     * @author wangweijun
     * @since 2024/6/25 10:17
     */
    Page<TaskInstance> findTaskInstances(String tenantId, Integer workflowInstanceId, ApproveDatesSegmentCondition approveDatesSegmentCondition, Integer page, Integer pageSize);

    /**
     * 查询审批任务
     *
     * @param tenantId                     租户 ID
     * @param workflowInstanceId           流程实例 ID
     * @param roleId                       审批人角色 ID
     * @param approverId                   审批人 ID
     * @param approveDatesSegmentCondition 审批时间段查询条件
     *
     * @return List<TaskInstance>
     *
     * @author wangweijun
     * @since 2024/6/25 10:17
     */
    List<TaskInstance> findTaskInstances(String tenantId, Integer workflowInstanceId, String roleId, String approverId, ApproveDatesSegmentCondition approveDatesSegmentCondition);

    /**
     * 查询审批任务
     *
     * @param tenantId                     租户 ID
     * @param workflowInstanceId           流程实例 ID
     * @param roleId                       审批人角色 ID
     * @param approverId                   审批人 ID
     * @param approveDatesSegmentCondition 审批时间段查询条件
     * @param page                         当前页
     * @param pageSize                     每页显示数量
     *
     * @return List<TaskInstance>
     *
     * @author wangweijun
     * @since 2024/6/25 10:17
     */
    Page<TaskInstance> findTaskInstances(String tenantId, Integer workflowInstanceId, String roleId, String approverId, ApproveDatesSegmentCondition approveDatesSegmentCondition, Integer page, Integer pageSize);

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
    TaskInstance getInCurrentlyEffectTaskInstance(String tenantId, Integer workflowInstanceId);

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
    NodeDefinition getInCurrentlyEffectNodeDefinition(String tenantId, Integer workflowInstanceId);

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
    List<Approver> findInCurrentlyEffectApprovers(String tenantId, Integer workflowInstanceId);

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
    List<RoleApprover> findInCurrentlyEffectRoleApprovers(String tenantId, Integer workflowInstanceId);

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
    TaskInstance getTaskInstance(String tenantId, Integer taskInstanceId);

    /**
     * 查找流程实例：根据发起人
     *
     * @param tenantId    租户 ID
     * @param requesterId 发起人 ID
     * @param statuses    流程实例状态
     * @param page        当前页
     * @param pageSize    每页显示数量
     *
     * @return List<WorkflowInstance>
     *
     * @author wangweijun
     * @since 2024/6/27 12:43
     */
    Page<WorkflowInstance> findWorkflowInstancesByRequestId(String tenantId, String requesterId, List<WorkflowStatus> statuses, Integer page, Integer pageSize);

    /**
     * 查找流程实例：根据发起人
     *
     * @param tenantId    租户 ID
     * @param requesterId 发起人 ID
     * @param status      流程实例状态
     *
     * @return List<WorkflowInstance>
     *
     * @author wangweijun
     * @since 2024/6/27 12:43
     */
    List<WorkflowInstance> findWorkflowInstancesByRequestId(String tenantId, String requesterId, WorkflowStatus status);

    /**
     * 查找流程实例：根据发起人
     *
     * @param tenantId    租户 ID
     * @param requesterId 发起人 ID
     *
     * @return List<WorkflowInstance>
     *
     * @author wangweijun
     * @since 2024/6/27 12:43
     */
    List<WorkflowInstance> findWorkflowInstancesByRequestId(String tenantId, String requesterId);

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
    Page<WorkflowInstance> findWorkflowInstancesByApproverId(String tenantId, String approverId, List<WorkflowStatus> workflowStatuses, List<ApproveStatus> approveStatuses, Integer page, Integer pageSize);

    /**
     * 查询审批日志
     *
     * @param tenantId 租户 ID
     * @param page     当前页
     * @param pageSize 每页显示数量
     *
     * @return Page<TaskHistory>
     *
     * @author wangweijun
     * @since 2024/7/11 15:06
     */
    Page<TaskHistoryResponse> findTaskHistories(String tenantId, Integer page, Integer pageSize);

    /**
     * 查询审批日志
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     * @param workflowInstanceId   流程实例 ID
     * @param requesterId          发起人 ID
     * @param page                 当前页
     * @param pageSize             每页显示数量
     *
     * @return Page<TaskHistory>
     *
     * @author wangweijun
     * @since 2024/7/11 15:06
     */
    Page<TaskHistoryResponse> findTaskHistories(String tenantId, Integer workflowDefinitionId, Integer workflowInstanceId, String requesterId, Integer page, Integer pageSize);


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
    WorkflowInstanceApproveRecords getWorkflowInstanceApproveRecords(String tenantId, Integer workflowInstanceId, List<String> curRoleIds, String curUserId);

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
    WorkflowInstanceApproveRecords getWorkflowInstanceApproveRecords(String tenantId, Integer workflowInstanceId);

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
     * @since 2024/9/12 13:42
     */
    List<WorkflowInstanceApproveRecords> findWorkflowInstanceApproveRecords(String tenantId, Integer workflowDefinitionId, List<String> curRoleIds, String curUserId);

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
    List<WorkflowInstanceApproveRecords> findWorkflowInstanceApproveRecords(String tenantId, Integer workflowDefinitionId);

    /**
     * 获取流程定义纲要
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程定义
     *
     * @return WorkflowDefinitionFlowSchema
     *
     * @author wangweijun
     * @since 2024/11/4 11:20
     */
    WorkflowDefinitionFlowSchema getWorkflowDefinitionFlowSchema(String tenantId, Integer workflowInstanceId);

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
    void dynamicAssignmentApprovers(String tenantId, Integer nodeDefinitionId, Integer taskInstanceId, List<Approver> approvers);

    /**
     * 更新审批人（未审批状态下）
     *
     * @param taskInstance     任务实例
     * @param sourceApproverId 原审批人
     * @param targetApprover   新审批人
     *
     * @author wangweijun
     * @since 2024/9/10 19:36
     */
    void updateApprover(TaskInstance taskInstance, String sourceApproverId, Approver targetApprover);

    /**
     * 更新审批人（未审批状态下）
     *
     * @param workflowInstance 流程实例
     * @param sourceApproverId 原审批人
     * @param targetApprover   新审批人
     *
     * @author wangweijun
     * @since 2024/9/10 19:36
     */
    void updateApprover(WorkflowInstance workflowInstance, String sourceApproverId, Approver targetApprover);

    /**
     * 更新审批人（未审批状态下）
     *
     * @param tenantId         租户 ID
     * @param sourceApproverId 原审批人
     * @param targetApprover   新审批人
     *
     * @author wangweijun
     * @since 2024/9/10 19:36
     */
    void updateApprover(String tenantId, String sourceApproverId, Approver targetApprover);

    /**
     * 更新角色审批人（未审批状态下）
     *
     * @param taskInstance         任务实例
     * @param sourceApproverRoleId 原审批角色
     * @param sourceApproverId     原审批人
     * @param targetRoleApprover   新审批人
     *
     * @author wangweijun
     * @since 2024/12/3 17:23
     */
    void updateRoleApprover(TaskInstance taskInstance, String sourceApproverRoleId, String sourceApproverId, RoleApprover targetRoleApprover);

    /**
     * 更新角色审批人（未审批状态下）
     *
     * @param workflowInstance     流程实例
     * @param sourceApproverRoleId 原审批角色
     * @param sourceApproverId     原审批人
     * @param targetRoleApprover   新审批人
     *
     * @author wangweijun
     * @since 2024/9/10 19:36
     */
    void updateRoleApprover(WorkflowInstance workflowInstance, String sourceApproverRoleId, String sourceApproverId, RoleApprover targetRoleApprover);

    /**
     * 更新角色审批人（未审批状态下）
     *
     * @param tenantId             租户 ID
     * @param sourceApproverRoleId 原审批角色
     * @param sourceApproverId     原审批人
     * @param targetRoleApprover   新审批人
     *
     * @author wangweijun
     * @since 2024/9/10 19:36
     */
    void updateRoleApprover(String tenantId, String sourceApproverRoleId, String sourceApproverId, RoleApprover targetRoleApprover);

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
    boolean isRoleTaskInstance(String tenantId, Integer taskInstanceId);

    /**
     * 获取角色 ID
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
    String getRoleIdByTaskInstanceId(String tenantId, Integer taskInstanceId, String approverId);

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
    TaskApprove getTaskApprove(String tenantId, Integer taskInstanceId, String approverId);

    /**
     * 获取角色审批记录
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     * @param roleId         角色 ID
     * @param approverId     用户 ID
     *
     * @return TaskRoleApprove
     *
     * @author wangweijun
     * @since 2024/10/11 13:51
     */
    TaskRoleApprove getTaskRoleApprove(String tenantId, Integer taskInstanceId, String roleId, String approverId);

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
    List<TaskApprove> findTaskApproves(String tenantId, Integer workflowInstanceId);

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
    List<TaskRoleApprove> findTaskRoleApproves(String tenantId, Integer workflowInstanceId);

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
    WorkflowInstance getWorkflowInstanceById(String tenantId, Integer workflowInstanceId);

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
    WorkflowInstance getWorkflowInstanceByTaskInstanceId(String tenantId, Integer taskInstanceId);

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
    Page<WorkflowInstance> findWorkflowInstancesByKey(String tenantId, String key, List<WorkflowStatus> workflowStatuses, Integer page, Integer pageSize);

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
    List<TaskDynamicAssignment> findTaskDynamicAssignments(String tenantId, Integer taskInstanceId);

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
    boolean isDynamicNodeAndUnSettingApprovers(String tenantId, Integer taskInstanceId);

    /**
     * 是否是动态审批节点
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     *
     * @return boolean
     *
     * @author wangweijun
     * @since 2024/10/18 14:54
     */
    boolean isDynamicNode(String tenantId, Integer taskInstanceId);

    /**
     * 查询流程实例
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
    List<WorkflowInstance> findWorkflowInstances(String tenantId, WorkflowStatus workflowStatus, Integer workflowDefinitionId);

    /**
     * 查询流程实例
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
    List<WorkflowInstance> findWorkflowInstances(String tenantId, WorkflowStatus workflowStatus, String workflowDefinitionKey);
}
