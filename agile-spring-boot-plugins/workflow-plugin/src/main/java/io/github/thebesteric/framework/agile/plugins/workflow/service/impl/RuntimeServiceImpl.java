package io.github.thebesteric.framework.agile.plugins.workflow.service.impl;

import cn.hutool.core.collection.CollUtil;
import io.github.thebesteric.framework.agile.commons.exception.InvalidDataException;
import io.github.thebesteric.framework.agile.plugins.database.core.jdbc.JdbcTemplateHelper;
import io.github.thebesteric.framework.agile.plugins.workflow.config.AgileWorkflowContext;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.*;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.Conditions;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.RequestConditions;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.assignment.NodeAssignmentExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.assignment.NodeAssignmentExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.definition.NodeDefinitionExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.definition.NodeDefinitionExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.relation.NodeRelationExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.relation.NodeRelationExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.task.approve.TaskApproveExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.task.approve.TaskApproveExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.task.history.TaskHistoryExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.task.instance.TaskInstanceExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.task.instance.TaskInstanceExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.definition.WorkflowDefinitionExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.definition.WorkflowDefinitionExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.instance.WorkflowInstanceExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.instance.WorkflowInstanceExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.*;
import io.github.thebesteric.framework.agile.plugins.workflow.exception.WorkflowException;
import io.github.thebesteric.framework.agile.plugins.workflow.service.AbstractRuntimeService;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 运行时 Service implementation
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-14 10:41:29
 */
public class RuntimeServiceImpl extends AbstractRuntimeService {

    private final WorkflowInstanceExecutorBuilder workflowInstanceExecutorBuilder;
    private final WorkflowDefinitionExecutorBuilder workflowDefinitionExecutorBuilder;
    private final NodeDefinitionExecutorBuilder nodeDefinitionExecutorBuilder;
    private final NodeAssignmentExecutorBuilder nodeAssignmentExecutorBuilder;
    private final NodeRelationExecutorBuilder nodeRelationExecutorBuilder;
    private final TaskInstanceExecutorBuilder taskInstanceExecutorBuilder;
    private final TaskApproveExecutorBuilder taskApproveExecutorBuilder;
    private final TaskHistoryExecutorBuilder taskHistoryExecutorBuilder;

