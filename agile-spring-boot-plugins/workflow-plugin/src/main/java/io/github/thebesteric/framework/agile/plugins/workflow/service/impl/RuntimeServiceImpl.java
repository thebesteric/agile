package io.github.thebesteric.framework.agile.plugins.workflow.service.impl;

import cn.hutool.core.collection.CollUtil;
import io.github.thebesteric.framework.agile.commons.exception.InvalidDataException;
import io.github.thebesteric.framework.agile.commons.util.JsonUtils;
import io.github.thebesteric.framework.agile.core.domain.Pair;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.Page;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.builder.Query;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.builder.QueryBuilderWrapper;
import io.github.thebesteric.framework.agile.plugins.database.core.jdbc.JdbcTemplateHelper;
import io.github.thebesteric.framework.agile.plugins.workflow.config.AgileWorkflowContext;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.*;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.*;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.assignment.NodeAssignmentExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.assignment.NodeAssignmentExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.assignment.NodeRoleAssignmentExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.assignment.NodeRoleAssignmentExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.definition.NodeDefinitionExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.definition.NodeDefinitionExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.relation.NodeRelationExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.relation.NodeRelationExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.task.approve.*;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.task.dynamic.TaskDynamicAssignmentBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.task.dynamic.TaskDynamicAssignmentExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.task.dynamic.TaskDynamicAssignmentExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.task.history.TaskHistoryExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.task.history.TaskHistoryExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.task.instance.TaskInstanceExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.task.instance.TaskInstanceExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.task.reassign.TaskReassignRecordBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.task.reassign.TaskReassignRecordExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.task.reassign.TaskReassignRecordExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.definition.WorkflowDefinitionExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.definition.WorkflowDefinitionExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.instance.WorkflowInstanceExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.instance.WorkflowInstanceExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.response.*;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.*;
import io.github.thebesteric.framework.agile.plugins.workflow.exception.WorkflowException;
import io.github.thebesteric.framework.agile.plugins.workflow.listener.*;
import io.github.thebesteric.framework.agile.plugins.workflow.service.AbstractRuntimeService;
import io.github.thebesteric.framework.agile.plugins.workflow.service.DeploymentService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private final NodeRoleAssignmentExecutorBuilder nodeRoleAssignmentExecutorBuilder;
    private final NodeRelationExecutorBuilder nodeRelationExecutorBuilder;
    private final TaskInstanceExecutorBuilder taskInstanceExecutorBuilder;
    private final TaskApproveExecutorBuilder taskApproveExecutorBuilder;
    private final TaskHistoryExecutorBuilder taskHistoryExecutorBuilder;
    private final TaskRoleApproveRecordExecutorBuilder taskRoleApproveRecordExecutorBuilder;
    private final TaskDynamicAssignmentExecutorBuilder taskDynamicAssignmentExecutorBuilder;
    private final TaskReassignRecordExecutorBuilder taskReassignRecordExecutorBuilder;
    private final DeploymentService deploymentService;

    public RuntimeServiceImpl(AgileWorkflowContext context) {
        super(context);
        JdbcTemplate jdbcTemplate = context.getJdbcTemplateHelper().getJdbcTemplate();
        workflowInstanceExecutorBuilder = WorkflowInstanceExecutorBuilder.builder(jdbcTemplate);
        workflowDefinitionExecutorBuilder = WorkflowDefinitionExecutorBuilder.builder(jdbcTemplate);
        nodeDefinitionExecutorBuilder = NodeDefinitionExecutorBuilder.builder(jdbcTemplate);
        nodeAssignmentExecutorBuilder = NodeAssignmentExecutorBuilder.builder(jdbcTemplate);
        nodeRoleAssignmentExecutorBuilder = NodeRoleAssignmentExecutorBuilder.builder(jdbcTemplate);
        nodeRelationExecutorBuilder = NodeRelationExecutorBuilder.builder(jdbcTemplate);
        taskInstanceExecutorBuilder = TaskInstanceExecutorBuilder.builder(jdbcTemplate);
        taskApproveExecutorBuilder = TaskApproveExecutorBuilder.builder(jdbcTemplate);
        taskHistoryExecutorBuilder = TaskHistoryExecutorBuilder.builder(jdbcTemplate);
        taskRoleApproveRecordExecutorBuilder = TaskRoleApproveRecordExecutorBuilder.builder(jdbcTemplate);
        taskDynamicAssignmentExecutorBuilder = TaskDynamicAssignmentExecutorBuilder.builder(jdbcTemplate);
        taskReassignRecordExecutorBuilder = TaskReassignRecordExecutorBuilder.builder(jdbcTemplate);
        deploymentService = new DeploymentServiceImpl(context);
    }

    /**
     * 启动流程
     *
     * @param tenantId              租户 ID
     * @param workflowDefinitionKey 流程定义 Key
     * @param requester             申请人
     * @param businessInfo          业务信息
     * @param desc                  描述
     * @param requestConditions     申请条件
     * @param dynamicApprovers      动态审批人
     */
    @Override
    public WorkflowInstance start(String tenantId, String workflowDefinitionKey, Requester requester, BusinessInfo businessInfo, String desc, RequestConditions requestConditions, List<Approver> dynamicApprovers) {
        JdbcTemplateHelper jdbcTemplateHelper = this.context.getJdbcTemplateHelper();
        return jdbcTemplateHelper.executeInTransaction(() -> {
            WorkflowDefinition workflowDefinition = getWorkflowDefinition(tenantId, workflowDefinitionKey);
            if (workflowDefinition == null) {
                throw new WorkflowException("未查询到对应的流程定义，请确认该流程定义是否存在: %s", workflowDefinitionKey);
            }
            if (PublishStatus.UNPUBLISHED == workflowDefinition.getPublish()) {
                throw new WorkflowException("流程尚未发布，请先发布流程后重试");
            }
            if (workflowDefinition.isLock()) {
                throw new WorkflowException("流程已锁定，请稍后重试");
            }

            // 获取流程实例
            WorkflowInstanceExecutor instanceExecutor = workflowInstanceExecutorBuilder.newInstance()
                    .tenantId(tenantId).workflowDefinitionId(workflowDefinition.getId())
                    .requester(requester).businessInfo(businessInfo)
                    .requestConditions(requestConditions).status(WorkflowStatus.WAITING).desc(desc).build();
            WorkflowInstance workflowInstance = instanceExecutor.save();
            Integer workflowInstanceId = workflowInstance.getId();

            // 获取开始节点定义
            NodeDefinitionExecutor nodeDefinitionExecutor = nodeDefinitionExecutorBuilder.build();
            NodeDefinition startNodeDefinition = nodeDefinitionExecutor.getStartNode(tenantId, workflowDefinition.getId());

            // 记录流程日志（提交审批）
            recordLogs(tenantId, workflowInstanceId, null, null, TaskHistoryMessage.INSTANCE_SUBMIT_FORM);

            // 创建审批开始实例
            TaskInstanceExecutor taskInstanceExecutor = taskInstanceExecutorBuilder.tenantId(tenantId)
                    .roleApprove(startNodeDefinition.isRoleApprove())
                    .status(NodeStatus.COMPLETED)
                    .workflowInstanceId(workflowInstanceId)
                    .nodeDefinitionId(startNodeDefinition.getId())
                    .build();
            TaskInstance startTaskInstance = taskInstanceExecutor.save();

            // 记录流程日志（开始审批）
            recordLogs(tenantId, workflowInstanceId, startTaskInstance.getId(), startNodeDefinition.getName(), TaskHistoryMessage.INSTANCE_STARTED);

            // 创建下一个审批实例（多个审批实例，通常是条件节点）
            List<NodeDefinition> toTaskNodes = new ArrayList<>(nodeDefinitionExecutor.findToTaskNodesByFromNodeId(tenantId, startNodeDefinition.getId()));
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
                    // 按优先级排序
                    toTaskNodes.sort(Comparator.comparingInt(nd -> {
                        if (nd.getConditions() == null) {
                            return 0;
                        }
                        return nd.getConditions().getPriority();
                    }));
                    for (NodeDefinition toTaskNode : toTaskNodes) {
                        // 没有审批条件，则添加所有的节点
                        Conditions conditions = toTaskNode.getConditions();
                        if (conditions == null) {
                            nextNodeDefinitions.add(toTaskNode);
                        }
                        // 存在并满足审批条件，则只会添加一个符合条件的节点
                        else if (conditions.matchRequestCondition(requestConditions)) {
                            nextNodeDefinitions.add(toTaskNode);
                            break;
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
                            .roleApprove(nextNodeDefinition.isRoleApprove())
                            .approvedCount(0)
                            // 设置总需要审批的次数
                            .totalCount(this.calcTotalCount(workflowDefinition, nextNodeDefinition, nextNodeAssignments, dynamicApprovers))
                            .build();
                    TaskInstance nextTaskInstance = taskInstanceExecutor.save();
                    Integer nextTaskInstanceId = nextTaskInstance.getId();

                    // 实例任务审批节点
                    List<TaskApprove> taskApproves = new ArrayList<>();

                    // 存在审批人：创建任务实例审批人
                    if (CollUtil.isNotEmpty(nextNodeAssignments)) {
                        ApproverIdType approverIdType = nextNodeDefinition.isRoleApprove() ? ApproverIdType.ROLE : ApproverIdType.USER;
                        ApproveType approveType = nextNodeDefinition.getApproveType();
                        // 动态审批节点
                        if (nextNodeDefinition.isDynamic()) {
                            if (CollectionUtils.isEmpty(dynamicApprovers)) {
                                throw new WorkflowException("动态审批节点，请先设置审批人");
                            }
                            TaskDynamicAssignmentBuilder taskDynamicAssignmentBuilder = TaskDynamicAssignmentBuilder.builder(tenantId, nextNodeDefinitionId, nextTaskInstanceId);
                            int i = 0;
                            for (Approver dynamicApprover : dynamicApprovers) {
                                // 保存 TaskDynamicAssignment
                                TaskDynamicAssignmentExecutor taskDynamicAssignmentExecutor = taskDynamicAssignmentExecutorBuilder.build();
                                TaskDynamicAssignment taskDynamicAssignment = taskDynamicAssignmentBuilder
                                        .approverInfo(approveType, dynamicApprover.getId(), dynamicApprover.getName(), dynamicApprover.getDesc())
                                        .build();
                                taskDynamicAssignmentExecutor.save(taskDynamicAssignment);
                                // 保存 TaskApprove
                                TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder.newInstance()
                                        .tenantId(tenantId)
                                        .workflowInstanceId(workflowInstanceId)
                                        .taskInstanceId(nextTaskInstanceId)
                                        .approverIdType(ApproverIdType.USER)
                                        .approverId(taskDynamicAssignment.getApproverId())
                                        .approveSeq(taskDynamicAssignment.getApproverSeq())
                                        .status(ApproveStatus.IN_PROGRESS)
                                        .active(ActiveStatus.ACTIVE).build();
                                // 顺序审批，后续审批人需要等待前一个审批人审批完成，后续的审批状态设置为：挂起
                                if (ApproveType.SEQ == approveType && i > 0) {
                                    taskApproveExecutorBuilder.status(ApproveStatus.SUSPEND);
                                }
                                TaskApprove taskApprove = taskApproveExecutor.save();
                                taskApproves.add(taskApprove);
                                i++;
                            }
                            taskDynamicAssignmentBuilder.resetSeq();
                        }
                        // 非动态审批节点
                        else {
                            int i = 0;
                            for (NodeAssignment nextNodeAssignment : nextNodeAssignments) {
                                TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder
                                        .newInstance()
                                        .tenantId(tenantId)
                                        .workflowInstanceId(workflowInstanceId)
                                        .taskInstanceId(nextTaskInstanceId)
                                        .approverId(nextNodeAssignment.getApproverId())
                                        .approverIdType(approverIdType)
                                        .approveSeq(nextNodeAssignment.getApproverSeq())
                                        .status(ApproveStatus.IN_PROGRESS)
                                        .active(ActiveStatus.ACTIVE).build();
                                // 顺序审批，后续审批人需要等待前一个审批人审批完成，后续的审批状态设置为：挂起
                                if (ApproveType.SEQ == approveType && i > 0) {
                                    taskApproveExecutorBuilder.status(ApproveStatus.SUSPEND);
                                }
                                TaskApprove taskApprove = taskApproveExecutor.save();
                                taskApproves.add(taskApprove);
                                i++;
                            }
                        }
                    }
                    // 没有审批人: 允许自动同意，则自动同意
                    else if (workflowDefinition.isAllowEmptyAutoApprove()) {
                        this.approve(tenantId, nextTaskInstanceId, null, WorkflowConstants.AUTO_APPROVER_ID, WorkflowConstants.AUTO_APPROVER_COMMENT);
                    }
                    // 其他未知情况
                    else {
                        throw new WorkflowException("未知异常，请联系系统管理员");
                    }

                    // 审批人列表为空
                    if (taskApproves.isEmpty()) {
                        // 获取当流程实例的默认审批人
                        Approver whenEmptyApprover = workflowDefinition.getWhenEmptyApprover();
                        // 实例的默认审批人为空，且不允许空节点自动审核
                        if (whenEmptyApprover == null && !workflowDefinition.isAllowEmptyAutoApprove()) {
                            throw new WorkflowException("任务实例审批表不能为空");
                        }
                    }

                    // 判断是否是角色审批
                    if (nextNodeDefinition.isRoleApprove()) {
                        // 添加角色审批记录
                        processTaskRoleApproveRecords(tenantId, nextNodeDefinition, nextTaskInstance, taskApproves);
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
     * @param requester             申请人
     * @param desc                  描述
     */
    @Override
    public WorkflowInstance start(String tenantId, String workflowDefinitionKey, Requester requester, BusinessInfo businessInfo, String desc) {
        return this.start(tenantId, workflowDefinitionKey, requester, businessInfo, desc, null, null);
    }

    /**
     * 启动流程
     *
     * @param tenantId              租户 ID
     * @param workflowDefinitionKey 流程定义 Key
     * @param requester             申请人
     * @param desc                  描述
     * @param requestConditions     申请条件
     */
    @Override
    public WorkflowInstance start(String tenantId, String workflowDefinitionKey, Requester requester, String desc, RequestConditions requestConditions) {
        return this.start(tenantId, workflowDefinitionKey, requester, null, desc, requestConditions, null);
    }

    /**
     * 启动流程
     *
     * @param tenantId              租户 ID
     * @param workflowDefinitionKey 流程定义 Key
     * @param requester             申请人
     * @param desc                  描述
     */
    @Override
    public WorkflowInstance start(String tenantId, String workflowDefinitionKey, Requester requester, String desc) {
        return this.start(tenantId, workflowDefinitionKey, requester, null, desc);
    }

    /**
     * 获取流程默认审批人
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     *
     * @return String
     *
     * @author wangweijun
     * @since 2024/10/25 14:23
     */
    private Approver getDefaultApprover(String tenantId, Integer workflowDefinitionId) {
        WorkflowDefinitionExecutor workflowDefinitionExecutor = workflowDefinitionExecutorBuilder.build();
        Approver defaultApprover = workflowDefinitionExecutor.getDefaultApprover(tenantId, workflowDefinitionId);
        if (defaultApprover == null) {
            defaultApprover = Approver.of(WorkflowConstants.AUTO_APPROVER_ID);
        }
        return defaultApprover;
    }

    /**
     * 处理角色审批记录
     *
     * @param tenantId           租户 ID
     * @param nextNodeDefinition 下一个节点定义
     * @param nextTaskInstance   下一个任务实例
     * @param taskApproves       实例任务审批集合
     *
     * @author wangweijun
     * @since 2024/9/24 11:13
     */
    private void processTaskRoleApproveRecords(String tenantId, NodeDefinition nextNodeDefinition, TaskInstance nextTaskInstance, List<TaskApprove> taskApproves) {
        // 忽略非角色审批的情况
        if (!nextNodeDefinition.isRoleApprove()) {
            return;
        }

        NodeRoleAssignmentExecutor nodeRoleAssignmentExecutor = nodeRoleAssignmentExecutorBuilder.build();
        TaskRoleApproveRecordExecutor taskRoleApproveRecordExecutor = taskRoleApproveRecordExecutorBuilder.build();

        // 角色审批类型
        RoleApproveType roleApproveType = nextNodeDefinition.getRoleApproveType();
        // 角色用户审批类型
        RoleUserApproveType roleUserApproveType = nextNodeDefinition.getRoleUserApproveType();

        // 查询所有角色审批人
        List<NodeRoleAssignment> nodeRoleAssignments = nodeRoleAssignmentExecutor.findByNodeDefinitionId(tenantId, nextNodeDefinition.getId(), NodeRoleAssignmentType.NORMAL);
        nodeRoleAssignments = new ArrayList<>(nodeRoleAssignments);

        // 角色和审批任务对应关系：key：roleId，value：taskApprove
        Map<String, TaskApprove> roleIdAndTaskApprovesMap = new HashMap<>();
        for (NodeRoleAssignment nodeRoleAssignment : nodeRoleAssignments) {
            String roleId = nodeRoleAssignment.getRoleId();
            taskApproves.stream().filter(i -> i.getApproverId().equals(roleId)).findFirst()
                    .ifPresent(taskApprove -> roleIdAndTaskApprovesMap.computeIfAbsent(roleId, k -> taskApprove));
        }

        // 按角色进行分组的角色审批人
        Map<String, List<NodeRoleAssignment>> nodeRoleAssignmentsByRoles = new LinkedHashMap<>();
        nodeRoleAssignments.forEach(nodeRoleAssignment -> {
            String roleId = nodeRoleAssignment.getRoleId();
            List<NodeRoleAssignment> list = nodeRoleAssignmentsByRoles.getOrDefault(roleId, new ArrayList<>());
            list.add(nodeRoleAssignment);
            nodeRoleAssignmentsByRoles.put(roleId, list);
        });

        // 角色审批类型为：SEQ
        if (RoleApproveType.SEQ == roleApproveType) {
            // 角色用户审批类型为：ALL
            if (RoleUserApproveType.ALL == roleUserApproveType || RoleUserApproveType.ANY == roleUserApproveType) {
                int roleIdIndex = 0;
                for (Map.Entry<String, List<NodeRoleAssignment>> entry : nodeRoleAssignmentsByRoles.entrySet()) {
                    String roleId = entry.getKey();
                    List<NodeRoleAssignment> nodeRoleAssignmentsByRole = entry.getValue();
                    Integer approveId = roleIdAndTaskApprovesMap.get(roleId).getId();
                    for (NodeRoleAssignment nodeRoleAssignment : nodeRoleAssignmentsByRole) {
                        TaskRoleApproveRecord taskRoleApproveRecord = new TaskRoleApproveRecord()
                                .setTenantId(tenantId)
                                .setWorkflowInstanceId(nextTaskInstance.getWorkflowInstanceId())
                                .setTaskInstanceId(nextTaskInstance.getId())
                                .setTaskApproveId(approveId)
                                .setNodeRoleAssignmentId(nodeRoleAssignment.getId())
                                .setStatus(roleIdIndex == 0 ? RoleApproveStatus.IN_PROGRESS : RoleApproveStatus.SUSPEND);
                        taskRoleApproveRecordExecutor.save(taskRoleApproveRecord);
                    }
                    roleIdIndex++;
                }
            }
            // 角色用户审批类型为：SEQ 时，在后续会处理
        }
        // 角色审批类型为：ANY 或 ALL
        else if (RoleApproveType.ANY == roleApproveType || RoleApproveType.ALL == roleApproveType) {
            // 角色用户审批类型为：ALL 或 ANY
            if (RoleUserApproveType.ANY == roleUserApproveType || RoleUserApproveType.ALL == roleUserApproveType) {
                // 按 ID 排序
                List<NodeRoleAssignment> sortedNodeRoleAssignment = nodeRoleAssignments.stream().sorted(Comparator.comparingInt(NodeRoleAssignment::getId)).toList();
                for (NodeRoleAssignment nodeRoleAssignment : sortedNodeRoleAssignment) {
                    Integer approveId = roleIdAndTaskApprovesMap.get(nodeRoleAssignment.getRoleId()).getId();
                    TaskRoleApproveRecord taskRoleApproveRecord = new TaskRoleApproveRecord()
                            .setTenantId(tenantId)
                            .setWorkflowInstanceId(nextTaskInstance.getWorkflowInstanceId())
                            .setTaskInstanceId(nextTaskInstance.getId())
                            .setTaskApproveId(approveId)
                            .setNodeRoleAssignmentId(nodeRoleAssignment.getId())
                            .setStatus(RoleApproveStatus.IN_PROGRESS);
                    taskRoleApproveRecordExecutor.save(taskRoleApproveRecord);
                }
            }
            // 角色用户审批类型为：SEQ 时，在后续会处理
        }

        // 角色用户审批类型为：SEQ，则需要提前创建角色审批记录
        if (RoleUserApproveType.SEQ == roleUserApproveType) {
            // 将用户保存至角色任务实例审批记录表中
            int roleIdIndex = 0;
            for (Map.Entry<String, List<NodeRoleAssignment>> entry : nodeRoleAssignmentsByRoles.entrySet()) {
                String roleId = entry.getKey();
                TaskApprove taskApprove = taskApproves.stream().filter(i -> roleId.equals(i.getApproverId())).findAny().orElse(null);
                if (null == taskApprove) {
                    throw new WorkflowException("角色审批表不能为空");
                }
                // 获取该角色下的所有角色用户
                List<NodeRoleAssignment> nodeRoleAssignmentsByRole = entry.getValue();
                // 按 roleSeq 和 userSeq 升序排序
                nodeRoleAssignmentsByRole.sort(Comparator.comparingInt(NodeRoleAssignment::getRoleSeq).thenComparingInt(NodeRoleAssignment::getUserSeq));

                for (int i = 0; i < nodeRoleAssignmentsByRole.size(); i++) {
                    NodeRoleAssignment nodeRoleAssignment = nodeRoleAssignmentsByRole.get(i);
                    TaskRoleApproveRecord taskRoleApproveRecord = new TaskRoleApproveRecord()
                            .setTenantId(tenantId)
                            .setWorkflowInstanceId(nextTaskInstance.getWorkflowInstanceId())
                            .setTaskInstanceId(nextTaskInstance.getId())
                            .setTaskApproveId(taskApprove.getId())
                            .setNodeRoleAssignmentId(nodeRoleAssignment.getId());
                    // 如果角色审批类型是 ANY，每个角色的第一个角色用户为 IN_PROGRESS，其余角色用户均为 SUSPEND
                    if (RoleApproveType.ANY == roleApproveType) {
                        taskRoleApproveRecord.setStatus(i == 0 ? RoleApproveStatus.IN_PROGRESS : RoleApproveStatus.SUSPEND);
                    }
                    // 如果角色审批类型是 SEQ，则只有第一个角色用户为 IN_PROGRESS，其他为 SUSPEND
                    else if (RoleApproveType.SEQ == roleApproveType || RoleApproveType.ALL == roleApproveType) {
                        taskRoleApproveRecord.setStatus(roleIdIndex == 0 && i == 0 ? RoleApproveStatus.IN_PROGRESS : RoleApproveStatus.SUSPEND);
                    }
                    taskRoleApproveRecordExecutor.save(taskRoleApproveRecord);
                }
                roleIdIndex++;
            }
        }
    }

    /**
     * 计算总需要审批的次数
     *
     * @param workflowDefinition 流程定义
     * @param nodeDefinition     节点定义
     * @param nodeAssignments    候选审批人
     * @param dynamicApprovers   动态候选审批人
     *
     * @return Integer
     *
     * @author wangweijun
     * @since 2024/7/15 09:44
     */
    private Integer calcTotalCount(WorkflowDefinition workflowDefinition, NodeDefinition nodeDefinition, List<NodeAssignment> nodeAssignments, List<Approver> dynamicApprovers) {
        int nodeAssignmentSize = nodeAssignments.size();
        // 判断是否是角色审批
        if (nodeDefinition.isRoleApprove()) {
            RoleUserApproveType roleUserApproveType = nodeDefinition.getRoleUserApproveType();
            RoleApproveType roleApproveType = nodeDefinition.getRoleApproveType();

            String tenantId = nodeDefinition.getTenantId();
            Integer nodeDefinitionId = nodeDefinition.getId();
            NodeRoleAssignmentExecutor nodeRoleAssignmentExecutor = nodeRoleAssignmentExecutorBuilder.build();
            // 查询到所有角色下的用户
            List<NodeRoleAssignment> nodeRoleAssignments = nodeRoleAssignmentExecutor.findByNodeDefinitionId(tenantId, nodeDefinitionId, NodeRoleAssignmentType.NORMAL);
            // 根据角色 ID 分组
            Map<String, List<NodeRoleAssignment>> nodeRoleAssignmentMap = nodeRoleAssignments.stream().collect(Collectors.groupingBy(NodeRoleAssignment::getRoleId));
            // 角色属于：或签
            if (RoleApproveType.ANY == roleApproveType) {
                // 角色用户属于：会签 或 顺签，则如：A 角色包含 2 个用户，B 角色包含 3 个用户，则总需要审批的次数为 2 * 3 = 6，真实需要审批的用户判断为：A 角色中：6 % 2 == 0 或 B 角色中：6 % 3 == 0，即表示审批完成
                int multiplication = Math.negateExact(nodeRoleAssignmentMap.values().stream().mapToInt(List::size).reduce(1, (a, b) -> a * b));
                // 角色用户属于：或签，则总需要审批的次数为 1，否则获取每个角色里的用户的乘积，并取取相反数
                return RoleUserApproveType.ANY == roleUserApproveType ? 1 : multiplication;
            }
            // 角色属于：会签 或 顺签
            else {
                // 角色用户属于：会签 或 顺签，求每个角色中用户的数量之和
                int sum = nodeRoleAssignmentMap.values().stream().mapToInt(List::size).sum();
                // 角色用户属于：或签，则总需要审批的次数为角色的数量，否则获取每个角色中用户的数量之和
                return RoleUserApproveType.ANY == roleUserApproveType ? nodeAssignmentSize : sum;
            }
        }
        // 用户审批的情况（非角色审批）：除或签、自动审批外，其余均需要审批全部
        ApproveType approveType = nodeDefinition.getApproveType();
        // 动态审批节点
        if (nodeDefinition.isDynamic()) {
            if (!workflowDefinition.isAllowEmptyAutoApprove() && CollectionUtils.isEmpty(dynamicApprovers)) {
                throw new WorkflowException("动态审批节点，动态审批人列表不能为空");
            }
            int dynamicApproversSize = dynamicApprovers == null ? 0 : dynamicApprovers.size();
            return ApproveType.ANY == approveType || (workflowDefinition.isAllowEmptyAutoApprove() && dynamicApproversSize == 0) ? 1 : dynamicApproversSize;
        }
        // 非动态审批节点
        return ApproveType.ANY == approveType || (workflowDefinition.isAllowEmptyAutoApprove() && nodeAssignmentSize == 0) ? 1 : nodeAssignmentSize;
    }

    /**
     * 计算总需要审批的次数
     *
     * @param workflowDefinition 流程定义
     * @param nodeDefinition     节点定义
     * @param nodeAssignments    候选审批人
     *
     * @return Integer
     *
     * @author wangweijun
     * @since 2024/7/15 09:44
     */
    private Integer calcTotalCount(WorkflowDefinition workflowDefinition, NodeDefinition nodeDefinition, List<NodeAssignment> nodeAssignments) {
        int nodeAssignmentSize = nodeAssignments.size();
        // 判断是否是角色审批
        if (nodeDefinition.isRoleApprove()) {
            RoleUserApproveType roleUserApproveType = nodeDefinition.getRoleUserApproveType();
            RoleApproveType roleApproveType = nodeDefinition.getRoleApproveType();

            String tenantId = nodeDefinition.getTenantId();
            Integer nodeDefinitionId = nodeDefinition.getId();
            NodeRoleAssignmentExecutor nodeRoleAssignmentExecutor = nodeRoleAssignmentExecutorBuilder.build();
            // 查询到所有角色下的用户
            List<NodeRoleAssignment> nodeRoleAssignments = nodeRoleAssignmentExecutor.findByNodeDefinitionId(tenantId, nodeDefinitionId, NodeRoleAssignmentType.NORMAL);
            // 根据角色 ID 分组
            Map<String, List<NodeRoleAssignment>> nodeRoleAssignmentMap = nodeRoleAssignments.stream().collect(Collectors.groupingBy(NodeRoleAssignment::getRoleId));
            // 角色属于：或签
            if (RoleApproveType.ANY == roleApproveType) {
                // 角色用户属于：会签 或 顺签，则如：A 角色包含 2 个用户，B 角色包含 3 个用户，则总需要审批的次数为 2 * 3 = 6，真实需要审批的用户判断为：A 角色中：6 % 2 == 0 或 B 角色中：6 % 3 == 0，即表示审批完成
                int multiplication = Math.negateExact(nodeRoleAssignmentMap.values().stream().mapToInt(List::size).reduce(1, (a, b) -> a * b));
                // 角色用户属于：或签，则总需要审批的次数为 1，否则获取每个角色里的用户的乘积，并取取相反数
                return RoleUserApproveType.ANY == roleUserApproveType ? 1 : multiplication;
            }
            // 角色属于：会签 或 顺签
            else {
                // 角色用户属于：会签 或 顺签，求每个角色中用户的数量之和
                int sum = nodeRoleAssignmentMap.values().stream().mapToInt(List::size).sum();
                // 角色用户属于：或签，则总需要审批的次数为角色的数量，否则获取每个角色中用户的数量之和
                return RoleUserApproveType.ANY == roleUserApproveType ? nodeAssignmentSize : sum;
            }
        }
        // 动态审批节点
        if (nodeDefinition.isDynamic()) {
            return nodeDefinition.getDynamicAssignmentNum();
        }
        // 用户审批的情况（非角色审批）：除或签、自动审批外，其余均需要审批全部
        ApproveType approveType = nodeDefinition.getApproveType();
        return ApproveType.ANY == approveType || (workflowDefinition.isAllowEmptyAutoApprove() && nodeAssignmentSize == 0) ? 1 : nodeAssignmentSize;
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
     * @param roleId             当前角色 ID
     * @param userId             当前用户 ID
     *
     * @return 返回下一个审批节点
     */
    @Override
    public List<TaskInstance> next(String tenantId, Integer prevTaskInstanceId, String roleId, String userId) {
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
                    // 检查当前节点是否符合审批条件，符合则保存任务实例
                    checkAndSaveNextTaskInstance(tenantId, workflowInstance, workflowDefinition, nextNodeDefinition, roleId, userId, nextTaskInstances);
                }
            }

            // 兜底方案：nextTaskInstances 为 empty 的情况，说明可能没有任何一个条件节点符合该审批情况，则需要判断执行策略
            if (CollectionUtils.isEmpty(nextTaskInstances)) {
                switch (workflowDefinition.getConditionNotMatchedAnyStrategy()) {
                    case PROCESS_APPROVED:
                        // 更新流程实例为：已完成
                        workflowInstance.setStatus(WorkflowStatus.COMPLETED);
                        workflowInstanceExecutor.updateById(workflowInstance);
                        break;
                    case PROCESS_REJECTED:
                        // 更新流程实例为：已拒绝
                        workflowInstance.setStatus(WorkflowStatus.REJECTED);
                        workflowInstanceExecutor.updateById(workflowInstance);
                        break;
                    case PROCESS_CONTINUE_TO_NEXT:
                        // 因为都没有符合条件的节点，故获取节点关系中最后一个节点定义
                        Integer lastNodeDefinitionId = nodeRelations.get(nodeRelations.size() - 1).getFromNodeId();
                        NodeDefinition lastNodeDefinition = nodeDefinitionExecutor.getById(lastNodeDefinitionId);
                        do {
                            // 如果是结束节点，则标记为已完成
                            if (NodeType.END == lastNodeDefinition.getNodeType()) {
                                // 更新流程实例为：已完成
                                workflowInstance.setStatus(WorkflowStatus.COMPLETED);
                                workflowInstanceExecutor.updateById(workflowInstance);
                                break;
                            }
                            // 不是结束节点，则需要找到下一个节点，
                            else {
                                // 查找下一个最小的节点
                                List<NodeDefinition> nodeDefinitions = nodeDefinitionExecutor.findByWorkflowDefinitionId(tenantId, workflowDefinition.getId());
                                final NodeDefinition finalLastNodeDefinition = lastNodeDefinition;
                                Double minSequence = nodeDefinitions.stream()
                                        .filter(n -> NodeType.TASK == n.getNodeType())
                                        .filter(n -> n.getSequence() > finalLastNodeDefinition.getSequence())
                                        .min(Comparator.comparing(NodeDefinition::getSequence))
                                        .map(NodeDefinition::getSequence)
                                        .stream().findFirst().orElse(null);
                                // 不存在下级节点定义
                                if (minSequence == null) {
                                    // 更新流程实例为：已完成
                                    workflowInstance.setStatus(WorkflowStatus.COMPLETED);
                                    workflowInstanceExecutor.updateById(workflowInstance);
                                    break;
                                }
                                // 存在下级节点定义
                                else {
                                    List<NodeDefinition> nextNodeDefinitions = nodeDefinitionExecutor.findBySequence(tenantId, workflowDefinition.getId(), minSequence);
                                    for (NodeDefinition nextNodeDefinition : nextNodeDefinitions) {
                                        // 检查当前节点是否符合审批条件，符合则保存任务实例
                                        checkAndSaveNextTaskInstance(tenantId, workflowInstance, workflowDefinition, nextNodeDefinition, roleId, userId, nextTaskInstances);
                                    }
                                    lastNodeDefinition = nextNodeDefinitions.get(nextNodeDefinitions.size() - 1);
                                }
                            }
                        } while (CollectionUtils.isEmpty(nextTaskInstances));
                        break;
                    default:
                        // 抛出异常
                        RequestConditions requestConditions = workflowInstance.getRequestConditions();
                        throw new WorkflowException("没有符合条件的审批节点: " + JsonUtils.toJson(requestConditions.getRequestConditions()));
                }
            }

            // 这里可能返回 null，也可能返回 empty
            // nextTaskInstances = null: 表示正常达到结束节点
            // nextTaskInstances = empty: 表示条件节点中，没有符合的审批节点，则结束流程
            return nextTaskInstances;
        });
    }

    /**
     * 检查并保存任务实例
     *
     * @param tenantId           租户 ID
     * @param workflowInstance   流程实例
     * @param workflowDefinition 流程定义
     * @param nextNodeDefinition 下一个节点定义
     * @param roleId             角色 ID
     * @param userId             用户 ID
     * @param nextTaskInstances  下一个任务实例列表
     *
     * @author wangweijun
     * @since 2024/10/25 09:27
     */
    private void checkAndSaveNextTaskInstance(String tenantId, WorkflowInstance workflowInstance, WorkflowDefinition workflowDefinition, NodeDefinition nextNodeDefinition, String roleId, String userId, List<TaskInstance> nextTaskInstances) {
        // 审批条件判断
        Conditions conditions = nextNodeDefinition.getConditions();
        RequestConditions requestConditions = workflowInstance.getRequestConditions();
        if ((conditions != null && requestConditions != null && !conditions.matchRequestCondition(requestConditions))
            || (conditions != null && requestConditions == null)) {
            return;
        }
        Integer nextNodeDefinitionId = nextNodeDefinition.getId();

        // 同一个类型的节点，只允许存在一个
        if (!nextTaskInstances.isEmpty()) {
            TaskInstance taskInstance = nextTaskInstances.stream().filter(i -> i.getNodeDefinitionId().equals(nextNodeDefinitionId)).findAny().orElse(null);
            if (taskInstance != null) {
                return;
            }
        }

        // 下一个节点的审批人
        List<NodeAssignment> nextNodeAssignments = new ArrayList<>();

        // 准备构建下一个节点实例
        taskInstanceExecutorBuilder.workflowInstanceId(workflowInstance.getId())
                .roleApprove(nextNodeDefinition.isRoleApprove())
                .nodeDefinitionId(nextNodeDefinitionId);

        // 已经是结束节点
        if (NodeType.END == nextNodeDefinition.getNodeType()) {
            // 更新下个节点状态为：已完成
            taskInstanceExecutorBuilder.status(NodeStatus.COMPLETED);

            // 更新流程实例为：已完成
            workflowInstance.setStatus(WorkflowStatus.COMPLETED);
            WorkflowInstanceExecutor workflowInstanceExecutor = workflowInstanceExecutorBuilder.build();
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
                    .totalCount(this.calcTotalCount(workflowDefinition, nextNodeDefinition, nextNodeAssignments))
                    .approvedCount(0);
        }

        // 保存任务节点
        TaskInstanceExecutor taskInstanceExecutor = taskInstanceExecutorBuilder.build();
        TaskInstance nextTaskInstance = taskInstanceExecutor.save();

        // 如果允许自动审批，并且没有审批人，则自动审批
        if (workflowDefinition.isAllowEmptyAutoApprove() && nextNodeAssignments.isEmpty()) {
            this.approve(tenantId, nextTaskInstance.getId(), null, WorkflowConstants.AUTO_APPROVER_ID, WorkflowConstants.AUTO_APPROVER_COMMENT);
        }

        // 获取连续审批模式
        ContinuousApproveMode continuousApproveMode = workflowDefinition.getContinuousApproveMode();

        // 创建任务实例审批人
        List<TaskApprove> taskApproves = new ArrayList<>();
        if (CollUtil.isNotEmpty(nextNodeAssignments) && NodeType.END != nextNodeDefinition.getNodeType()) {
            int i = 0;
            for (NodeAssignment nextNodeAssignment : nextNodeAssignments) {
                taskApproveExecutorBuilder.newInstance()
                        .tenantId(tenantId)
                        .workflowInstanceId(nextTaskInstance.getWorkflowInstanceId())
                        .taskInstanceId(nextTaskInstance.getId())
                        .approverId(nextNodeAssignment.getApproverId())
                        .approverIdType(nextNodeDefinition.isRoleApprove() ? ApproverIdType.ROLE : ApproverIdType.USER)
                        .approveSeq(nextNodeAssignment.getApproverSeq())
                        .active(ActiveStatus.ACTIVE)
                        .status(ApproveStatus.IN_PROGRESS)
                        .build();

                // 顺序审批，后续审批人需要等待前一个审批人审批完成，后续的审批状态设置为：挂起
                if (ApproveType.SEQ == nextNodeDefinition.getApproveType() && i > 0) {
                    taskApproveExecutorBuilder.status(ApproveStatus.SUSPEND);
                }
                TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder.build();
                TaskApprove taskApprove = taskApproveExecutor.save();
                taskApproves.add(taskApprove);
                i++;

                // 判断连续审批模式
                this.continuousApproveModeProcess(tenantId, nextTaskInstance, roleId, userId, nextNodeAssignment.getApproverId(), continuousApproveMode);
            }
        }

        // 判断是否是角色审批，如果是角色审批则创建角色审批记录
        if (nextNodeDefinition.isRoleApprove()) {
            this.processTaskRoleApproveRecords(tenantId, nextNodeDefinition, nextTaskInstance, taskApproves);
            // 判断连续审批模式
            for (NodeAssignment nextNodeAssignment : nextNodeAssignments) {
                // 判断连续审批模式
                this.continuousApproveModeProcess(tenantId, nextTaskInstance, roleId, userId, nextNodeAssignment.getApproverId(), continuousApproveMode);
            }
        }

        nextTaskInstances.add(nextTaskInstance);
    }

    /**
     * 连续审批模式处理
     *
     * @param tenantId              租户 ID
     * @param nextTaskInstance      下一个审批节点
     * @param roleId                当前审批人角色 ID
     * @param approverId            当前审批人 ID
     * @param nextApproverId        下一个审批人 ID
     * @param continuousApproveMode 连续审批模式
     *
     * @author wangweijun
     * @since 2024/9/11 11:46
     */
    private void continuousApproveModeProcess(String tenantId, TaskInstance nextTaskInstance,
                                              @Nullable String roleId, String approverId,
                                              String nextApproverId,
                                              ContinuousApproveMode continuousApproveMode) {

        // 如果流程实例已经完成，则不处理
        WorkflowInstanceExecutor workflowInstanceExecutor = workflowInstanceExecutorBuilder.build();
        WorkflowInstance workflowInstance = workflowInstanceExecutor.getById(nextTaskInstance.getWorkflowInstanceId());
        if (workflowInstance.isFinished()) {
            return;
        }

        // 任务审批记录如果不存在，则不处理
        TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder.build();
        TaskApprove activeTaskApprove = taskApproveExecutor.getByTaskInstanceIdAndRoleIdAndApproverId(tenantId, nextTaskInstance.getId(), ActiveStatus.ACTIVE, null, nextApproverId);
        if (activeTaskApprove == null) {
            return;
        }

        // 获取自动审批处理器
        AgileApproveListener agileApproveListener = this.context.getAgileApproveListener();

        // 获取该流程实例下已经完成审批和即将要审批的审批人
        List<TaskApprove> taskApproves = taskApproveExecutor.findByTWorkflowInstanceId(tenantId, nextTaskInstance.getWorkflowInstanceId(), null, null);

        // 下个节点是：角色审批实例
        if (nextTaskInstance.isRoleApprove()) {
            Integer nextTaskInstanceId = nextTaskInstance.getId();
            // 获取流程实例下的角色审批记录
            TaskRoleApproveRecordExecutor taskRoleApproveRecordExecutor = taskRoleApproveRecordExecutorBuilder.build();
            List<TaskRoleApproveRecord> taskRoleApproveRecords = taskRoleApproveRecordExecutor.findByTWorkflowInstanceId(tenantId, workflowInstance.getId());
            // 获取任务实例下的审批记录，是为了保证角色用户审批已经审核过了
            if (taskRoleApproveRecordExecutor.findByTaskInstanceId(tenantId, nextTaskInstanceId).isEmpty()) {
                return;
            }
            // 获取角色审批记录对应的角色用户
            List<NodeRoleAssignment> nodeRoleAssignments = taskRoleApproveRecords.stream().map(taskRoleApproveRecord -> {
                NodeRoleAssignmentExecutor nodeRoleAssignmentExecutor = nodeRoleAssignmentExecutorBuilder.build();
                return nodeRoleAssignmentExecutor.getById(taskRoleApproveRecord.getNodeRoleAssignmentId());
            }).toList();
            // 判断是否是同一用户
            if (CollectionUtils.isNotEmpty(nodeRoleAssignments)) {
                switch (continuousApproveMode) {
                    case APPROVE_FIRST:
                        // 下个审批人已经存在审批的节点，则自动审批
                        // 已经完成审核的任务
                        List<TaskApprove> approvedTaskApproves = taskApproves.stream().filter(taskApprove -> ApproveStatus.APPROVED == taskApprove.getStatus()).toList();
                        // 已完成审核的角色用户
                        List<TaskRoleApproveRecord> approvedTaskRoleApproveRecords = taskRoleApproveRecords.stream().filter(taskRoleApproveRecord -> RoleApproveStatus.APPROVED == taskRoleApproveRecord.getStatus()).toList();
                        for (TaskApprove approvedTaskApprove : approvedTaskApproves) {
                            if (ApproverIdType.USER == approvedTaskApprove.getApproverIdType()) {
                                // 找到相同用户 ID 的审批人
                                Optional<NodeRoleAssignment> nodeRoleAssignmentOptional = nodeRoleAssignments.stream()
                                        .filter(nodeRoleAssignment -> Objects.equals(nodeRoleAssignment.getUserId(), approvedTaskApprove.getApproverId()))
                                        .findAny();
                                if (nodeRoleAssignmentOptional.isPresent()) {
                                    NodeRoleAssignment sameNodeRoleAssignment = nodeRoleAssignmentOptional.get();
                                    // 找到当前角色用户已经审核过的实例
                                    Optional<TaskRoleApproveRecord> approvedTaskRoleApproveRecord = approvedTaskRoleApproveRecords.stream()
                                            .filter(taskRoleApproveRecord -> Objects.equals(taskRoleApproveRecord.getTaskInstanceId(), nextTaskInstanceId))
                                            .filter(taskRoleApproveRecord -> RoleApproveStatus.APPROVED == taskRoleApproveRecord.getStatus())
                                            .filter(taskRoleApproveRecord -> Objects.equals(taskRoleApproveRecord.getNodeRoleAssignmentId(), sameNodeRoleAssignment.getId()))
                                            .findAny();
                                    // 如果不存在已经审核过的实例，则进行审核
                                    if (approvedTaskRoleApproveRecord.isEmpty() && (roleId == null || roleId.equals(sameNodeRoleAssignment.getRoleId()))) {
                                        String nodeRoleAssignmentRoleId = sameNodeRoleAssignment.getRoleId();
                                        String nodeRoleAssignmentUserId = sameNodeRoleAssignment.getUserId();
                                        // 自动审批之前
                                        String comment = agileApproveListener.preAutoApprove(ContinuousApproveMode.APPROVE_FIRST, nextTaskInstance, nodeRoleAssignmentRoleId, nodeRoleAssignmentUserId);
                                        comment = comment == null ? WorkflowConstants.AUTO_APPROVER_COMMENT : comment;
                                        this.approve(tenantId, nextTaskInstance.getId(), nodeRoleAssignmentRoleId, nodeRoleAssignmentUserId, comment, true);
                                        // 自动审批之后
                                        agileApproveListener.postAutoApproved(ContinuousApproveMode.APPROVE_FIRST, nextTaskInstance, nodeRoleAssignmentRoleId, nodeRoleAssignmentUserId, comment);
                                    }
                                }
                            } else {
                                // 将 approvedTaskRoleApproveRecords 按 nodeRoleAssignmentId 去重
                                List<Integer> nodeRoleAssignmentIds = approvedTaskRoleApproveRecords.stream().map(TaskRoleApproveRecord::getNodeRoleAssignmentId).distinct().toList();
                                nodeRoleAssignments.forEach(nodeRoleAssignment -> {
                                    if (nodeRoleAssignmentIds.contains(nodeRoleAssignment.getId())) {
                                        // 找到当前角色用户已经审核过的实例
                                        Optional<TaskRoleApproveRecord> approvedTaskRoleApproveRecord = approvedTaskRoleApproveRecords.stream()
                                                .filter(taskRoleApproveRecord -> Objects.equals(taskRoleApproveRecord.getTaskInstanceId(), nextTaskInstanceId))
                                                .filter(taskRoleApproveRecord -> RoleApproveStatus.APPROVED == taskRoleApproveRecord.getStatus())
                                                .filter(taskRoleApproveRecord -> Objects.equals(taskRoleApproveRecord.getNodeRoleAssignmentId(), nodeRoleAssignment.getId()))
                                                .findAny();
                                        // 如果不存在已经审核过的实例，则进行审核
                                        if (approvedTaskRoleApproveRecord.isEmpty() && (roleId == null || roleId.equals(nodeRoleAssignment.getRoleId()))) {
                                            String nodeRoleAssignmentRoleId = nodeRoleAssignment.getRoleId();
                                            String nodeRoleAssignmentUserId = nodeRoleAssignment.getUserId();
                                            // 自动审批之前
                                            String comment = agileApproveListener.preAutoApprove(ContinuousApproveMode.APPROVE_FIRST, nextTaskInstance, nodeRoleAssignmentRoleId, nodeRoleAssignmentUserId);
                                            comment = comment == null ? WorkflowConstants.AUTO_APPROVER_COMMENT : comment;
                                            // 审核
                                            this.approve(tenantId, nextTaskInstance.getId(), nodeRoleAssignmentRoleId, nodeRoleAssignmentUserId, comment, true);
                                            // 自动审批之后
                                            agileApproveListener.postAutoApproved(ContinuousApproveMode.APPROVE_FIRST, nextTaskInstance, nodeRoleAssignmentRoleId, nodeRoleAssignmentUserId, comment);
                                        }
                                    }
                                });
                            }
                        }
                        break;
                    case APPROVE_CONTINUOUS:
                        // 下个审批人已经存在审批的节点，且已审批的节点的审批人和下一个节点的审批人是同一个人，则自动审批
                        // 找到相同角色 ID，相同用户 ID 的审批人
                        Optional<NodeRoleAssignment> nodeRoleAssignmentOptional = nodeRoleAssignments.stream()
                                .filter(nodeRoleAssignment -> Objects.equals(nodeRoleAssignment.getRoleId(), nextApproverId))
                                .filter(nodeRoleAssignment -> Objects.equals(nodeRoleAssignment.getUserId(), approverId))
                                .findAny();
                        // 如果存在相同审批人
                        if (nodeRoleAssignmentOptional.isPresent()) {
                            NodeRoleAssignment sameNodeRoleAssignment = nodeRoleAssignmentOptional.get();
                            String sameRoleId = sameNodeRoleAssignment.getRoleId();
                            String sameUserId = sameNodeRoleAssignment.getUserId();
                            // 自动审批之前
                            String comment = agileApproveListener.preAutoApprove(ContinuousApproveMode.APPROVE_CONTINUOUS, nextTaskInstance, sameRoleId, sameUserId);
                            comment = comment == null ? WorkflowConstants.AUTO_APPROVER_COMMENT : comment;
                            // 审核
                            this.approve(tenantId, nextTaskInstance.getId(), sameRoleId, sameUserId, comment, true);
                            // 自动审批之后
                            agileApproveListener.postAutoApproved(ContinuousApproveMode.APPROVE_CONTINUOUS, nextTaskInstance, sameRoleId, sameUserId, comment);
                        }
                        break;
                    case APPROVE_ALL:
                    default:
                        break;
                }
            }
        }
        // 下个节点是：用户审批实例
        else {
            // 相同的用户并且已经审核的用户
            Optional<String> sameUserId = Optional.empty();

            // 下一个审批人的审批情况（下一个审批人在之前的审批记录中是否存在，并且是通过审核的）
            Optional<TaskApprove> nextApproverIdApproveOptional = taskApproves.stream()
                    .filter(approve -> nextApproverId.equals(approve.getApproverId()) && ApproveStatus.APPROVED == approve.getStatus()).findAny();
            if (nextApproverIdApproveOptional.isPresent()) {
                sameUserId = Optional.of(nextApproverId);
            }
            // 继续从角色审批记录中查询
            else {
                // 获取流程实例下的角色审批记录
                TaskRoleApproveRecordExecutor taskRoleApproveRecordExecutor = taskRoleApproveRecordExecutorBuilder.build();
                List<TaskRoleApproveRecord> taskRoleApproveRecords = taskRoleApproveRecordExecutor.findByTWorkflowInstanceId(tenantId, workflowInstance.getId());
                // 已完成审核的角色用户审核记录
                List<TaskRoleApproveRecord> approvedTaskRoleApproveRecords = taskRoleApproveRecords.stream().filter(taskRoleApproveRecord -> RoleApproveStatus.APPROVED == taskRoleApproveRecord.getStatus()).toList();
                // 在已完成的角色审核记录中获取与当前审核人相同的角色用户
                Optional<NodeRoleAssignment> nodeRoleAssignmentOptional = approvedTaskRoleApproveRecords.stream().map(taskRoleApproveRecord -> {
                    NodeRoleAssignmentExecutor nodeRoleAssignmentExecutor = nodeRoleAssignmentExecutorBuilder.build();
                    return nodeRoleAssignmentExecutor.getById(taskRoleApproveRecord.getNodeRoleAssignmentId());
                }).filter(nodeRoleAssignment -> Objects.equals(nodeRoleAssignment.getUserId(), nextApproverId)).findAny();
                if (nodeRoleAssignmentOptional.isPresent()) {
                    sameUserId = Optional.of(nextApproverId);
                }
            }

            switch (continuousApproveMode) {
                case APPROVE_FIRST:
                    // 下个审批人已经存在审批的节点，则自动审批
                    if (sameUserId.isPresent()) {
                        // 自动审批之前
                        String comment = agileApproveListener.preAutoApprove(ContinuousApproveMode.APPROVE_FIRST, nextTaskInstance, null, nextApproverId);
                        comment = comment == null ? WorkflowConstants.AUTO_APPROVER_COMMENT : comment;
                        // 审核
                        this.approve(tenantId, nextTaskInstance.getId(), null, nextApproverId, comment, true);
                        // 自动审批之后
                        agileApproveListener.postAutoApproved(ContinuousApproveMode.APPROVE_FIRST, nextTaskInstance, null, nextApproverId, comment);
                    }
                    break;
                case APPROVE_CONTINUOUS:
                    // 下个审批人已经存在审批的节点，且已审批的节点的审批人和下一个节点的审批人是同一个人，则自动审批
                    if (sameUserId.isPresent() && approverId.equals(nextApproverId)) {
                        // 自动审批之前
                        String comment = agileApproveListener.preAutoApprove(ContinuousApproveMode.APPROVE_CONTINUOUS, nextTaskInstance, null, nextApproverId);
                        comment = comment == null ? WorkflowConstants.AUTO_APPROVER_COMMENT : comment;
                        // 审核
                        this.approve(tenantId, nextTaskInstance.getId(), null, nextApproverId, comment, true);
                        // 自动审批之后
                        agileApproveListener.postAutoApproved(ContinuousApproveMode.APPROVE_CONTINUOUS, nextTaskInstance, null, nextApproverId, comment);
                    }
                    break;
                case APPROVE_ALL:
                default:
                    break;
            }
        }
    }

    @Override
    public void approve(String tenantId, Integer taskInstanceId, String roleId, String userId, String comment) {
        this.approve(tenantId, taskInstanceId, roleId, userId, comment, false);
    }

    /**
     * 审批
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     * @param roleId         角色 ID
     * @param userId         用户 ID
     * @param comment        审批意见
     * @param autoApprove    是否是自动审批
     */
    @Override
    public void approve(String tenantId, Integer taskInstanceId, String roleId, String userId, String comment, boolean autoApprove) {
        JdbcTemplateHelper jdbcTemplateHelper = this.context.getJdbcTemplateHelper();
        jdbcTemplateHelper.executeInTransaction(() -> {

            // 检查审批意见是否必填
            checkIfRequiredComment(taskInstanceId, comment);

            // 修改当前 TaskInstance 的 approved_count 数量
            TaskInstanceExecutor taskInstanceExecutor = taskInstanceExecutorBuilder.build();
            TaskInstance taskInstance = taskInstanceExecutor.getById(taskInstanceId);

            // 审核之前调用
            AgileApproveListener agileApproveListener = context.getAgileApproveListener();
            String userComment = agileApproveListener.preApprove(taskInstance, roleId, userId);
            // 审批意见返回 null 或者时自动审核返回的 comment，则不修改
            if (userComment == null || autoApprove) {
                userComment = comment;
            }

            // 设置流程实例状态为：进行中
            WorkflowInstanceExecutor workflowInstanceExecutor = workflowInstanceExecutorBuilder.build();
            WorkflowInstance workflowInstance = workflowInstanceExecutor.getById(taskInstance.getWorkflowInstanceId());
            if (WorkflowStatus.WAITING == workflowInstance.getStatus()) {
                workflowInstance.setStatus(WorkflowStatus.IN_PROGRESS);
                workflowInstanceExecutor.updateById(workflowInstance);
            }

            // 获取当前审批任务
            TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder.build();
            TaskApprove taskApprove;
            // 系统审批人自动审批的情况
            if (WorkflowConstants.AUTO_APPROVER_ID.equals(userId)) {
                Integer workflowDefinitionId = workflowInstance.getWorkflowDefinitionId();
                // 获取默认审批人
                Approver defaultApprover = this.getDefaultApprover(tenantId, workflowDefinitionId);
                // 创建一个 approver
                taskApprove = TaskApproveBuilder.builder()
                        .tenantId(tenantId)
                        .workflowInstanceId(workflowInstance.getId())
                        .taskInstanceId(taskInstanceId)
                        .approverId(defaultApprover.getId())
                        .active(ActiveStatus.INACTIVE)
                        .status(ApproveStatus.APPROVED)
                        .comment(userComment)
                        .build();
                taskApproveExecutor.save(taskApprove);
            }
            // 非系统审批人自动审批的情况
            else {
                taskApprove = taskApproveExecutor.getByTaskInstanceIdAndRoleIdAndApproverId(tenantId, taskInstanceId, ActiveStatus.ACTIVE, roleId, userId);
                if (taskApprove == null) {
                    if (autoApprove) {
                        return;
                    }
                    throw new WorkflowException("未查询到符合的任务审批记录: TaskApprove");
                }

                // 用户审批的情况
                if (ApproverIdType.ROLE != taskApprove.getApproverIdType()) {
                    // 更新 TaskApprove 的 active、status 和 comment
                    taskApprove.setActive(ActiveStatus.INACTIVE);
                    taskApprove.setStatus(ApproveStatus.APPROVED);
                    taskApprove.setComment(userComment);
                    taskApproveExecutor.updateById(taskApprove);
                }
                // 角色审批的情况，在下面的代码会进行更新
            }

            // 获取当前实例的节点定义
            NodeDefinitionExecutor nodeDefinitionExecutor = nodeDefinitionExecutorBuilder.build();
            NodeDefinition nodeDefinition = nodeDefinitionExecutor.getById(taskInstance.getNodeDefinitionId());

            // 判断是否是顺序审批
            checkIfApproveSeqStatus(tenantId, taskInstanceId, nodeDefinition.getApproveType());

            List<TaskInstance> nextTaskInstances = null;

            // 角色审批且满足进入下一个审批节点的条件
            boolean roleApproveGotoNext = false;
            // 角色审批的情况
            if (nodeDefinition.isRoleApprove()) {
                roleApproveGotoNext = this.processApproveRoleApprove(tenantId, nodeDefinition, taskInstance, taskApprove, roleId, userId, userComment);
            }
            // 用户审批的情况
            else {
                // 审批人数 +1
                taskInstance.setApprovedCount(taskInstance.getApprovedCount() + 1);
            }


            // 已完成审批的人数 approved_count 等于所有需要审批的人数 total_count
            if (Objects.equals(taskInstance.getApprovedCount(), taskInstance.getTotalCount()) || roleApproveGotoNext) {
                // 获取其他的审批人节点
                List<TaskApprove> otherActiveTaskApproves = taskApproveExecutor.findByTaskInstanceId(tenantId, taskInstanceId, ActiveStatus.ACTIVE);

                // 非角色审批
                if (ApproveType.ANY == nodeDefinition.getApproveType() && !nodeDefinition.isRoleApprove()) {
                    // 将其他审批人节点设置为 SKIP，表示跳过
                    otherActiveTaskApproves.forEach(otherActiveTaskApprove -> {
                        otherActiveTaskApprove.convertToApproveStatusSkipped();
                        taskApproveExecutor.updateById(otherActiveTaskApprove);
                    });
                }

                // 角色审批
                if (nodeDefinition.isRoleApprove()) {
                    RoleApproveType roleApproveType = nodeDefinition.getRoleApproveType();
                    RoleUserApproveType roleUserApproveType = nodeDefinition.getRoleUserApproveType();
                    // 角色审批类型为 ANY
                    if (RoleApproveType.ANY == nodeDefinition.getRoleApproveType()) {
                        // 将所有角色审批节点设置为 SKIP，表示跳过
                        otherActiveTaskApproves.forEach(otherActiveTaskApprove -> {
                            if (ApproveStatus.ABANDONED != otherActiveTaskApprove.getStatus()) {
                                otherActiveTaskApprove.convertToApproveStatusSkipped();
                                taskApproveExecutor.updateById(otherActiveTaskApprove);
                            }
                        });
                    }
                    // 角色审批类型为：ALL 或 SEQ 且 角色用户审批类型为：ANY
                    else if ((RoleApproveType.ALL == roleApproveType || RoleApproveType.SEQ == roleApproveType) && RoleUserApproveType.ANY == roleUserApproveType) {
                        // 将所有角色审批节点设置为 APPROVED，表示全部同意
                        String finalUserComment = userComment;
                        otherActiveTaskApproves.forEach(otherActiveTaskApprove -> {
                            otherActiveTaskApprove.convertToApproveStatusApproved(finalUserComment);
                            taskApproveExecutor.updateById(otherActiveTaskApprove);
                        });
                    }
                }

                taskInstance.setStatus(NodeStatus.COMPLETED);
                // 指向下一个审批节点
                nextTaskInstances = this.next(tenantId, taskInstance.getId(), roleId, userId);
            }

            // 更新 taskInstance
            taskInstanceExecutor.updateById(taskInstance);

            // 记录流程日志（审批通过）
            recordLogs(tenantId, taskInstance.getWorkflowInstanceId(), taskInstanceId, nodeDefinition.getName(), TaskHistoryMessage.INSTANCE_APPROVED);

            // 调用审批处理器：审核完成
            agileApproveListener.postApproved(taskInstance, roleId, userId, userComment);

            // 没有下一级审批节点，且当前审批节点全部审批完成，则表示流程已经结束
            // nextTaskInstances = null: 表示正常达到结束节点
            // nextTaskInstances = empty: 表示条件节点中，没有符合的审批节点，则结束流程
            if (CollectionUtils.isEmpty(nextTaskInstances) && taskInstance.isCompleted()) {
                Integer workflowDefinitionId = nodeDefinition.getWorkflowDefinitionId();
                WorkflowDefinitionExecutor workflowDefinitionExecutor = workflowDefinitionExecutorBuilder.build();
                WorkflowDefinition workflowDefinition = workflowDefinitionExecutor.getById(workflowDefinitionId);
                // 获取条件节点不符合时的处理策略
                ConditionNotMatchedAnyStrategy conditionNotMatchedAnyStrategy = workflowDefinition.getConditionNotMatchedAnyStrategy();
                NodeStatus nodeStatus = ConditionNotMatchedAnyStrategy.PROCESS_REJECTED == conditionNotMatchedAnyStrategy ? NodeStatus.REJECTED : NodeStatus.COMPLETED;
                // 创建结束节点实例
                NodeDefinition endNodeDefinition = nodeDefinitionExecutor.getEndNode(tenantId, workflowDefinitionId);
                Integer workflowInstanceId = taskInstance.getWorkflowInstanceId();
                taskInstanceExecutor = taskInstanceExecutorBuilder.newInstance()
                        .tenantId(tenantId)
                        .workflowInstanceId(workflowInstanceId)
                        .nodeDefinitionId(endNodeDefinition.getId())
                        .roleApprove(endNodeDefinition.isRoleApprove())
                        .status(nodeStatus)
                        .build();
                taskInstanceExecutor.save();

                // 获取最新状态
                workflowInstance = workflowInstanceExecutor.getById(taskInstance.getWorkflowInstanceId());

                // 保存流程定义
                this.saveWorkflowSchema(tenantId, workflowInstance);
                // 保存流程实例的审批记录
                this.saveWorkflowInstanceApproveRecords(tenantId, workflowInstance);

                // 记录流程日志（审批结束）
                recordLogs(tenantId, workflowInstanceId, taskInstance.getId(), endNodeDefinition.getName(), TaskHistoryMessage.INSTANCE_ENDED);

                // 调用审批处理器：审核完成
                agileApproveListener.approveCompleted(workflowInstance, taskInstance, roleId, userId, userComment);
            }
        });
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
    @Override
    public void reassign(String tenantId, Integer taskInstanceId, String roleId, String userId, Invitee invitee, String comment) {
        JdbcTemplateHelper jdbcTemplateHelper = this.context.getJdbcTemplateHelper();
        jdbcTemplateHelper.executeInTransaction(() -> {
            // 参数校验
            if (StringUtils.isEmpty(userId) || StringUtils.isEmpty(invitee.getUserId())) {
                throw new WorkflowException("转派失败: 用户 ID 不能为空");
            }

            // 检查审批意见是否必填
            checkIfRequiredComment(taskInstanceId, comment);

            TaskReassignRecordExecutor taskReassignRecordExecutor = taskReassignRecordExecutorBuilder.build();

            // 获取当前任务实例
            TaskInstanceExecutor taskInstanceExecutor = taskInstanceExecutorBuilder.build();
            TaskInstance taskInstance = taskInstanceExecutor.getById(taskInstanceId);

            // 获取流程实例
            WorkflowInstanceExecutor workflowInstanceExecutor = workflowInstanceExecutorBuilder.build();
            WorkflowInstance workflowInstance = workflowInstanceExecutor.getById(taskInstance.getWorkflowInstanceId());
            // 检查流程实例是否已经结束
            checkIfWorkflowInstanceIsFinished(workflowInstance);

            // 获取当前实例的节点定义
            NodeDefinitionExecutor nodeDefinitionExecutor = nodeDefinitionExecutorBuilder.build();
            NodeDefinition nodeDefinition = nodeDefinitionExecutor.getById(taskInstance.getNodeDefinitionId());

            // 获取当前审批任务
            TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder.build();
            TaskApprove taskApprove = taskApproveExecutor.getByTaskInstanceIdAndRoleIdAndApproverId(tenantId, taskInstanceId, ActiveStatus.ACTIVE, roleId, userId);
            if (taskApprove == null || ApproveStatus.IN_PROGRESS != taskApprove.getStatus()) {
                throw new WorkflowException("转派失败: 当前审批任务不存在或未生效");
            }

            // 获取当前节点的审批人
            NodeAssignmentExecutor nodeAssignmentExecutor = nodeAssignmentExecutorBuilder.build();
            NodeAssignment nodeAssignment = nodeAssignmentExecutor.getByNodeDefinitionIdAndApproverId(tenantId, nodeDefinition.getId(), taskApprove.getApproverId());

            // 将委派人封装为 inviter
            Inviter inviter;
            NodeRoleAssignmentExecutor nodeRoleAssignmentExecutor = nodeRoleAssignmentExecutorBuilder.build();
            if (nodeAssignment != null) {
                if (nodeDefinition.isRoleApprove()) {
                    NodeRoleAssignment nodeRoleAssignment = nodeRoleAssignmentExecutor.getByNodeDefinitionIdAndRoleIdAndApproverId(tenantId, nodeDefinition.getId(), roleId, userId, null);
                    inviter = Inviter.of(nodeRoleAssignment);
                } else {
                    inviter = Inviter.of(nodeAssignment);
                }
            } else {
                TaskReassignRecord toUser = taskReassignRecordExecutor.getToUser(tenantId, taskApprove.getId(), roleId, userId);
                if (toUser == null) {
                    throw new WorkflowException("转派失败: 转派人不存在");
                }
                inviter = Inviter.of(toUser);
            }

            // 角色审批
            if (nodeDefinition.isRoleApprove()) {
                // 审批人与转派人相同，直接结束
                if (Objects.equals(roleId, invitee.getRoleId()) && Objects.equals(userId, invitee.getUserId())) {
                    return;
                }
                if (!Objects.equals(roleId, invitee.getRoleId())) {
                    throw new WorkflowException("转派失败: 相同角色下的用户才可以转派");
                }
                if (StringUtils.isEmpty(roleId) || StringUtils.isEmpty(invitee.getRoleId())) {
                    throw new WorkflowException("转派失败: 角色 ID 不能为空");
                }

                // 检查是否重复转派
                TaskReassignRecord taskReassignRecord = taskReassignRecordExecutor.getRecord(tenantId, taskApprove.getId(), roleId, userId, invitee.getRoleId(), invitee.getUserId());
                if (taskReassignRecord != null) {
                    throw new WorkflowException("转派失败: 已存在相同的转派记录");
                }

                // 获取被转派人是否已经是被转派的状态
                TaskRoleApproveRecordExecutor taskRoleApproveRecordExecutor = taskRoleApproveRecordExecutorBuilder.build();
                TaskRoleApproveRecord inviteeTaskRoleApproveRecord = taskRoleApproveRecordExecutor.getByTaskInstanceIdAndRoleIdAndUserId(tenantId, taskInstanceId, invitee.getRoleId(), invitee.getUserId());
                // 检查转派状态：是否是已转派
                if (inviteeTaskRoleApproveRecord != null) {
                    // 同一个审批节点，转派人最终转给的还是转派人的情况：xxx -> yyy -> zzz -> xxx 的情况
                    if (RoleApproveStatus.REASSIGNED == inviteeTaskRoleApproveRecord.getStatus()) {
                        throw new WorkflowException("转派失败: 无法转派给已转派的角色用户");
                    }
                    // 同一个审批节点，审批人都转给同一个用户的情况：xxx -> yyy; zzz -> yyy；或者 yyy 本身就是改审批实例的一个审批人
                    throw new WorkflowException("转派失败: 角色审批下，同一个审批节点，无法转派给同一个审批人");
                }

                // 新增角色审批用户（被委派人）
                NodeRoleAssignment reassignNodeRoleAssignment = nodeRoleAssignmentExecutor.getByNodeDefinitionIdAndRoleIdAndApproverId(tenantId, nodeDefinition.getId(), invitee.getRoleId(), invitee.getUserId(), null);
                if (reassignNodeRoleAssignment == null || (
                        !Objects.equals(invitee.getRoleName(), reassignNodeRoleAssignment.getRoleName()) ||
                        !Objects.equals(invitee.getRoleDesc(), reassignNodeRoleAssignment.getRoleDesc()) ||
                        !Objects.equals(invitee.getUserName(), reassignNodeRoleAssignment.getUserName()) ||
                        !Objects.equals(invitee.getUserDesc(), reassignNodeRoleAssignment.getUserDesc())
                )) {
                    reassignNodeRoleAssignment = NodeRoleAssignment.reassignFrom(nodeDefinition, taskApprove, inviter, invitee);
                    nodeRoleAssignmentExecutor.save(reassignNodeRoleAssignment);
                }

                // 更改原角色用户审批记录：状态更改为：被委派，并更新备注
                TaskRoleApproveRecord taskRoleApproveRecord = taskRoleApproveRecordExecutor.getByTaskInstanceIdAndRoleIdAndUserId(tenantId, taskInstanceId, roleId, userId);
                taskRoleApproveRecord.setStatus(RoleApproveStatus.REASSIGNED);
                taskRoleApproveRecord.setComment(comment);

                // 新增新角色用户审批记录：状态设置为：进行中，并指向新的审批人 ID
                TaskRoleApproveRecord reassignTaskRoleApproveRecord = TaskRoleApproveRecord.reassignFrom(taskRoleApproveRecord);
                reassignTaskRoleApproveRecord.setStatus(RoleApproveStatus.IN_PROGRESS);
                reassignTaskRoleApproveRecord.setNodeRoleAssignmentId(reassignNodeRoleAssignment.getId());
                taskRoleApproveRecordExecutor.save(reassignTaskRoleApproveRecord);

                // 更改原角色用户审批记录：记录转派角色用户审批记录的 ID
                taskRoleApproveRecord.setReassignedTaskRoleApproveRecordId(reassignTaskRoleApproveRecord.getId());
                taskRoleApproveRecordExecutor.updateById(taskRoleApproveRecord);

                // 更改审批人 ID 为被委派人的角色 ID
                taskApprove.setApproverId(invitee.getRoleId());
                taskApproveExecutor.updateById(taskApprove);

                // 记录转派记录
                taskReassignRecord = TaskReassignRecordBuilder.builder().ofRoleIdType(taskApprove, inviter, invitee, comment).build();
                taskReassignRecordExecutor.save(taskReassignRecord);
            }
            // 用户审批
            else {
                // 审批人与转派人相同，直接结束
                if (Objects.equals(userId, invitee.getUserId())) {
                    return;
                }

                // 转派校验
                TaskApprove reassignTaskApprove = taskApproveExecutor.getByTaskInstanceIdAndApproverId(tenantId, taskInstanceId, invitee.getUserId());
                if (reassignTaskApprove != null) {
                    // 同一个审批节点，转派人最终转给的还是转派人的情况：xxx -> yyy -> zzz -> xxx 的情况
                    if (ApproveStatus.REASSIGNED == reassignTaskApprove.getStatus()) {
                        throw new WorkflowException("转派失败: 无法转派给已转派的用户");
                    }
                    // 同一个审批节点，审批人都转给同一个用户的情况：xxx -> yyy; zzz -> yyy；或者 yyy 本身就是改审批实例的一个审批人
                    if (ApproveStatus.IN_PROGRESS == reassignTaskApprove.getStatus() || ApproveStatus.SUSPEND == reassignTaskApprove.getStatus()) {
                        throw new WorkflowException("转派失败: 被转派人正处于审批或等待审批中，无法转派");
                    }
                }

                // 新增新用户审批记录：状态设置为：进行中，并指向新的审批人 ID
                reassignTaskApprove = TaskApprove.reassignFrom(taskApprove);
                reassignTaskApprove.setStatus(ApproveStatus.IN_PROGRESS);
                reassignTaskApprove.setApproverId(invitee.getUserId());
                taskApproveExecutor.save(reassignTaskApprove);

                // 更改审批人 ID 为被委派人 ID
                taskApprove.setStatus(ApproveStatus.REASSIGNED);
                taskApprove.setActive(ActiveStatus.INACTIVE);
                taskApprove.setComment(comment);
                taskApprove.setReassignedTaskApproveId(reassignTaskApprove.getId());
                taskApproveExecutor.updateById(taskApprove);

                // 记录转派记录
                TaskReassignRecord taskReassignRecord = TaskReassignRecordBuilder.builder().ofUserIdType(reassignTaskApprove, inviter, invitee, comment).build();
                taskReassignRecordExecutor.save(taskReassignRecord);
            }
        });
    }

    /**
     * 检查流程实例是否已经结束
     *
     * @param workflowInstance 流程实例
     *
     * @author wangweijun
     * @since 2024/11/7 17:07
     */
    private void checkIfWorkflowInstanceIsFinished(WorkflowInstance workflowInstance) {
        if (workflowInstance.isFinished()) {
            throw new WorkflowException("流程实例已结束: %s", workflowInstance.getStatus().getDesc());
        }
    }

    /**
     * 保存流程定义
     *
     * @param tenantId         租户 ID
     * @param workflowInstance 流程实例
     *
     * @author wangweijun
     * @since 2024/11/4 10:55
     */
    private void saveWorkflowSchema(String tenantId, WorkflowInstance workflowInstance) {
        WorkflowInstanceExecutor workflowInstanceExecutor = workflowInstanceExecutorBuilder.build();
        WorkflowDefinitionFlowSchema workflowDefinitionFlowSchema = this.deploymentService.getWorkflowDefinitionFlowSchema(tenantId, workflowInstance.getWorkflowDefinitionId());
        workflowInstance.setFlowSchema(workflowDefinitionFlowSchema);
        workflowInstanceExecutor.updateById(workflowInstance);
    }

    /**
     * 保存流程实例的审批记录
     *
     * @param tenantId         租户 ID
     * @param workflowInstance 流程实例
     *
     * @author wangweijun
     * @since 2024/11/4 10:28
     */
    private void saveWorkflowInstanceApproveRecords(String tenantId, WorkflowInstance workflowInstance) {
        if (workflowInstance != null && workflowInstance.isFinished()) {
            WorkflowInstanceExecutor workflowInstanceExecutor = workflowInstanceExecutorBuilder.build();
            WorkflowInstanceApproveRecords workflowInstanceApproveRecords = this.getWorkflowInstanceApproveRecords(tenantId, workflowInstance.getId(), null, null);
            workflowInstance.setApproveRecords(workflowInstanceApproveRecords);
            workflowInstanceExecutor.updateById(workflowInstance);
        }
    }

    /**
     * 处理审批-角色审批的情况
     *
     * @param tenantId       租户 ID
     * @param nodeDefinition 节点定义
     * @param taskInstance   任务实例
     * @param taskApprove    任务审批
     * @param roleId         角色 ID
     * @param comment        审批意见
     *
     * @return boolean
     *
     * @author wangweijun
     * @since 2024/9/19 17:51
     */
    private boolean processApproveRoleApprove(String tenantId, NodeDefinition nodeDefinition, TaskInstance taskInstance, TaskApprove taskApprove, String roleId, String userId, String comment) {
        boolean roleApproveGotoNext = false;
        Integer taskInstanceId = taskInstance.getId();
        RoleApproveType roleApproveType = nodeDefinition.getRoleApproveType();
        RoleUserApproveType roleUserApproveType = nodeDefinition.getRoleUserApproveType();

        TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder.build();
        TaskRoleApproveRecordExecutor taskRoleApproveRecordExecutor = taskRoleApproveRecordExecutorBuilder.build();
        NodeRoleAssignmentExecutor nodeRoleAssignmentExecutor = nodeRoleAssignmentExecutorBuilder.build();

        // 角色审批类型为 ANY 且 角色用户审批类型为 ALL 或 SEQ，计算是否达到进入下一个审批节点的条件
        if (RoleApproveType.ANY == roleApproveType && RoleUserApproveType.ANY != roleUserApproveType) {

            // 角色用户审批类型为：SEQ
            if (RoleUserApproveType.SEQ == roleUserApproveType) {
                // 查找当前角色用户审批记录，并修改为：APPROVED
                TaskRoleApproveRecord curTaskRoleApproveRecord = taskRoleApproveRecordExecutor.getByTaskInstanceIdAndRoleIdAndUserId(tenantId, taskInstanceId, roleId, userId);
                if (curTaskRoleApproveRecord == null || RoleApproveStatus.IN_PROGRESS != curTaskRoleApproveRecord.getStatus()) {
                    throw new WorkflowException("角色用户审批记录不存在或状态不正确");
                }
                curTaskRoleApproveRecord.setComment(comment);
                curTaskRoleApproveRecord.setStatus(RoleApproveStatus.APPROVED);
                taskRoleApproveRecordExecutor.updateById(curTaskRoleApproveRecord);

                // 查找下一个角色用户
                List<NodeRoleAssignment> nextNodeRoleAssignments = new ArrayList<>();
                List<TaskRoleApproveRecord> nextTaskRoleApproveRecords = taskRoleApproveRecordExecutor.findByTaskInstanceIdAndRoleIdAndStatus(tenantId, taskInstanceId, roleId, ApproveStatus.SUSPEND);
                for (TaskRoleApproveRecord nextTaskRoleApproveRecord : nextTaskRoleApproveRecords) {
                    Integer nodeRoleAssignmentId = nextTaskRoleApproveRecord.getNodeRoleAssignmentId();
                    NodeRoleAssignment nextNodeRoleAssignment = nodeRoleAssignmentExecutor.getById(nodeRoleAssignmentId);
                    nextNodeRoleAssignments.add(nextNodeRoleAssignment);
                }
                // 找到 userSeq 最小的角色用户
                nextNodeRoleAssignments.stream().min(Comparator.comparingInt(NodeRoleAssignment::getUserSeq)).ifPresent(nextNodeRoleAssignment -> {
                    // 找到对应的角色审批记录
                    nextTaskRoleApproveRecords.stream().filter(i -> nextNodeRoleAssignment.getId().equals(i.getNodeRoleAssignmentId())).findAny().ifPresent(taskRoleApproveRecord -> {
                        // 将下一个角色用户审批记录设置为 IN_PROGRESS
                        taskRoleApproveRecord.setStatus(RoleApproveStatus.IN_PROGRESS);
                        taskRoleApproveRecordExecutor.updateById(taskRoleApproveRecord);
                    });
                });
            }
            // 角色用户审批类型为：ALL
            else if (RoleUserApproveType.ALL == roleUserApproveType) {
                // 查找当前角色用户审批记录，并修改为：APPROVED
                TaskRoleApproveRecord curTaskRoleApproveRecord = taskRoleApproveRecordExecutor.getByTaskInstanceIdAndRoleIdAndUserId(tenantId, taskInstanceId, roleId, userId);
                if (curTaskRoleApproveRecord == null || RoleApproveStatus.IN_PROGRESS != curTaskRoleApproveRecord.getStatus()) {
                    throw new WorkflowException("角色用户审批记录不存在或状态不正确");
                }
                curTaskRoleApproveRecord.setComment(comment);
                curTaskRoleApproveRecord.setStatus(RoleApproveStatus.APPROVED);
                taskRoleApproveRecordExecutor.updateById(curTaskRoleApproveRecord);
            }

            // 审批人数 +1
            taskInstance.setApprovedCount(taskInstance.getApprovedCount() + 1);

            // 获取所有角色审批记录中，审批通过的用户，并按角色进行分组
            List<TaskRoleApproveRecord> approvedRecords = findApprovedTaskRoleApproveRecords(tenantId, taskInstanceId);

            // 获取所有角色审批用户
            List<NodeRoleAssignment> nodeRoleAssignments = nodeRoleAssignmentExecutor.findByNodeDefinitionId(tenantId, nodeDefinition.getId(), NodeRoleAssignmentType.NORMAL);
            // 将角色审批用户按角色分组
            Map<String, List<NodeRoleAssignment>> nodeRoleAssignmentsByRoles = nodeRoleAssignments.stream().collect(Collectors.groupingBy(NodeRoleAssignment::getRoleId));

            // 记录已经审核过的角色用户，并按角色分组
            Map<String, List<NodeRoleAssignment>> roleApprovedMap = new HashMap<>();
            approvedRecords.forEach(i -> {
                NodeRoleAssignment nodeRoleAssignment = nodeRoleAssignmentExecutor.getById(i.getNodeRoleAssignmentId());
                roleApprovedMap.computeIfAbsent(nodeRoleAssignment.getRoleId(), k -> new ArrayList<>()).add(nodeRoleAssignment);
            });

            // 获取根据角色分组后，已经审核过的用户最大的数
            int maxApprovedCount = roleApprovedMap.values().stream().mapToInt(List::size).max().orElse(0);

            // 用角色中审批通过的用户与总审批人数进行取模计算，如果余数为 0 则表示审批完成
            if (maxApprovedCount > 0) {
                // 查询该角色下有多少角色用户
                List<NodeRoleAssignment> nodeRoleAssignmentsByRole = nodeRoleAssignmentsByRoles.get(roleId);
                // 查询该角色下已经完成审核的用户
                List<NodeRoleAssignment> approvedNodeRoleAssignments = roleApprovedMap.get(roleId);

                // 如果数量相等则表示该角色下所有用户均完成了审核
                if (nodeRoleAssignmentsByRole.size() == approvedNodeRoleAssignments.size()) {
                    roleApproveGotoNext = true;
                }

                // 更新 taskApprove
                if (roleApproveGotoNext) {
                    // 更新 TaskApprove
                    taskApprove.convertToApproveStatusApproved(comment);
                    taskApproveExecutor.updateById(taskApprove);

                    // 角色用户审批类型为 SEQ 或 ALL
                    if (RoleUserApproveType.SEQ == roleUserApproveType || RoleUserApproveType.ALL == roleUserApproveType) {
                        List<TaskRoleApproveRecord> taskRoleApproveRecords = taskRoleApproveRecordExecutor.findByTaskInstanceId(tenantId, taskInstanceId);
                        // 获取所有未审核的审批记录
                        List<TaskRoleApproveRecord> unApprovedRecords = taskRoleApproveRecords.stream().filter(i -> RoleApproveStatus.IN_PROGRESS == i.getStatus() || RoleApproveStatus.SUSPEND == i.getStatus()).toList();
                        // 将所有 IN_PROGRESS 的审批记录设置为 SKIPPED
                        unApprovedRecords.forEach(unApprovedRecord -> {
                            unApprovedRecord.setStatus(RoleApproveStatus.SKIPPED);
                            taskRoleApproveRecordExecutor.updateById(unApprovedRecord);
                        });
                    }
                }
            }
        }
        // 角色审批类型为 ALL 或 SEQ，角色用户审批条件为 ANY，计算是否达到进入下一个审批节点的条件
        else if ((RoleApproveType.ALL == roleApproveType || RoleApproveType.SEQ == roleApproveType) && RoleUserApproveType.ANY == roleUserApproveType) {

            // 查找当前角色用户审批记录，并修改为：APPROVED
            TaskRoleApproveRecord curTaskRoleApproveRecord = taskRoleApproveRecordExecutor.getByTaskInstanceIdAndRoleIdAndUserId(tenantId, taskInstanceId, roleId, userId);
            if (curTaskRoleApproveRecord == null || RoleApproveStatus.IN_PROGRESS != curTaskRoleApproveRecord.getStatus()) {
                throw new WorkflowException("角色用户审批记录不存在或状态不正确");
            }
            curTaskRoleApproveRecord.setComment(comment);
            curTaskRoleApproveRecord.setStatus(RoleApproveStatus.APPROVED);
            taskRoleApproveRecordExecutor.updateById(curTaskRoleApproveRecord);

            // 当前角色下其他用户修改为：SKIPPED
            List<TaskRoleApproveRecord> nextTaskRoleApproveRecords = taskRoleApproveRecordExecutor.findByTaskInstanceIdAndRoleIdAndStatus(tenantId, taskInstanceId, roleId, ApproveStatus.IN_PROGRESS);
            for (TaskRoleApproveRecord nextTaskRoleApproveRecord : nextTaskRoleApproveRecords) {
                nextTaskRoleApproveRecord.setStatus(RoleApproveStatus.SKIPPED);
                taskRoleApproveRecordExecutor.updateById(nextTaskRoleApproveRecord);
            }

            // 将当前角色的审批记录设置为：APPROVED
            taskApprove.convertToApproveStatusApproved(comment);
            taskApproveExecutor.updateById(taskApprove);


            // 角色审批类型为：SEQ
            if (RoleApproveType.SEQ == roleApproveType) {
                // 获取当前的审批用户
                NodeRoleAssignment curNodeRoleAssignment = nodeRoleAssignmentExecutor.getById(curTaskRoleApproveRecord.getNodeRoleAssignmentId());

                // 获取所有角色审批用户
                List<NodeRoleAssignment> nodeRoleAssignments = nodeRoleAssignmentExecutor.findByNodeDefinitionId(tenantId, nodeDefinition.getId(), NodeRoleAssignmentType.NORMAL);
                // 从 nodeRoleAssignments 去除当前的 roleId，然后找到 roleSeq 最小的角色 ID
                nodeRoleAssignments.stream().filter(i -> !i.getRoleId().equals(roleId))
                        .filter(i -> i.getRoleSeq() > curNodeRoleAssignment.getRoleSeq())
                        .min(Comparator.comparingInt(NodeRoleAssignment::getRoleSeq))
                        .flatMap(i -> Optional.of(i.getRoleId())).ifPresent(nextRoleId -> {
                            // 获取下一个角色的所有角色用户的审批记录
                            List<TaskRoleApproveRecord> nextRoleTaskRoleApproveRecords = taskRoleApproveRecordExecutor.findByTaskInstanceIdAndRoleId(tenantId, taskInstanceId, nextRoleId);
                            // 将下一个角色的用户审批记录设置为：IN_PROGRESS
                            for (TaskRoleApproveRecord nextRoleTaskRoleApproveRecord : nextRoleTaskRoleApproveRecords) {
                                nextRoleTaskRoleApproveRecord.setStatus(RoleApproveStatus.IN_PROGRESS);
                                taskRoleApproveRecordExecutor.updateById(nextRoleTaskRoleApproveRecord);
                            }
                        });
            }

            // 获取所有角色审批记录中，审批通过的用户（包含放弃），并按角色进行分组
            List<TaskRoleApproveRecord> approvedRecords = findApprovedTaskRoleApproveRecords(tenantId, taskInstanceId);

            // 记录已经审核过的角色用户（不包含放弃）
            Map<String, List<NodeRoleAssignment>> roleApprovedMap = new HashMap<>();

            // 获取所有角色审批用户
            approvedRecords.forEach(i -> {
                if (RoleApproveStatus.APPROVED == i.getStatus()) {
                    NodeRoleAssignment nodeRoleAssignment = nodeRoleAssignmentExecutor.getById(i.getNodeRoleAssignmentId());
                    roleApprovedMap.computeIfAbsent(nodeRoleAssignment.getRoleId(), k -> new ArrayList<>()).add(nodeRoleAssignment);
                }
            });

            // 当前角色只有首次审批的时候，才会 +1
            List<NodeRoleAssignment> list = roleApprovedMap.get(roleId);
            if (list.size() == 1) {
                // 审批人数 +1
                taskInstance.setApprovedCount(taskInstance.getApprovedCount() + 1);
            }

            // 是否当前角色下的所有用户都已经完成了审批
            if (Objects.equals(taskInstance.getApprovedCount(), taskInstance.getTotalCount())) {
                // 进入下一个节点
                roleApproveGotoNext = true;
            }
        }
        // 角色审批类型为 ALL 或 SEQ，角色用户审批条件为 ALL，计算是否达到进入下一个审批节点的条件
        else if (RoleApproveType.ANY != roleApproveType && RoleUserApproveType.ALL == roleUserApproveType) {
            // 角色审批类型为 SEQ
            // 查找当前角色用户审批记录，并修改为：APPROVED
            TaskRoleApproveRecord curTaskRoleApproveRecord = taskRoleApproveRecordExecutor.getByTaskInstanceIdAndRoleIdAndUserId(tenantId, taskInstanceId, roleId, userId);
            if (curTaskRoleApproveRecord == null || RoleApproveStatus.IN_PROGRESS != curTaskRoleApproveRecord.getStatus()) {
                throw new WorkflowException("角色用户审批记录不存在或状态不正确");
            }
            curTaskRoleApproveRecord.setComment(comment);
            curTaskRoleApproveRecord.setStatus(RoleApproveStatus.APPROVED);
            taskRoleApproveRecordExecutor.updateById(curTaskRoleApproveRecord);

            // 如果当前角色审批类型为：SEQ
            if (RoleApproveType.SEQ == roleApproveType) {
                // 当前角色下的用户审批记录
                List<TaskRoleApproveRecord> approvedTaskRoleApproveRecords = taskRoleApproveRecordExecutor.findByTaskInstanceIdAndRoleIdAndStatus(tenantId, taskInstanceId, roleId, ApproveStatus.APPROVED);
                // 当前角色下的用户数量
                List<NodeRoleAssignment> nodeRoleAssignmentsByRole = nodeRoleAssignmentExecutor.findByNodeDefinitionIdRoleId(tenantId, nodeDefinition.getId(), roleId);
                // 当前角色下所有用户都已经审批完成，此时需要看是否有其他角色待审核
                if (nodeRoleAssignmentsByRole.size() == approvedTaskRoleApproveRecords.size()) {
                    // 获取所有角色审批用户
                    List<NodeRoleAssignment> allNodeRoleAssignments = nodeRoleAssignmentExecutor.findByNodeDefinitionId(tenantId, nodeDefinition.getId(), NodeRoleAssignmentType.NORMAL);
                    // 已经完成审核的角色用户审批记录
                    List<TaskRoleApproveRecord> approveRecords = taskRoleApproveRecordExecutor.findByTaskInstanceIdAndStatuses(tenantId, taskInstanceId, Arrays.asList(ApproveStatus.APPROVED, ApproveStatus.ABANDONED));

                    // 从 nodeRoleAssignments 去除当前的 roleId 和已经审核过的用户，然后找到 roleSeq 最小的角色 ID
                    allNodeRoleAssignments.stream().filter(i -> !i.getRoleId().equals(roleId))
                            // 去除已经审核过的用户
                            .filter(i -> approveRecords.stream().noneMatch(approveRecord -> approveRecord.getNodeRoleAssignmentId().equals(i.getId())))
                            // 找到 roleSeq 最小的角色 ID
                            .min(Comparator.comparingInt(NodeRoleAssignment::getRoleSeq))
                            // 获取下一个角色 ID
                            .flatMap(i -> Optional.of(i.getRoleId())).ifPresent(nextRoleId -> {
                                // 下一个角色的所有角色用户的审批记录
                                List<TaskRoleApproveRecord> nextRoleTaskRoleApproveRecords = taskRoleApproveRecordExecutor.findByTaskInstanceIdAndRoleId(tenantId, taskInstanceId, nextRoleId);
                                // 找到对应的角色审批记录
                                nextRoleTaskRoleApproveRecords.forEach(nextRoletaskRoleApproveRecord -> {
                                    // 将下一个角色用户审批记录设置为 IN_PROGRESS
                                    nextRoletaskRoleApproveRecord.convertToApproveStatusInProgress();
                                    taskRoleApproveRecordExecutor.updateById(nextRoletaskRoleApproveRecord);
                                });
                            });
                }
            }


            // 审批人数 +1
            taskInstance.setApprovedCount(taskInstance.getApprovedCount() + 1);

            // 获取所有角色审批记录中，审批通过的用户，并按角色进行分组
            List<TaskRoleApproveRecord> taskRoleApproveRecords = taskRoleApproveRecordExecutor.findByTaskInstanceId(tenantId, taskInstanceId);
            List<TaskRoleApproveRecord> approvedRecords = taskRoleApproveRecords.stream()
                    .filter(i -> RoleApproveStatus.APPROVED == i.getStatus() || RoleApproveStatus.ABANDONED == i.getStatus()).toList();

            // 记录已经审核过的角色用户
            Map<String, List<NodeRoleAssignment>> roleApprovedMap = new HashMap<>();

            // 获取所有角色审批用户
            approvedRecords.forEach(i -> {
                NodeRoleAssignment nodeRoleAssignment = nodeRoleAssignmentExecutor.getById(i.getNodeRoleAssignmentId());
                roleApprovedMap.computeIfAbsent(nodeRoleAssignment.getRoleId(), k -> new ArrayList<>()).add(nodeRoleAssignment);
            });

            // 获取当前审批通过的用户
            List<NodeRoleAssignment> approvedNodeRoleAssignments = roleApprovedMap.get(roleId);

            // 获取所有角色审批用户，并按角色分组
            List<NodeRoleAssignment> nodeRoleAssignmentsByRole = findNodeRoleAssignmentsByRoleId(tenantId, nodeDefinition.getId(), roleId, NodeRoleAssignmentType.NORMAL);

            // 当前角色下所有用户都已经审批通过
            if (Objects.equals(approvedNodeRoleAssignments.size(), nodeRoleAssignmentsByRole.size())) {
                taskApprove.setActive(ActiveStatus.INACTIVE);
                taskApprove.setStatus(ApproveStatus.APPROVED);
                taskApprove.setComment(comment);
                taskApproveExecutor.updateById(taskApprove);
            }

            if (Objects.equals(taskInstance.getApprovedCount(), taskInstance.getTotalCount())) {
                // 进入下一个节点
                roleApproveGotoNext = true;
            }
        }
        // 角色审批类型为 ALL 或 SEQ，角色用户审批条件为 SEQ，计算是否达到进入下一个审批节点的条件
        else if (RoleApproveType.ANY != roleApproveType && RoleUserApproveType.SEQ == roleUserApproveType) {
            // 查找当前角色用户审批记录，并修改为：APPROVED
            TaskRoleApproveRecord curTaskRoleApproveRecord = taskRoleApproveRecordExecutor.getByTaskInstanceIdAndRoleIdAndUserId(tenantId, taskInstanceId, roleId, userId);
            if (curTaskRoleApproveRecord == null || RoleApproveStatus.IN_PROGRESS != curTaskRoleApproveRecord.getStatus()) {
                throw new WorkflowException("角色用户审批记录不存在或状态不正确");
            }
            curTaskRoleApproveRecord.setComment(comment);
            curTaskRoleApproveRecord.setStatus(RoleApproveStatus.APPROVED);
            taskRoleApproveRecordExecutor.updateById(curTaskRoleApproveRecord);

            // 查找下一个角色用户审批记录
            List<NodeRoleAssignment> nextNodeRoleAssignments = new ArrayList<>();
            List<TaskRoleApproveRecord> nextTaskRoleApproveRecords = taskRoleApproveRecordExecutor.findByTaskInstanceIdAndStatus(tenantId, taskInstanceId, RoleApproveStatus.SUSPEND);
            for (TaskRoleApproveRecord nextTaskRoleApproveRecord : nextTaskRoleApproveRecords) {
                Integer nodeRoleAssignmentId = nextTaskRoleApproveRecord.getNodeRoleAssignmentId();
                NodeRoleAssignment nextNodeRoleAssignment = nodeRoleAssignmentExecutor.getById(nodeRoleAssignmentId);
                nextNodeRoleAssignments.add(nextNodeRoleAssignment);
            }
            // 找到 roleSeq 和 userSeq 最小的角色用户
            nextNodeRoleAssignments.stream().min(Comparator.comparingInt(NodeRoleAssignment::getRoleSeq).thenComparing(NodeRoleAssignment::getUserSeq)).ifPresent(nextNodeRoleAssignment -> {
                // 找到对应的角色审批记录
                nextTaskRoleApproveRecords.stream().filter(i -> nextNodeRoleAssignment.getId().equals(i.getNodeRoleAssignmentId())).findAny().ifPresent(taskRoleApproveRecord -> {
                    // 将下一个角色用户审批记录设置为 IN_PROGRESS
                    taskRoleApproveRecord.setStatus(RoleApproveStatus.IN_PROGRESS);
                    taskRoleApproveRecordExecutor.updateById(taskRoleApproveRecord);
                });
            });

            // 审批人数 +1
            taskInstance.setApprovedCount(taskInstance.getApprovedCount() + 1);

            // 获取所有角色审批记录中，审批通过的用户，并按角色进行分组
            List<TaskRoleApproveRecord> taskRoleApproveRecords = taskRoleApproveRecordExecutor.findByTaskInstanceId(tenantId, taskInstanceId);
            List<TaskRoleApproveRecord> approvedRecords = taskRoleApproveRecords.stream().filter(i -> RoleApproveStatus.APPROVED == i.getStatus() || RoleApproveStatus.ABANDONED == i.getStatus()).toList();

            // 记录已经审核过的角色用户
            Map<String, List<NodeRoleAssignment>> roleApprovedMapByRole = new HashMap<>();

            // 获取所有角色审批用户
            approvedRecords.forEach(i -> {
                NodeRoleAssignment nodeRoleAssignment = nodeRoleAssignmentExecutor.getById(i.getNodeRoleAssignmentId());
                roleApprovedMapByRole.computeIfAbsent(nodeRoleAssignment.getRoleId(), k -> new ArrayList<>()).add(nodeRoleAssignment);
            });

            // 获取当前审批通过的用户
            List<NodeRoleAssignment> approvedNodeRoleAssignments = roleApprovedMapByRole.get(roleId);

            // 获取所有角色审批用户，并按角色分组
            List<NodeRoleAssignment> nodeRoleAssignmentsByRole = findNodeRoleAssignmentsByRoleId(tenantId, nodeDefinition.getId(), roleId, NodeRoleAssignmentType.NORMAL);

            // 当前角色下所有用户都已经审批通过
            if (Objects.equals(approvedNodeRoleAssignments.size(), nodeRoleAssignmentsByRole.size())) {
                taskApprove.setActive(ActiveStatus.INACTIVE);
                taskApprove.setStatus(ApproveStatus.APPROVED);
                taskApprove.setComment(comment);
                taskApproveExecutor.updateById(taskApprove);
            }

            if (Objects.equals(taskInstance.getApprovedCount(), taskInstance.getTotalCount())) {
                // 进入下一个节点
                roleApproveGotoNext = true;
            }
        }
        // 角色审批类型为 ANY，角色用户审批条件为 ANY
        else if (RoleApproveType.ANY == roleApproveType && RoleUserApproveType.ANY == roleUserApproveType) {
            // 查找当前角色用户审批记录，并修改为：APPROVED
            TaskRoleApproveRecord curTaskRoleApproveRecord = taskRoleApproveRecordExecutor.getByTaskInstanceIdAndRoleIdAndUserId(tenantId, taskInstanceId, roleId, userId);
            if (curTaskRoleApproveRecord == null || RoleApproveStatus.IN_PROGRESS != curTaskRoleApproveRecord.getStatus()) {
                throw new WorkflowException("角色用户审批记录不存在或状态不正确");
            }
            curTaskRoleApproveRecord.setComment(comment);
            curTaskRoleApproveRecord.setStatus(RoleApproveStatus.APPROVED);
            taskRoleApproveRecordExecutor.updateById(curTaskRoleApproveRecord);

            // 审批人数 +1
            taskInstance.setApprovedCount(taskInstance.getApprovedCount() + 1);

            // 获取所有角色审批记录，将剩余的审批记录设置为 SKIPPED
            List<TaskRoleApproveRecord> taskRoleApproveRecords = taskRoleApproveRecordExecutor.findByTaskInstanceId(tenantId, taskInstanceId);
            taskRoleApproveRecords.stream().filter(i -> !curTaskRoleApproveRecord.getId().equals(i.getId())).filter(i -> RoleApproveStatus.IN_PROGRESS == i.getStatus())
                    .forEach(otherTaskRoleApproveRecord -> {
                        otherTaskRoleApproveRecord.setStatus(RoleApproveStatus.SKIPPED);
                        taskRoleApproveRecordExecutor.updateById(otherTaskRoleApproveRecord);
                    });

            // 将当前的审批记录设置为：APPROVED
            taskApprove.convertToApproveStatusApproved(comment);
            taskApproveExecutor.updateById(taskApprove);

            // 获取其他的审批记录设置为：SKIPPED
            List<TaskApprove> taskApproves = taskApproveExecutor.findByTaskInstanceId(tenantId, taskInstanceId);
            taskApproves.stream().filter(i -> !taskApprove.getId().equals(i.getId())).forEach(otherTaskApprove -> {
                otherTaskApprove.convertToApproveStatusSkipped();
                taskApproveExecutor.updateById(otherTaskApprove);
            });

            // 进入下一个节点
            roleApproveGotoNext = true;
        }

        return roleApproveGotoNext;
    }

    /**
     * 根据任务实例 ID 获取已审批的角色记录
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     *
     * @return List<TaskRoleApproveRecord>
     *
     * @author wangweijun
     * @since 2024/9/19 11:09
     */
    private List<TaskRoleApproveRecord> findApprovedTaskRoleApproveRecords(String tenantId, Integer taskInstanceId) {
        TaskRoleApproveRecordExecutor taskRoleApproveRecordExecutor = taskRoleApproveRecordExecutorBuilder.build();
        List<TaskRoleApproveRecord> taskRoleApproveRecords = taskRoleApproveRecordExecutor.findByTaskInstanceId(tenantId, taskInstanceId);
        // 放弃也是一种审核通过
        return taskRoleApproveRecords.stream().filter(i -> RoleApproveStatus.APPROVED == i.getStatus() || RoleApproveStatus.ABANDONED == i.getStatus()).toList();
    }

    /**
     * 根据角色 ID 获取所有角色用户
     *
     * @param tenantId               租户 ID
     * @param nodeDefinitionId       节点定义 ID
     * @param roleId                 角色 ID
     * @param nodeRoleAssignmentType 角色用户类型
     *
     * @return List<NodeRoleAssignment>
     *
     * @author wangweijun
     * @since 2024/9/19 10:56
     */
    private List<NodeRoleAssignment> findNodeRoleAssignmentsByRoleId(String tenantId, Integer nodeDefinitionId, String roleId, NodeRoleAssignmentType nodeRoleAssignmentType) {
        // 获取所有角色审批用户，并按角色分组
        NodeRoleAssignmentExecutor nodeRoleAssignmentExecutor = nodeRoleAssignmentExecutorBuilder.build();
        List<NodeRoleAssignment> nodeRoleAssignments = nodeRoleAssignmentExecutor.findByNodeDefinitionId(tenantId, nodeDefinitionId, nodeRoleAssignmentType);
        Map<String, List<NodeRoleAssignment>> nodeRoleAssignmentsByRoles = nodeRoleAssignments.stream().collect(Collectors.groupingBy(NodeRoleAssignment::getRoleId));
        return nodeRoleAssignmentsByRoles.get(roleId);
    }

    /**
     * 获取所有角色用户
     *
     * @param tenantId         租户 ID
     * @param nodeDefinitionId 节点定义 ID
     *
     * @return List<NodeRoleAssignment>
     *
     * @author wangweijun
     * @since 2024/9/19 10:56
     */
    private List<NodeRoleAssignment> findNodeRoleAssignments(String tenantId, Integer nodeDefinitionId) {
        // 获取所有角色审批用户
        NodeRoleAssignmentExecutor nodeRoleAssignmentExecutor = nodeRoleAssignmentExecutorBuilder.build();
        return new ArrayList<>(nodeRoleAssignmentExecutor.findByNodeDefinitionId(tenantId, nodeDefinitionId, NodeRoleAssignmentType.NORMAL));
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
    @Override
    public void redo(String tenantId, Integer taskInstanceId, String roleId, String userId, String comment) {
        JdbcTemplateHelper jdbcTemplateHelper = this.context.getJdbcTemplateHelper();
        jdbcTemplateHelper.executeInTransaction(() -> {

            WorkflowDefinitionExecutor workflowDefinitionExecutor = workflowDefinitionExecutorBuilder.build();
            WorkflowDefinition workflowDefinition = workflowDefinitionExecutor.getByTaskInstanceId(taskInstanceId);
            // 检查是否允许撤回
            if (!workflowDefinition.isAllowRedo()) {
                throw new WorkflowException("流程定义不允许撤回");
            }

            // 当前审批节点
            TaskInstanceExecutor taskInstanceExecutor = taskInstanceExecutorBuilder.build();
            TaskInstance currTaskInstance = taskInstanceExecutor.getById(taskInstanceId);

            // 审批撤回监听器
            AgileRedoListener agileRedoListener = context.getAgileRedoListener();
            // 撤回之前调用
            String newComment = agileRedoListener.preRedo(currTaskInstance, roleId, userId, comment);
            if (newComment == null) {
                newComment = comment;
            }

            // 检查审批意见是否必填
            checkIfRequiredComment(taskInstanceId, newComment);

            // 判断流程实例是否已完成
            WorkflowInstanceExecutor workflowInstanceExecutor = workflowInstanceExecutorBuilder.build();
            Integer workflowInstanceId = currTaskInstance.getWorkflowInstanceId();
            WorkflowInstance workflowInstance = workflowInstanceExecutor.getById(workflowInstanceId);
            if (!Objects.equals(WorkflowStatus.IN_PROGRESS, workflowInstance.getStatus())) {
                throw new WorkflowException("流程实例已经完成审批，无法撤回");
            }

            // 获取当前实例的节点定义
            NodeDefinitionExecutor nodeDefinitionExecutor = nodeDefinitionExecutorBuilder.build();

            // 判断当前审批节点是否是多人审批模式
            TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder.build();
            List<TaskApprove> currTaskApproves = taskApproveExecutor.findByTaskInstanceId(tenantId, taskInstanceId);

            // 获取当前节点定义
            NodeDefinition nodeDefinition = nodeDefinitionExecutor.getById(currTaskInstance.getNodeDefinitionId());

            // 拷贝一份，因为原 currTaskApproves 是不可变的集合
            currTaskApproves = new ArrayList<>(currTaskApproves);

            // 判断是否是角色审批，如果是角色审批，需要将 approverId 替换为 roleId
            String approverRoleId = null;
            // 角色审批
            if (nodeDefinition.isRoleApprove()) {
                // 过滤掉当前审批人的审批节点
                TaskApprove currTaskApprove = null;
                for (TaskApprove taskApprove : currTaskApproves) {
                    if (roleId.equals(taskApprove.getApproverId())) {
                        currTaskApprove = taskApprove;
                        currTaskApproves.remove(taskApprove);
                        approverRoleId = roleId;
                        break;
                    }
                }

                if (currTaskApprove == null) {
                    throw new WorkflowException("未查询到相关审批任务实例");
                }
                this.processRedoRoleApprove(tenantId, nodeDefinition, currTaskInstance, currTaskApprove, roleId, userId, newComment);
            }
            // 非角色审批
            else {
                // 过滤掉当前审批人的审批节点
                currTaskApproves.removeIf(currTaskApprove -> userId.equals(currTaskApprove.getApproverId()));
            }

            if (!currTaskApproves.isEmpty()) {
                for (TaskApprove currTaskApprove : currTaskApproves) {
                    // 如果存在其他节点完成审批（非进行中状态），则无法撤回
                    if (ApproveStatus.IN_PROGRESS != currTaskApprove.getStatus() && ActiveStatus.ACTIVE == currTaskApprove.getActive()) {
                        throw new WorkflowException("已存在其他节点完成审批，无法撤回");
                    }
                }

                // 判断是否需要还原其他审批节点
                ApproveType approveType = nodeDefinition.getApproveType();
                RoleApproveType roleApproveType = nodeDefinition.getRoleApproveType();

                // 节点属于：用户审批 且 用户审批类型为 ANY  或属于：角色审批 且 角色审批类型为 ANY
                if ((nodeDefinition.isUserApprove() && ApproveType.ANY == approveType) || (nodeDefinition.isRoleApprove() && RoleApproveType.ANY == roleApproveType)) {
                    currTaskApproves.forEach(taskApprove -> {
                        taskApprove.convertToApproveStatusInProgress();
                        taskApproveExecutor.updateById(taskApprove);
                    });
                }
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

                        // 判断是否是动态审批节点
                        NodeDefinition nextNodeDefinition = nodeDefinitionExecutor.getById(nextTaskInstance.getNodeDefinitionId());
                        // 如果是动态审批节点，则更新为未设置的动态审批人
                        if (nextNodeDefinition.isDynamic()) {
                            NodeAssignmentExecutor nodeAssignmentExecutor = nodeAssignmentExecutorBuilder.build();
                            List<NodeAssignment> dynamicNodeAssignments = nodeAssignmentExecutor.findByNodeDefinitionId(tenantId, nextNodeDefinition.getId());
                            for (NodeAssignment dynamicNodeAssignment : dynamicNodeAssignments) {
                                Integer dynamicApproverNum = dynamicNodeAssignment.getDynamicApproverNum();
                                if (dynamicApproverNum == null) {
                                    throw new WorkflowException("动态审批人数量解析错误: {}", dynamicNodeAssignment.getApproverId());
                                }
                                dynamicNodeAssignment.setApproverId(WorkflowConstants.DYNAMIC_ASSIGNMENT_APPROVER_VALUE.formatted(dynamicApproverNum));
                                nodeAssignmentExecutor.updateById(dynamicNodeAssignment);
                            }
                        }

                        // 删除角色审批记录
                        if (nextTaskInstance.isRoleApprove()) {
                            TaskRoleApproveRecordExecutor taskRoleApproveRecordExecutor = taskRoleApproveRecordExecutorBuilder.build();
                            taskRoleApproveRecordExecutor.deleteByTaskInstanceId(tenantId, workflowInstanceId, nextTaskInstance.getId());
                        }

                        // 删除下一个审批节点
                        taskInstanceExecutor.deleteById(nextTaskInstance.getId());
                    }
                }
            }

            // 更新当前 TaskApprove 的 status
            query = QueryBuilderWrapper.createLambda(TaskApprove.class)
                    .eq(TaskApprove::getTenantId, tenantId)
                    .eq(TaskApprove::getTaskInstanceId, taskInstanceId)
                    .eq(TaskApprove::getApproverId, nodeDefinition.isRoleApprove() ? approverRoleId : userId)
                    .eq(TaskApprove::getState, 1).build();
            TaskApprove currTaskApprove = taskApproveExecutor.get(query);
            currTaskApprove.convertToApproveStatusInProgress();
            taskApproveExecutor.updateById(currTaskApprove);

            // 修改当前 TaskInstance 的 approved_count 数量，并将 status 设置为 IN_PROGRESS
            currTaskInstance.setApprovedCount(currTaskInstance.getApprovedCount() - 1);
            currTaskInstance.setStatus(NodeStatus.IN_PROGRESS);
            taskInstanceExecutor.updateById(currTaskInstance);

            // 判断是否是第一个审批节点，是否需要将流程实例状态修改为：WAITING
            List<NodeDefinition> taskNodes = nodeDefinitionExecutor.findTaskNodes(tenantId, workflowDefinition.getId());
            NodeDefinition firstNode = taskNodes.stream().min(Comparator.comparingDouble(NodeDefinition::getSequence)).orElseThrow(() -> new WorkflowException("节点定义不存在"));
            if (Objects.equals(firstNode.getId(), nodeDefinition.getId()) && currTaskInstance.getApprovedCount() == 0) {
                workflowInstance.setStatus(WorkflowStatus.WAITING);
                workflowInstanceExecutor.updateById(workflowInstance);
            }

            // 撤回之后调用
            agileRedoListener.postRedid(currTaskInstance, roleId, userId, comment);

            // 记录流程日志（审批撤回）
            recordLogs(tenantId, workflowInstanceId, taskInstanceId, nodeDefinition.getName(), TaskHistoryMessage.INSTANCE_REDO);
        });
    }

    /**
     * 处理撤回-角色审批的情况
     *
     * @param tenantId       租户 ID
     * @param nodeDefinition 节点定义
     * @param taskInstance   任务实例
     * @param taskApprove    任务审批
     * @param roleId         角色 ID
     * @param userId         用户 ID
     * @param comment        审批意见
     *
     * @author wangweijun
     * @since 2024/9/20 09:45
     */
    private void processRedoRoleApprove(String tenantId, NodeDefinition nodeDefinition, TaskInstance taskInstance, TaskApprove taskApprove, String roleId, String userId, String comment) {
        // 获取当前任务实例 ID
        Integer taskInstanceId = taskInstance.getId();
        // 获取当前节点定义 ID
        Integer nodeDefinitionId = nodeDefinition.getId();

        // 获取角色审批类型
        RoleApproveType roleApproveType = nodeDefinition.getRoleApproveType();
        // 获取角色用户审批类型
        RoleUserApproveType roleUserApproveType = nodeDefinition.getRoleUserApproveType();

        NodeRoleAssignmentExecutor nodeRoleAssignmentExecutor = nodeRoleAssignmentExecutorBuilder.build();
        NodeRoleAssignment nodeRoleAssignment = nodeRoleAssignmentExecutor.getByNodeDefinitionIdAndRoleIdAndApproverId(tenantId, nodeDefinitionId, roleId, userId, null);
        if (nodeRoleAssignment == null) {
            throw new WorkflowException("未查询到相关角色用户，请确认是否审批人是否正确");
        }

        // 获取所有的角色审批人
        List<NodeRoleAssignment> allNodeRoleAssignments = nodeRoleAssignmentExecutor.findByNodeDefinitionId(tenantId, nodeDefinitionId, NodeRoleAssignmentType.NORMAL);
        // 按角色 ID 分组
        Map<String, List<NodeRoleAssignment>> nodeRoleAssignmentMap = allNodeRoleAssignments.stream().collect(Collectors.groupingBy(NodeRoleAssignment::getRoleId));
        List<NodeRoleAssignment> nodeRoleAssignmentsByRole = nodeRoleAssignmentMap.get(roleId);


        TaskRoleApproveRecordExecutor taskRoleApproveRecordExecutor = taskRoleApproveRecordExecutorBuilder.build();
        // 获取当前角色审批记录
        List<TaskRoleApproveRecord> taskRoleApproveRecordsByRole = taskRoleApproveRecordExecutor.findByTaskApproveId(tenantId, taskApprove.getId());
        // 获取所有角色审批记录
        List<TaskRoleApproveRecord> allTaskInstanceApproveRecords = taskRoleApproveRecordExecutor.findByTaskInstanceId(tenantId, taskInstanceId);

        // 获取当前任务审批记录
        TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder.build();
        List<TaskApprove> taskApproves = taskApproveExecutor.findByTaskInstanceId(tenantId, taskInstanceId);

        // 角色审核记录
        TaskRoleApproveRecord taskRoleApproveRecord = null;
        // 判断撤回条件是否满足：角色审批为 ALL 或 SEQ，角色用户审批为 ALL
        if (RoleApproveType.ANY != roleApproveType && RoleUserApproveType.ALL == roleUserApproveType) {
            // 获取当前角色的审批用户
            taskRoleApproveRecord = taskRoleApproveRecordsByRole.stream()
                    .filter(i -> ApproveStatus.APPROVED.equals(i.getStatus()) || ApproveStatus.ABANDONED.equals(i.getStatus()))
                    .filter(i -> {
                        Integer nodeRoleAssignmentId = i.getNodeRoleAssignmentId();
                        NodeRoleAssignment nra = nodeRoleAssignmentExecutor.getById(nodeRoleAssignmentId);
                        return Objects.equals(nra.getRoleId(), roleId) && Objects.equals(nra.getUserId(), userId);
                    }).findAny().orElse(null);
        }
        // 判断撤回条件是否满足：角色审批为 ANY、ALL 或 SEQ，角色用户审批为 SEQ
        else if (RoleUserApproveType.SEQ == roleUserApproveType) {
            // 获取当前角色的审批用户
            List<NodeRoleAssignment> nodeRoleAssignments = new ArrayList<>();
            for (TaskRoleApproveRecord roleApproveRecord : taskRoleApproveRecordsByRole) {
                if (RoleApproveStatus.APPROVED == roleApproveRecord.getStatus() || RoleApproveStatus.ABANDONED == roleApproveRecord.getStatus()) {
                    Integer nodeRoleAssignmentId = roleApproveRecord.getNodeRoleAssignmentId();
                    nodeRoleAssignments.add(nodeRoleAssignmentExecutor.getById(nodeRoleAssignmentId));
                }
            }

            // 按 userSeq 倒序，并获取第一个审批用户
            nodeRoleAssignments.sort(Comparator.comparingInt(NodeRoleAssignment::getUserSeq).reversed());
            NodeRoleAssignment curNodeRoleAssignment = nodeRoleAssignments.get(0);

            // 找到已经完成审批，且和当前用户一致的记录
            taskRoleApproveRecord = taskRoleApproveRecordsByRole.stream()
                    .filter(i -> RoleApproveStatus.APPROVED == i.getStatus() || RoleApproveStatus.ABANDONED == i.getStatus())
                    .filter(i -> {
                        Integer nodeRoleAssignmentId = i.getNodeRoleAssignmentId();
                        return nodeRoleAssignmentId.equals(curNodeRoleAssignment.getId());
                    }).findAny().orElse(null);
        }
        // 判断撤回条件是否满足：其他情况
        else {
            // 获取审批状态为审批通过，且创建时间最近的一条记录
            taskRoleApproveRecord = taskRoleApproveRecordsByRole.stream()
                    .filter(i -> RoleApproveStatus.APPROVED == i.getStatus() || RoleApproveStatus.ABANDONED == i.getStatus())
                    .max(Comparator.comparing(TaskRoleApproveRecord::getUpdatedAt).thenComparing(TaskRoleApproveRecord::getCreatedAt))
                    .orElseThrow(() -> new WorkflowException("未查询到相关审批记录"));
        }

        if (taskRoleApproveRecord == null) {
            throw new WorkflowException("未查询到相关角色审批记录，请确认是否审批人是否正确");
        }

        Integer nodeRoleAssignmentId = taskRoleApproveRecord.getNodeRoleAssignmentId();
        nodeRoleAssignment = nodeRoleAssignmentExecutor.getById(nodeRoleAssignmentId);
        if (!roleId.equals(nodeRoleAssignment.getRoleId()) || !userId.equals(nodeRoleAssignment.getUserId())) {
            throw new WorkflowException("当前审批实例非当前角色审批人创建，无法撤回，请核实");
        }

        // 角色审批为 ANY
        if (RoleApproveType.ANY == roleApproveType) {
            // 角色审批为 ANY，角色用户审批为 ANY
            if (RoleUserApproveType.ANY == roleUserApproveType) {
                // 将所有的角色审批记录都设置为：IN_PROGRESS
                for (TaskRoleApproveRecord approveRecord : allTaskInstanceApproveRecords) {
                    // 记录撤回的审批意见
                    if (RoleApproveStatus.APPROVED == approveRecord.getStatus()) {
                        approveRecord.setComment(comment);
                    }
                    // 更新为：IN_PROGRESS
                    approveRecord.setStatus(RoleApproveStatus.IN_PROGRESS);
                    taskRoleApproveRecordExecutor.updateById(approveRecord);
                }
            }
            // 角色审批为 ANY，角色用户审批为 ALL
            else if (RoleUserApproveType.ALL == roleUserApproveType) {
                // 获取当前角色下已经审批的记录
                List<TaskRoleApproveRecord> approvedRecords = taskRoleApproveRecordsByRole.stream()
                        .filter(i -> RoleApproveStatus.APPROVED == i.getStatus() || RoleApproveStatus.ABANDONED == i.getStatus()).toList();

                // 找到当前的审批记录，并设置为：IN_PROGRESS
                TaskRoleApproveRecord curTaskRoleApproveRecord = taskRoleApproveRecordExecutor.getByTaskInstanceIdAndRoleIdAndUserId(tenantId, taskInstanceId, roleId, userId);
                curTaskRoleApproveRecord.convertToApproveStatusInProgress(comment);
                taskRoleApproveRecordExecutor.updateById(curTaskRoleApproveRecord);

                // 该角色已经全部完成了审批
                if (approvedRecords.size() == nodeRoleAssignmentsByRole.size()) {
                    // 获取其他角色的审批记录
                    List<TaskRoleApproveRecord> otherRoleTaskRoleApproveRecords = allTaskInstanceApproveRecords.stream().filter(i -> !i.getTaskApproveId().equals(taskApprove.getId())).toList();
                    for (TaskRoleApproveRecord otherRoleTaskRoleApproveRecord : otherRoleTaskRoleApproveRecords) {
                        // 将状态为 SKIPPED 的记录设置为：IN_PROGRESS
                        if (RoleApproveStatus.SKIPPED == otherRoleTaskRoleApproveRecord.getStatus()) {
                            otherRoleTaskRoleApproveRecord.setStatus(RoleApproveStatus.IN_PROGRESS);
                            taskRoleApproveRecordExecutor.updateById(otherRoleTaskRoleApproveRecord);
                        }
                    }
                }

            }
            // 角色审批为 ANY，角色用户审批为 SEQ
            else if (RoleUserApproveType.SEQ == roleUserApproveType) {
                // 找到当前的审批记录，并设置为：IN_PROGRESS
                TaskRoleApproveRecord curTaskRoleApproveRecord = taskRoleApproveRecordExecutor.getByTaskInstanceIdAndRoleIdAndUserId(tenantId, taskInstanceId, roleId, userId);
                curTaskRoleApproveRecord.setComment(comment);
                curTaskRoleApproveRecord.setStatus(RoleApproveStatus.IN_PROGRESS);
                taskRoleApproveRecordExecutor.updateById(curTaskRoleApproveRecord);

                // 获取该角色的最后一个角色审批人
                nodeRoleAssignmentsByRole.sort(Comparator.comparingInt(NodeRoleAssignment::getUserSeq).reversed());
                NodeRoleAssignment lastNodeRoleAssignmentByRole = nodeRoleAssignmentsByRole.get(0);

                // 获取当前角色审批人
                Integer curNodeRoleAssignmentId = curTaskRoleApproveRecord.getNodeRoleAssignmentId();
                NodeRoleAssignment curNodeRoleAssignment = nodeRoleAssignmentExecutor.getById(curNodeRoleAssignmentId);

                // 撤回逻辑：是最后一个审批人
                if (curNodeRoleAssignmentId.equals(lastNodeRoleAssignmentByRole.getId())) {
                    // 从 nodeRoleAssignmentMap 过滤掉当前的角色
                    Map<String, List<NodeRoleAssignment>> otherNodeRoleAssignmentMap = nodeRoleAssignmentMap.entrySet().stream()
                            .filter(i -> !roleId.equals(i.getKey()))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                    for (Map.Entry<String, List<NodeRoleAssignment>> entry : otherNodeRoleAssignmentMap.entrySet()) {
                        // 角色 ID
                        String otherRoleId = entry.getKey();
                        // 角色 ID 对应的用户
                        List<NodeRoleAssignment> otherNodeRoleAssignments = entry.getValue();
                        // 对 otherNodeRoleAssignments 按 userSeq 进行升序排序
                        otherNodeRoleAssignments.sort(Comparator.comparingInt(NodeRoleAssignment::getUserSeq));
                        // 获取第一个审批用户
                        NodeRoleAssignment firstNodeRoleAssignment = otherNodeRoleAssignments.get(0);
                        // 其他角色的审批记录
                        List<TaskRoleApproveRecord> otherTaskRoleApproveRecords = taskRoleApproveRecordExecutor.findByTaskInstanceIdAndRoleId(tenantId, taskInstanceId, otherRoleId);
                        // 获取第一个审批记录
                        Optional<TaskRoleApproveRecord> firstApproveRecordOptional = otherTaskRoleApproveRecords.stream().filter(i -> i.getNodeRoleAssignmentId().equals(firstNodeRoleAssignment.getId())).findFirst();
                        // 存在第一个审批记录
                        if (firstApproveRecordOptional.isPresent()) {
                            TaskRoleApproveRecord firstApproveRecord = firstApproveRecordOptional.get();
                            // 第一个审批节点是：SKIPPED，则修改为：IN_PROGRESS，剩下的节点全部设置为：SUSPEND
                            if (RoleApproveStatus.SKIPPED == firstApproveRecord.getStatus()) {
                                firstApproveRecord.setStatus(RoleApproveStatus.IN_PROGRESS);
                                taskRoleApproveRecordExecutor.updateById(firstApproveRecord);
                                // 剩下的节点全部设置为：SUSPEND
                                otherTaskRoleApproveRecords.stream().filter(i -> !i.getNodeRoleAssignmentId().equals(firstNodeRoleAssignment.getId())).forEach(i -> {
                                    i.setStatus(RoleApproveStatus.SUSPEND);
                                    taskRoleApproveRecordExecutor.updateById(i);
                                });
                            }
                            // 第一个审批节点是：APPROVED，则将下一个 SKIPPED 状态的审批节点设置为：IN_PROGRESS，剩下的设置为：SUSPEND
                            else if (RoleApproveStatus.APPROVED == firstApproveRecord.getStatus() && otherNodeRoleAssignments.size() > 1) {
                                // 下一个 SKIPPED 状态的审批节点设置为：IN_PROGRESS
                                Optional<TaskRoleApproveRecord> firstSkippedOptional = otherTaskRoleApproveRecords.stream().filter(i -> RoleApproveStatus.SKIPPED == i.getStatus()).findFirst();
                                if (firstSkippedOptional.isPresent()) {
                                    // 下一个 SKIPPED 状态的审批节点设置为：IN_PROGRESS
                                    TaskRoleApproveRecord firstSkippedApproveRecord = firstSkippedOptional.get();
                                    firstSkippedApproveRecord.setStatus(RoleApproveStatus.IN_PROGRESS);
                                    taskRoleApproveRecordExecutor.updateById(firstSkippedApproveRecord);
                                    // 剩下的设置为：SUSPEND
                                    otherTaskRoleApproveRecords.stream()
                                            // 过滤掉第一个 SKIPPED 状态的节点
                                            .filter(i -> !firstSkippedApproveRecord.getId().equals(i.getId()))
                                            // 获取其他所有 SKIPPED 状态的节点
                                            .filter(i -> RoleApproveStatus.SKIPPED == i.getStatus())
                                            // 剩下的设置为：SUSPEND
                                            .forEach(i -> {
                                                i.setStatus(RoleApproveStatus.SUSPEND);
                                                taskRoleApproveRecordExecutor.updateById(i);
                                            });
                                }
                            }
                        }
                    }

                }
                // 撤回逻辑：不是最后一个审批人
                else {
                    // 获取 userSeq 大于 当前审批人 userSeq 的审批人
                    List<NodeRoleAssignment> others = nodeRoleAssignmentsByRole.stream().filter(i -> i.getUserSeq() > curNodeRoleAssignment.getUserSeq()).toList();
                    // 将其他审批人设置为 SUSPEND
                    others.forEach(other -> taskRoleApproveRecordsByRole.stream().filter(i -> i.getNodeRoleAssignmentId().equals(other.getId()) && RoleApproveStatus.SUSPEND != i.getStatus())
                            .findFirst()
                            .ifPresent(approveRecord -> {
                                approveRecord.setStatus(RoleApproveStatus.SUSPEND);
                                taskRoleApproveRecordExecutor.updateById(approveRecord);
                            }));
                }

                // 将所有 SKIPPED 状态的审批记录修改为：SUSPEND
                List<TaskRoleApproveRecord> skippedTaskRoleApproveRecords = taskRoleApproveRecordExecutor.findByTaskInstanceIdAndStatus(tenantId, taskInstanceId, RoleApproveStatus.SKIPPED);
                for (TaskRoleApproveRecord skippedTaskRoleApproveRecord : skippedTaskRoleApproveRecords) {
                    skippedTaskRoleApproveRecord.setStatus(RoleApproveStatus.IN_PROGRESS);
                    taskRoleApproveRecordExecutor.updateById(skippedTaskRoleApproveRecord);
                }
            }
        }
        // 角色审批类型为：ALL
        else if (RoleApproveType.ALL == roleApproveType) {
            // 角色用户审批类型为：ANY
            if (RoleUserApproveType.ANY == roleUserApproveType) {
                // 获取相同任务实例 ID 和相同角色 ID 的角色审批记录
                List<TaskRoleApproveRecord> sameTaskInstanceApproveRecords = taskRoleApproveRecordExecutor.findByTaskInstanceIdAndRoleId(tenantId, taskInstanceId, roleId);
                // 更新相同任务实例 ID 和相同角色 ID 的角色审批记录为 IN_PROGRESS
                sameTaskInstanceApproveRecords.forEach(sameTaskInstanceApproveRecord -> {
                    sameTaskInstanceApproveRecord.convertToApproveStatusInProgress();
                    taskRoleApproveRecordExecutor.updateById(sameTaskInstanceApproveRecord);
                });
            }
            // 角色用户审批类型为：ALL
            else if (RoleUserApproveType.ALL == roleUserApproveType) {
                // 更新当前审批记录为 IN_PROGRESS
                taskRoleApproveRecord.convertToApproveStatusInProgress(comment);
                taskRoleApproveRecordExecutor.updateById(taskRoleApproveRecord);

                // 如果当前任务审批状态为 APPROVED，则将当前任务审批状态设置为 IN_PROGRESS
                if (ApproveStatus.APPROVED == taskApprove.getStatus()) {
                    taskApprove.convertToApproveStatusInProgress(comment);
                    taskApproveExecutor.updateById(taskApprove);
                }
            }
        }
        // 角色审批类型为：SEQ
        else if (RoleApproveType.SEQ == roleApproveType) {
            // 角色用户审批类型为：ANY
            if (RoleUserApproveType.ANY == roleUserApproveType) {
                // 判断是否是跨序撤回，即：roleSeq=2 的角色已经完成了审核，此时 roleSeq=1 的角色来进行撤回，这种情况是不允许的
                Set<String> approvedRoleIds = taskApproves.stream().filter(i -> ApproveStatus.APPROVED == i.getStatus())
                        .flatMap(i -> Stream.of(i.getApproverId())).collect(Collectors.toSet());
                // 查找已经审核的 roleSeq 最大的 roleId，此时表示只有最大的 roleId 的角色用户才可以进行审批撤回
                allNodeRoleAssignments.stream().filter(i -> approvedRoleIds.contains(i.getRoleId())).max(Comparator.comparingInt(NodeRoleAssignment::getRoleSeq))
                        .flatMap(i -> Optional.of(i.getRoleId())).ifPresent(maxRoleIdByRoleSeq -> {
                            if (!roleId.equals(maxRoleIdByRoleSeq)) {
                                throw new WorkflowException("下个审批流程已经完成审核，无法撤回");
                            }
                        });

                // 当前角色下，所有的角色审批用户，均设置为：IN_PROGRESS
                for (TaskRoleApproveRecord approveRecord : taskRoleApproveRecordsByRole) {
                    // 审批状态为：APPROVED 的角色用户设置为：IN_PROGRESS
                    if (RoleApproveStatus.APPROVED == approveRecord.getStatus()) {
                        approveRecord.convertToApproveStatusInProgress(comment);
                    }
                    // 其他状态为：SKIPPED 的角色用户设置为：IN_PROGRESS
                    else {
                        approveRecord.convertToApproveStatusInProgress();
                    }
                    taskRoleApproveRecordExecutor.updateById(approveRecord);
                }

                // 找到 roleSeq 大于当前角色的 roleSeq 的角色中 roleSeq 最小的角色对应的审批记录
                Integer curRoleSeq = nodeRoleAssignment.getRoleSeq();
                List<TaskRoleApproveRecord> nextTaskRoleApproveRecords = allNodeRoleAssignments.stream().filter(i -> i.getRoleSeq() > curRoleSeq)
                        .min(Comparator.comparingInt(NodeRoleAssignment::getRoleSeq)).flatMap(i -> {
                            List<TaskRoleApproveRecord> taskRoleApproveRecords = taskRoleApproveRecordExecutor.findByTaskInstanceIdAndRoleId(tenantId, taskInstanceId, i.getRoleId());
                            return Optional.of(taskRoleApproveRecords);
                        }).orElse(null);
                // 将所有对应的角色审批记录设置为：SUSPEND
                if (CollectionUtils.isNotEmpty(nextTaskRoleApproveRecords)) {
                    nextTaskRoleApproveRecords.forEach(nextTaskRoleApproveRecord -> {
                        nextTaskRoleApproveRecord.convertToApproveStatusSuspended();
                        taskRoleApproveRecordExecutor.updateById(nextTaskRoleApproveRecord);
                    });
                }

                // 将当前任务审批状态设置为：IN_PROGRESS
                taskApprove.convertToApproveStatusInProgress(comment);
                taskApproveExecutor.updateById(taskApprove);
            }
            // 角色用户审批类型为：ALL
            else if (RoleUserApproveType.ALL == roleUserApproveType) {
                // 更新当前审批记录为：IN_PROGRESS
                taskRoleApproveRecord.convertToApproveStatusInProgress(comment);
                taskRoleApproveRecordExecutor.updateById(taskRoleApproveRecord);

                // 查询该角色下所有已完成审核的审批记录
                List<TaskRoleApproveRecord> approvedRecords = taskRoleApproveRecordsByRole.stream().filter(i -> RoleApproveStatus.APPROVED == i.getStatus() || RoleApproveStatus.ABANDONED == i.getStatus()).toList();

                // 表示此时撤销的是当前角色下最后一个用户，则需要将下一个角色的用户均设置为：SUSPEND
                if (approvedRecords.size() == nodeRoleAssignmentsByRole.size() - 1) {
                    // 找到 roleSeq 大于当前角色的 roleSeq 的角色中 roleSeq 最小的角色的角色审批记录
                    Integer curRoleSeq = nodeRoleAssignment.getRoleSeq();
                    // 将所有对应的角色审批记录设置为：SUSPEND
                    allNodeRoleAssignments.stream().filter(i -> i.getRoleSeq() > curRoleSeq).min(Comparator.comparingInt(NodeRoleAssignment::getRoleSeq))
                            .stream().flatMap(i -> Stream.of(i.getRoleId())).findFirst().ifPresent(nextRoleId -> {
                                // 获取到下个角色的审批记录
                                List<TaskRoleApproveRecord> nextTaskRoleApproveRecords = taskRoleApproveRecordExecutor.findByTaskInstanceIdAndRoleId(tenantId, taskInstanceId, nextRoleId);
                                // 将所有对应的角色审批记录设置为：SUSPEND
                                if (CollectionUtils.isNotEmpty(nextTaskRoleApproveRecords)) {
                                    for (TaskRoleApproveRecord nextTaskRoleApproveRecord : nextTaskRoleApproveRecords) {
                                        if (RoleApproveStatus.APPROVED == nextTaskRoleApproveRecord.getStatus() || RoleApproveStatus.ABANDONED == nextTaskRoleApproveRecord.getStatus()) {
                                            throw new WorkflowException("下个审批流程已经完成审核，无法撤回");
                                        }
                                        nextTaskRoleApproveRecord.convertToApproveStatusSuspended();
                                        taskRoleApproveRecordExecutor.updateById(nextTaskRoleApproveRecord);
                                    }
                                }
                            });
                }

                // 将当前任务审批状态设置为：IN_PROGRESS
                taskApprove.convertToApproveStatusInProgress(comment);
                taskApproveExecutor.updateById(taskApprove);
            }
        }


        // 角色用户审批类型为：SEQ
        if (RoleUserApproveType.SEQ == roleUserApproveType) {
            // 更新当前审批记录为 IN_PROGRESS
            taskRoleApproveRecord.convertToApproveStatusInProgress(comment);
            taskRoleApproveRecordExecutor.updateById(taskRoleApproveRecord);

            // 通过当前角色的审批用户，过滤掉当前用户，并找到相同角色下 userSeq 大于当前用户的审批记录
            NodeRoleAssignment curNodeRoleAssignment = nodeRoleAssignment;
            List<NodeRoleAssignment> otherNodeRoleAssignments = nodeRoleAssignmentsByRole.stream()
                    .filter(i -> !Objects.equals(i.getId(), curNodeRoleAssignment.getId()))
                    .filter(i -> Objects.equals(i.getRoleId(), curNodeRoleAssignment.getRoleId()))
                    .filter(i -> i.getUserSeq() > curNodeRoleAssignment.getUserSeq())
                    .toList();

            // 将相同角色下 userSeq 大于当前用户的审批记录更新为：SUSPEND
            for (NodeRoleAssignment otherNodeRoleAssignment : otherNodeRoleAssignments) {
                taskRoleApproveRecordsByRole.stream()
                        .filter(i -> Objects.equals(i.getNodeRoleAssignmentId(), otherNodeRoleAssignment.getId()))
                        .findAny()
                        .ifPresent(otherTaskRoleApproveRecord -> {
                            otherTaskRoleApproveRecord.setStatus(RoleApproveStatus.SUSPEND);
                            taskRoleApproveRecordExecutor.updateById(otherTaskRoleApproveRecord);
                        });
            }

            // 角色审批类型为：ALL 或 SEQ
            if (RoleApproveType.ALL == roleApproveType || RoleApproveType.SEQ == roleApproveType) {
                // 当前实例下所有的角色审批记录
                List<TaskRoleApproveRecord> sameTaskInstanceApproveRecords = taskRoleApproveRecordExecutor.findByTaskInstanceId(tenantId, taskInstanceId);
                // 过滤掉当前的审批记录，并将 IN_PROGRESS 的记录更新为 SUSPEND
                sameTaskInstanceApproveRecords.stream()
                        .filter(i -> !Objects.equals(i.getTaskApproveId(), taskApprove.getId()))
                        .filter(i -> RoleApproveStatus.IN_PROGRESS == i.getStatus())
                        .findFirst()
                        .ifPresent(ameTaskInstanceApproveRecord -> {
                            ameTaskInstanceApproveRecord.setStatus(RoleApproveStatus.SUSPEND);
                            taskRoleApproveRecordExecutor.updateById(ameTaskInstanceApproveRecord);
                        });
            }

        }
    }

    /**
     * 驳回
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     * @param roleId         角色 ID
     * @param userId         用户 ID
     * @param comment        审批意见
     */
    @Override
    public void reject(String tenantId, Integer taskInstanceId, String roleId, String userId, String comment) {
        JdbcTemplateHelper jdbcTemplateHelper = this.context.getJdbcTemplateHelper();
        jdbcTemplateHelper.executeInTransaction(() -> {

            // 任务实例
            TaskInstanceExecutor taskInstanceExecutor = taskInstanceExecutorBuilder.build();
            TaskInstance taskInstance = taskInstanceExecutor.getById(taskInstanceId);

            // 实例任务审批记录
            TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder.build();
            TaskApprove taskApprove = taskApproveExecutor.getByTaskInstanceIdAndRoleIdAndApproverId(tenantId, taskInstanceId, ActiveStatus.ACTIVE, roleId, userId);

            // 审批拒绝回调器
            AgileRejectListener agileRejectListener = context.getAgileRejectListener();
            // 审批拒绝之前进行回调
            String newComment = agileRejectListener.preReject(taskInstance, taskApprove, roleId, userId, comment);
            if (newComment == null) {
                newComment = comment;
            }

            // 检查审批意见是否必填
            checkIfRequiredComment(taskInstanceId, newComment);

            // 更新 TaskApprove 的 comment，并将状态设置为：已驳回
            taskApprove.setActive(ActiveStatus.INACTIVE);
            taskApprove.setStatus(ApproveStatus.REJECTED);
            taskApprove.setComment(newComment);
            taskApproveExecutor.updateById(taskApprove);

            // 找到所有未进行审批的审批人节点，并将其状态设置为：已失效
            List<TaskApprove> activeTaskApproves = taskApproveExecutor.findByTaskInstanceId(tenantId, taskInstanceId, ActiveStatus.ACTIVE);
            activeTaskApproves.forEach(activeTaskApprove -> {
                activeTaskApprove.convertToApproveStatusSkipped();
                taskApproveExecutor.updateById(activeTaskApprove);
            });

            // 修改当前 TaskInstance 的 approved_count 数量，并将 status 更新为：已驳回
            taskInstance.setApprovedCount(taskInstance.getApprovedCount() + 1);
            taskInstance.setStatus(NodeStatus.REJECTED);
            taskInstanceExecutor.updateById(taskInstance);

            // 获取当前实例的节点定义
            NodeDefinitionExecutor nodeDefinitionExecutor = nodeDefinitionExecutorBuilder.build();
            NodeDefinition nodeDefinition = nodeDefinitionExecutor.getById(taskInstance.getNodeDefinitionId());

            // 判断是否属于角色审批
            if (nodeDefinition.isRoleApprove()) {
                this.processRejectRoleApprove(tenantId, nodeDefinition, taskInstance, taskApprove, roleId, userId, newComment);
            }

            // 流程定义
            Integer workflowInstanceId = taskInstance.getWorkflowInstanceId();
            WorkflowInstanceExecutor workflowInstanceExecutor = workflowInstanceExecutorBuilder.build();
            WorkflowInstance workflowInstance = workflowInstanceExecutor.getById(workflowInstanceId);

            // 找到结束节点定义
            NodeDefinition endNodeDefinition = nodeDefinitionExecutor.getEndNode(tenantId, workflowInstance.getWorkflowDefinitionId());

            // 创建结束节点实例，将状态设置为：已驳回
            taskInstanceExecutor = taskInstanceExecutorBuilder.tenantId(tenantId)
                    .workflowInstanceId(workflowInstanceId).nodeDefinitionId(endNodeDefinition.getId())
                    .roleApprove(endNodeDefinition.isRoleApprove())
                    .status(NodeStatus.REJECTED).build();
            taskInstanceExecutor.save();

            // 更新工作流实例，将状态设置为：已驳回
            workflowInstance.setStatus(WorkflowStatus.REJECTED);
            workflowInstanceExecutor.updateById(workflowInstance);

            // 保存流程定义
            this.saveWorkflowSchema(tenantId, workflowInstance);
            // 保存流程实例审批记录
            this.saveWorkflowInstanceApproveRecords(tenantId, workflowInstance);

            // 审批拒绝之后进行回调
            agileRejectListener.postRejected(taskInstance, taskApprove, roleId, userId, newComment);

            // 记录流程日志（审批驳回）
            recordLogs(tenantId, taskInstance.getWorkflowInstanceId(), taskInstanceId, nodeDefinition.getName(), TaskHistoryMessage.INSTANCE_REJECTED);
            // 记录流程日志（审批结束）
            recordLogs(tenantId, taskInstance.getWorkflowInstanceId(), null, endNodeDefinition.getName(), TaskHistoryMessage.INSTANCE_ENDED);
        });
    }

    /**
     * 处理拒绝-角色审批的情况
     *
     * @param tenantId       租户 ID
     * @param nodeDefinition 节点定义
     * @param taskInstance   任务实例
     * @param taskApprove    任务审批
     * @param roleId         角色 ID
     * @param userId         用户 ID
     * @param comment        审批意见
     *
     * @author wangweijun
     * @since 2024/9/20 14:35
     */
    private void processRejectRoleApprove(String tenantId, NodeDefinition nodeDefinition, TaskInstance taskInstance, TaskApprove taskApprove, String roleId, String userId, String comment) {
        // 任务实例 ID
        Integer taskInstanceId = taskInstance.getId();

        // 获取当前角色审批记录
        TaskRoleApproveRecordExecutor taskRoleApproveRecordExecutor = taskRoleApproveRecordExecutorBuilder.build();
        TaskRoleApproveRecord curTaskRoleApproveRecord = taskRoleApproveRecordExecutor.getByTaskInstanceIdAndRoleIdAndUserId(tenantId, taskInstanceId, roleId, userId);

        // 将当前审批记录设置为：REJECTED
        curTaskRoleApproveRecord.convertToApproveStatusRejected(comment);
        taskRoleApproveRecordExecutor.updateById(curTaskRoleApproveRecord);

        // 获取所有审批记录
        List<TaskRoleApproveRecord> allTaskRoleApproveRecords = taskRoleApproveRecordExecutor.findByTaskInstanceId(tenantId, taskInstanceId);

        // 将所有 SUSPEND 和 IN_PROGRESS 状态的记录设置为：SKIPPED
        allTaskRoleApproveRecords.stream()
                // 过滤掉当前记录
                .filter(i -> !i.getId().equals(curTaskRoleApproveRecord.getId()))
                // 获取 SUSPEND 和 IN_PROGRESS 状态的节点
                .filter(i -> RoleApproveStatus.SUSPEND == i.getStatus() || RoleApproveStatus.IN_PROGRESS == i.getStatus())
                // 设置为 SKIPPED
                .forEach(taskRoleApproveRecord -> {
                    taskRoleApproveRecord.setStatus(RoleApproveStatus.SKIPPED);
                    taskRoleApproveRecordExecutor.updateById(taskRoleApproveRecord);
                });

    }

    /**
     * 弃权
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     * @param roleId         角色 ID
     * @param userId         用户 ID
     * @param comment        审批意见
     */
    @Override
    public void abandon(String tenantId, Integer taskInstanceId, String roleId, String userId, String comment) {
        JdbcTemplateHelper jdbcTemplateHelper = this.context.getJdbcTemplateHelper();
        jdbcTemplateHelper.executeInTransaction(() -> {

            TaskInstanceExecutor taskInstanceExecutor = taskInstanceExecutorBuilder.build();
            NodeDefinitionExecutor nodeDefinitionExecutor = nodeDefinitionExecutorBuilder.build();
            TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder.build();
            WorkflowInstanceExecutor workflowInstanceExecutor = workflowInstanceExecutorBuilder.build();

            // 任务实例
            TaskInstance taskInstance = taskInstanceExecutor.getById(taskInstanceId);

            // 审批放弃监听器
            AgileAbandonListener agileAbandonListener = context.getAgileAbandonListener();
            // 放弃之前调用
            String newComment = agileAbandonListener.preAbandon(taskInstance, roleId, userId, comment);
            if (newComment == null) {
                newComment = comment;
            }

            // 检查审批意见是否必填
            checkIfRequiredComment(taskInstanceId, newComment);

            // 流程实例
            WorkflowInstance workflowInstance = workflowInstanceExecutor.getById(taskInstance.getWorkflowInstanceId());
            if (WorkflowStatus.WAITING == workflowInstance.getStatus()) {
                workflowInstance.setStatus(WorkflowStatus.IN_PROGRESS);
                workflowInstanceExecutor.updateById(workflowInstance);
            }

            // 获取节点定义，找到审批类型
            NodeDefinition nodeDefinition = nodeDefinitionExecutor.getById(taskInstance.getNodeDefinitionId());

            // 判断是否是顺序审批
            checkIfApproveSeqStatus(tenantId, taskInstanceId, nodeDefinition.getApproveType());

            // 获取当前审批记录
            TaskApprove taskApprove = taskApproveExecutor.getByTaskInstanceIdAndRoleIdAndApproverId(tenantId, taskInstanceId, ActiveStatus.ACTIVE, roleId, userId);

            // 获取当前实例是否有同意的审批结果
            boolean hasApproved = false;

            // 判断是否属于角色审批
            if (nodeDefinition.isRoleApprove()) {
                this.processAbandonRoleApprove(tenantId, nodeDefinition, taskInstance, taskApprove, roleId, userId, newComment);
            }
            // 非角色审批
            else {
                // 获取当前审批任务，并修改当前审批任务的 approved_count 数量
                taskInstance.setApprovedCount(taskInstance.getApprovedCount() + 1);
                taskInstanceExecutor.updateById(taskInstance);

                // 更新 TaskApprove 的 comment，并将状态设置为：已弃权
                taskApprove.setActive(ActiveStatus.INACTIVE);
                taskApprove.setStatus(ApproveStatus.ABANDONED);
                taskApprove.setComment(newComment);
                taskApproveExecutor.updateById(taskApprove);

                // 如果是会签，则只要有一个同意，则同意
                if (ApproveType.ALL == nodeDefinition.getApproveType()) {
                    List<TaskApprove> taskApproves = taskApproveExecutor.findByTaskInstanceId(tenantId, taskInstanceId, ActiveStatus.ACTIVE);
                    TaskApprove anyApproved = taskApproves.stream().filter(i -> ApproveStatus.APPROVED == i.getStatus()).findAny().orElse(null);
                    if (anyApproved != null) {
                        hasApproved = true;
                    }
                }
            }


            List<TaskInstance> nextTaskInstances = null;
            // 已完成审批的人数 approved_count 等于所有需要审批的人数 total_count，则表示为当前是最后一个审批人
            if (Objects.equals(taskInstance.getApprovedCount(), taskInstance.getTotalCount())) {
                if (hasApproved) {
                    // 进入下一个审批流程
                    nextTaskInstances = this.next(tenantId, taskInstanceId, roleId, userId);
                } else {
                    // 最后一个审批人，无法放弃审批
                    throw new WorkflowException("最后一个审批人无法放弃审批");
                }
            }

            // 放弃审批之后调用
            agileAbandonListener.postAbandoned(taskInstance, roleId, userId, comment);

            // 记录流程日志（审批弃权）
            recordLogs(tenantId, taskInstance.getWorkflowInstanceId(), taskInstanceId, nodeDefinition.getName(), TaskHistoryMessage.INSTANCE_ABANDONED);

            // 没有下一级审批节点，且当前审批节点全部审批完成，则表示流程已经结束
            // nextTaskInstances = null: 表示正常达到结束节点
            // nextTaskInstances = empty: 表示条件节点中，没有符合的审批节点，则结束流程
            if (CollectionUtils.isEmpty(nextTaskInstances) && taskInstance.isCompleted()) {
                // 记录流程日志（审批结束）
                NodeDefinition endNodeDefinition = nodeDefinitionExecutor.getEndNode(tenantId, nodeDefinition.getWorkflowDefinitionId());
                recordLogs(tenantId, taskInstance.getWorkflowInstanceId(), null, endNodeDefinition.getName(), TaskHistoryMessage.INSTANCE_ENDED);
            }
        });
    }

    /**
     * 处理放弃-角色审批的情况
     *
     * @param tenantId       租户 ID
     * @param nodeDefinition 节点定义
     * @param taskInstance   任务实例
     * @param taskApprove    审批记录
     * @param roleId         角色 ID
     * @param userId         用户 ID
     * @param comment        审批意见
     *
     * @author wangweijun
     * @since 2024/9/20 16:22
     */
    private void processAbandonRoleApprove(String tenantId, NodeDefinition nodeDefinition, TaskInstance taskInstance, TaskApprove taskApprove, String roleId, String userId, String comment) {
        // 任务实例 ID
        Integer taskInstanceId = taskInstance.getId();
        // 节点定义 ID
        Integer nodeDefinitionId = nodeDefinition.getId();

        // 角色审批类型
        RoleApproveType roleApproveType = nodeDefinition.getRoleApproveType();
        // 角色用户审批类型
        RoleUserApproveType roleUserApproveType = nodeDefinition.getRoleUserApproveType();

        if (RoleApproveType.ANY == roleApproveType && RoleUserApproveType.ANY == roleUserApproveType) {
            throw new WorkflowException("当前审批模式，无法放弃审批");
        }

        // 获取所有角色用户
        NodeRoleAssignmentExecutor nodeRoleAssignmentExecutor = nodeRoleAssignmentExecutorBuilder.build();
        List<NodeRoleAssignment> allNodeRoleAssignments = nodeRoleAssignmentExecutor.findByNodeDefinitionId(tenantId, nodeDefinitionId, NodeRoleAssignmentType.NORMAL);
        // 当前角色用户
        NodeRoleAssignment curNodeRoleAssignment = nodeRoleAssignmentExecutor.getByNodeDefinitionIdAndRoleIdAndApproverId(tenantId, nodeDefinitionId, roleId, userId, null);

        // 角色审批用户按角色分组
        Map<String, List<NodeRoleAssignment>> roleAssignmentsRoleMap = allNodeRoleAssignments.stream().collect(Collectors.groupingBy(NodeRoleAssignment::getRoleId));
        // 获取角色下所有用户
        List<NodeRoleAssignment> nodeRoleAssignmentsByRole = roleAssignmentsRoleMap.get(roleId);

        // 获取所有的角色审批记录
        TaskRoleApproveRecordExecutor taskRoleApproveRecordExecutor = taskRoleApproveRecordExecutorBuilder.build();
        List<TaskRoleApproveRecord> allTaskRoleApproveRecords = taskRoleApproveRecordExecutor.findByTaskInstanceId(tenantId, taskInstanceId);

        // 根据角色获取该角色的审批记录
        List<TaskRoleApproveRecord> taskRoleApproveRecordsByRole = taskRoleApproveRecordExecutor.findByTaskInstanceIdAndRoleId(tenantId, taskInstanceId, roleId);

        // 角色审批类型为：ANY
        if (RoleApproveType.ANY == roleApproveType) {
            // 角色用户审批类型为：ALL
            if (RoleUserApproveType.ALL == roleUserApproveType) {
                // 获取该角色下已经放弃审批的审批记录
                List<TaskRoleApproveRecord> abandonedApproveRecordsByRole = taskRoleApproveRecordsByRole.stream().filter(i -> RoleApproveStatus.ABANDONED == i.getStatus()).toList();
                if (abandonedApproveRecordsByRole.size() == nodeRoleAssignmentsByRole.size() - 1) {
                    throw new WorkflowException("最后一个审批人无法放弃审批");
                }

                // 获取当前的审批记录
                TaskRoleApproveRecord curTaskRoleApproveRecord = taskRoleApproveRecordExecutor.getByTaskInstanceIdAndRoleIdAndUserId(tenantId, taskInstanceId, roleId, userId);
                curTaskRoleApproveRecord.setComment(comment);
                curTaskRoleApproveRecord.setStatus(RoleApproveStatus.ABANDONED);
                taskRoleApproveRecordExecutor.updateById(curTaskRoleApproveRecord);
            }
        }
        // 角色审批类型为：ALL 或 SEQ
        else if (RoleApproveType.ALL == roleApproveType || RoleApproveType.SEQ == roleApproveType) {
            // 角色用户审批类型为：ANY
            if (RoleUserApproveType.ANY == roleUserApproveType) {
                // 获取该角色下已经放弃审批的审批记录
                List<TaskRoleApproveRecord> abandonedApproveRecordsByRole = taskRoleApproveRecordsByRole.stream().filter(i -> RoleApproveStatus.ABANDONED == i.getStatus()).toList();
                if (abandonedApproveRecordsByRole.size() == nodeRoleAssignmentsByRole.size() - 1) {
                    throw new WorkflowException("最后一个审批人无法放弃审批");
                }

                // 获取当前的审批记录
                TaskRoleApproveRecord curTaskRoleApproveRecord = taskRoleApproveRecordExecutor.getByTaskInstanceIdAndRoleIdAndUserId(tenantId, taskInstanceId, roleId, userId);
                curTaskRoleApproveRecord.setComment(comment);
                curTaskRoleApproveRecord.setStatus(RoleApproveStatus.ABANDONED);
                taskRoleApproveRecordExecutor.updateById(curTaskRoleApproveRecord);
            }
            // 角色用户审批类型为：ALL
            else if (RoleUserApproveType.ALL == roleUserApproveType) {
                // 获取已经放弃审批的审批记录
                List<TaskRoleApproveRecord> abandonedApproveRecords = allTaskRoleApproveRecords.stream().filter(i -> RoleApproveStatus.ABANDONED == i.getStatus()).toList();
                if (abandonedApproveRecords.size() == allNodeRoleAssignments.size() - 1) {
                    throw new WorkflowException("最后一个审批人无法放弃审批");
                }

                // 获取当前的审批记录
                TaskRoleApproveRecord curTaskRoleApproveRecord = taskRoleApproveRecordExecutor.getByTaskInstanceIdAndRoleIdAndUserId(tenantId, taskInstanceId, roleId, userId);
                curTaskRoleApproveRecord.setComment(comment);
                curTaskRoleApproveRecord.setStatus(RoleApproveStatus.ABANDONED);
                taskRoleApproveRecordExecutor.updateById(curTaskRoleApproveRecord);
            }
        }

        // 角色用户审批类型为：SEQ
        if (RoleApproveType.SEQ == roleApproveType || RoleUserApproveType.SEQ == roleUserApproveType) {

            // 角色审批类型为：ANY
            if (RoleApproveType.ANY == roleApproveType) {
                // 获取该角色下已经放弃审批的审批记录
                List<TaskRoleApproveRecord> abandonedApproveRecordsByRole = taskRoleApproveRecordsByRole.stream().filter(i -> RoleApproveStatus.ABANDONED == i.getStatus()).toList();
                if (abandonedApproveRecordsByRole.size() == nodeRoleAssignmentsByRole.size() - 1) {
                    throw new WorkflowException("最后一个审批人无法放弃审批");
                }
            }

            // 获取当前的审批记录
            TaskRoleApproveRecord curTaskRoleApproveRecord = taskRoleApproveRecordExecutor.getByTaskInstanceIdAndRoleIdAndUserId(tenantId, taskInstanceId, roleId, userId);
            curTaskRoleApproveRecord.setComment(comment);
            curTaskRoleApproveRecord.setStatus(RoleApproveStatus.ABANDONED);
            taskRoleApproveRecordExecutor.updateById(curTaskRoleApproveRecord);

            // 查询 userSeq 大于当前角色用户的角色用户
            NodeRoleAssignment nodeRoleAssignment = nodeRoleAssignmentsByRole.stream().filter(i -> i.getRoleId().equals(curNodeRoleAssignment.getRoleId()))
                    .filter(i -> i.getUserSeq() > curNodeRoleAssignment.getUserSeq()).min(Comparator.comparingInt(NodeRoleAssignment::getUserSeq)).orElse(null);
            // 将最近的一个 SUSPEND 的审批记录设置为：IN_PROGRESS
            if (nodeRoleAssignment != null) {
                taskRoleApproveRecordsByRole.stream().filter(i -> i.getId().equals(nodeRoleAssignment.getId())).filter(i -> RoleApproveStatus.SUSPEND == i.getStatus())
                        .findFirst().ifPresent(taskRoleApproveRecord -> {
                            taskRoleApproveRecord.setStatus(RoleApproveStatus.IN_PROGRESS);
                            taskRoleApproveRecordExecutor.updateById(taskRoleApproveRecord);
                        });
            }
            // 表示同组下已经没有审批用户了，此时需要将其他组的审批用户设置为 IN_PROGRESS
            else {
                String nextRoleId = null;
                // 角色审批类型为：ALL，因为角色的 roleSeq 都是同一个值
                if (RoleApproveType.ALL == roleApproveType) {
                    // 找到下一个审批角色用户
                    NodeRoleAssignment nextNodeRoleAssignment = allNodeRoleAssignments.stream().filter(i -> i.getUserSeq() > curNodeRoleAssignment.getUserSeq()).findFirst().orElse(null);
                    if (nextNodeRoleAssignment != null) {
                        nextRoleId = nextNodeRoleAssignment.getRoleId();
                    }
                }
                // 其他情况
                else {
                    // 查找大于当前 roleId 的最小的一个 roleId
                    nextRoleId = allNodeRoleAssignments.stream().filter(i -> i.getRoleSeq() > curNodeRoleAssignment.getRoleSeq()).sorted(Comparator.comparingInt(NodeRoleAssignment::getRoleSeq))
                            .flatMap(i -> Stream.of(i.getRoleId())).findFirst().orElse(null);
                }

                if (nextRoleId != null) {
                    // 下一个角色的审批用户
                    List<NodeRoleAssignment> nextNodeRoleAssignments = roleAssignmentsRoleMap.get(nextRoleId);
                    // 找到下一个角色的审批用户中 userSeq 最靠前的一个
                    NodeRoleAssignment nextNodeRoleAssignment = nextNodeRoleAssignments.stream().min(Comparator.comparingInt(NodeRoleAssignment::getUserSeq)).orElse(null);
                    if (nextNodeRoleAssignment != null) {
                        allTaskRoleApproveRecords.stream().filter(i -> i.getNodeRoleAssignmentId().equals(nextNodeRoleAssignment.getId())).findFirst().
                                ifPresent(otherTaskRoleApproveRecord -> {
                                    otherTaskRoleApproveRecord.setStatus(RoleApproveStatus.IN_PROGRESS);
                                    taskRoleApproveRecordExecutor.updateById(otherTaskRoleApproveRecord);
                                });
                    }
                }

            }
        }

        // 获取当前任务下所有角色的审批记录
        List<TaskRoleApproveRecord> taskRoleApproveRecordByRole = taskRoleApproveRecordExecutor.findByTaskInstanceIdAndRoleId(tenantId, taskInstanceId, roleId);
        // 同角色下已经通过审批的用户
        List<TaskRoleApproveRecord> approvedApproveRecordsByRole = taskRoleApproveRecordByRole.stream().filter(i -> RoleApproveStatus.ABANDONED == i.getStatus() || RoleApproveStatus.APPROVED == i.getStatus()).toList();
        // 同角色下全部放弃审批的用户
        List<TaskRoleApproveRecord> abandonedApproveRecordsByRole = approvedApproveRecordsByRole.stream().filter(i -> RoleApproveStatus.ABANDONED == i.getStatus()).toList();

        // 角色审批类型为：ALL 或 SEQ，角色用户审批类型为：ANY，则不增加审批人数，同时判断是会否是相同角色下，最后一个审批人
        if (RoleApproveType.ANY != roleApproveType && RoleUserApproveType.ANY == roleUserApproveType) {
            // 同角色下已经放弃审批的人数
            // 同角色下的人数 - 同角色下已经放弃审批的人数 = 0
            if (nodeRoleAssignmentsByRole.size() - abandonedApproveRecordsByRole.size() == 0) {
                throw new WorkflowException("相同角色下，最后一个审批人无法放弃审批");
            }
        } else {
            // 获取当前审批任务，并修改当前审批任务的 approved_count 数量
            TaskInstanceExecutor taskInstanceExecutor = taskInstanceExecutorBuilder.build();
            taskInstance.setApprovedCount(taskInstance.getApprovedCount() + 1);
            taskInstanceExecutor.updateById(taskInstance);

        }

        // 是否该角色已经完成审核
        if (approvedApproveRecordsByRole.size() == nodeRoleAssignmentsByRole.size()) {
            // 是否该角色下所有的用户均放弃审批
            if (abandonedApproveRecordsByRole.size() == nodeRoleAssignmentsByRole.size()) {
                taskApprove.convertToApproveStatusAbandoned(comment);
                TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder.build();
                taskApproveExecutor.updateById(taskApprove);
            }
            // 存在已经同意的审批记录
            else {
                taskApprove.convertToApproveStatusApproved();
                TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder.build();
                taskApproveExecutor.updateById(taskApprove);
            }
        }

    }

    /**
     * 取消（针对提交人）
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     */
    @Override
    public void cancel(String tenantId, Integer workflowInstanceId) {
        JdbcTemplateHelper jdbcTemplateHelper = this.context.getJdbcTemplateHelper();
        jdbcTemplateHelper.executeInTransaction(() -> {
            // 查看流程状态
            WorkflowInstanceExecutor workflowInstanceExecutor = workflowInstanceExecutorBuilder.build();
            WorkflowInstance workflowInstance = workflowInstanceExecutor.getById(workflowInstanceId);

            // 检查流程实例是否已经结束
            checkIfWorkflowInstanceIsFinished(workflowInstance);

            // 获取该流程下所有的审批人
            TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder.build();
            List<TaskApprove> taskApproves = taskApproveExecutor.findByTWorkflowInstanceId(tenantId, workflowInstanceId, null, null);
            // 查看是否已经有人参与了审批
            TaskApprove taskApprove = taskApproves.stream()
                    .filter(i -> ApproveStatus.IN_PROGRESS != i.getStatus() && ApproveStatus.SUSPEND != i.getStatus()).findFirst().orElse(null);
            if (taskApprove != null) {
                throw new WorkflowException("流程实例已被审批: %s", taskApprove.getApproverId());
            }

            // 未审批的审批人节点：设置为已失效
            List<TaskApprove> inProgressApproves = taskApproves.stream()
                    .filter(i -> ApproveStatus.IN_PROGRESS == i.getStatus() || ApproveStatus.SUSPEND == i.getStatus()).toList();
            inProgressApproves.forEach(inProgressApprove -> {
                inProgressApprove.convertToApproveStatusSkipped();
                taskApproveExecutor.updateById(inProgressApprove);
                // 角色审批人
                if (ApproverIdType.ROLE == inProgressApprove.getApproverIdType()) {
                    TaskRoleApproveRecordExecutor taskRoleApproveRecordExecutor = taskRoleApproveRecordExecutorBuilder.build();
                    List<TaskRoleApproveRecord> taskRoleApproveRecords = taskRoleApproveRecordExecutor.findByTWorkflowInstanceId(tenantId, workflowInstanceId);
                    taskRoleApproveRecords.forEach(taskRoleApproveRecord -> {
                        if (RoleApproveStatus.IN_PROGRESS == taskRoleApproveRecord.getStatus() || RoleApproveStatus.SUSPEND == taskRoleApproveRecord.getStatus()) {
                            taskRoleApproveRecord.convertToApproveStatusSkipped();
                            taskRoleApproveRecordExecutor.updateById(taskRoleApproveRecord);
                        }
                    });
                }
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
            taskInstanceExecutor = taskInstanceExecutorBuilder.tenantId(tenantId)
                    .workflowInstanceId(workflowInstanceId)
                    .nodeDefinitionId(endNodeDefinition.getId())
                    .roleApprove(endNodeDefinition.isRoleApprove())
                    .status(NodeStatus.CANCELED)
                    .build();
            taskInstanceExecutor.save();

            // 流程实例标记为：已取消
            workflowInstance.setStatus(WorkflowStatus.CANCELED);
            workflowInstanceExecutor.updateById(workflowInstance);

            // 记录流程日志（审批取消）
            recordLogs(tenantId, workflowInstanceId, null, endNodeDefinition.getName(), TaskHistoryMessage.INSTANCE_CANCELED);
        });
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
    @Override
    public void interrupt(String tenantId, Integer workflowInstanceId, String comment) {
        JdbcTemplateHelper jdbcTemplateHelper = this.context.getJdbcTemplateHelper();
        jdbcTemplateHelper.executeInTransaction(() -> {
            // 查看流程状态
            WorkflowInstanceExecutor workflowInstanceExecutor = workflowInstanceExecutorBuilder.build();
            WorkflowInstance workflowInstance = workflowInstanceExecutor.getById(workflowInstanceId);
            // 检查流程实例是否已经结束
            checkIfWorkflowInstanceIsFinished(workflowInstance);

            // 获取该流程下所有的审批人
            TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder.build();
            List<TaskApprove> taskApproves = taskApproveExecutor.findByTWorkflowInstanceId(tenantId, workflowInstanceId);

            // 未审批的审批人节点：设置为已失效
            List<TaskApprove> inProgressApproves = taskApproves.stream()
                    .filter(i -> ApproveStatus.IN_PROGRESS == i.getStatus() || ApproveStatus.SUSPEND == i.getStatus()).toList();
            inProgressApproves.forEach(inProgressApprove -> {
                inProgressApprove.convertToApproveStatusInterrupted(comment);
                taskApproveExecutor.updateById(inProgressApprove);
                // 角色审批人
                if (ApproverIdType.ROLE == inProgressApprove.getApproverIdType()) {
                    TaskRoleApproveRecordExecutor taskRoleApproveRecordExecutor = taskRoleApproveRecordExecutorBuilder.build();
                    List<TaskRoleApproveRecord> taskRoleApproveRecords = taskRoleApproveRecordExecutor.findByTWorkflowInstanceId(tenantId, workflowInstanceId);
                    taskRoleApproveRecords.forEach(taskRoleApproveRecord -> {
                        if (RoleApproveStatus.IN_PROGRESS == taskRoleApproveRecord.getStatus() || RoleApproveStatus.SUSPEND == taskRoleApproveRecord.getStatus()) {
                            taskRoleApproveRecord.convertToApproveStatusInterrupted(comment);
                            taskRoleApproveRecordExecutor.updateById(taskRoleApproveRecord);
                        }
                    });
                }
            });

            // 获取所有进行中的审批节点，并设置为：已取消
            TaskInstanceExecutor taskInstanceExecutor = taskInstanceExecutorBuilder.build();
            List<TaskInstance> taskInstances = taskInstanceExecutor.findByWorkflowInstanceIdAndNodeType(tenantId, workflowInstanceId, NodeType.TASK);
            for (TaskInstance taskInstance : taskInstances) {
                if (NodeStatus.IN_PROGRESS == taskInstance.getStatus()) {
                    taskInstance.setStatus(NodeStatus.INTERRUPTED);
                    taskInstanceExecutor.updateById(taskInstance);
                }
            }

            // 获取结束节点
            NodeDefinitionExecutor nodeDefinitionExecutor = nodeDefinitionExecutorBuilder.build();
            NodeDefinition endNodeDefinition = nodeDefinitionExecutor.getEndNode(tenantId, workflowInstance.getWorkflowDefinitionId());

            // 创建取消节点实例
            taskInstanceExecutor = taskInstanceExecutorBuilder.tenantId(tenantId)
                    .workflowInstanceId(workflowInstanceId)
                    .nodeDefinitionId(endNodeDefinition.getId())
                    .roleApprove(endNodeDefinition.isRoleApprove())
                    .status(NodeStatus.INTERRUPTED)
                    .build();
            taskInstanceExecutor.save();

            // 流程实例标记为：已中断
            workflowInstance.setStatus(WorkflowStatus.INTERRUPTED);
            workflowInstanceExecutor.updateById(workflowInstance);

            // 记录流程日志（审批中断）
            recordLogs(tenantId, workflowInstanceId, null, endNodeDefinition.getName(), TaskHistoryMessage.INSTANCE_INTERRUPTED);
        });
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
        Query query = QueryBuilderWrapper.createLambda(TaskInstance.class)
                .eq(TaskInstance::getTenantId, tenantId)
                .eq(TaskInstance::getWorkflowInstanceId, workflowInstanceId)
                .page(1, pageSize)
                .build();
        TaskInstanceExecutor taskInstanceExecutor = taskInstanceExecutorBuilder.build();
        return taskInstanceExecutor.find(query);
    }

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
     */
    @Override
    public Page<TaskInstance> findTaskInstances(String tenantId, Integer workflowInstanceId,
                                                ApproveDatesSegmentCondition approveDatesSegmentCondition, Integer page, Integer pageSize) {
        return this.findTaskInstances(tenantId, workflowInstanceId, null, null, approveDatesSegmentCondition, page, pageSize);
    }

    /**
     * 查询审批任务
     *
     * @param tenantId                     租户 ID
     * @param workflowInstanceId           流程实例 ID
     * @param approveDatesSegmentCondition 审批时间段查询条件
     *
     * @return List<TaskInstance>
     */
    @Override
    public List<TaskInstance> findTaskInstances(String tenantId, Integer workflowInstanceId,
                                                ApproveDatesSegmentCondition approveDatesSegmentCondition) {
        return this.findTaskInstances(tenantId, workflowInstanceId, approveDatesSegmentCondition, 1, Integer.MAX_VALUE).getRecords();
    }

    /**
     * 查询审批任务
     *
     * @param tenantId                     租户 ID
     * @param workflowInstanceId           流程实例 ID
     * @param approverId                   审批人 ID
     * @param approveDatesSegmentCondition 审批时间段查询条件
     *
     * @return List<TaskInstance>
     */
    @Override
    public List<TaskInstance> findTaskInstances(String tenantId, Integer workflowInstanceId, String roleId, String approverId,
                                                ApproveDatesSegmentCondition approveDatesSegmentCondition) {
        List<String> roleIds = StringUtils.isEmpty(roleId) ? null : List.of(roleId);
        return this.findTaskInstances(tenantId, workflowInstanceId, roleIds, approverId, new ArrayList<>(), new ArrayList<>(), approveDatesSegmentCondition, 1, Integer.MAX_VALUE).getRecords();
    }

    /**
     * 分页查询审批任务
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
     */
    @Override
    public Page<TaskInstance> findTaskInstances(String tenantId, Integer workflowInstanceId, String roleId, String approverId,
                                                ApproveDatesSegmentCondition approveDatesSegmentCondition, Integer page, Integer pageSize) {
        List<String> roleIds = StringUtils.isEmpty(roleId) ? null : List.of(roleId);
        return this.findTaskInstances(tenantId, workflowInstanceId, roleIds, approverId, new ArrayList<>(), new ArrayList<>(), approveDatesSegmentCondition, page, pageSize);
    }

    /**
     * 分页查询审批任务
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
     */
    @Override
    public List<TaskInstance> findTaskInstances(String tenantId, Integer workflowInstanceId, String roleId, String approverId,
                                                NodeStatus nodeStatus, ApproveStatus approveStatus, ApproveDatesSegmentCondition approveDatesSegmentCondition) {
        return this.findTaskInstances(tenantId, workflowInstanceId, roleId, approverId, nodeStatus, approveStatus, approveDatesSegmentCondition, 1, Integer.MAX_VALUE).getRecords();
    }

    /**
     * 分页查询审批任务
     *
     * @param tenantId                     租户 ID
     * @param workflowInstanceId           流程实例 ID
     * @param roleId                       审批人角色 ID
     * @param approverId                   审批人 ID
     * @param nodeStatus                   节点状态
     * @param approveStatus                审批人审批状态
     * @param approveDatesSegmentCondition 审批时间段查询条件
     * @param page                         页码
     * @param pageSize                     每页数量
     *
     * @return List<TaskInstance>
     */
    @Override
    public Page<TaskInstance> findTaskInstances(String tenantId, Integer workflowInstanceId, String roleId, String approverId,
                                                NodeStatus nodeStatus, ApproveStatus approveStatus, ApproveDatesSegmentCondition approveDatesSegmentCondition, Integer page, Integer pageSize) {
        List<NodeStatus> nodeStatuses = nodeStatus == null ? null : List.of(nodeStatus);
        List<ApproveStatus> approveStatuses = approveStatus == null ? null : List.of(approveStatus);
        List<String> roleIds = StringUtils.isEmpty(roleId) ? null : List.of(roleId);
        return this.findTaskInstances(tenantId, workflowInstanceId, roleIds, approverId, nodeStatuses, approveStatuses, approveDatesSegmentCondition, page, pageSize);
    }

    /**
     * 分页查询审批任务
     *
     * @param tenantId                     租户 ID
     * @param workflowInstanceId           流程实例 ID
     * @param roleIds                      审批人角色 ID
     * @param approverId                   审批人 ID
     * @param nodeStatuses                 节点状态
     * @param approveStatuses              审批人审批状态
     * @param approveDatesSegmentCondition 审批时间段查询条件
     * @param page                         页码
     * @param pageSize                     每页数量
     *
     * @return List<TaskInstance>
     */
    @Override
    public Page<TaskInstance> findTaskInstances(String tenantId, Integer workflowInstanceId, List<String> roleIds, String approverId,
                                                List<NodeStatus> nodeStatuses, List<ApproveStatus> approveStatuses, ApproveDatesSegmentCondition approveDatesSegmentCondition, Integer page, Integer pageSize) {
        TaskInstanceExecutor taskInstanceExecutor = taskInstanceExecutorBuilder.build();
        return taskInstanceExecutor.findByApproverId(tenantId, workflowInstanceId, roleIds, approverId, nodeStatuses, approveStatuses, approveDatesSegmentCondition, page, pageSize);
    }

    /**
     * 分页查询审批任务
     *
     * @param tenantId                     租户 ID
     * @param workflowInstanceId           流程实例 ID
     * @param approverId                   审批人 ID
     * @param nodeStatus                   节点状态
     * @param approveStatuses              审批人审批状态
     * @param approveDatesSegmentCondition 审批时间段查询条件
     * @param page                         页码
     * @param pageSize                     每页数量
     *
     * @return List<TaskInstance>
     */
    @Override
    public Page<TaskInstance> findTaskInstances(String tenantId, Integer workflowInstanceId, String roleId, String approverId,
                                                NodeStatus nodeStatus, List<ApproveStatus> approveStatuses, ApproveDatesSegmentCondition approveDatesSegmentCondition, Integer page, Integer pageSize) {
        List<NodeStatus> nodeStatuses = nodeStatus == null ? null : List.of(nodeStatus);
        List<String> roleIds = StringUtils.isEmpty(roleId) ? null : List.of(roleId);
        return this.findTaskInstances(tenantId, workflowInstanceId, roleIds, approverId, nodeStatuses, approveStatuses, approveDatesSegmentCondition, page, pageSize);
    }

    /**
     * 分页查询审批任务
     *
     * @param tenantId                     租户 ID
     * @param workflowInstanceId           流程实例 ID
     * @param approverId                   审批人 ID
     * @param nodeStatuses                 节点状态
     * @param approveStatus                审批人审批状态
     * @param approveDatesSegmentCondition 审批时间段查询条件
     * @param page                         页码
     * @param pageSize                     每页数量
     *
     * @return List<TaskInstance>
     */
    @Override
    public Page<TaskInstance> findTaskInstances(String tenantId, Integer workflowInstanceId, String roleId, String approverId,
                                                List<NodeStatus> nodeStatuses, ApproveStatus approveStatus, ApproveDatesSegmentCondition approveDatesSegmentCondition, Integer page, Integer pageSize) {
        List<ApproveStatus> approveStatuses = approveStatus == null ? null : List.of(approveStatus);
        List<String> roleIds = StringUtils.isEmpty(roleId) ? null : List.of(roleId);
        return this.findTaskInstances(tenantId, workflowInstanceId, roleIds, approverId, nodeStatuses, approveStatuses, approveDatesSegmentCondition, page, pageSize);
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
        return taskInstanceExecutor.getInCurrentlyEffectTaskInstance(tenantId, workflowInstanceId);
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
    @Override
    public NodeDefinition getInCurrentlyEffectNodeDefinition(String tenantId, Integer workflowInstanceId) {
        TaskInstance inCurrentlyEffectTaskInstance = this.getInCurrentlyEffectTaskInstance(tenantId, workflowInstanceId);
        if (inCurrentlyEffectTaskInstance != null) {
            Integer nodeDefinitionId = inCurrentlyEffectTaskInstance.getNodeDefinitionId();
            NodeDefinitionExecutor nodeDefinitionExecutor = nodeDefinitionExecutorBuilder.build();
            return nodeDefinitionExecutor.getById(tenantId, nodeDefinitionId);
        }
        return null;
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
    @Override
    public List<Approver> findInCurrentlyEffectApprovers(String tenantId, Integer workflowInstanceId) {
        // 获取正在进行中的审批记录
        TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder.build();
        List<TaskApprove> inProgressTaskApproves = taskApproveExecutor.findByTWorkflowInstanceId(tenantId, workflowInstanceId, null, ApproveStatus.IN_PROGRESS);

        TaskInstanceExecutor taskInstanceExecutor = taskInstanceExecutorBuilder.build();
        NodeAssignmentExecutor nodeAssignmentExecutor = nodeAssignmentExecutorBuilder.build();
        NodeDefinitionExecutor nodeDefinitionExecutor = nodeDefinitionExecutorBuilder.build();
        // 获取所有审批人
        return inProgressTaskApproves.stream().map(taskApprove -> {
            TaskInstance taskInstance = taskInstanceExecutor.getById(taskApprove.getTaskInstanceId());
            Integer nodeDefinitionId = taskInstance.getNodeDefinitionId();
            NodeDefinition nodeDefinition = nodeDefinitionExecutor.getById(tenantId, nodeDefinitionId);
            // 处理动态审批节点
            if (nodeDefinition.isDynamic()) {
                TaskDynamicAssignmentExecutor taskDynamicAssignmentExecutor = taskDynamicAssignmentExecutorBuilder.build();
                TaskDynamicAssignment taskDynamicAssignment = taskDynamicAssignmentExecutor.getByTaskInstanceIdAndApproverId(tenantId, taskInstance.getId(), taskApprove.getApproverId());
                return Approver.of(taskDynamicAssignment, nodeDefinition.isRoleApprove());
            }
            NodeAssignment nodeAssignment = nodeAssignmentExecutor.getByNodeDefinitionIdAndApproverId(tenantId, nodeDefinitionId, taskApprove.getApproverId());
            return Approver.of(nodeAssignment, nodeDefinition.isRoleApprove());
        }).toList();
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
    @Override
    public List<RoleApprover> findInCurrentlyEffectRoleApprovers(String tenantId, Integer workflowInstanceId) {
        // 获取正在进行中的角色审批记录
        TaskRoleApproveRecordExecutor taskRoleApproveRecordExecutor = taskRoleApproveRecordExecutorBuilder.build();
        List<TaskRoleApproveRecord> inProgressTaskRoleApproveRecords = taskRoleApproveRecordExecutor.findByTWorkflowInstanceId(tenantId, workflowInstanceId, ApproveStatus.IN_PROGRESS);
        List<Integer> nodeRoleAssignmentIds = inProgressTaskRoleApproveRecords.stream().map(TaskRoleApproveRecord::getNodeRoleAssignmentId).toList();
        // 获取所有角色审批人
        NodeRoleAssignmentExecutor nodeRoleAssignmentExecutor = nodeRoleAssignmentExecutorBuilder.build();
        return nodeRoleAssignmentIds.stream().map(nodeRoleAssignmentExecutor::getById).map(RoleApprover::of).toList();
    }

    /**
     * 根据 ID 查找任务实例
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     *
     * @return TaskInstance
     */
    @Override
    public TaskInstance getTaskInstance(String tenantId, Integer taskInstanceId) {
        TaskInstanceExecutor taskInstanceExecutor = taskInstanceExecutorBuilder.build();
        TaskInstance taskInstance = taskInstanceExecutor.getById(taskInstanceId);
        return taskInstance != null && tenantId.equals(taskInstance.getTenantId()) ? taskInstance : null;
    }

    /**
     * 根据流程实例 ID 和任务实例 ID 查找任务实例
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     * @param nodeDefinitionId   节点定义 ID
     *
     * @return TaskInstance
     *
     * @author wangweijun
     * @since 2025/1/10 15:15
     */
    @Override
    public TaskInstance getTaskInstance(String tenantId, Integer workflowInstanceId, Integer nodeDefinitionId) {
        TaskInstanceExecutor taskInstanceExecutor = taskInstanceExecutorBuilder.build();
        Query query = QueryBuilderWrapper.createLambda(TaskInstance.class)
                .eq(TaskInstance::getTenantId, tenantId)
                .eq(TaskInstance::getWorkflowInstanceId, workflowInstanceId)
                .eq(TaskInstance::getNodeDefinitionId, nodeDefinitionId)
                .build();
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
        WorkflowDefinitionExecutor definitionExecutor = workflowDefinitionExecutorBuilder.build();
        return definitionExecutor.getByTenantAndKey(tenantId, workflowDefinitionKey);
    }

    /**
     * 根据租户 ID 和 流程定义 Key 获取流程定义
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     *
     * @return WorkflowDefinition
     *
     * @author wangweijun
     * @since 2024/6/24 11:24
     */
    private WorkflowDefinition getWorkflowDefinition(String tenantId, Integer workflowDefinitionId) {
        WorkflowDefinitionExecutor definitionExecutor = workflowDefinitionExecutorBuilder.build();
        return definitionExecutor.getByTenantAndId(tenantId, workflowDefinitionId);
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
     * @param tenantId                        租户 ID
     * @param workflowInstanceId              流程实例 ID
     * @param approveStatusDescCustomizer     审批状态描述自定义器
     * @param roleApproveStatusDescCustomizer 角色审批状态描述自定义器
     *
     * @return WorkflowInstanceApproveRecords
     *
     * @author wangweijun
     * @since 2024/9/12 13:42
     */
    @Override
    public WorkflowInstanceApproveRecords getWorkflowInstanceApproveRecords(String tenantId, Integer workflowInstanceId,
                                                                            ApproveStatusDescCustomizer approveStatusDescCustomizer, RoleApproveStatusDescCustomizer roleApproveStatusDescCustomizer) {
        return this.getWorkflowInstanceApproveRecords(tenantId, workflowInstanceId, null, null, approveStatusDescCustomizer, roleApproveStatusDescCustomizer);
    }

    /**
     * 获取流程审批记录
     *
     * @param tenantId                        租户 ID
     * @param workflowDefinitionId            流程定义 ID
     * @param approveStatusDescCustomizer     审批状态描述自定义器
     * @param roleApproveStatusDescCustomizer 角色审批状态描述自定义器
     *
     * @return List<WorkflowInstanceApproveRecords>
     *
     * @author wangweijun
     * @since 2024/9/12 13:42
     */
    @Override
    public List<WorkflowInstanceApproveRecords> findWorkflowInstanceApproveRecords(String tenantId, Integer workflowDefinitionId,
                                                                                   ApproveStatusDescCustomizer approveStatusDescCustomizer, RoleApproveStatusDescCustomizer roleApproveStatusDescCustomizer) {
        return this.findWorkflowInstanceApproveRecords(tenantId, workflowDefinitionId, List.of(), null, approveStatusDescCustomizer, roleApproveStatusDescCustomizer);
    }

    /**
     * 获取流程审批记录
     *
     * @param tenantId                        租户 ID
     * @param workflowDefinitionId            流程定义 ID
     * @param curRoleIds                      当前角色 IDs
     * @param curUserId                       当前用户 ID
     * @param approveStatusDescCustomizer     审批状态描述自定义器
     * @param roleApproveStatusDescCustomizer 角色审批状态描述自定义器
     *
     * @return List<WorkflowInstanceApproveRecords>
     *
     * @author wangweijun
     * @since 2024/9/12 13:42
     */
    @Override
    public List<WorkflowInstanceApproveRecords> findWorkflowInstanceApproveRecords(String tenantId, Integer workflowDefinitionId, List<String> curRoleIds, String curUserId,
                                                                                   ApproveStatusDescCustomizer approveStatusDescCustomizer, RoleApproveStatusDescCustomizer roleApproveStatusDescCustomizer) {
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
            WorkflowInstanceApproveRecords records = this.getWorkflowInstanceApproveRecords(tenantId, workflowInstance.getId(), curRoleIds, curUserId, approveStatusDescCustomizer, roleApproveStatusDescCustomizer);
            recordsList.add(records);
        }

        return recordsList;
    }

    /**
     * 获取流程审批记录
     *
     * @param tenantId                        租户 ID
     * @param workflowInstanceId              流程实例 ID
     * @param curRoleIds                      当前角色 IDs
     * @param curUserId                       当前用户 ID
     * @param approveStatusDescCustomizer     审批状态描述自定义器
     * @param roleApproveStatusDescCustomizer 角色审批状态描述自定义器
     *
     * @return WorkflowInstanceApproveRecords
     *
     * @author wangweijun
     * @since 2024/9/12 13:42
     */
    @Override
    public WorkflowInstanceApproveRecords getWorkflowInstanceApproveRecords(String tenantId, Integer workflowInstanceId, List<String> curRoleIds, String curUserId,
                                                                            ApproveStatusDescCustomizer approveStatusDescCustomizer, RoleApproveStatusDescCustomizer roleApproveStatusDescCustomizer) {
        // 流程实例
        WorkflowInstanceExecutor workflowInstanceExecutor = workflowInstanceExecutorBuilder.build();
        WorkflowInstance workflowInstance = workflowInstanceExecutor.getById(workflowInstanceId);
        if (workflowInstance == null) {
            WorkflowException.throwWorkflowInstanceNotFoundException();
        }

        // 查询是否存在审批记录，如果有则直接返回（存在审批记录，表示该流程实例已经完成审核）
        Assert.notNull(workflowInstance, "流程实例不存在");
        // 判断是否需要读取缓存
        boolean cacheable = approveStatusDescCustomizer == null && roleApproveStatusDescCustomizer == null;
        WorkflowInstanceApproveRecords workflowInstanceApproveRecords = workflowInstance.getApproveRecords();
        if (workflowInstance.isFinished() && workflowInstanceApproveRecords != null && cacheable) {
            return workflowInstanceApproveRecords;
        }

        // 审批状态描述自定义器
        if (approveStatusDescCustomizer == null) {
            approveStatusDescCustomizer = ApproveStatusDescCustomizer.builder().build();
        }

        // 角色审批状态描述自定义器
        if (roleApproveStatusDescCustomizer == null) {
            roleApproveStatusDescCustomizer = RoleApproveStatusDescCustomizer.builder().build();
        }

        // 流程定义
        WorkflowDefinitionExecutor workflowDefinitionExecutor = workflowDefinitionExecutorBuilder.build();
        WorkflowDefinition workflowDefinition = workflowDefinitionExecutor.getById(workflowInstance.getWorkflowDefinitionId());

        // 任务实例
        TaskInstanceExecutor taskInstanceExecutor = taskInstanceExecutorBuilder.build();

        // 节点定义
        NodeDefinitionExecutor nodeDefinitionExecutor = nodeDefinitionExecutorBuilder.build();
        List<NodeDefinition> nodeDefinitions = nodeDefinitionExecutor.findByWorkflowDefinitionId(tenantId, workflowDefinition.getId());

        // 节点审批人
        NodeAssignmentExecutor nodeAssignmentExecutor = nodeAssignmentExecutorBuilder.build();
        List<NodeAssignment> nodeAssignments = nodeAssignmentExecutor.findByWorkflowInstanceId(tenantId, workflowInstanceId);

        // 节点定义动态节点审批人的对应关系
        TaskDynamicAssignmentExecutor taskDynamicAssignmentExecutor = taskDynamicAssignmentExecutorBuilder.build();
        Map<NodeDefinition, List<TaskDynamicAssignment>> nodeDefAndTaskDynamicAssignmentMap = new LinkedHashMap<>();

        List<Pair<NodeDefinition, TaskInstance>> nodeDefAndTasks = new ArrayList<>();
        for (NodeDefinition nodeDefinition : nodeDefinitions) {
            TaskInstance taskInstance = taskInstanceExecutor.getByWorkflowInstanceIdAndNodeDefinitionId(tenantId, workflowInstanceId, nodeDefinition.getId());
            nodeDefAndTasks.add(Pair.of(nodeDefinition, taskInstance));

            // 封装：节点定义动态节点审批人的对应关系
            List<TaskDynamicAssignment> taskDynamicAssignments = new ArrayList<>();
            if (nodeDefinition.isDynamic()) {
                taskDynamicAssignments = taskDynamicAssignmentExecutor.findByNodeDefinitionId(tenantId, nodeDefinition.getId());
            }
            nodeDefAndTaskDynamicAssignmentMap.put(nodeDefinition, taskDynamicAssignments);
        }

        // 审批人
        TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder.build();
        List<TaskApprove> taskApproves = taskApproveExecutor.findByTWorkflowInstanceId(tenantId, workflowInstanceId);


        // 用户审批情况：审批记录与节点审批人的对应关系
        Map<TaskApprove, NodeAssignment> taskApproveAndNodeAssignmentMap = new LinkedHashMap<>();

        // 角色审批情况：审批记录与角色审批记录对应表
        TaskRoleApproveRecordExecutor taskRoleApproveRecordExecutor = taskRoleApproveRecordExecutorBuilder.build();
        Map<TaskApprove, List<TaskRoleApproveRecord>> taskApproveAndRoleApproveRecordsMap = new LinkedHashMap<>();

        // 角色审批记录与角色用户对应表
        NodeRoleAssignmentExecutor nodeRoleAssignmentExecutor = nodeRoleAssignmentExecutorBuilder.build();
        Map<TaskRoleApproveRecord, NodeRoleAssignment> taskRoleRecordAndNodeRoleAssignmentMap = new LinkedHashMap<>();

        // 节点审批人与委派用户关系
        TaskReassignRecordExecutor taskReassignRecordExecutor = taskReassignRecordExecutorBuilder.build();
        Map<TaskApprove, TaskReassignRecord> taskApproveAndTaskReassignMap = new LinkedHashMap<>();

        for (TaskApprove taskApprove : taskApproves) {
            Integer taskInstanceId = taskApprove.getTaskInstanceId();
            TaskInstance taskInstance = taskInstanceExecutor.getById(taskInstanceId);
            Integer nodeDefinitionId = taskInstance.getNodeDefinitionId();
            NodeDefinition nodeDefinition = nodeDefinitionExecutor.getById(nodeDefinitionId);
            List<TaskRoleApproveRecord> taskRoleApproveRecords = null;
            // 角色审批
            if (nodeDefinition.isRoleApprove()) {
                taskRoleApproveRecords = taskRoleApproveRecordExecutor.findByTaskInstanceIdAndRoleId(tenantId, taskInstanceId, taskApprove.getApproverId());
                taskRoleApproveRecords.forEach(taskRoleApproveRecord -> {
                    NodeRoleAssignment nodeRoleAssignment = nodeRoleAssignmentExecutor.getById(taskRoleApproveRecord.getNodeRoleAssignmentId());
                    taskRoleRecordAndNodeRoleAssignmentMap.put(taskRoleApproveRecord, nodeRoleAssignment);
                });
            }
            NodeAssignment nodeAssignment = nodeAssignmentExecutor.getByNodeDefinitionIdAndApproverId(tenantId, nodeDefinitionId, taskApprove.getApproverId());
            taskApproveAndNodeAssignmentMap.put(taskApprove, nodeAssignment);
            taskApproveAndRoleApproveRecordsMap.put(taskApprove, taskRoleApproveRecords);

            // 表示没有找到审批人节点，即发生了委派审批
            if (nodeAssignment == null) {
                TaskReassignRecord latestTaskReassignRecord = taskReassignRecordExecutor.getByLatestTaskApproveId(tenantId, taskApprove.getId());
                if (latestTaskReassignRecord != null) {
                    taskApproveAndTaskReassignMap.put(taskApprove, latestTaskReassignRecord);
                }
            }
        }

        // 构建 WorkflowInstanceApproveRecords
        workflowInstanceApproveRecords = WorkflowInstanceApproveRecords.of(workflowDefinition, workflowInstance, nodeDefAndTasks, taskApproves,
                taskApproveAndNodeAssignmentMap, taskApproveAndTaskReassignMap, taskApproveAndRoleApproveRecordsMap, taskRoleRecordAndNodeRoleAssignmentMap, nodeDefAndTaskDynamicAssignmentMap,
                nodeAssignments, curRoleIds, curUserId, approveStatusDescCustomizer, roleApproveStatusDescCustomizer);
        // 判断是否需要缓存
        if (workflowInstance.isFinished() && workflowInstance.getApproveRecords() == null && cacheable) {
            workflowInstance.setApproveRecords(workflowInstanceApproveRecords);
            workflowInstanceExecutor.updateById(workflowInstance);
        }
        return workflowInstanceApproveRecords;
    }

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
    @Override
    public WorkflowDefinitionFlowSchema getWorkflowDefinitionFlowSchema(String tenantId, Integer workflowInstanceId) {
        // 流程实例
        WorkflowInstanceExecutor workflowInstanceExecutor = workflowInstanceExecutorBuilder.build();
        WorkflowInstance workflowInstance = workflowInstanceExecutor.getById(workflowInstanceId);
        if (workflowInstance == null) {
            WorkflowException.throwWorkflowInstanceNotFoundException();
        }
        if (workflowInstance.getFlowSchema() != null) {
            return workflowInstance.getFlowSchema();
        }
        WorkflowDefinitionFlowSchema flowSchema = this.deploymentService.getWorkflowDefinitionFlowSchema(tenantId, workflowInstance.getWorkflowDefinitionId());
        if (workflowInstance.isFinished() && workflowInstance.getFlowSchema() == null) {
            workflowInstance.setFlowSchema(flowSchema);
            workflowInstanceExecutor.updateById(workflowInstance);
        }
        return flowSchema;
    }

    /**
     * 动态设置审批人
     *
     * @param tenantId         租户 ID
     * @param nodeDefinitionId 节点定义 ID
     * @param taskInstanceId   任务实例 ID
     * @param curApproverId    当前审批人 ID
     * @param approvers        审批人列表
     *
     * @author wangweijun
     * @since 2024/9/9 13:58
     */
    @Override
    public void dynamicAssignmentApprovers(String tenantId, Integer nodeDefinitionId, Integer taskInstanceId, String curApproverId, List<Approver> approvers) {
        JdbcTemplateHelper jdbcTemplateHelper = this.context.getJdbcTemplateHelper();
        jdbcTemplateHelper.executeInTransaction(() -> {
            // 获取到节点定义
            NodeDefinitionExecutor nodeDefinitionExecutor = nodeDefinitionExecutorBuilder.build();
            NodeDefinition nodeDefinition = nodeDefinitionExecutor.getById(nodeDefinitionId);
            if (!nodeDefinition.isDynamic()) {
                throw new WorkflowException("非动态审批节点，无法设置动态审批人");
            }
            // 查找节点审批人
            NodeAssignmentExecutor nodeAssignmentExecutor = nodeAssignmentExecutorBuilder.build();
            List<NodeAssignment> nodeAssignments = new ArrayList<>(nodeAssignmentExecutor.findByNodeDefinitionId(tenantId, nodeDefinitionId));
            if (CollectionUtils.isEmpty(nodeAssignments) || nodeDefinition.getDynamicAssignmentNum() == 0) {
                throw new WorkflowException("节点审批人不存在，请核实审批节点是否正确");
            }
            if (nodeDefinition.getDynamicAssignmentNum() >= 1 && nodeAssignments.size() != approvers.size()) {
                throw new WorkflowException("审批人数量不一致，节点审批人数量：%s，审批人数量：%s", nodeAssignments.size(), approvers.size());
            }

            // 创建动态审批人
            List<TaskDynamicAssignment> taskDynamicAssignments = new ArrayList<>();
            TaskDynamicAssignmentExecutor taskDynamicAssignmentExecutor = taskDynamicAssignmentExecutorBuilder.build();
            ApproveType approveType = nodeDefinition.getApproveType();
            for (Approver approver : approvers) {
                // 判断合法性：不允许非动态审批节点使用 {assignment:n} 的格式
                if (approver.getId().startsWith(WorkflowConstants.DYNAMIC_ASSIGNMENT_APPROVER_VALUE_PREFIX) && approver.getId().endsWith(WorkflowConstants.DYNAMIC_ASSIGNMENT_APPROVER_VALUE_SUFFIX)) {
                    throw new WorkflowException("审批人格式设置错误");
                }
                TaskDynamicAssignment taskDynamicAssignment = TaskDynamicAssignmentBuilder.builder(tenantId, nodeDefinitionId, taskInstanceId)
                        .approverInfo(approveType, approver.getId(), approver.getName(), approver.getDesc()).build();
                taskDynamicAssignmentExecutor.save(taskDynamicAssignment);
                taskDynamicAssignments.add(taskDynamicAssignment);
            }
            // 排序
            taskDynamicAssignments.sort(Comparator.comparing(TaskDynamicAssignment::getApproverSeq, Comparator.nullsLast(Comparator.naturalOrder())));

            // 此时的 taskApprove 为 {dynamic:n} 的形式，需要修改为真实的用户
            TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder.build();
            List<TaskApprove> taskApproves = taskApproveExecutor.findByTaskInstanceId(tenantId, taskInstanceId, ActiveStatus.ACTIVE);
            // 获取所有未设置的动态审批人
            List<TaskApprove> unSettingDynamicTaskApproves = new ArrayList<>(taskApproves.stream().filter(TaskApprove::isUnSettingApprover).toList());
            unSettingDynamicTaskApproves.sort(Comparator.comparing(TaskApprove::getApproverSeq, Comparator.nullsLast(Comparator.naturalOrder())));

            // 获取动态审批人数量
            Integer dynamicApproverNum = null;
            if (unSettingDynamicTaskApproves.size() == 1) {
                TaskApprove unSettingDynamicTaskApprove = unSettingDynamicTaskApproves.get(0);
                dynamicApproverNum = unSettingDynamicTaskApprove.getDynamicApproverNum();
                if (dynamicApproverNum == null) {
                    throw new WorkflowException("动态审批人数量解析错误: {}", unSettingDynamicTaskApprove.getApproverId());
                }
            }

            // 动态审批人为不限定的情况，根据实际传入的审批人数量进行设置
            if (dynamicApproverNum != null && dynamicApproverNum == -1) {
                TaskApprove templateTaskApprove = unSettingDynamicTaskApproves.get(0);
                // 根据第一个任务审批记录设置其他任务审批记录
                for (int i = 1; i < taskDynamicAssignments.size(); i++) {
                    TaskApprove taskApprove = TaskApprove.copyOf(templateTaskApprove);
                    taskApproveExecutor.save(taskApprove);
                    unSettingDynamicTaskApproves.add(taskApprove);
                }
            }

            // 再次判断数量
            if (unSettingDynamicTaskApproves.size() != taskDynamicAssignments.size()) {
                throw new WorkflowException("审批人数量不一致，节点审批人数量：%s，动态审批人数量：%s", unSettingDynamicTaskApproves.size(), taskDynamicAssignments.size());
            }

            // 更新 taskApprove
            for (int i = 0; i < taskDynamicAssignments.size(); i++) {
                TaskDynamicAssignment taskDynamicAssignment = taskDynamicAssignments.get(i);
                TaskApprove unSettingDynamicTaskApprove = unSettingDynamicTaskApproves.get(i);
                unSettingDynamicTaskApprove.setApproverId(taskDynamicAssignment.getApproverId());
                unSettingDynamicTaskApprove.setApproverSeq(taskDynamicAssignment.getApproverSeq());
                taskApproveExecutor.updateById(unSettingDynamicTaskApprove);
            }

            // 更新 taskInstance
            TaskInstanceExecutor taskInstanceExecutor = taskInstanceExecutorBuilder.build();
            TaskInstance taskInstance = taskInstanceExecutor.getById(taskInstanceId);
            Integer totalCount = taskInstance.getTotalCount();
            // 表示是动态审批，但是没有设置具体的值
            if (totalCount == -1) {
                if (ApproveType.ANY == nodeDefinition.getApproveType()) {
                    taskInstance.setTotalCount(1);
                } else {
                    taskInstance.setTotalCount(taskDynamicAssignments.size());
                }
                taskInstanceExecutor.updateById(taskInstance);
            }

            // 判断是否需要自动审批
            if (curApproverId != null) {
                WorkflowDefinitionExecutor workflowDefinitionExecutor = workflowDefinitionExecutorBuilder.build();
                WorkflowDefinition workflowDefinition = workflowDefinitionExecutor.getById(nodeDefinition.getWorkflowDefinitionId());
                ContinuousApproveMode continuousApproveMode = workflowDefinition.getContinuousApproveMode();
                for (TaskDynamicAssignment taskDynamicAssignment : taskDynamicAssignments) {
                    continuousApproveModeProcess(tenantId, taskInstance, null, curApproverId, taskDynamicAssignment.getApproverId(), continuousApproveMode);
                }
            }
        });
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
    @Override
    public void replaceApprover(String tenantId, String sourceApproverId, Approver targetApprover) {
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
            doUpdateApprover(tenantId, taskInstances, sourceApproverId, targetApprover);
        });
    }

    /**
     * 更新审批人（未审批状态下）
     *
     * @param taskInstance     任务实例
     * @param sourceApproverId 原审批人
     * @param targetApprover   新审批人
     *
     * @author wangweijun
     * @since 2024/9/12 11:21
     */
    @Override
    public void replaceApprover(TaskInstance taskInstance, String sourceApproverId, Approver targetApprover) {
        JdbcTemplateHelper jdbcTemplateHelper = this.context.getJdbcTemplateHelper();
        jdbcTemplateHelper.executeInTransaction(() -> {
            String tenantId = taskInstance.getTenantId();
            NodeStatus nodeStatus = taskInstance.getStatus();
            if (nodeStatus != NodeStatus.IN_PROGRESS) {
                throw new WorkflowException("节点状态异常，当前节点状态：%s", nodeStatus.getDesc());
            }

            // 执行更新
            doUpdateApprover(tenantId, Collections.singletonList(taskInstance), sourceApproverId, targetApprover);
        });
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
    @Override
    public void replaceApprover(WorkflowInstance workflowInstance, String sourceApproverId, Approver targetApprover) {
        JdbcTemplateHelper jdbcTemplateHelper = this.context.getJdbcTemplateHelper();
        jdbcTemplateHelper.executeInTransaction(() -> {
            String tenantId = workflowInstance.getTenantId();
            Integer workflowInstanceId = workflowInstance.getId();

            TaskInstanceExecutor taskInstanceExecutor = taskInstanceExecutorBuilder.build();
            QueryBuilderWrapper.Builder<TaskInstance> builder = QueryBuilderWrapper.createLambda(TaskInstance.class)
                    .eq(TaskInstance::getTenantId, tenantId)
                    .eq(TaskInstance::getStatus, NodeStatus.IN_PROGRESS.getCode())
                    .eq(TaskInstance::getState, 1);
            if (workflowInstanceId != null) {
                builder.eq(TaskInstance::getWorkflowInstanceId, workflowInstanceId);
            }
            Query query = builder.build();
            Page<TaskInstance> taskInstancePage = taskInstanceExecutor.find(query);
            List<TaskInstance> taskInstances = taskInstancePage.getRecords();
            if (taskInstances.isEmpty()) {
                WorkflowException.throwWorkflowInstanceNotFoundException();
            }

            // 执行更新
            doUpdateApprover(tenantId, taskInstances, sourceApproverId, targetApprover);
        });
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
    @Override
    public void replaceRoleApprover(String tenantId, String sourceApproverRoleId, String sourceApproverId, RoleApprover targetRoleApprover) {
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
            doUpdateRoleApprover(tenantId, taskInstances, sourceApproverRoleId, sourceApproverId, targetRoleApprover);
        });
    }

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
    @Override
    public void replaceRoleApprover(TaskInstance taskInstance, String sourceApproverRoleId, String sourceApproverId, RoleApprover targetRoleApprover) {
        JdbcTemplateHelper jdbcTemplateHelper = this.context.getJdbcTemplateHelper();
        jdbcTemplateHelper.executeInTransaction(() -> {
            String tenantId = taskInstance.getTenantId();
            NodeStatus nodeStatus = taskInstance.getStatus();
            if (nodeStatus != NodeStatus.IN_PROGRESS) {
                throw new WorkflowException("节点状态异常，当前节点状态：%s", nodeStatus.getDesc());
            }

            // 执行更新
            doUpdateRoleApprover(tenantId, Collections.singletonList(taskInstance), sourceApproverRoleId, sourceApproverId, targetRoleApprover);
        });
    }

    /**
     * 替换角色审批人（未审批状态下）
     *
     * @param workflowInstance     流程实例
     * @param sourceApproverRoleId 原审批角色
     * @param sourceApproverId     原审批人
     * @param targetRoleApprover   新审批人
     *
     * @author wangweijun
     * @since 2024/9/10 19:36
     */
    @Override
    public void replaceRoleApprover(WorkflowInstance workflowInstance, String sourceApproverRoleId, String sourceApproverId, RoleApprover targetRoleApprover) {
        JdbcTemplateHelper jdbcTemplateHelper = this.context.getJdbcTemplateHelper();
        jdbcTemplateHelper.executeInTransaction(() -> {
            String tenantId = workflowInstance.getTenantId();
            Integer workflowInstanceId = workflowInstance.getId();

            TaskInstanceExecutor taskInstanceExecutor = taskInstanceExecutorBuilder.build();
            QueryBuilderWrapper.Builder<TaskInstance> builder = QueryBuilderWrapper.createLambda(TaskInstance.class)
                    .eq(TaskInstance::getTenantId, tenantId)
                    .eq(TaskInstance::getStatus, NodeStatus.IN_PROGRESS.getCode())
                    .eq(TaskInstance::getState, 1);
            if (workflowInstanceId != null) {
                builder.eq(TaskInstance::getWorkflowInstanceId, workflowInstanceId);
            }
            Query query = builder.build();
            Page<TaskInstance> taskInstancePage = taskInstanceExecutor.find(query);
            List<TaskInstance> taskInstances = taskInstancePage.getRecords();
            if (taskInstances.isEmpty()) {
                WorkflowException.throwWorkflowInstanceNotFoundException();
            }

            // 执行更新
            doUpdateRoleApprover(tenantId, taskInstances, sourceApproverRoleId, sourceApproverId, targetRoleApprover);
        });
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
    @Override
    public boolean isRoleTaskInstance(String tenantId, Integer taskInstanceId) {
        // 获取审批实例
        TaskInstanceExecutor taskInstanceExecutor = taskInstanceExecutorBuilder.build();
        TaskInstance taskInstance = taskInstanceExecutor.getById(taskInstanceId);
        // 获取节点定义
        Integer nodeDefinitionId = taskInstance.getNodeDefinitionId();
        NodeDefinitionExecutor nodeDefinitionExecutor = nodeDefinitionExecutorBuilder.build();
        NodeDefinition nodeDefinition = nodeDefinitionExecutor.getById(nodeDefinitionId);
        // 判断是否时角色审批
        return nodeDefinition.isRoleApprove();
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
    @Override
    public String getRoleIdByTaskInstanceId(String tenantId, Integer taskInstanceId, String approverId) {
        // 获取审批实例
        TaskInstanceExecutor taskInstanceExecutor = taskInstanceExecutorBuilder.build();
        TaskInstance taskInstance = taskInstanceExecutor.getById(taskInstanceId);
        // 获取节点定义
        Integer nodeDefinitionId = taskInstance.getNodeDefinitionId();
        NodeDefinitionExecutor nodeDefinitionExecutor = nodeDefinitionExecutorBuilder.build();
        NodeDefinition nodeDefinition = nodeDefinitionExecutor.getById(nodeDefinitionId);
        if (nodeDefinition.isUserApprove()) {
            return null;
        }
        // 获取角色审批用户
        NodeRoleAssignmentExecutor nodeRoleAssignmentExecutor = nodeRoleAssignmentExecutorBuilder.build();
        NodeRoleAssignment nodeRoleAssignment = nodeRoleAssignmentExecutor.getByNodeDefinitionIdAndApproverId(tenantId, nodeDefinitionId, approverId);
        return nodeRoleAssignment == null ? null : nodeRoleAssignment.getRoleId();
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
    @Override
    public TaskApprove getTaskApprove(String tenantId, Integer taskInstanceId, String approverId) {
        TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder.build();
        return taskApproveExecutor.getByTaskInstanceIdAndApproverId(tenantId, taskInstanceId, approverId);
    }

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
    @Override
    public TaskRoleApprove getTaskRoleApprove(String tenantId, Integer taskInstanceId, String roleId, String approverId) {
        // 获取角色审批记录
        TaskRoleApproveRecordExecutor taskRoleApproveRecordExecutor = taskRoleApproveRecordExecutorBuilder.build();
        TaskRoleApproveRecord taskRoleApproveRecord = taskRoleApproveRecordExecutor.getByTaskInstanceIdAndRoleIdAndUserId(tenantId, taskInstanceId, roleId, approverId);
        if (taskRoleApproveRecord == null) {
            return null;
        }
        // 获取审批记录
        TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder.build();
        TaskApprove taskApprove = taskApproveExecutor.getById(taskRoleApproveRecord.getTaskApproveId());
        // 获取角色审批人
        NodeRoleAssignmentExecutor nodeRoleAssignmentExecutor = nodeRoleAssignmentExecutorBuilder.build();
        NodeRoleAssignment nodeRoleAssignment = nodeRoleAssignmentExecutor.getById(taskRoleApproveRecord.getNodeRoleAssignmentId());
        // 封装
        return TaskRoleApprove.of(taskRoleApproveRecord, taskApprove, nodeRoleAssignment);
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
    @Override
    public List<TaskApprove> findTaskApproves(String tenantId, Integer workflowInstanceId) {
        TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder.build();
        return taskApproveExecutor.findByTWorkflowInstanceId(tenantId, workflowInstanceId);
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
    @Override
    public List<TaskRoleApprove> findTaskRoleApproves(String tenantId, Integer workflowInstanceId) {
        TaskRoleApproveRecordExecutor taskRoleApproveRecordExecutor = taskRoleApproveRecordExecutorBuilder.build();
        List<TaskRoleApproveRecord> taskRoleApproveRecords = taskRoleApproveRecordExecutor.findByTWorkflowInstanceId(tenantId, workflowInstanceId);
        return taskRoleApproveRecords.stream().map(taskRoleApproveRecord -> {
            // 获取审批记录
            TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder.build();
            TaskApprove taskApprove = taskApproveExecutor.getById(taskRoleApproveRecord.getTaskApproveId());
            // 获取角色审批人
            NodeRoleAssignmentExecutor nodeRoleAssignmentExecutor = nodeRoleAssignmentExecutorBuilder.build();
            NodeRoleAssignment nodeRoleAssignment = nodeRoleAssignmentExecutor.getById(taskRoleApproveRecord.getNodeRoleAssignmentId());
            // 封装
            return TaskRoleApprove.of(taskRoleApproveRecord, taskApprove, nodeRoleAssignment);
        }).toList();
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
    @Override
    public WorkflowInstance getWorkflowInstanceById(String tenantId, Integer workflowInstanceId) {
        WorkflowInstanceExecutor workflowInstanceExecutor = workflowInstanceExecutorBuilder.build();
        WorkflowInstance workflowInstance = workflowInstanceExecutor.getById(workflowInstanceId);
        if (workflowInstance == null || !tenantId.equals(workflowInstance.getTenantId())) {
            return null;
        }
        return workflowInstance;
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
    @Override
    public WorkflowInstance getWorkflowInstanceByTaskInstanceId(String tenantId, Integer taskInstanceId) {
        TaskInstanceExecutor taskInstanceExecutor = taskInstanceExecutorBuilder.build();
        TaskInstance taskInstance = taskInstanceExecutor.getById(taskInstanceId);
        if (taskInstance == null || !tenantId.equals(taskInstance.getTenantId())) {
            return null;
        }
        return this.getWorkflowInstanceById(tenantId, taskInstance.getWorkflowInstanceId());
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
    @Override
    public Page<WorkflowInstance> findWorkflowInstancesByKey(String tenantId, String key, List<WorkflowStatus> workflowStatuses, Integer page, Integer pageSize) {
        WorkflowInstanceExecutor workflowInstanceExecutor = workflowInstanceExecutorBuilder.build();
        return workflowInstanceExecutor.findWorkflowInstancesByKey(tenantId, key, workflowStatuses, page, pageSize);
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
    @Override
    public List<TaskDynamicAssignment> findTaskDynamicAssignments(String tenantId, Integer taskInstanceId) {
        // 查找任务实例
        TaskInstanceExecutor taskInstanceExecutor = taskInstanceExecutorBuilder.build();
        TaskInstance taskInstance = taskInstanceExecutor.getById(taskInstanceId);
        // 查找节点定义
        Integer nodeDefinitionId = taskInstance.getNodeDefinitionId();
        NodeDefinitionExecutor nodeDefinitionExecutor = nodeDefinitionExecutorBuilder.build();
        NodeDefinition nodeDefinition = nodeDefinitionExecutor.getById(tenantId, nodeDefinitionId);
        // 非动态节点，直接返回
        if (!nodeDefinition.isDynamic()) {
            return List.of();
        }
        TaskDynamicAssignmentExecutor taskDynamicAssignmentExecutor = taskDynamicAssignmentExecutorBuilder.build();
        return taskDynamicAssignmentExecutor.findByTaskInstanceId(tenantId, taskInstanceId);
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
    @Override
    public boolean isDynamicNodeAndUnSettingApprovers(String tenantId, Integer taskInstanceId) {
        // 判断是否是动态审批节点
        if (!this.isDynamicNode(tenantId, taskInstanceId)) {
            return false;
        }
        // 判断是否设置了动态审批人
        List<TaskDynamicAssignment> taskDynamicAssignments = this.findTaskDynamicAssignments(tenantId, taskInstanceId);
        return CollectionUtils.isEmpty(taskDynamicAssignments);
    }

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
    @Override
    public boolean isDynamicNode(String tenantId, Integer taskInstanceId) {
        // 查找任务实例
        TaskInstanceExecutor taskInstanceExecutor = taskInstanceExecutorBuilder.build();
        TaskInstance taskInstance = taskInstanceExecutor.getById(taskInstanceId);
        // 查找节点定义
        Integer nodeDefinitionId = taskInstance.getNodeDefinitionId();
        NodeDefinitionExecutor nodeDefinitionExecutor = nodeDefinitionExecutorBuilder.build();
        NodeDefinition nodeDefinition = nodeDefinitionExecutor.getById(tenantId, nodeDefinitionId);
        // 判断是否是动态审批节点
        return nodeDefinition.isDynamic();
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
    @Override
    public List<WorkflowInstance> findWorkflowInstances(String tenantId, WorkflowStatus workflowStatus, Integer workflowDefinitionId) {
        // 检查是否有正在进行的流程实例
        WorkflowInstanceExecutor workflowInstanceExecutor = this.workflowInstanceExecutorBuilder.build();
        QueryBuilderWrapper.Builder<WorkflowInstance> builder = QueryBuilderWrapper.createLambda(WorkflowInstance.class)
                .eq(WorkflowInstance::getTenantId, tenantId)
                .eq(WorkflowInstance::getWorkflowDefinitionId, workflowDefinitionId)
                .eq(WorkflowInstance::getState, 1);
        if (workflowStatus != null) {
            builder.eq(WorkflowInstance::getStatus, workflowStatus.getCode());
        }
        Query query = builder.build();
        Page<WorkflowInstance> page = workflowInstanceExecutor.find(query);
        return page.getRecords();
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
    @Override
    public List<WorkflowInstance> findWorkflowInstances(String tenantId, WorkflowStatus workflowStatus, String workflowDefinitionKey) {
        WorkflowDefinitionExecutor workflowDefinitionExecutor = workflowDefinitionExecutorBuilder.build();
        WorkflowDefinition workflowDefinition = workflowDefinitionExecutor.getByTenantAndKey(tenantId, workflowDefinitionKey);
        return this.findWorkflowInstances(tenantId, workflowStatus, workflowDefinition.getId());
    }

    /**
     * 设置业务信息
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     * @param businessInfo       业务信息
     *
     * @author wangweijun
     * @since 2024/12/9 19:39
     */
    @Override
    public void setBusinessInfo(String tenantId, Integer workflowInstanceId, BusinessInfo businessInfo) {
        WorkflowInstance workflowInstance = getWorkflowInstanceById(tenantId, workflowInstanceId);
        workflowInstance.setBusinessInfo(businessInfo);
        WorkflowInstanceExecutor workflowInstanceExecutor = workflowInstanceExecutorBuilder.build();
        workflowInstanceExecutor.updateById(workflowInstance);
    }

    /**
     * 锁定流程定义（不会改变流程发布状态）
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     */
    @Override
    public void lock(String tenantId, Integer workflowDefinitionId) {
        WorkflowDefinition workflowDefinition = getWorkflowDefinition(tenantId, workflowDefinitionId);
        this.doLock(workflowDefinition);
    }

    /**
     * 锁定流程定义（不会改变流程发布状态）
     *
     * @param tenantId              租户 ID
     * @param workflowDefinitionKey 流程定义 key
     */
    @Override
    public void lock(String tenantId, String workflowDefinitionKey) {
        WorkflowDefinition workflowDefinition = getWorkflowDefinition(tenantId, workflowDefinitionKey);
        this.doLock(workflowDefinition);
    }

    /**
     * 解锁流程定义（不会更新流程定义）
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     */
    @Override
    public void unlock(String tenantId, Integer workflowDefinitionId) {
        WorkflowDefinition workflowDefinition = getWorkflowDefinition(tenantId, workflowDefinitionId);
        this.doUnlock(workflowDefinition);
    }

    /**
     * 解锁流程定义（不会更新流程定义）
     *
     * @param tenantId              租户 ID
     * @param workflowDefinitionKey 流程定义 key
     */
    @Override
    public void unlock(String tenantId, String workflowDefinitionKey) {
        WorkflowDefinition workflowDefinition = getWorkflowDefinition(tenantId, workflowDefinitionKey);
        this.doUnlock(workflowDefinition);
    }

    /**
     * 锁定
     *
     * @param workflowDefinition 流程定义
     *
     * @author wangweijun
     * @since 2024/12/13 15:01
     */
    private void doLock(WorkflowDefinition workflowDefinition) {
        WorkflowDefinitionExecutor workflowDefinitionExecutor = workflowDefinitionExecutorBuilder.build();
        workflowDefinition.setLock(true);
        workflowDefinitionExecutor.updateById(workflowDefinition);
    }

    /**
     * 解锁
     *
     * @param workflowDefinition 流程定义
     *
     * @author wangweijun
     * @since 2024/12/13 15:01
     */
    private void doUnlock(WorkflowDefinition workflowDefinition) {
        WorkflowDefinitionExecutor workflowDefinitionExecutor = workflowDefinitionExecutorBuilder.build();
        workflowDefinition.setLock(false);
        workflowDefinitionExecutor.updateById(workflowDefinition);
    }

    /**
     * doUpdateApprover
     *
     * @param tenantId         租户 ID
     * @param taskInstances    流程实例列表
     * @param sourceApproverId 原审批人
     * @param targetApprover   新审批人
     *
     * @author wangweijun
     * @since 2024/9/12 11:10
     */
    private void doUpdateApprover(String tenantId, List<TaskInstance> taskInstances, String sourceApproverId, Approver targetApprover) {
        NodeAssignmentExecutor nodeAssignmentExecutor = nodeAssignmentExecutorBuilder.build();
        TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder.build();

        for (TaskInstance taskInstance : taskInstances) {
            // 角色审批节点
            if (taskInstance.isRoleApprove()) {
                throw new WorkflowException("方法调用错误，请使用角色审批人更新接口");
            }
            Integer nodeDefinitionId = taskInstance.getNodeDefinitionId();
            // 更新用户任务关联表
            Query query = QueryBuilderWrapper.createLambda(NodeAssignment.class)
                    .eq(NodeAssignment::getTenantId, tenantId)
                    .eq(NodeAssignment::getNodeDefinitionId, nodeDefinitionId)
                    .eq(NodeAssignment::getApproverId, sourceApproverId)
                    .eq(NodeAssignment::getState, 1)
                    .build();
            Page<NodeAssignment> nodeAssignmentPage = nodeAssignmentExecutor.find(query);
            List<NodeAssignment> nodeAssignments = nodeAssignmentPage.getRecords();
            if (CollectionUtils.isNotEmpty(nodeAssignments)) {
                for (NodeAssignment nodeAssignment : nodeAssignments) {
                    // 设置为目标值
                    nodeAssignment.setApproverId(targetApprover.getId());
                    nodeAssignment.setApproverName(targetApprover.getName());
                    nodeAssignment.setApproverDesc(targetApprover.getDesc());
                    // 更新
                    nodeAssignmentExecutor.updateById(nodeAssignment);
                    // 记录日志
                    String message = String.format("用户任务关联表 ID: %s, 原审批人: [%s]，现审批人: [%s]", nodeAssignment.getId(), sourceApproverId, targetApprover);
                    recordLogs(tenantId, taskInstance.getWorkflowInstanceId(), taskInstance.getId(), TaskHistoryMessage.NODE_ASSIGNMENT_CHANGED.getTemplate(), TaskHistoryMessage.custom(message));
                }
            }
            // 更新审批人
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
                    taskApprove.setApproverId(targetApprover.getId());
                    taskApprove.setApproverIdType(ApproverIdType.USER);
                    // 更新
                    taskApproveExecutor.updateById(taskApprove);
                    // 记录日志
                    String message = String.format("任务实例审批表 ID: %s, 原审批人: [%s], 现审批人: [%s]", taskApprove.getId(), sourceApproverId, targetApprover);
                    recordLogs(tenantId, taskInstance.getWorkflowInstanceId(), taskInstance.getId(), TaskHistoryMessage.TASK_APPROVE_CHANGED.getTemplate(), TaskHistoryMessage.custom(message));
                }
            }
        }
    }

    private void doUpdateRoleApprover(String tenantId, List<TaskInstance> taskInstances, String sourceApproverRoleId, String sourceApproverId, RoleApprover targetRoleApprover) {
        NodeAssignmentExecutor nodeAssignmentExecutor = nodeAssignmentExecutorBuilder.build();
        TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder.build();
        NodeRoleAssignmentExecutor nodeRoleAssignmentExecutor = nodeRoleAssignmentExecutorBuilder.build();
        TaskRoleApproveRecordExecutor taskRoleApproveRecordExecutor = taskRoleApproveRecordExecutorBuilder.build();

        for (TaskInstance taskInstance : taskInstances) {
            // 角色审批节点
            if (!taskInstance.isRoleApprove()) {
                throw new WorkflowException("方法调用错误，请使用用户审批人更新接口");
            }
            Integer nodeDefinitionId = taskInstance.getNodeDefinitionId();

            // 更新角色用户关联表：NodeRoleAssignment
            Query query = QueryBuilderWrapper.createLambda(NodeRoleAssignment.class)
                    .eq(NodeRoleAssignment::getTenantId, tenantId)
                    .eq(NodeRoleAssignment::getNodeDefinitionId, nodeDefinitionId)
                    .eq(NodeRoleAssignment::getRoleId, sourceApproverRoleId)
                    .eq(NodeRoleAssignment::getUserId, sourceApproverId)
                    .eq(NodeRoleAssignment::getState, 1)
                    .build();
            NodeRoleAssignment nodeRoleAssignment = nodeRoleAssignmentExecutor.get(query);
            if (nodeRoleAssignment == null) {
                throw new WorkflowException("角色用户不存在: 角色 ID: %s, 用户 ID: %s", sourceApproverRoleId, sourceApproverId);
            }

            query = QueryBuilderWrapper.createLambda(NodeRoleAssignment.class)
                    .eq(NodeRoleAssignment::getTenantId, tenantId)
                    .eq(NodeRoleAssignment::getNodeDefinitionId, nodeDefinitionId)
                    .eq(NodeRoleAssignment::getRoleId, targetRoleApprover.getRoleId())
                    .eq(NodeRoleAssignment::getUserId, targetRoleApprover.getUserId())
                    .eq(NodeRoleAssignment::getState, 1)
                    .build();
            NodeRoleAssignment targetNodeRoleAssignment = nodeRoleAssignmentExecutor.get(query);
            if (targetNodeRoleAssignment != null) {
                throw new WorkflowException("目标角色用户已存在: 角色 ID: %s, 用户 ID: %s", targetRoleApprover.getRoleId(), targetRoleApprover.getUserId());
            }

            // 创建：新 NodeRoleAssignment
            targetNodeRoleAssignment = NodeRoleAssignment.copyOf(nodeRoleAssignment);
            targetNodeRoleAssignment.setRoleId(targetRoleApprover.getRoleId());
            targetNodeRoleAssignment.setRoleName(targetRoleApprover.getRoleName());
            targetNodeRoleAssignment.setRoleDesc(targetRoleApprover.getRoleDesc());
            targetNodeRoleAssignment.setUserId(targetRoleApprover.getUserId());
            targetNodeRoleAssignment.setUserName(targetRoleApprover.getUserName());
            targetNodeRoleAssignment.setUserDesc(targetRoleApprover.getUserDesc());
            // 新增：新 NodeRoleAssignment
            nodeRoleAssignmentExecutor.save(targetNodeRoleAssignment);
            // 删除：原 NodeRoleAssignment
            nodeRoleAssignmentExecutor.delete(nodeRoleAssignment);
            // 记录日志
            String message = String.format("角色用户关联表 ID: %s, 原审批人: [%s, %s]，现审批人: [%s， %s]", targetNodeRoleAssignment.getId(), sourceApproverRoleId, sourceApproverId, targetNodeRoleAssignment.getRoleId(), targetNodeRoleAssignment.getUserId());
            recordLogs(tenantId, taskInstance.getWorkflowInstanceId(), taskInstance.getId(), TaskHistoryMessage.NODE_ROLE_ASSIGNMENT_CHANGED.getTemplate(), TaskHistoryMessage.custom(message));

            // 更新审批人
            query = QueryBuilderWrapper.createLambda(TaskApprove.class)
                    .eq(TaskApprove::getTenantId, tenantId)
                    .eq(TaskApprove::getWorkflowInstanceId, taskInstance.getWorkflowInstanceId())
                    .eq(TaskApprove::getTaskInstanceId, taskInstance.getId())
                    .eq(TaskApprove::getApproverId, sourceApproverRoleId)
                    .eq(TaskApprove::getStatus, ApproveStatus.IN_PROGRESS.getCode())
                    .eq(TaskApprove::getActive, ActiveStatus.ACTIVE.getCode())
                    .eq(TaskApprove::getState, 1)
                    .build();
            TaskApprove taskApprove = taskApproveExecutor.get(query);
            if (taskApprove == null) {
                throw new WorkflowException("任务实例审批人不存在: TaskApprove");
            }
            // 原角色审批人和目标角色审批人角色不同
            TaskApprove targetTaskApprove = taskApprove;
            if (!Objects.equals(sourceApproverRoleId, targetRoleApprover.getRoleId())) {
                // 如果角色用户为空，则删除
                List<NodeRoleAssignment> sourceNodeRoleAssignments = nodeRoleAssignmentExecutor.findByNodeDefinitionIdRoleId(tenantId, nodeDefinitionId, sourceApproverRoleId);
                if (sourceNodeRoleAssignments.isEmpty()) {
                    taskApproveExecutor.delete(taskApprove);
                }
                // 查询目标组是否存在
                query = QueryBuilderWrapper.createLambda(TaskApprove.class)
                        .eq(TaskApprove::getTenantId, tenantId)
                        .eq(TaskApprove::getWorkflowInstanceId, taskInstance.getWorkflowInstanceId())
                        .eq(TaskApprove::getTaskInstanceId, taskInstance.getId())
                        .eq(TaskApprove::getApproverId, targetRoleApprover.getRoleId())
                        .eq(TaskApprove::getStatus, ApproveStatus.IN_PROGRESS.getCode())
                        .eq(TaskApprove::getActive, ActiveStatus.ACTIVE.getCode())
                        .eq(TaskApprove::getState, 1)
                        .build();
                targetTaskApprove = taskApproveExecutor.get(query);
                if (targetTaskApprove == null) {
                    // 新增：新 TaskApprove
                    targetTaskApprove = TaskApprove.copyOf(taskApprove);
                    targetTaskApprove.setApproverId(targetRoleApprover.getRoleId());
                    targetTaskApprove.setApproverIdType(ApproverIdType.ROLE);
                    taskApproveExecutor.save(targetTaskApprove);
                    // 记录日志
                    message = String.format("任务实例审批表 ID: %s, 原审批人: [%s], 现审批人: [%s]", taskApprove.getId(), sourceApproverId, targetRoleApprover);
                    recordLogs(tenantId, taskInstance.getWorkflowInstanceId(), taskInstance.getId(), TaskHistoryMessage.TASK_APPROVE_CHANGED.getTemplate(), TaskHistoryMessage.custom(message));
                }
            }

            // 更新角色审批记录：TaskRoleApproveRecord
            query = QueryBuilderWrapper.createLambda(TaskRoleApproveRecord.class)
                    .eq(TaskRoleApproveRecord::getTenantId, tenantId)
                    .eq(TaskRoleApproveRecord::getWorkflowInstanceId, taskInstance.getWorkflowInstanceId())
                    .eq(TaskRoleApproveRecord::getTaskInstanceId, taskInstance.getId())
                    .eq(TaskRoleApproveRecord::getTaskApproveId, taskApprove.getId())
                    .eq(TaskRoleApproveRecord::getNodeRoleAssignmentId, nodeRoleAssignment.getId())
                    .eq(TaskRoleApproveRecord::getState, 1)
                    .build();
            TaskRoleApproveRecord taskRoleApproveRecord = taskRoleApproveRecordExecutor.get(query);
            if (taskRoleApproveRecord == null) {
                throw new WorkflowException("角色任务实例审批记录不存在: TaskRoleApproveRecord");
            }
            // 新增：新 TaskRoleApproveRecord
            TaskRoleApproveRecord newTaskRoleApproveRecord = TaskRoleApproveRecord.copyOf(taskRoleApproveRecord);
            newTaskRoleApproveRecord.setTaskApproveId(targetTaskApprove.getId());
            newTaskRoleApproveRecord.setNodeRoleAssignmentId(targetNodeRoleAssignment.getId());
            taskRoleApproveRecordExecutor.save(newTaskRoleApproveRecord);
            // 删除：旧 TaskRoleApproveRecord
            taskRoleApproveRecordExecutor.delete(taskRoleApproveRecord);

            // 更新用户任务关联表：NodeAssignment
            query = QueryBuilderWrapper.createLambda(NodeAssignment.class)
                    .eq(NodeAssignment::getTenantId, tenantId)
                    .eq(NodeAssignment::getNodeDefinitionId, nodeDefinitionId)
                    .eq(NodeAssignment::getApproverId, sourceApproverRoleId)
                    .eq(NodeAssignment::getState, 1)
                    .build();
            NodeAssignment nodeAssignment = nodeAssignmentExecutor.get(query);
            if (nodeAssignment == null) {
                throw new WorkflowException("节点用户定义不存在: NodeAssignment");
            }
            // 原角色审批人和目标角色审批人角色不同
            NodeAssignment targetNodeAssignment = nodeAssignment;
            if (!Objects.equals(sourceApproverRoleId, targetRoleApprover.getRoleId())) {
                query = QueryBuilderWrapper.createLambda(NodeAssignment.class)
                        .eq(NodeAssignment::getTenantId, tenantId)
                        .eq(NodeAssignment::getNodeDefinitionId, nodeDefinitionId)
                        .eq(NodeAssignment::getApproverId, targetRoleApprover.getRoleId())
                        .eq(NodeAssignment::getState, 1)
                        .build();
                targetNodeAssignment = nodeAssignmentExecutor.get(query);
                if (targetNodeAssignment == null) {
                    // 设置为目标值
                    targetNodeAssignment = NodeAssignment.copyOf(nodeAssignment);
                    targetNodeAssignment.setApproverId(targetRoleApprover.getRoleId());
                    targetNodeAssignment.setApproverName(targetRoleApprover.getRoleName());
                    targetNodeAssignment.setApproverDesc(targetRoleApprover.getRoleDesc());
                    // 新增：新 NodeAssignment
                    nodeAssignmentExecutor.save(targetNodeAssignment);
                }
                // 如果角色用户为空，则删除
                List<NodeRoleAssignment> sourceNodeRoleAssignments = nodeRoleAssignmentExecutor.findByNodeDefinitionIdRoleId(tenantId, nodeDefinitionId, sourceApproverRoleId);
                if (sourceNodeRoleAssignments.isEmpty()) {
                    // 删除：原 NodeAssignment
                    nodeAssignmentExecutor.delete(nodeAssignment);
                }

                // 记录日志
                message = String.format("用户任务关联表 ID: %s, 原审批人: [%s, %s]，现审批人: [%s]", nodeAssignment.getId(), sourceApproverRoleId, sourceApproverId, targetRoleApprover);
                recordLogs(tenantId, taskInstance.getWorkflowInstanceId(), taskInstance.getId(), TaskHistoryMessage.NODE_ASSIGNMENT_CHANGED.getTemplate(), TaskHistoryMessage.custom(message));
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
                // 将下一个审批人设置为：IN_PROGRESS
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
