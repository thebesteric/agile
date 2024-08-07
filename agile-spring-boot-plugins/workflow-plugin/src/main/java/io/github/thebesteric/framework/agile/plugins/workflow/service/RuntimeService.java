package io.github.thebesteric.framework.agile.plugins.workflow.service;

import io.github.thebesteric.framework.agile.plugins.database.core.domain.Page;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.ApproveStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.NodeStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.WorkflowStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.RequestConditions;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.response.TaskHistoryResponse;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.TaskInstance;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowInstance;

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
     * @param requesterId           申请人 ID
     * @param businessId            业务标识
     * @param businessType          业务类型
     * @param desc                  描述
     * @param requestConditions     申请条件
     *
     * @return WorkflowInstanceBuilder
     *
     * @author wangweijun
     * @since 2024/6/14 11:02
     */
    WorkflowInstance start(String tenantId, String workflowDefinitionKey, String requesterId, String businessId, String businessType, String desc, RequestConditions requestConditions);

    /**
     * 启动流程
     *
     * @param tenantId              租户 ID
     * @param workflowDefinitionKey 流程定义 Key
     * @param requesterId           申请人 ID
     * @param businessId            业务标识
     * @param businessType          业务类型
     * @param desc                  描述
     *
     * @return WorkflowInstanceBuilder
     *
     * @author wangweijun
     * @since 2024/6/14 11:02
     */
    WorkflowInstance start(String tenantId, String workflowDefinitionKey, String requesterId, String businessId, String businessType, String desc);

    /**
     * 启动流程
     *
     * @param tenantId              租户 ID
     * @param workflowDefinitionKey 流程定义 Key
     * @param requesterId           申请人 ID
     * @param desc                  描述
     * @param requestConditions     申请条件
     *
     * @return WorkflowInstanceBuilder
     *
     * @author wangweijun
     * @since 2024/6/14 11:02
     */
    WorkflowInstance start(String tenantId, String workflowDefinitionKey, String requesterId, String desc, RequestConditions requestConditions);


    /**
     * 启动流程
     *
     * @param tenantId              租户 ID
     * @param workflowDefinitionKey 流程定义 Key
     * @param requesterId           申请人 ID
     * @param desc                  描述
     *
     * @return WorkflowInstanceBuilder
     *
     * @author wangweijun
     * @since 2024/6/14 11:02
     */
    WorkflowInstance start(String tenantId, String workflowDefinitionKey, String requesterId, String desc);

    /**
     * 获取下一节点
     *
     * @param tenantId           租户 ID
     * @param fromTaskInstanceId 当前任务实例 ID
     *
     * @return List<TaskInstance>
     *
     * @author wangweijun
     * @since 2024/6/24 19:43
     */
    List<TaskInstance> next(String tenantId, Integer fromTaskInstanceId);

    /**
     * 审批-同意
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     * @param approverId     审批人 ID
     * @param comment        审批意见
     *
     * @author wangweijun
     * @since 2024/6/24 19:43
     */
    void approve(String tenantId, Integer taskInstanceId, String approverId, String comment);

    /**
     * 审批-驳回
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     * @param approverId     审批人 ID
     * @param comment        审批意见
     *
     * @author wangweijun
     * @since 2024/6/24 19:43
     */
    void reject(String tenantId, Integer taskInstanceId, String approverId, String comment);

    /**
     * 弃权
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     * @param approverId     审批人 ID
     * @param comment        审批意见
     *
     * @author wangweijun
     * @since 2024/6/24 19:43
     */
    void abandon(String tenantId, Integer taskInstanceId, String approverId, String comment);

    /**
     * 取消流程
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     *
     * @author wangweijun
     * @since 2024/6/27 12:43
     */
    void cancel(String tenantId, Integer workflowInstanceId);

    /**
     * 查询审批任务
     *
     * @param tenantId      租户 ID
     * @param approverId    审批人 ID
     * @param nodeStatus    节点状态
     * @param approveStatus 审批人审批状态
     * @param page          页码
     * @param pageSize      每页大小
     *
     * @return List<TaskInstance>
     *
     * @author wangweijun
     * @since 2024/6/25 10:17
     */
    Page<TaskInstance> findTaskInstances(String tenantId, String approverId, NodeStatus nodeStatus, ApproveStatus approveStatus, Integer page, Integer pageSize);

    /**
     * 查询审批任务
     *
     * @param tenantId      租户 ID
     * @param approverId    审批人 ID
     * @param nodeStatus    节点状态
     * @param approveStatus 审批人审批状态
     *
     * @return List<TaskInstance>
     *
     * @author wangweijun
     * @since 2024/6/25 10:17
     */
    List<TaskInstance> findTaskInstances(String tenantId, String approverId, NodeStatus nodeStatus, ApproveStatus approveStatus);

    /**
     * 查询审批任务
     *
     * @param tenantId 租户 ID
     * @param page     当前页
     * @param pageSize 每页显示数量
     *
     * @return List<TaskInstance>
     *
     * @author wangweijun
     * @since 2024/6/25 10:17
     */
    Page<TaskInstance> findTaskInstances(String tenantId, Integer page, Integer pageSize);

    /**
     * 查询审批任务
     *
     * @param tenantId   租户 ID
     * @param approverId 审批人 ID
     *
     * @return List<TaskInstance>
     *
     * @author wangweijun
     * @since 2024/6/25 10:17
     */
    List<TaskInstance> findTaskInstances(String tenantId, String approverId);

    /**
     * 查询审批任务
     *
     * @param tenantId   租户 ID
     * @param approverId 审批人 ID
     * @param page       当前页
     * @param pageSize   每页显示数量
     *
     * @return List<TaskInstance>
     *
     * @author wangweijun
     * @since 2024/6/25 10:17
     */
    Page<TaskInstance> findTaskInstances(String tenantId, String approverId, Integer page, Integer pageSize);

    /**
     * 根据发起人 ID 获取流程实例
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
    Page<WorkflowInstance> findWorkflowInstances(String tenantId, String requesterId, List<WorkflowStatus> statuses, Integer page, Integer pageSize);

    /**
     * 根据发起人 ID 获取流程实例
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
    List<WorkflowInstance> findWorkflowInstances(String tenantId, String requesterId, WorkflowStatus status);

    /**
     * 根据发起人 ID 获取流程实例
     *
     * @param tenantId    租户 ID
     * @param requesterId 发起人 ID
     *
     * @return List<WorkflowInstance>
     *
     * @author wangweijun
     * @since 2024/6/27 12:43
     */
    List<WorkflowInstance> findWorkflowInstances(String tenantId, String requesterId);

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
}