    public RuntimeServiceImpl(AgileWorkflowContext context) {
        super(context);
        JdbcTemplate jdbcTemplate = context.getJdbcTemplateHelper().getJdbcTemplate();
        workflowInstanceExecutorBuilder = WorkflowInstanceExecutorBuilder.builder(jdbcTemplate);
        workflowDefinitionExecutorBuilder = WorkflowDefinitionExecutorBuilder.builder(jdbcTemplate);
        nodeDefinitionExecutorBuilder = NodeDefinitionExecutorBuilder.builder(jdbcTemplate);
        nodeAssignmentExecutorBuilder = NodeAssignmentExecutorBuilder.builder(jdbcTemplate);
        nodeRelationExecutorBuilder = NodeRelationExecutorBuilder.builder(jdbcTemplate);
        taskInstanceExecutorBuilder = TaskInstanceExecutorBuilder.builder(jdbcTemplate);
        taskApproveExecutorBuilder = TaskApproveExecutorBuilder.builder(jdbcTemplate);
        taskHistoryExecutorBuilder = TaskHistoryExecutorBuilder.builder(jdbcTemplate);
    }

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
     */
    @Override
    public WorkflowInstance start(String tenantId, String workflowDefinitionKey, String requesterId, String businessId, String businessType, String desc, RequestConditions requestConditions) {
        JdbcTemplateHelper jdbcTemplateHelper = this.context.getJdbcTemplateHelper();
        return jdbcTemplateHelper.executeInTransaction(() -> {
            WorkflowDefinition workflowDefinition = getWorkflowDefinition(tenantId, workflowDefinitionKey);
            if (workflowDefinition == null) {
                throw new WorkflowException("未查询到对应的流程定义，请确认该流程定义是否存在: %s", workflowDefinitionKey);
            }
            WorkflowInstanceExecutor instanceExecutor = workflowInstanceExecutorBuilder.tenantId(tenantId).workflowDefinitionId(workflowDefinition.getId())
                    .requesterId(requesterId).businessId(businessId).businessType(businessType).requestConditions(requestConditions).status(WorkflowStatus.IN_PROGRESS).desc(desc).build();
            WorkflowInstance workflowInstance = instanceExecutor.save();
            Integer workflowInstanceId = workflowInstance.getId();

            // 记录流程日志（提交审批）
            recordLogs(tenantId, workflowInstanceId, null, TaskHistoryMessage.INSTANCE_SUBMIT_FORM);

            // 获取开始节点定义
            NodeDefinitionExecutor nodeDefinitionExecutor = nodeDefinitionExecutorBuilder.build();
            NodeDefinition startNode = nodeDefinitionExecutor.getStartNode(tenantId, workflowDefinition.getId());

            // 创建审批开始实例
            TaskInstanceExecutor taskInstanceExecutor = taskInstanceExecutorBuilder.tenantId(tenantId).status(NodeStatus.COMPLETED)
                    .workflowInstanceId(workflowInstanceId).nodeDefinitionId(startNode.getId()).build();
            TaskInstance startTaskInstance = taskInstanceExecutor.save();

            // 记录流程日志（开始审批）
            recordLogs(tenantId, workflowInstanceId, startTaskInstance.getId(), TaskHistoryMessage.INSTANCE_STARTED);

            // 创建下一个审批实例
            List<NodeDefinition> toTaskNodes = nodeDefinitionExecutor.findToTaskNodesByFromNodeId(tenantId, startNode.getId());
            if (CollUtil.isNotEmpty(toTaskNodes)) {
                List<NodeDefinition> nextNodeDefinitions = new ArrayList<>();
                // 只是一个审批节点
                if (toTaskNodes.size() == 1) {
                    NodeDefinition toTaskNode = toTaskNodes.get(0);
                    Conditions conditions = toTaskNode.getConditions();
                    // 没有审批条件或满足审批条件
                    if (conditions == null || conditions.matchRequestCondition(requestConditions)) {
                        nextNodeDefinitions.add(toTaskNode);
                    }
                } else {
                    for (NodeDefinition toTaskNode : toTaskNodes) {
                        // 没有审批条件或满足审批条件
                        Conditions conditions = toTaskNode.getConditions();
                        if (conditions == null || conditions.matchRequestCondition(requestConditions)) {
                            nextNodeDefinitions.add(toTaskNode);
                        }
                    }
                }

                // 异常校验
                if (nextNodeDefinitions.isEmpty()) {
                    throw new InvalidDataException("没有匹配的审批节点，请核实设置是否正确");
                }

                // 保存下个审批节点
                for (NodeDefinition nextNodeDefinition : nextNodeDefinitions) {
                    Integer nextNodeDefinitionId = nextNodeDefinition.getId();
                    // 查找节点审批人
                    NodeAssignmentExecutor nodeAssignmentExecutor = nodeAssignmentExecutorBuilder.build();
                    List<NodeAssignment> nextNodeAssignments = nodeAssignmentExecutor.findByNodeDefinitionId(tenantId, nextNodeDefinitionId);

                    // 保存任务节点
                    taskInstanceExecutor = taskInstanceExecutorBuilder.status(NodeStatus.IN_PROGRESS)
                            .tenantId(tenantId)
                            .workflowInstanceId(workflowInstanceId).nodeDefinitionId(nextNodeDefinitionId)
                            .totalCount(nextNodeAssignments.size()).approvedCount(0)
                            .build();
                    TaskInstance nextTaskInstance = taskInstanceExecutor.save();
                    Integer nextTaskInstanceId = nextTaskInstance.getId();

                    // 创建任务实例审批人
                    if (CollUtil.isNotEmpty(nextNodeAssignments)) {
                        for (NodeAssignment nextNodeAssignment : nextNodeAssignments) {
                            TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder.tenantId(tenantId)
                                    .workflowInstanceId(workflowInstanceId).taskInstanceId(nextTaskInstanceId)
                                    .approverId(nextNodeAssignment.getUserId()).status(ApproveStatus.IN_PROGRESS).build();
                            taskApproveExecutor.save();
                        }
                    }
                }
            }
            return workflowInstance;
        });
    }

