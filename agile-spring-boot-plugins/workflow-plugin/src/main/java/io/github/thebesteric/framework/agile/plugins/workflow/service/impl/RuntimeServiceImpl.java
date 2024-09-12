package io.github.thebesteric.framework.agile.plugins.workflow.service.impl;

import cn.hutool.core.collection.CollUtil;
import io.github.thebesteric.framework.agile.commons.exception.InvalidDataException;
import io.github.thebesteric.framework.agile.core.domain.Pair;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.Page;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.builder.Query;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.builder.QueryBuilderWrapper;
import io.github.thebesteric.framework.agile.plugins.database.core.jdbc.JdbcTemplateHelper;
import io.github.thebesteric.framework.agile.plugins.workflow.config.AgileWorkflowContext;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.*;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.Approver;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.Conditions;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.RequestConditions;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.assignment.NodeAssignmentExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.assignment.NodeAssignmentExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.definition.NodeDefinitionExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.definition.NodeDefinitionExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.relation.NodeRelationExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.relation.NodeRelationExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.task.approve.TaskApproveBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.task.approve.TaskApproveExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.task.approve.TaskApproveExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.task.history.TaskHistoryExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.task.history.TaskHistoryExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.task.instance.TaskInstanceExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.task.instance.TaskInstanceExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.definition.WorkflowDefinitionExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.definition.WorkflowDefinitionExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.instance.WorkflowInstanceExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.instance.WorkflowInstanceExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.response.TaskHistoryResponse;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.response.WorkflowInstanceApproveRecords;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.*;
import io.github.thebesteric.framework.agile.plugins.workflow.exception.WorkflowException;
import io.github.thebesteric.framework.agile.plugins.workflow.service.AbstractRuntimeService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;

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
            if (PublishStatus.UNPUBLISHED == workflowDefinition.getPublish()) {
                throw new WorkflowException("流程尚未发布，请先发布流程后重试");
            }

            // 获取流程实例
            WorkflowInstanceExecutor instanceExecutor = workflowInstanceExecutorBuilder.tenantId(tenantId).workflowDefinitionId(workflowDefinition.getId())
                    .requesterId(requesterId).businessId(businessId).businessType(businessType).requestConditions(requestConditions).status(WorkflowStatus.IN_PROGRESS).desc(desc).build();
            WorkflowInstance workflowInstance = instanceExecutor.save();
            Integer workflowInstanceId = workflowInstance.getId();

            // 获取开始节点定义
            NodeDefinitionExecutor nodeDefinitionExecutor = nodeDefinitionExecutorBuilder.build();
            NodeDefinition startNodeDefinition = nodeDefinitionExecutor.getStartNode(tenantId, workflowDefinition.getId());

            // 记录流程日志（提交审批）
            recordLogs(tenantId, workflowInstanceId, null, null, TaskHistoryMessage.INSTANCE_SUBMIT_FORM);

            // 创建审批开始实例
            TaskInstanceExecutor taskInstanceExecutor = taskInstanceExecutorBuilder.tenantId(tenantId).status(NodeStatus.COMPLETED)
                    .workflowInstanceId(workflowInstanceId).nodeDefinitionId(startNodeDefinition.getId()).build();
            TaskInstance startTaskInstance = taskInstanceExecutor.save();

            // 记录流程日志（开始审批）
            recordLogs(tenantId, workflowInstanceId, startTaskInstance.getId(), startNodeDefinition.getName(), TaskHistoryMessage.INSTANCE_STARTED);

            // 创建下一个审批实例（多个审批实例，通常是条件节点）
            List<NodeDefinition> toTaskNodes = nodeDefinitionExecutor.findToTaskNodesByFromNodeId(tenantId, startNodeDefinition.getId());
            if (CollUtil.isNotEmpty(toTaskNodes)) {
                List<NodeDefinition> nextNodeDefinitions = new ArrayList<>();
                // 只有一个审批节点
                if (toTaskNodes.size() == 1) {
                    NodeDefinition toTaskNode = toTaskNodes.get(0);
                    Conditions conditions = toTaskNode.getConditions();
                    // 没有审批条件或满足审批条件
                    if (conditions == null || conditions.matchRequestCondition(requestConditions)) {
                        nextNodeDefinitions.add(toTaskNode);
                    }
                }
                // 存在多个审批节点
                else {
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

                    // 查询动态审批人
                    Optional<NodeAssignment> anyDynamicAssignmentApprover = nextNodeAssignments.stream()
                            .filter(nodeAssignment -> nodeAssignment.getUserId().startsWith(WorkflowConstants.DYNAMIC_ASSIGNMENT_APPROVER_VALUE_PREFIX)).findAny();
                    // 存在动态指定审批人，且待审批人未配置
                    if (nextNodeDefinition.isDynamicAssignment() && anyDynamicAssignmentApprover.isPresent()) {
                        throw new WorkflowException("指定审批人节点，不允许创建审批实例，请先设置审批人");
                    }

                    // 保存任务节点
                    taskInstanceExecutor = taskInstanceExecutorBuilder.status(NodeStatus.IN_PROGRESS)
                            .tenantId(tenantId)
                            .workflowInstanceId(workflowInstanceId).nodeDefinitionId(nextNodeDefinitionId)
                            .approvedCount(0)
                            // 设置总需要审批的次数
                            .totalCount(calcTotalCount(workflowDefinition, nextNodeDefinition.getApproveType(), nextNodeAssignments.size()))
                            .build();
                    TaskInstance nextTaskInstance = taskInstanceExecutor.save();
                    Integer nextTaskInstanceId = nextTaskInstance.getId();

                    // 存在审批人：创建任务实例审批人
                    if (CollUtil.isNotEmpty(nextNodeAssignments)) {
                        int i = 0;
                        for (NodeAssignment nextNodeAssignment : nextNodeAssignments) {
                            taskApproveExecutorBuilder
                                    .tenantId(tenantId)
                                    .workflowInstanceId(workflowInstanceId)
                                    .taskInstanceId(nextTaskInstanceId)
                                    .approverId(nextNodeAssignment.getUserId())
                                    .approveSeq(nextNodeAssignment.getUserSeq())
                                    .status(ApproveStatus.IN_PROGRESS)
                                    .active(ActiveStatus.ACTIVE).build();
                            // 顺序审批，后续审批人需要等待前一个审批人审批完成，后续的审批状态设置为：挂起
                            if (ApproveType.SEQ == nextNodeDefinition.getApproveType() && i > 0) {
                                taskApproveExecutorBuilder.status(ApproveStatus.SUSPEND);
                            }
                            TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder.build();
                            taskApproveExecutor.save();
                            i++;
                        }
                    }
                    // 没有审批人: 允许自动同意，则自动同意
                    else if (workflowDefinition.isAllowEmptyAutoApprove()) {
                        this.approve(tenantId, nextTaskInstanceId, WorkflowConstants.AUTO_APPROVER, WorkflowConstants.AUTO_APPROVER_COMMENT);
                    }
                    // 其他未知情况
                    else {
                        throw new WorkflowException("未知异常，请联系系统管理员");
                    }
                }
            }
            return workflowInstance;
        });
    }

    /**
     * 计算总需要审批的次数
     *
     * @param workflowDefinition 流程定义
     * @param approveType        审批类型
     * @param assignmentSize     候选审批人数量
     *
     * @return Integer
     *
     * @author wangweijun
     * @since 2024/7/15 09:44
     */
    private Integer calcTotalCount(WorkflowDefinition workflowDefinition, ApproveType approveType, Integer assignmentSize) {
        // 除或签、自动审批外，其余均需要审批全部
        return ApproveType.ANY == approveType || (workflowDefinition.isAllowEmptyAutoApprove() && assignmentSize == 0) ? 1 : assignmentSize;
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
     * @param title              日志标题
     * @param message            日志信息
     */
    private void recordLogs(String tenantId, Integer workflowInstanceId, Integer taskInstanceId, String title, TaskHistoryMessage message) {
        taskHistoryExecutorBuilder.tenantId(tenantId).workflowInstanceId(workflowInstanceId).taskInstanceId(taskInstanceId)
                .title(title).message(message).build().save();
    }

    /**
     * 获取下一个审批节点
     *
     * @param tenantId           租户 ID
     * @param prevTaskInstanceId 上一个节点实例 ID
     * @param approverId         当前审批人 ID
     *
     * @return 返回下一个审批节点
     */
    @Override
    public List<TaskInstance> next(String tenantId, Integer prevTaskInstanceId, String approverId) {
        JdbcTemplateHelper jdbcTemplateHelper = this.context.getJdbcTemplateHelper();
        return jdbcTemplateHelper.executeInTransaction(() -> {
            NodeRelationExecutor nodeRelationExecutor = nodeRelationExecutorBuilder.build();
            NodeDefinitionExecutor nodeDefinitionExecutor = nodeDefinitionExecutorBuilder.build();
            WorkflowInstanceExecutor workflowInstanceExecutor = workflowInstanceExecutorBuilder.build();
            TaskInstanceExecutor taskInstanceExecutor = taskInstanceExecutorBuilder.build();
            WorkflowDefinitionExecutor workflowDefinitionExecutor = workflowDefinitionExecutorBuilder.build();

            // 上一个节点实例
            TaskInstance prevTaskInstance = taskInstanceExecutor.getById(prevTaskInstanceId);

            // 获取流程实例
            WorkflowInstance workflowInstance = workflowInstanceExecutor.getById(prevTaskInstance.getWorkflowInstanceId());

            // 获取流程定义
            WorkflowDefinition workflowDefinition = workflowDefinitionExecutor.getById(workflowInstance.getWorkflowDefinitionId());

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

                return null;
            }

            List<NodeRelation> nodeRelations;
            List<Integer> toNodeIds = toNodeDefinitions.stream().map(NodeDefinition::getId).toList();
            if (toNodeIds.size() == 1) {
                nodeRelations = nodeRelationExecutor.findByFromNodeId(tenantId, toNodeIds.get(0));
            } else {
                nodeRelations = nodeRelationExecutor.findByFromNodeIds(tenantId, toNodeIds);
            }

            // 下一个审批节点集合
            List<TaskInstance> nextTaskInstances = new ArrayList<>();

            if (!nodeRelations.isEmpty()) {
                for (NodeRelation nodeRelation : nodeRelations) {

                    // 下一个节点定义
                    Integer nextNodeDefinitionId = nodeRelation.getFromNodeId();
                    NodeDefinition nextNodeDefinition = nodeDefinitionExecutor.getById(nextNodeDefinitionId);

                    // 判断节点关系是否是逆向条件
                    Integer toNodeId = nodeRelation.getToNodeId();
                    NodeDefinition toNodeDefinition = nodeDefinitionExecutor.getById(toNodeId);
                    if (nodeRelations.size() > 1 && NodeType.END == toNodeDefinition.getNodeType() && !nextNodeDefinition.hasConditions()) {
                        continue;
                    }

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
                                .status(NodeStatus.IN_PROGRESS)
                                // 总审批人数：或签 => 总审批人数为 1，其他 => 总审批人数为节点审批人数量
                                .totalCount(ApproveType.ANY == nextNodeDefinition.getApproveType() ? 1 : nextNodeAssignments.size())
                                .approvedCount(0);
                    }

                    // 保存任务节点
                    taskInstanceExecutor = taskInstanceExecutorBuilder.build();
                    TaskInstance nextTaskInstance = taskInstanceExecutor.save();

                    // 如果允许自动审批，并且没有审批人，则自动审批
                    if (workflowDefinition.isAllowEmptyAutoApprove() && nextNodeAssignments.isEmpty()) {
                        this.approve(tenantId, nextTaskInstance.getId(), WorkflowConstants.AUTO_APPROVER, WorkflowConstants.AUTO_APPROVER_COMMENT);
                    }

                    // 创建任务实例审批人
                    if (CollUtil.isNotEmpty(nextNodeAssignments) && NodeType.END != nextNodeDefinition.getNodeType()) {
                        int i = 0;
                        for (NodeAssignment nextNodeAssignment : nextNodeAssignments) {
                            taskApproveExecutorBuilder
                                    .tenantId(tenantId)
                                    .workflowInstanceId(nextTaskInstance.getWorkflowInstanceId())
                                    .taskInstanceId(nextTaskInstance.getId())
                                    .approverId(nextNodeAssignment.getUserId())
                                    .approveSeq(nextNodeAssignment.getUserSeq())
                                    .active(ActiveStatus.ACTIVE)
                                    .status(ApproveStatus.IN_PROGRESS)
                                    .build();

                            // 顺序审批，后续审批人需要等待前一个审批人审批完成，后续的审批状态设置为：挂起
                            if (ApproveType.SEQ == nextNodeDefinition.getApproveType() && i > 0) {
                                taskApproveExecutorBuilder.status(ApproveStatus.SUSPEND);
                            }
                            TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder.build();
                            taskApproveExecutor.save();
                            i++;

                            // 判断连续审批模式
                            ContinuousApproveMode continuousApproveMode = workflowDefinition.getContinuousApproveMode();
                            this.continuousApproveModeProcess(tenantId, nextTaskInstance, approverId, nextNodeAssignment.getUserId(), continuousApproveMode);
                        }
                    }

                    nextTaskInstances.add(nextTaskInstance);
                }
            }
            return nextTaskInstances;
        });
    }

    /**
     * 连续审批模式处理
     *
     * @param tenantId              租户 ID
     * @param nextTaskInstance      下一个审批节点
     * @param approverId            当前审批人 ID
     * @param nextApproverId        下一个审批人 ID
     * @param continuousApproveMode 连续审批模式
     *
     * @author wangweijun
     * @since 2024/9/11 11:46
     */
    private void continuousApproveModeProcess(String tenantId, TaskInstance nextTaskInstance,
                                              String approverId, String nextApproverId,
                                              ContinuousApproveMode continuousApproveMode) {

        TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder.build();

        // 获取该流程实例下已经完成审批和即将要审批的审批人
        List<TaskApprove> taskApproves = taskApproveExecutor.findByTWorkflowInstanceId(tenantId, nextTaskInstance.getWorkflowInstanceId(), null);

        // 下一个审批人的审批情况
        Optional<TaskApprove> nextApproverIdApproveOptional = taskApproves.stream()
                .filter(approve -> nextApproverId.equals(approve.getApproverId()) && ApproveStatus.APPROVED == approve.getStatus()).findAny();

        switch (continuousApproveMode) {
            case APPROVE_FIRST:
                // 下个审批人已经存在审批的节点，则自动审批
                if (nextApproverIdApproveOptional.isPresent()) {
                    this.approve(tenantId, nextTaskInstance.getId(), nextApproverId, WorkflowConstants.AUTO_APPROVER_COMMENT);
                }
                break;
            case APPROVE_CONTINUOUS:
                // 下个审批人已经存在审批的节点，且已审批的节点的审批人和下一个节点的审批人是同一个人，则自动审批
                if (nextApproverIdApproveOptional.isPresent() && approverId.equals(nextApproverId)) {
                    this.approve(tenantId, nextTaskInstance.getId(), nextApproverId, WorkflowConstants.AUTO_APPROVER_COMMENT);
                }
                break;
            case APPROVE_ALL:
            default:
                break;
        }
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

            // 检查审批意见是否必填
            checkIfRequiredComment(taskInstanceId, comment);

            // 修改当前 TaskInstance 的 approved_count 数量
            TaskInstanceExecutor taskInstanceExecutor = taskInstanceExecutorBuilder.build();
            TaskInstance taskInstance = taskInstanceExecutor.getById(taskInstanceId);
            taskInstance.setApprovedCount(taskInstance.getApprovedCount() + 1);


            TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder.build();
            // 自动审批的情况
            if (WorkflowConstants.AUTO_APPROVER.equals(approverId)) {
                // 创建一个 approver
                TaskApprove taskApprove = TaskApproveBuilder.builder()
                        .tenantId(tenantId)
                        .workflowInstanceId(taskInstance.getWorkflowInstanceId())
                        .taskInstanceId(taskInstanceId)
                        .approverId(approverId)
                        .active(ActiveStatus.INACTIVE)
                        .status(ApproveStatus.APPROVED)
                        .comment(comment)
                        .build();
                taskApproveExecutor.save(taskApprove);
            }
            // 非自动审批的情况
            else {
                // 更新 TaskApprove 的 active、status 和 comment
                TaskApprove taskApprove = taskApproveExecutor.getByTaskInstanceIdAndApproverId(tenantId, taskInstanceId, approverId);
                taskApprove.setActive(ActiveStatus.INACTIVE);
                taskApprove.setStatus(ApproveStatus.APPROVED);
                taskApprove.setComment(comment);
                taskApproveExecutor.updateById(taskApprove);
            }

            // 获取当前实例的节点定义
            NodeDefinitionExecutor nodeDefinitionExecutor = nodeDefinitionExecutorBuilder.build();
            NodeDefinition nodeDefinition = nodeDefinitionExecutor.getById(taskInstance.getNodeDefinitionId());

            // 判断是否是顺序审批
            checkIfApproveSeqStatus(tenantId, taskInstanceId, nodeDefinition.getApproveType());

            List<TaskInstance> nextTaskInstances = null;
            // 已完成审批的人数 approved_count 等于所有需要审批的人数 total_count
            if (Objects.equals(taskInstance.getApprovedCount(), taskInstance.getTotalCount())) {
                // 任一审批人审批通过的条件下，将其他审批人节点设置为 SKIP，表示跳过
                if (ApproveType.ANY == nodeDefinition.getApproveType()) {
                    // 获取其他的审批人节点
                    List<TaskApprove> otherActiveTaskApproves = taskApproveExecutor.findByTaskInstanceId(tenantId, taskInstanceId, ActiveStatus.ACTIVE);
                    otherActiveTaskApproves.forEach(otherActiveTaskApprove -> {
                        // 将其他审批人节点设置为 SKIP，表示跳过
                        otherActiveTaskApprove.convertToApproveStatusSkip();
                        taskApproveExecutor.updateById(otherActiveTaskApprove);
                    });
                }
                taskInstance.setStatus(NodeStatus.COMPLETED);
                // 指向下一个审批节点
                nextTaskInstances = this.next(tenantId, taskInstance.getId(), approverId);
            }
            taskInstanceExecutor.updateById(taskInstance);

            // 记录流程日志（审批通过）
            recordLogs(tenantId, taskInstance.getWorkflowInstanceId(), taskInstanceId, nodeDefinition.getName(), TaskHistoryMessage.INSTANCE_APPROVED);

            // 没有下一级审批节点，且当前审批节点全部审批完成，则表示流程已经结束
            if (nextTaskInstances == null && taskInstance.isCompleted()) {
                // 记录流程日志（审批结束）
                NodeDefinition endNodeDefinition = nodeDefinitionExecutor.getEndNode(tenantId, nodeDefinition.getWorkflowDefinitionId());
                recordLogs(tenantId, taskInstance.getWorkflowInstanceId(), null, endNodeDefinition.getName(), TaskHistoryMessage.INSTANCE_ENDED);
            }
        });

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
    @Override
    public void redo(String tenantId, Integer taskInstanceId, String approverId, String comment) {
        JdbcTemplateHelper jdbcTemplateHelper = this.context.getJdbcTemplateHelper();
        jdbcTemplateHelper.executeInTransaction(() -> {

            WorkflowDefinitionExecutor workflowDefinitionExecutor = workflowDefinitionExecutorBuilder.build();
            WorkflowDefinition workflowDefinition = workflowDefinitionExecutor.getByTaskInstanceId(taskInstanceId);
            // 检查是否允许撤回
            if (!workflowDefinition.isAllowRedo()) {
                throw new WorkflowException("流程定义不允许撤回");
            }

            // 检查审批意见是否必填
            checkIfRequiredComment(taskInstanceId, comment);

            // 当前审批节点
            TaskInstanceExecutor taskInstanceExecutor = taskInstanceExecutorBuilder.build();
            TaskInstance currTaskInstance = taskInstanceExecutor.getById(taskInstanceId);

            // 判断流程实例是否已完成
            WorkflowInstanceExecutor workflowInstanceExecutor = workflowInstanceExecutorBuilder.build();
            Integer workflowInstanceId = currTaskInstance.getWorkflowInstanceId();
            WorkflowInstance workflowInstance = workflowInstanceExecutor.getById(workflowInstanceId);
            if (!Objects.equals(WorkflowStatus.IN_PROGRESS, workflowInstance.getStatus())) {
                throw new WorkflowException("流程实例已经完成审批，无法撤回");
            }

            // 判断当前审批节点是否是多人审批模式
            TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder.build();
            List<TaskApprove> currTaskApproves = taskApproveExecutor.findByTaskInstanceId(tenantId, taskInstanceId);
            // 过滤掉当前审批人的审批节点
            currTaskApproves = new ArrayList<>(currTaskApproves);
            currTaskApproves.removeIf(currTaskApprove -> approverId.equals(currTaskApprove.getApproverId()));
            if (!currTaskApproves.isEmpty()) {
                for (TaskApprove currTaskApprove : currTaskApproves) {
                    // 如果存在其他节点完成审批（非进行中状态），则无法撤回
                    if (ApproveStatus.IN_PROGRESS != currTaskApprove.getStatus() && ActiveStatus.ACTIVE == currTaskApprove.getActive()) {
                        throw new WorkflowException("已存在其他节点完成审批，无法撤回");
                    }
                }
                // 还原其他审批节点
                currTaskApproves.forEach(taskApprove -> {
                    taskApprove.convertToApproveStatusInProgress();
                    taskApproveExecutor.updateById(taskApprove);
                });
            }


            // 下一个任务实例节点集合
            List<TaskInstance> nextTaskInstances;
            Query query = QueryBuilderWrapper.createLambda(TaskInstance.class)
                    .eq(TaskInstance::getTenantId, tenantId)
                    .eq(TaskInstance::getWorkflowInstanceId, workflowInstanceId)
                    .eq(TaskInstance::getStatus, NodeStatus.IN_PROGRESS.getCode())
                    .eq(TaskInstance::getState, 1).build();
            Page<TaskInstance> taskInstancePage = taskInstanceExecutor.find(query);
            nextTaskInstances = taskInstancePage.getRecords();


            // 如果存在其他的审批人节点，将其他审批人节点设置为 SUSPEND，表示挂起
            if (!nextTaskInstances.isEmpty()) {
                for (TaskInstance nextTaskInstance : nextTaskInstances) {
                    // 表示已经到下一个审批流程（实例）：非同一流程多人审批的情况
                    if (!Objects.equals(currTaskInstance.getId(), nextTaskInstance.getId())) {
                        List<TaskApprove> otherActiveTaskApproves = taskApproveExecutor.findByTaskInstanceId(tenantId, nextTaskInstance.getId());
                        if (!otherActiveTaskApproves.isEmpty()) {
                            Set<Integer> readyToDeleteOtherActiveTaskApproveIds = new HashSet<>();
                            // 判断是否有其他审批节点已经完成审批
                            otherActiveTaskApproves.forEach(otherActiveTaskApprove -> {
                                if (ApproveStatus.IN_PROGRESS != otherActiveTaskApprove.getStatus()) {
                                    throw new WorkflowException("下个审批节点已经完成审批，无法撤回");
                                }
                                readyToDeleteOtherActiveTaskApproveIds.add(otherActiveTaskApprove.getId());
                            });
                            // 删除其他审批人节点
                            for (Integer taskApproveId : readyToDeleteOtherActiveTaskApproveIds) {
                                taskApproveExecutor.deleteById(taskApproveId);
                            }
                        }
                        // 删除下一个审批节点
                        taskInstanceExecutor.deleteById(nextTaskInstance.getId());
                    }
                }
            }


            // 更新当前 TaskApprove 的 status 和 comment
            query = QueryBuilderWrapper.createLambda(TaskApprove.class)
                    .eq(TaskApprove::getTenantId, tenantId)
                    .eq(TaskApprove::getTaskInstanceId, taskInstanceId)
                    .eq(TaskApprove::getApproverId, approverId)
                    .eq(TaskApprove::getState, 1).build();
            TaskApprove currTaskApprove = taskApproveExecutor.get(query);
            currTaskApprove.convertToApproveStatusInProgress(comment);
            taskApproveExecutor.updateById(currTaskApprove);

            // 修改当前 TaskInstance 的 approved_count 数量，并将 status 设置为 IN_PROGRESS
            currTaskInstance.setApprovedCount(currTaskInstance.getApprovedCount() - 1);
            currTaskInstance.setStatus(NodeStatus.IN_PROGRESS);
            taskInstanceExecutor.updateById(currTaskInstance);

            // 获取当前实例的节点定义
            NodeDefinitionExecutor nodeDefinitionExecutor = nodeDefinitionExecutorBuilder.build();
            NodeDefinition nodeDefinition = nodeDefinitionExecutor.getById(currTaskInstance.getNodeDefinitionId());

            // 记录流程日志（审批撤回）
            recordLogs(tenantId, workflowInstanceId, taskInstanceId, nodeDefinition.getName(), TaskHistoryMessage.INSTANCE_REDO);

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

            // 检查审批意见是否必填
            checkIfRequiredComment(taskInstanceId, comment);

            // 更新 TaskApprove 的 comment，并将状态设置为：已驳回
            TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder.build();
            TaskApprove taskApprove = taskApproveExecutor.getByTaskInstanceIdAndApproverId(tenantId, taskInstanceId, approverId);
            taskApprove.setActive(ActiveStatus.INACTIVE);
            taskApprove.setStatus(ApproveStatus.REJECTED);
            taskApprove.setComment(comment);
            taskApproveExecutor.updateById(taskApprove);

            // 找到所有未进行审批的审批人节点，并将其状态设置为：已失效
            List<TaskApprove> activeTaskApproves = taskApproveExecutor.findByTaskInstanceId(tenantId, taskInstanceId, ActiveStatus.ACTIVE);
            activeTaskApproves.forEach(activeTaskApprove -> {
                activeTaskApprove.convertToApproveStatusSkip();
                taskApproveExecutor.updateById(activeTaskApprove);
            });

            // 修改当前 TaskInstance 的 approved_count 数量，并将 status 更新为：已驳回
            TaskInstanceExecutor taskInstanceExecutor = taskInstanceExecutorBuilder.build();
            TaskInstance taskInstance = taskInstanceExecutor.getById(taskInstanceId);
            taskInstance.setApprovedCount(taskInstance.getApprovedCount() + 1);
            taskInstance.setStatus(NodeStatus.REJECTED);
            taskInstanceExecutor.updateById(taskInstance);

            // 获取当前实例的节点定义
            NodeDefinitionExecutor nodeDefinitionExecutor = nodeDefinitionExecutorBuilder.build();
            NodeDefinition nodeDefinition = nodeDefinitionExecutor.getById(taskInstance.getNodeDefinitionId());

            // 找到流程定义
            Integer workflowInstanceId = taskInstance.getWorkflowInstanceId();
            WorkflowInstanceExecutor workflowInstanceExecutor = workflowInstanceExecutorBuilder.build();
            WorkflowInstance workflowInstance = workflowInstanceExecutor.getById(workflowInstanceId);

            // 找到结束节点定义
            NodeDefinition endNodeDefinition = nodeDefinitionExecutor.getEndNode(tenantId, workflowInstance.getWorkflowDefinitionId());

            // 创建结束节点实例，将状态设置为：已驳回
            taskInstanceExecutor = taskInstanceExecutorBuilder.tenantId(tenantId)
                    .workflowInstanceId(workflowInstanceId).nodeDefinitionId(endNodeDefinition.getId())
                    .status(NodeStatus.REJECTED).build();
            taskInstanceExecutor.save();

            // 更新工作流实例，将状态设置为：已驳回
            workflowInstance.setStatus(WorkflowStatus.REJECTED);
            workflowInstanceExecutor.updateById(workflowInstance);

            // 记录流程日志（审批驳回）
            recordLogs(tenantId, taskInstance.getWorkflowInstanceId(), taskInstanceId, nodeDefinition.getName(), TaskHistoryMessage.INSTANCE_REJECTED);
            // 记录流程日志（审批结束）
            recordLogs(tenantId, taskInstance.getWorkflowInstanceId(), null, endNodeDefinition.getName(), TaskHistoryMessage.INSTANCE_ENDED);
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

            // 检查审批意见是否必填
            checkIfRequiredComment(taskInstanceId, comment);

            // 更新 TaskApprove 的 comment，并将状态设置为：已弃权
            TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder.build();
            TaskApprove taskApprove = taskApproveExecutor.getByTaskInstanceIdAndApproverId(tenantId, taskInstanceId, approverId);
            taskApprove.setActive(ActiveStatus.INACTIVE);
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
                List<TaskApprove> taskApproves = taskApproveExecutor.findByTaskInstanceId(tenantId, taskInstanceId, ActiveStatus.ACTIVE);
                TaskApprove anyApproved = taskApproves.stream().filter(i -> ApproveStatus.APPROVED == i.getStatus()).findAny().orElse(null);
                if (anyApproved != null) {
                    hasApproved = true;
                }
            }

            // 判断是否是顺序审批
            checkIfApproveSeqStatus(tenantId, taskInstanceId, nodeDefinition.getApproveType());

            List<TaskInstance> nextTaskInstances = null;
            // 已完成审批的人数 approved_count 等于所有需要审批的人数 total_count，则表示为当前是最后一个审批人
            if (Objects.equals(taskInstance.getApprovedCount(), taskInstance.getTotalCount())) {
                if (hasApproved) {
                    // 进入下一个审批流程
                    nextTaskInstances = this.next(tenantId, taskInstanceId, approverId);
                } else {
                    // 最后一个审批人，无法放弃审批
                    throw new WorkflowException("最后一个审批人无法放弃审批");
                }
            }
            taskInstanceExecutor.updateById(taskInstance);

            // 记录流程日志（审批弃权）
            recordLogs(tenantId, taskInstance.getWorkflowInstanceId(), taskInstanceId, nodeDefinition.getName(), TaskHistoryMessage.INSTANCE_ABANDONED);

            // 没有下一级审批节点，且当前审批节点全部审批完成，则表示流程已经结束
            if (nextTaskInstances == null && taskInstance.isCompleted()) {
                // 记录流程日志（审批结束）
                NodeDefinition endNodeDefinition = nodeDefinitionExecutor.getEndNode(tenantId, nodeDefinition.getWorkflowDefinitionId());
                recordLogs(tenantId, taskInstance.getWorkflowInstanceId(), null, endNodeDefinition.getName(), TaskHistoryMessage.INSTANCE_ENDED);
            }
        });
    }

    /**
     * 取消（针对提交人）
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
            throw new WorkflowException("流程实例已结束: %s", workflowInstance.getStatus().getDesc());
        }

        // 获取该流程下所有的审批人
        TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder.build();
        List<TaskApprove> taskApproves = taskApproveExecutor.findByTWorkflowInstanceId(tenantId, workflowInstanceId, null);
        // 查看是否已经有人参与了审批
        TaskApprove taskApprove = taskApproves.stream().filter(i -> ApproveStatus.IN_PROGRESS != i.getStatus()).findFirst().orElse(null);
        if (taskApprove != null) {
            throw new WorkflowException("流程实例已被审批: %s", taskApprove.getApproverId());
        }

        // 未审批的审批人节点：设置为已失效
        List<TaskApprove> inProgressApproves = taskApproves.stream().filter(i -> ApproveStatus.IN_PROGRESS == i.getStatus()).toList();
        inProgressApproves.forEach(inProgressApprove -> {
            inProgressApprove.convertToApproveStatusSkip();
            taskApproveExecutor.updateById(inProgressApprove);
        });

        // 获取所有审批节点，并设置为：已取消
        TaskInstanceExecutor taskInstanceExecutor = taskInstanceExecutorBuilder.build();
        List<TaskInstance> taskInstances = taskInstanceExecutor.findByWorkflowInstanceIdAndNodeType(tenantId, workflowInstanceId, NodeType.TASK);
        for (TaskInstance taskInstance : taskInstances) {
            taskInstance.setStatus(NodeStatus.CANCELED);
            taskInstanceExecutor.updateById(taskInstance);
        }

        // 获取结束节点
        NodeDefinitionExecutor nodeDefinitionExecutor = nodeDefinitionExecutorBuilder.build();
        NodeDefinition endNodeDefinition = nodeDefinitionExecutor.getEndNode(tenantId, workflowInstance.getWorkflowDefinitionId());

        // 创建取消节点实例
        taskInstanceExecutor = taskInstanceExecutorBuilder.tenantId(tenantId).workflowInstanceId(workflowInstanceId)
                .nodeDefinitionId(endNodeDefinition.getId()).status(NodeStatus.CANCELED).build();
        taskInstanceExecutor.save();

        // 流程实例标记为：已取消
        workflowInstance.setStatus(WorkflowStatus.CANCELED);
        workflowInstanceExecutor.updateById(workflowInstance);

        // 记录流程日志（审批取消）
        recordLogs(tenantId, workflowInstanceId, null, endNodeDefinition.getName(), TaskHistoryMessage.INSTANCE_CANCELED);
    }

    /**
     * 查询审批任务
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     * @param page               当前页
     * @param pageSize           每页显示数量
     *
     * @return List<TaskInstance>
     */
    @Override
    public Page<TaskInstance> findTaskInstances(String tenantId, Integer workflowInstanceId, Integer page, Integer pageSize) {
        return this.findTaskInstances(tenantId, workflowInstanceId, null, page, pageSize);
    }

    /**
     * 查询审批任务
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     * @param approverId         审批人 ID
     *
     * @return List<TaskInstance>
     */
    @Override
    public List<TaskInstance> findTaskInstances(String tenantId, Integer workflowInstanceId, String approverId) {
        return this.findTaskInstances(tenantId, workflowInstanceId, approverId, null, null, 1, Integer.MAX_VALUE).getRecords();
    }

    /**
     * 分页查询审批任务
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     * @param approverId         审批人 ID
     * @param page               当前页
     * @param pageSize           每页显示数量
     *
     * @return List<TaskInstance>
     */
    @Override
    public Page<TaskInstance> findTaskInstances(String tenantId, Integer workflowInstanceId, String approverId, Integer page, Integer pageSize) {
        return this.findTaskInstances(tenantId, workflowInstanceId, approverId, null, null, page, pageSize);
    }

    /**
     * 分页查询审批任务
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     * @param approverId         审批人 ID
     * @param nodeStatus         节点状态
     * @param approveStatus      审批人审批状态
     *
     * @return List<TaskInstance>
     */
    @Override
    public List<TaskInstance> findTaskInstances(String tenantId, Integer workflowInstanceId, String approverId, NodeStatus nodeStatus, ApproveStatus approveStatus) {
        return this.findTaskInstances(tenantId, workflowInstanceId, approverId, nodeStatus, approveStatus, 1, Integer.MAX_VALUE).getRecords();
    }

    /**
     * 分页查询审批任务
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     * @param approverId         审批人 ID
     * @param nodeStatus         节点状态
     * @param approveStatus      审批人审批状态
     * @param page               页码
     * @param pageSize           每页数量
     *
     * @return List<TaskInstance>
     */
    @Override
    public Page<TaskInstance> findTaskInstances(String tenantId, Integer workflowInstanceId, String approverId, NodeStatus nodeStatus, ApproveStatus approveStatus, Integer page, Integer pageSize) {
        TaskInstanceExecutor taskInstanceExecutor = taskInstanceExecutorBuilder.build();
        List<NodeStatus> nodeStatuses = nodeStatus == null ? null : List.of(nodeStatus);
        List<ApproveStatus> approveStatuses = approveStatus == null ? null : List.of(approveStatus);
        return taskInstanceExecutor.findByApproverId(tenantId, workflowInstanceId, approverId, nodeStatuses, approveStatuses, page, pageSize);
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
    @Override
    public TaskInstance getInCurrentlyEffectTaskInstance(String tenantId, Integer workflowInstanceId) {
        TaskInstanceExecutor taskInstanceExecutor = taskInstanceExecutorBuilder.build();
        Query query = QueryBuilderWrapper.createLambda(TaskInstance.class)
                .eq(TaskInstance::getTenantId, tenantId)
                .eq(TaskInstance::getWorkflowInstanceId, workflowInstanceId)
                .eq(TaskInstance::getStatus, NodeStatus.IN_PROGRESS.getCode())
                .eq(TaskInstance::getState, 1).build();
        return taskInstanceExecutor.get(query);
    }

    /**
     * 查找流程实例：根据发起人
     *
     * @param tenantId    租户 ID
     * @param requesterId 请求人 ID
     * @param statuses    工作流状态
     * @param page        页码
     * @param pageSize    每页数量
     *
     * @return List<WorkflowInstance>
     */
    @Override
    public Page<WorkflowInstance> findWorkflowInstancesByRequestId(String tenantId, String requesterId, List<WorkflowStatus> statuses, Integer page, Integer pageSize) {
        WorkflowInstanceExecutor workflowInstanceExecutor = workflowInstanceExecutorBuilder.build();
        return workflowInstanceExecutor.findByRequesterId(tenantId, requesterId, statuses, page, pageSize);
    }

    /**
     * 查找流程实例：根据发起人
     *
     * @param tenantId    租户 ID
     * @param requesterId 请求人 ID
     * @param status      工作流状态
     *
     * @return List<WorkflowInstance>
     */
    @Override
    public List<WorkflowInstance> findWorkflowInstancesByRequestId(String tenantId, String requesterId, WorkflowStatus status) {
        Page<WorkflowInstance> page = this.findWorkflowInstancesByRequestId(tenantId, requesterId, List.of(status), 1, Integer.MAX_VALUE);
        return page.getRecords();
    }

    /**
     * 查找流程实例：根据发起人
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
    @Override
    public Page<WorkflowInstance> findWorkflowInstancesByApproverId(String tenantId, String approverId, List<WorkflowStatus> workflowStatuses, List<ApproveStatus> approveStatuses, Integer page, Integer pageSize) {
        WorkflowInstanceExecutor workflowInstanceExecutor = workflowInstanceExecutorBuilder.build();
        return workflowInstanceExecutor.findByApproverId(tenantId, approverId, workflowStatuses, approveStatuses, page, pageSize);
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
    @Override
    public Page<TaskHistoryResponse> findTaskHistories(String tenantId, Integer page, Integer pageSize) {
        return this.findTaskHistories(tenantId, null, null, null, page, pageSize);
    }

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
    @Override
    public Page<TaskHistoryResponse> findTaskHistories(String tenantId, Integer workflowDefinitionId, Integer workflowInstanceId, String requesterId, Integer page, Integer pageSize) {
        TaskHistoryExecutor taskHistoryExecutor = taskHistoryExecutorBuilder.build();
        WorkflowInstanceExecutor workflowInstanceExecutor = workflowInstanceExecutorBuilder.build();
        WorkflowDefinitionExecutor workflowDefinitionExecutor = workflowDefinitionExecutorBuilder.build();
        Page<TaskHistory> result = taskHistoryExecutor.findTaskHistories(tenantId, workflowDefinitionId, workflowInstanceId, requesterId, page, pageSize);

        List<TaskHistoryResponse> responses = new ArrayList<>();
        result.getRecords().forEach(taskHistory -> {
            WorkflowInstance workflowInstance = workflowInstanceExecutor.getById(taskHistory.getWorkflowInstanceId());
            WorkflowDefinition workflowDefinition = workflowDefinitionExecutor.getById(workflowInstance.getWorkflowDefinitionId());
            TaskHistoryResponse taskHistoryResponse = TaskHistoryResponse.of(workflowDefinition, workflowInstance, taskHistory);
            responses.add(taskHistoryResponse);
        });

        return Page.of(page, pageSize, result.getTotal(), responses);
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
    @Override
    public WorkflowInstanceApproveRecords getWorkflowInstanceApproveRecords(String tenantId, Integer workflowInstanceId) {
        return getWorkflowInstanceApproveRecords(tenantId, workflowInstanceId, null);
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
    @Override
    public WorkflowInstanceApproveRecords getWorkflowInstanceApproveRecords(String tenantId, Integer workflowInstanceId, String currentUserId) {
        // 流程实例
        WorkflowInstanceExecutor workflowInstanceExecutor = workflowInstanceExecutorBuilder.build();
        WorkflowInstance workflowInstance = workflowInstanceExecutor.getById(workflowInstanceId);
        if (workflowInstance == null) {
            WorkflowException.throwWorkflowInstanceNotFoundException();
        }
        // 流程定义
        WorkflowDefinitionExecutor workflowDefinitionExecutor = workflowDefinitionExecutorBuilder.build();
        WorkflowDefinition workflowDefinition = workflowDefinitionExecutor.getById(workflowInstance.getWorkflowDefinitionId());

        // 任务实例
        TaskInstanceExecutor taskInstanceExecutor = taskInstanceExecutorBuilder.build();

        // 节点定义
        NodeDefinitionExecutor nodeDefinitionExecutor = nodeDefinitionExecutorBuilder.build();
        List<NodeDefinition> nodeDefinitions = nodeDefinitionExecutor.findByWorkflowDefinitionId(tenantId, workflowDefinition.getId());

        List<Pair<NodeDefinition, TaskInstance>> nodeDefAndTasks = new ArrayList<>();
        for (NodeDefinition nodeDefinition : nodeDefinitions) {
            TaskInstance taskInstance = taskInstanceExecutor.getByWorkflowInstanceIdAndNodeDefinitionId(tenantId, workflowInstanceId, nodeDefinition.getId());
            nodeDefAndTasks.add(Pair.of(nodeDefinition, taskInstance));
        }

        // 审批人
        TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder.build();
        List<TaskApprove> taskApproves = taskApproveExecutor.findByTWorkflowInstanceId(tenantId, workflowInstanceId);

        // 节点审批人
        NodeAssignmentExecutor nodeAssignmentExecutor = nodeAssignmentExecutorBuilder.build();
        List<NodeAssignment> nodeAssignments = nodeAssignmentExecutor.findByWorkflowInstanceId(tenantId, workflowInstanceId);

        // 返回 WorkflowInstanceApproveRecords
        return WorkflowInstanceApproveRecords.of(workflowDefinition, workflowInstance, nodeDefAndTasks, taskApproves, nodeAssignments, currentUserId);
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
    @Override
    public List<WorkflowInstanceApproveRecords> findWorkflowInstanceApproveRecords(String tenantId, Integer workflowDefinitionId) {
        return this.findWorkflowInstanceApproveRecords(tenantId, workflowDefinitionId, null);
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
    @Override
    public List<WorkflowInstanceApproveRecords> findWorkflowInstanceApproveRecords(String tenantId, Integer workflowDefinitionId, String currentUserId) {
        // 流程定义
        WorkflowDefinitionExecutor workflowDefinitionExecutor = workflowDefinitionExecutorBuilder.build();
        WorkflowDefinition workflowDefinition = workflowDefinitionExecutor.getById(workflowDefinitionId);
        if (workflowDefinition == null) {
            throw new WorkflowException("流程定义不存在");
        }

        List<WorkflowInstanceApproveRecords> recordsList = new ArrayList<>();
        WorkflowInstanceExecutor workflowInstanceExecutor = workflowInstanceExecutorBuilder.build();
        List<WorkflowInstance> workflowInstances = workflowInstanceExecutor.findByWorkflowDefinitionId(tenantId, workflowDefinitionId);
        for (WorkflowInstance workflowInstance : workflowInstances) {
            WorkflowInstanceApproveRecords records = this.getWorkflowInstanceApproveRecords(tenantId, workflowInstance.getId(), currentUserId);
            recordsList.add(records);
        }

        return recordsList;
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
    @Override
    public void dynamicAssignmentApprovers(String tenantId, Integer nodeDefinitionId, List<Approver> approvers) {
        JdbcTemplateHelper jdbcTemplateHelper = this.context.getJdbcTemplateHelper();
        jdbcTemplateHelper.executeInTransaction(() -> {
            // 查找节点审批人
            NodeAssignmentExecutor nodeAssignmentExecutor = nodeAssignmentExecutorBuilder.build();
            List<NodeAssignment> nodeAssignments = nodeAssignmentExecutor.findByNodeDefinitionId(tenantId, nodeDefinitionId);
            if (CollectionUtils.isEmpty(nodeAssignments)) {
                throw new WorkflowException("节点审批人不存在，请核实审批节点是否正确");
            }
            if (nodeAssignments.size() != approvers.size()) {
                throw new WorkflowException("审批人数量不一致，节点审批人数量：%s，审批人数量：%s", nodeAssignments.size(), approvers.size());
            }
            for (int i = 0; i < nodeAssignments.size(); i++) {
                NodeAssignment nodeAssignment = nodeAssignments.get(i);
                Approver approver = approvers.get(i);
                // 判断合法性
                if (approver.getId().startsWith(WorkflowConstants.DYNAMIC_ASSIGNMENT_APPROVER_VALUE_PREFIX) && approver.getId().endsWith(WorkflowConstants.DYNAMIC_ASSIGNMENT_APPROVER_VALUE_SUFFIX)) {
                    throw new WorkflowException("审批人设置错误");
                }
                // 重新设置节点审批人
                nodeAssignment.setUserId(approver.getId());
                nodeAssignmentExecutor.updateById(nodeAssignment);

                // 处理未设置具体审批人的情况：查询审批实例
                TaskInstanceExecutor taskInstanceExecutor = taskInstanceExecutorBuilder.build();
                Query query = QueryBuilderWrapper.createLambda(TaskInstance.class)
                        .eq(TaskInstance::getTenantId, tenantId)
                        .eq(TaskInstance::getNodeDefinitionId, nodeDefinitionId)
                        .eq(TaskInstance::getStatus, NodeStatus.IN_PROGRESS.getCode())
                        .eq(TaskInstance::getState, 1).build();
                TaskInstance taskInstance = taskInstanceExecutor.get(query);
                if (taskInstance != null) {
                    TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder.build();
                    query = QueryBuilderWrapper.createLambda(TaskApprove.class)
                            .eq(TaskApprove::getTenantId, tenantId)
                            .eq(TaskApprove::getTaskInstanceId, taskInstance.getId())
                            .eq(TaskApprove::getActive, ActiveStatus.ACTIVE.getCode())
                            .eq(TaskApprove::getState, 1).build();
                    TaskApprove taskApprove = taskApproveExecutor.get(query);
                    // 是否是未设置的审批人
                    if (taskApprove.isUnSettingApprover()) {
                        taskApprove.setApproverId(approver.getId());
                        taskApproveExecutor.updateById(taskApprove);
                    }
                }
            }
        });
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
    @Override
    public void updateApprover(String tenantId, String sourceApproverId, String targetApproverId) {
        JdbcTemplateHelper jdbcTemplateHelper = this.context.getJdbcTemplateHelper();
        jdbcTemplateHelper.executeInTransaction(() -> {

            TaskInstanceExecutor taskInstanceExecutor = taskInstanceExecutorBuilder.build();
            Query query = QueryBuilderWrapper.createLambda(TaskInstance.class)
                    .eq(TaskInstance::getTenantId, tenantId)
                    .eq(TaskInstance::getStatus, NodeStatus.IN_PROGRESS.getCode())
                    .eq(TaskInstance::getState, 1)
                    .build();
            Page<TaskInstance> taskInstancePage = taskInstanceExecutor.find(query);
            List<TaskInstance> taskInstances = taskInstancePage.getRecords();
            if (taskInstances.isEmpty()) {
                WorkflowException.throwWorkflowInstanceNotFoundException();
            }

            // 执行更新
            doUpdateApprover(tenantId, taskInstances, sourceApproverId, targetApproverId);
        });
    }

    /**
     * 更新审批人（未审批状态下）
     *
     * @param taskInstance     任务实例
     * @param sourceApproverId 原审批人
     * @param targetApproverId 新审批人
     *
     * @author wangweijun
     * @since 2024/9/12 11:21
     */
    @Override
    public void updateApprover(TaskInstance taskInstance, String sourceApproverId, String targetApproverId) {
        JdbcTemplateHelper jdbcTemplateHelper = this.context.getJdbcTemplateHelper();
        jdbcTemplateHelper.executeInTransaction(() -> {

            String tenantId = taskInstance.getTenantId();

            NodeStatus nodeStatus = taskInstance.getStatus();
            if (nodeStatus != NodeStatus.IN_PROGRESS) {
                throw new WorkflowException("节点状态异常，当前节点状态：%s", nodeStatus.getDesc());
            }

            // 执行更新
            doUpdateApprover(tenantId, Collections.singletonList(taskInstance), sourceApproverId, targetApproverId);
        });
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
    @Override
    public void updateApprover(WorkflowInstance workflowInstance, String sourceApproverId, String targetApproverId) {
        JdbcTemplateHelper jdbcTemplateHelper = this.context.getJdbcTemplateHelper();
        jdbcTemplateHelper.executeInTransaction(() -> {

            String tenantId = workflowInstance.getTenantId();
            Integer workflowInstanceId = workflowInstance.getId();

            TaskInstanceExecutor taskInstanceExecutor = taskInstanceExecutorBuilder.build();
            Query query = QueryBuilderWrapper.createLambda(TaskInstance.class)
                    .eq(TaskInstance::getTenantId, tenantId)
                    .eq(workflowInstanceId != null, TaskInstance::getWorkflowInstanceId, workflowInstanceId)
                    .eq(TaskInstance::getStatus, NodeStatus.IN_PROGRESS.getCode())
                    .eq(TaskInstance::getState, 1)
                    .build();
            Page<TaskInstance> taskInstancePage = taskInstanceExecutor.find(query);
            List<TaskInstance> taskInstances = taskInstancePage.getRecords();
            if (taskInstances.isEmpty()) {
                WorkflowException.throwWorkflowInstanceNotFoundException();
            }

            // 执行更新
            doUpdateApprover(tenantId, taskInstances, sourceApproverId, targetApproverId);
        });
    }

    /**
     * doUpdateApprover
     *
     * @param tenantId         租户 ID
     * @param taskInstances    流程实例列表
     * @param sourceApproverId 原审批人
     * @param targetApproverId 新审批人
     *
     * @author wangweijun
     * @since 2024/9/12 11:10
     */
    private void doUpdateApprover(String tenantId, List<TaskInstance> taskInstances, String sourceApproverId, String targetApproverId) {
        NodeAssignmentExecutor nodeAssignmentExecutor = nodeAssignmentExecutorBuilder.build();
        TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder.build();

        for (TaskInstance taskInstance : taskInstances) {
            Integer nodeDefinitionId = taskInstance.getNodeDefinitionId();
            // 查询用户任务关联表
            Query query = QueryBuilderWrapper.createLambda(NodeAssignment.class)
                    .eq(NodeAssignment::getTenantId, tenantId)
                    .eq(NodeAssignment::getNodeDefinitionId, nodeDefinitionId)
                    .eq(NodeAssignment::getUserId, sourceApproverId)
                    .eq(NodeAssignment::getState, 1)
                    .build();
            Page<NodeAssignment> nodeAssignmentPage = nodeAssignmentExecutor.find(query);
            List<NodeAssignment> nodeAssignments = nodeAssignmentPage.getRecords();
            if (CollectionUtils.isNotEmpty(nodeAssignments)) {
                for (NodeAssignment nodeAssignment : nodeAssignments) {
                    // 设置为目标值
                    nodeAssignment.setUserId(targetApproverId);
                    // 更新
                    nodeAssignmentExecutor.updateById(nodeAssignment);
                    // 记录日志
                    String message = String.format("用户任务关联表 ID: %s, 原审批人: %s，现审批人: %s", nodeAssignment.getId(), sourceApproverId, targetApproverId);
                    recordLogs(tenantId, taskInstance.getWorkflowInstanceId(), taskInstance.getId(), TaskHistoryMessage.NODE_ASSIGNMENT_CHANGED.getTemplate(), TaskHistoryMessage.custom(message));
                }
            }

            // 查询审批人
            query = QueryBuilderWrapper.createLambda(TaskApprove.class)
                    .eq(TaskApprove::getTenantId, tenantId)
                    .eq(TaskApprove::getWorkflowInstanceId, taskInstance.getWorkflowInstanceId())
                    .eq(TaskApprove::getTaskInstanceId, taskInstance.getId())
                    .eq(TaskApprove::getApproverId, sourceApproverId)
                    .eq(TaskApprove::getStatus, ApproveStatus.IN_PROGRESS.getCode())
                    .eq(TaskApprove::getActive, ActiveStatus.ACTIVE.getCode())
                    .eq(TaskApprove::getState, 1)
                    .build();
            Page<TaskApprove> taskApprovePage = taskApproveExecutor.find(query);
            List<TaskApprove> taskApproves = taskApprovePage.getRecords();
            if (CollectionUtils.isNotEmpty(taskApproves)) {
                for (TaskApprove taskApprove : taskApproves) {
                    // 设置为目标值
                    taskApprove.setApproverId(targetApproverId);
                    // 更新
                    taskApproveExecutor.updateById(taskApprove);
                    // 记录日志
                    String message = String.format("任务实例审批表 ID: %s, 原审批人: %s, 现审批人: %s", taskApprove.getId(), sourceApproverId, targetApproverId);
                    recordLogs(tenantId, taskInstance.getWorkflowInstanceId(), taskInstance.getId(), TaskHistoryMessage.TASK_APPROVE_CHANGED.getTemplate(), TaskHistoryMessage.custom(message));
                }
            }
        }
    }

    /**
     * 检查是否是顺序审批
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     * @param approveType    审批类型
     *
     * @author wangweijun
     * @since 2024/8/28 17:32
     */
    private void checkIfApproveSeqStatus(String tenantId, Integer taskInstanceId, ApproveType approveType) {
        if (ApproveType.SEQ == approveType) {
            TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder.build();
            // 获取下一个审批人的状态，并修改审批状态为：IN_PROGRESS
            List<TaskApprove> taskApproves = taskApproveExecutor.findByTaskInstanceId(tenantId, taskInstanceId, ActiveStatus.ACTIVE);
            if (CollUtil.isNotEmpty(taskApproves)) {
                // 获取下一个审批人
                TaskApprove nextTaskApprove = taskApproves.stream()
                        .filter(t -> ApproveStatus.SUSPEND == t.getStatus())
                        .min(Comparator.comparingInt(TaskApprove::getApproverSeq)).orElse(null);
                if (nextTaskApprove != null) {
                    nextTaskApprove.convertToApproveStatusInProgress();
                    taskApproveExecutor.updateById(nextTaskApprove);
                }
            }
        }
    }

    /**
     * 检查审批意见是否必填
     *
     * @param taskInstanceId 任务实例 ID
     * @param comment        审批意见
     *
     * @author wangweijun
     * @since 2024/9/12 10:34
     */
    private void checkIfRequiredComment(Integer taskInstanceId, String comment) {
        WorkflowDefinitionExecutor workflowDefinitionExecutor = workflowDefinitionExecutorBuilder.build();
        WorkflowDefinition workflowDefinition = workflowDefinitionExecutor.getByTaskInstanceId(taskInstanceId);
        if (StringUtils.isBlank(comment) && workflowDefinition.isRequiredComment()) {
            throw new WorkflowException("审批意见不能为空");
        }
    }
}