    /**
     * 启动流程
     *
     * @param tenantId              租户 ID
     * @param workflowDefinitionKey 流程定义 Key
     * @param requesterId           申请人 ID
     * @param businessId            业务标识
     * @param businessType          业务类型
     * @param desc                  描述
     */
    @Override
    public WorkflowInstance start(String tenantId, String workflowDefinitionKey, String requesterId, String businessId, String businessType, String desc) {
        return this.start(tenantId, workflowDefinitionKey, requesterId, businessId, businessType, desc, null);
    }

    /**
     * 启动流程
     *
     * @param tenantId              租户 ID
     * @param workflowDefinitionKey 流程定义 Key
     * @param requesterId           申请人 ID
     * @param desc                  描述
     * @param requestConditions     申请条件
     */
    @Override
    public WorkflowInstance start(String tenantId, String workflowDefinitionKey, String requesterId, String desc, RequestConditions requestConditions) {
        return this.start(tenantId, workflowDefinitionKey, requesterId, null, null, desc, requestConditions);
    }

    /**
     * 启动流程
     *
     * @param tenantId              租户 ID
     * @param workflowDefinitionKey 流程定义 Key
     * @param requesterId           申请人 ID
     * @param desc                  描述
     */
    @Override
    public WorkflowInstance start(String tenantId, String workflowDefinitionKey, String requesterId, String desc) {
        return this.start(tenantId, workflowDefinitionKey, requesterId, desc, null);
    }

    /**
     * 记录流程日志
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 上一个节点实例 ID
     * @param taskInstanceId     当前节点实例 ID
     * @param message            日志信息
     */
    private void recordLogs(String tenantId, Integer workflowInstanceId, Integer taskInstanceId, TaskHistoryMessage message) {
        taskHistoryExecutorBuilder.tenantId(tenantId).workflowInstanceId(workflowInstanceId).taskInstanceId(taskInstanceId)
                .message(message).build().save();
    }

    /**
     * 获取下一个审批节点
     *
     * @param tenantId           租户 ID
     * @param prevTaskInstanceId 上一个节点实例 ID
     */
    @Override
    public void next(String tenantId, Integer prevTaskInstanceId) {
        JdbcTemplateHelper jdbcTemplateHelper = this.context.getJdbcTemplateHelper();
        jdbcTemplateHelper.executeInTransaction(() -> {
            NodeRelationExecutor nodeRelationExecutor = nodeRelationExecutorBuilder.build();
            NodeDefinitionExecutor nodeDefinitionExecutor = nodeDefinitionExecutorBuilder.build();
            WorkflowInstanceExecutor workflowInstanceExecutor = workflowInstanceExecutorBuilder.build();
            TaskInstanceExecutor taskInstanceExecutor = taskInstanceExecutorBuilder.build();

            // 上一个节点实例
            TaskInstance prevTaskInstance = taskInstanceExecutor.getById(prevTaskInstanceId);

            // 获取流程实例
            WorkflowInstance workflowInstance = workflowInstanceExecutor.getById(prevTaskInstance.getWorkflowInstanceId());

            // 获取节点关系（临时），主要是要找到下一个（多个）审批节点，存在多个表示是条件审批
            List<NodeDefinition> toNodeDefinitions = new ArrayList<>();
            List<NodeRelation> tempNodeRelations = nodeRelationExecutor.findByFromNodeId(tenantId, prevTaskInstance.getNodeDefinitionId());
            for (NodeRelation tempNodeRelation : tempNodeRelations) {
                Integer toNodeDefinitionId = tempNodeRelation.getToNodeId();
                NodeDefinition nodeDefinition = nodeDefinitionExecutor.getById(toNodeDefinitionId);
                // 排出掉结束节点
                if (NodeType.END == nodeDefinition.getNodeType()) {
                    continue;
                }
                // 获取到正在的下一级需要的审批节点
                toNodeDefinitions.add(nodeDefinition);
            }

            // 表示已经到达结束节点
            if (toNodeDefinitions.isEmpty()) {
                // 更新节点状态
                prevTaskInstance.setStatus(NodeStatus.COMPLETED);
                taskInstanceExecutor.updateById(prevTaskInstance);
                // 更新流程实例为：已完成
                workflowInstance.setStatus(WorkflowStatus.COMPLETED);
                workflowInstanceExecutor.updateById(workflowInstance);
                return;
            }

            List<NodeRelation> nodeRelations;
            List<Integer> toNodeIds = toNodeDefinitions.stream().map(NodeDefinition::getId).toList();
            if (toNodeIds.size() == 1) {
                nodeRelations = nodeRelationExecutor.findByFromNodeId(tenantId, toNodeIds.get(0));
            } else {
                nodeRelations = nodeRelationExecutor.findByFromNodeIds(tenantId, toNodeIds);
            }

            if (!nodeRelations.isEmpty()) {
                for (NodeRelation nodeRelation : nodeRelations) {
                    // 下一个节点定义
                    Integer nextNodeDefinitionId = nodeRelation.getFromNodeId();
                    NodeDefinition nextNodeDefinition = nodeDefinitionExecutor.getById(nextNodeDefinitionId);

                    // 审批条件判断
                    Conditions conditions = nextNodeDefinition.getConditions();
                    RequestConditions requestConditions = workflowInstance.getRequestConditions();
                    if ((conditions != null && requestConditions != null && !conditions.matchRequestCondition(requestConditions))
                        || (conditions != null && requestConditions == null)) {
                        continue;
                    }

                    // 下一个节点的审批人
                    List<NodeAssignment> nextNodeAssignments = new ArrayList<>();

                    // 准备构建下一个节点实例
                    taskInstanceExecutorBuilder.workflowInstanceId(workflowInstance.getId()).nodeDefinitionId(nextNodeDefinitionId);

                    // 已经是结束节点
                    if (NodeType.END == nextNodeDefinition.getNodeType()) {
                        // 更新下个节点状态为：已完成
                        taskInstanceExecutorBuilder.status(NodeStatus.COMPLETED);

                        // 更新流程实例为：已完成
                        workflowInstance.setStatus(WorkflowStatus.COMPLETED);
                        workflowInstanceExecutor.updateById(workflowInstance);
                    }
                    // 不是结束节点
                    else {
                        // 查找节点审批人
                        NodeAssignmentExecutor nodeAssignmentExecutor = nodeAssignmentExecutorBuilder.build();
                        nextNodeAssignments = nodeAssignmentExecutor.findByNodeDefinitionId(tenantId, nextNodeDefinitionId);
                        // 更新下个节点状态为：进行中，并更新节点审批人数量
                        taskInstanceExecutorBuilder.tenantId(tenantId)
                                .status(NodeStatus.IN_PROGRESS).totalCount(nextNodeAssignments.size()).approvedCount(0);
                    }

                    // 保存任务节点
                    taskInstanceExecutor = taskInstanceExecutorBuilder.build();
                    TaskInstance nextTaskInstance = taskInstanceExecutor.save();

                    // 创建任务实例审批人
                    if (CollUtil.isNotEmpty(nextNodeAssignments) && NodeType.END != nextNodeDefinition.getNodeType()) {
                        for (NodeAssignment nextNodeAssignment : nextNodeAssignments) {
                            TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder.tenantId(tenantId)
                                    .workflowInstanceId(nextTaskInstance.getWorkflowInstanceId()).taskInstanceId(nextTaskInstance.getId())
                                    .approverId(nextNodeAssignment.getUserId()).status(ApproveStatus.IN_PROGRESS).build();
                            taskApproveExecutor.save();
                        }
                    }
                }
            }
        });
    }

    /**
     * 审批
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     * @param approverId     审批人 ID
     * @param comment        审批意见
     */
    @Override
    public void approve(String tenantId, Integer taskInstanceId, String approverId, String comment) {
        JdbcTemplateHelper jdbcTemplateHelper = this.context.getJdbcTemplateHelper();
        jdbcTemplateHelper.executeInTransaction(() -> {
            // 更新 TaskApprove 的 status 和 comment
            TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder.build();
            TaskApprove taskApprove = taskApproveExecutor.getByTaskInstanceIdAndApproverId(tenantId, taskInstanceId, approverId);
            taskApprove.setStatus(ApproveStatus.APPROVED);
            taskApprove.setComment(comment);
            taskApproveExecutor.updateById(taskApprove);

            // 修改当前 TaskInstance 的 approved_count 数量
            TaskInstanceExecutor taskInstanceExecutor = taskInstanceExecutorBuilder.build();
            TaskInstance taskInstance = taskInstanceExecutor.getById(taskInstanceId);
            taskInstance.setApprovedCount(taskInstance.getApprovedCount() + 1);
            // 已完成审批的人数 approved_count 等于所有需要审批的人数 total_count
            if (Objects.equals(taskInstance.getApprovedCount(), taskInstance.getTotalCount())) {
                taskInstance.setStatus(NodeStatus.COMPLETED);
                // 指向下一个审批节点
                this.next(tenantId, taskInstance.getId());
            }
            taskInstanceExecutor.updateById(taskInstance);

            // 记录流程日志（审批通过）
            recordLogs(tenantId, taskInstance.getWorkflowInstanceId(), taskInstanceId, TaskHistoryMessage.INSTANCE_APPROVED);
        });

    }

    /**
     * 驳回
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     * @param approverId     审批人 ID
     * @param comment        审批意见
     */
    @Override
    public void reject(String tenantId, Integer taskInstanceId, String approverId, String comment) {
        JdbcTemplateHelper jdbcTemplateHelper = this.context.getJdbcTemplateHelper();
        jdbcTemplateHelper.executeInTransaction(() -> {
            // 更新 TaskApprove 的 comment，并将状态设置为：已驳回
            TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder.build();
            TaskApprove taskApprove = taskApproveExecutor.getByTaskInstanceIdAndApproverId(tenantId, taskInstanceId, approverId);
            taskApprove.setStatus(ApproveStatus.REJECTED);
            taskApprove.setComment(comment);
            taskApproveExecutor.updateById(taskApprove);

            // 修改当前 TaskInstance 的 approved_count 数量，并将 status 更新为：已驳回
            TaskInstanceExecutor taskInstanceExecutor = taskInstanceExecutorBuilder.build();
            TaskInstance taskInstance = taskInstanceExecutor.getById(taskInstanceId);
            taskInstance.setApprovedCount(taskInstance.getApprovedCount() + 1);
            taskInstance.setStatus(NodeStatus.REJECTED);
            taskInstanceExecutor.updateById(taskInstance);

            // 找到流程定义
            Integer workflowInstanceId = taskInstance.getWorkflowInstanceId();
            WorkflowInstanceExecutor workflowInstanceExecutor = workflowInstanceExecutorBuilder.build();
            WorkflowInstance workflowInstance = workflowInstanceExecutor.getById(workflowInstanceId);

            // 找到结束节点定义
            NodeDefinitionExecutor nodeDefinitionExecutor = nodeDefinitionExecutorBuilder.build();
            NodeDefinition endNode = nodeDefinitionExecutor.getEndNode(tenantId, workflowInstance.getWorkflowDefinitionId());

            // 创建结束节点实例，将状态设置为：已驳回
            taskInstanceExecutor = taskInstanceExecutorBuilder.tenantId(tenantId)
                    .workflowInstanceId(workflowInstanceId).nodeDefinitionId(endNode.getId())
                    .status(NodeStatus.REJECTED).build();
            taskInstanceExecutor.save();

            // 更新工作流实例，将状态设置为：已驳回
            workflowInstance.setStatus(WorkflowStatus.REJECTED);
            workflowInstanceExecutor.updateById(workflowInstance);

            // 记录流程日志（审批驳回）
            recordLogs(tenantId, taskInstance.getWorkflowInstanceId(), taskInstanceId, TaskHistoryMessage.INSTANCE_REJECTED);
        });
    }

    /**
     * 弃权
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     * @param approverId     审批人 ID
     * @param comment        审批意见
     */
    @Override
    public void abandon(String tenantId, Integer taskInstanceId, String approverId, String comment) {
        JdbcTemplateHelper jdbcTemplateHelper = this.context.getJdbcTemplateHelper();
        jdbcTemplateHelper.executeInTransaction(() -> {
            // 更新 TaskApprove 的 comment，并将状态设置为：已弃权
            TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder.build();
            TaskApprove taskApprove = taskApproveExecutor.getByTaskInstanceIdAndApproverId(tenantId, taskInstanceId, approverId);
            taskApprove.setStatus(ApproveStatus.ABANDONED);
            taskApprove.setComment(comment);
            taskApproveExecutor.updateById(taskApprove);

            // 获取当前审批任务，并修改当前审批任务的 approved_count 数量
            TaskInstanceExecutor taskInstanceExecutor = taskInstanceExecutorBuilder.build();
            TaskInstance taskInstance = taskInstanceExecutor.getById(taskInstanceId);
            taskInstance.setApprovedCount(taskInstance.getApprovedCount() + 1);

            // 获取节点定义，找到审批类型
            NodeDefinitionExecutor nodeDefinitionExecutor = nodeDefinitionExecutorBuilder.build();
            NodeDefinition nodeDefinition = nodeDefinitionExecutor.getById(taskInstance.getNodeDefinitionId());

            // 获取当前实例是否有同意的审批结果
            boolean hasApproved = false;
            if (ApproveType.ALL == nodeDefinition.getApproveType()) {
                List<TaskApprove> taskApproves = taskApproveExecutor.findByTaskInstanceId(tenantId, taskInstanceId);
                TaskApprove anyApproved = taskApproves.stream().filter(i -> ApproveStatus.APPROVED == i.getStatus()).findAny().orElse(null);
                if (anyApproved != null) {
                    hasApproved = true;
                }
            }

            // 已完成审批的人数 approved_count 等于所有需要审批的人数 total_count，则表示为当前是最后一个审批人
            if (Objects.equals(taskInstance.getApprovedCount(), taskInstance.getTotalCount())) {
                if (hasApproved) {
                    // 进入下一个审批流程
                    this.next(tenantId, taskInstanceId);
                } else {
                    // 最后一个审批人，无法放弃审批
                    throw new WorkflowException("The last approver can't give up the approval");
                }
            }
            taskInstanceExecutor.updateById(taskInstance);

            // 记录流程日志（审批弃权）
            recordLogs(tenantId, taskInstance.getWorkflowInstanceId(), taskInstanceId, TaskHistoryMessage.INSTANCE_ABANDONED);
        });
    }

    /**
     * 取消
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     */
    @Override
    public void cancel(String tenantId, Integer workflowInstanceId) {
        // 查看流程状态
        WorkflowInstanceExecutor workflowInstanceExecutor = workflowInstanceExecutorBuilder.build();
        WorkflowInstance workflowInstance = workflowInstanceExecutor.getById(workflowInstanceId);
        if (WorkflowStatus.IN_PROGRESS != workflowInstance.getStatus()) {
            throw new WorkflowException("The workflow instance status is not in progress: %s", workflowInstance.getStatus().getDesc());
        }

        // 查找审批状态
        TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder.build();
        List<TaskApprove> taskApproves = taskApproveExecutor.findByTWorkflowInstanceId(tenantId, workflowInstanceId);
        TaskApprove taskApprove = taskApproves.stream().filter(i -> ApproveStatus.IN_PROGRESS != i.getStatus()).findFirst().orElse(null);
        if (taskApprove != null) {
            throw new WorkflowException("The workflow instance has been approved by: %s", taskApprove.getApproverId());
        }

        // 获取所有审批节点，并设置为：已取消
        TaskInstanceExecutor taskInstanceExecutor = taskInstanceExecutorBuilder.build();
        List<TaskInstance> taskInstances = taskInstanceExecutor.findByWorkflowInstanceIdAndNodeType(tenantId, workflowInstanceId, NodeType.TASK);
        for (TaskInstance taskInstance : taskInstances) {
            taskInstance.setStatus(NodeStatus.CANCELED);
            taskInstanceExecutor.updateById(taskInstance);
        }

        // 获取结束节点
        NodeDefinitionExecutor nodeDefinitionExecutor = nodeDefinitionExecutorBuilder.build();
        NodeDefinition endNode = nodeDefinitionExecutor.getEndNode(tenantId, workflowInstance.getWorkflowDefinitionId());

        // 创建取消节点实例
        taskInstanceExecutor = taskInstanceExecutorBuilder.tenantId(tenantId).workflowInstanceId(workflowInstanceId)
                .nodeDefinitionId(endNode.getId()).status(NodeStatus.CANCELED).build();
        taskInstanceExecutor.save();

        // 流程实例标记为：已取消
        workflowInstance.setStatus(WorkflowStatus.CANCELED);
        workflowInstanceExecutor.updateById(workflowInstance);

        // 记录流程日志（审批取消）
        recordLogs(tenantId, workflowInstanceId, null, TaskHistoryMessage.INSTANCE_CANCELED);
    }


    /**
     * 查询审批任务
     *
     * @param tenantId   租户 ID
     * @param approverId 审批人 ID
     *
     * @return List<TaskInstance>
     */
    @Override
    public List<TaskInstance> findTaskInstances(String tenantId, String approverId) {
        return this.findTaskInstances(tenantId, approverId, null, null);
    }

    /**
     * 分页查询审批任务
     *
     * @param tenantId      租户 ID
     * @param approverId    审批人 ID
     * @param nodeStatus    节点状态
     * @param approveStatus 审批人审批状态
     *
     * @return List<TaskInstance>
     */
    @Override
    public List<TaskInstance> findTaskInstances(String tenantId, String approverId, NodeStatus nodeStatus, ApproveStatus approveStatus) {
        return this.findTaskInstances(tenantId, approverId, nodeStatus, approveStatus, 1, Integer.MAX_VALUE);
    }

    /**
     * 分页查询审批任务
     *
     * @param tenantId      租户 ID
     * @param approverId    审批人 ID
     * @param nodeStatus    节点状态
     * @param approveStatus 审批人审批状态
     * @param page          页码
     * @param pageSize      每页数量
     *
     * @return List<TaskInstance>
     */
    @Override
    public List<TaskInstance> findTaskInstances(String tenantId, String approverId, NodeStatus nodeStatus, ApproveStatus approveStatus, Integer page, Integer pageSize) {
        TaskInstanceExecutor taskInstanceExecutor = taskInstanceExecutorBuilder.build();
        return taskInstanceExecutor.findByApproverId(tenantId, approverId, nodeStatus, approveStatus, page, pageSize);
    }

    /**
     * 根据发起人 ID 查询工作流实例
     *
     * @param tenantId    租户 ID
     * @param requesterId 请求人 ID
     * @param status      工作流状态
     * @param page        页码
     * @param pageSize    每页数量
     *
     * @return List<WorkflowInstance>
     */
    @Override
    public List<WorkflowInstance> findWorkflowInstancesByRequestId(String tenantId, String requesterId, WorkflowStatus status, Integer page, Integer pageSize) {
        WorkflowInstanceExecutor workflowInstanceExecutor = workflowInstanceExecutorBuilder.build();
        return workflowInstanceExecutor.findByRequesterId(tenantId, requesterId, status, page, pageSize);
    }

    /**
     * 根据发起人 ID 查询工作流实例
     *
     * @param tenantId    租户 ID
     * @param requesterId 请求人 ID
     * @param status      工作流状态
     *
     * @return List<WorkflowInstance>
     */
    @Override
    public List<WorkflowInstance> findWorkflowInstancesByRequestId(String tenantId, String requesterId, WorkflowStatus status) {
        return this.findWorkflowInstancesByRequestId(tenantId, requesterId, status, 1, Integer.MAX_VALUE);
    }

    /**
     * 根据发起人 ID 查询工作流实例
     *
     * @param tenantId    租户 ID
     * @param requesterId 请求人 ID
     *
     * @return List<WorkflowInstance>
     */
    @Override
    public List<WorkflowInstance> findWorkflowInstancesByRequestId(String tenantId, String requesterId) {
        return this.findWorkflowInstancesByRequestId(tenantId, requesterId, null);
    }

    /**
     * 根据租户 ID 和 流程定义 Key 获取流程定义
     *
     * @param tenantId              租户 ID
     * @param workflowDefinitionKey 流程定义 Key
     *
     * @return WorkflowDefinition
     *
     * @author wangweijun
     * @since 2024/6/24 11:24
     */
    private WorkflowDefinition getWorkflowDefinition(String tenantId, String workflowDefinitionKey) {
        WorkflowDefinitionExecutor definitionExecutor = workflowDefinitionExecutorBuilder.tenantId(tenantId).key(workflowDefinitionKey).build();
        return definitionExecutor.getByTenantAndKey();
    }
}
