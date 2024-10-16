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
import io.github.thebesteric.framework.agile.plugins.workflow.service.WorkflowService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

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
    private final WorkflowService workflowService;

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
        workflowService = new WorkflowServiceImpl(context);
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
                            .filter(nodeAssignment -> nodeAssignment.getApproverId().startsWith(WorkflowConstants.DYNAMIC_ASSIGNMENT_APPROVER_VALUE_PREFIX)).findAny();
                    // 存在动态指定审批人，且待审批人未配置
                    if (nextNodeDefinition.isDynamicAssignment() && anyDynamicAssignmentApprover.isPresent()) {
                        throw new WorkflowException("动态审批节点，请先设置审批人");
                    }

                    // 保存任务节点
                    taskInstanceExecutor = taskInstanceExecutorBuilder.status(NodeStatus.IN_PROGRESS)
                            .tenantId(tenantId)
                            .workflowInstanceId(workflowInstanceId).nodeDefinitionId(nextNodeDefinitionId)
                            .roleApprove(nextNodeDefinition.isRoleApprove())
                            .approvedCount(0)
                            // 设置总需要审批的次数
                            .totalCount(this.calcTotalCount(workflowDefinition, nextNodeDefinition, nextNodeAssignments))
                            .build();
                    TaskInstance nextTaskInstance = taskInstanceExecutor.save();
                    Integer nextTaskInstanceId = nextTaskInstance.getId();

                    List<TaskApprove> taskApproves = new ArrayList<>();
                    // 存在审批人：创建任务实例审批人
                    if (CollUtil.isNotEmpty(nextNodeAssignments)) {
                        int i = 0;
                        ApproverIdType approverIdType = nextNodeDefinition.isRoleApprove() ? ApproverIdType.ROLE : ApproverIdType.USER;
                        for (NodeAssignment nextNodeAssignment : nextNodeAssignments) {
                            TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder
                                    .newEntity()
                                    .tenantId(tenantId)
                                    .workflowInstanceId(workflowInstanceId)
                                    .taskInstanceId(nextTaskInstanceId)
                                    .approverId(nextNodeAssignment.getApproverId())
                                    .approverIdType(approverIdType)
                                    .approveSeq(nextNodeAssignment.getApproverSeq())
                                    .status(ApproveStatus.IN_PROGRESS)
                                    .active(ActiveStatus.ACTIVE).build();
                            // 顺序审批，后续审批人需要等待前一个审批人审批完成，后续的审批状态设置为：挂起
                            if (ApproveType.SEQ == nextNodeDefinition.getApproveType() && i > 0) {
                                taskApproveExecutorBuilder.status(ApproveStatus.SUSPEND);
                            }
                            TaskApprove taskApprove = taskApproveExecutor.save();
                            taskApproves.add(taskApprove);
                            i++;
                        }
                    }
                    // 没有审批人: 允许自动同意，则自动同意
                    else if (workflowDefinition.isAllowEmptyAutoApprove()) {
                        this.approve(tenantId, nextTaskInstanceId, null, WorkflowConstants.AUTO_APPROVER, WorkflowConstants.AUTO_APPROVER_COMMENT);
                    }
                    // 其他未知情况
                    else {
                        throw new WorkflowException("未知异常，请联系系统管理员");
                    }

                    // 审批人列表为空
                    if (taskApproves.isEmpty()) {
                        // 获取当流程实例的默认审批人
                        Set<Approver> whenEmptyApprovers = workflowDefinition.getWhenEmptyApprovers();
                        // 实例的默认审批人为空，且不允许空节点自动审核
                        if (CollectionUtils.isEmpty(whenEmptyApprovers) && !workflowDefinition.isAllowEmptyAutoApprove()) {
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
        List<NodeRoleAssignment> nodeRoleAssignments = nodeRoleAssignmentExecutor.findByNodeDefinitionId(tenantId, nextNodeDefinition.getId());
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
                                .setStatus(roleIdIndex == 0 ? ApproveStatus.IN_PROGRESS : ApproveStatus.SUSPEND);
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
                            .setStatus(ApproveStatus.IN_PROGRESS);
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
                        taskRoleApproveRecord.setStatus(i == 0 ? ApproveStatus.IN_PROGRESS : ApproveStatus.SUSPEND);
                    }
                    // 如果角色审批类型是 SEQ，则只有第一个角色用户为 IN_PROGRESS，其他为 SUSPEND
                    else if (RoleApproveType.SEQ == roleApproveType || RoleApproveType.ALL == roleApproveType) {
                        taskRoleApproveRecord.setStatus(roleIdIndex == 0 && i == 0 ? ApproveStatus.IN_PROGRESS : ApproveStatus.SUSPEND);
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
            List<NodeRoleAssignment> nodeRoleAssignments = nodeRoleAssignmentExecutor.findByNodeDefinitionId(tenantId, nodeDefinitionId);
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
        return ApproveType.ANY == approveType || (workflowDefinition.isAllowEmptyAutoApprove() && nodeAssignmentSize == 0) ? 1 : nodeAssignmentSize;
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
                    taskInstanceExecutorBuilder.workflowInstanceId(workflowInstance.getId())
                            .roleApprove(nextNodeDefinition.isRoleApprove())
                            .nodeDefinitionId(nextNodeDefinitionId);

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
                                .totalCount(this.calcTotalCount(workflowDefinition, nextNodeDefinition, nextNodeAssignments))
                                .approvedCount(0);
                    }

                    // 保存任务节点
                    taskInstanceExecutor = taskInstanceExecutorBuilder.build();
                    TaskInstance nextTaskInstance = taskInstanceExecutor.save();

                    // 如果允许自动审批，并且没有审批人，则自动审批
                    if (workflowDefinition.isAllowEmptyAutoApprove() && nextNodeAssignments.isEmpty()) {
                        this.approve(tenantId, nextTaskInstance.getId(), null, WorkflowConstants.AUTO_APPROVER, WorkflowConstants.AUTO_APPROVER_COMMENT);
                    }

                    // 创建任务实例审批人
                    List<TaskApprove> taskApproves = new ArrayList<>();
                    if (CollUtil.isNotEmpty(nextNodeAssignments) && NodeType.END != nextNodeDefinition.getNodeType()) {
                        int i = 0;
                        for (NodeAssignment nextNodeAssignment : nextNodeAssignments) {
                            taskApproveExecutorBuilder
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
                            ContinuousApproveMode continuousApproveMode = workflowDefinition.getContinuousApproveMode();
                            this.continuousApproveModeProcess(tenantId, nextTaskInstance, roleId, userId, null, nextNodeAssignment.getApproverId(), continuousApproveMode);
                        }
                    }

                    // 判断是否是角色审批，如果是角色审批则创建角色审批记录
                    if (nextNodeDefinition.isRoleApprove()) {
                        this.processTaskRoleApproveRecords(tenantId, nextNodeDefinition, nextTaskInstance, taskApproves);
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
     * @param roleId                当前审批人角色 ID
     * @param approverId            当前审批人 ID
     * @param nextRoleId            下一个审批人角色 ID
     * @param nextApproverId        下一个审批人 ID
     * @param continuousApproveMode 连续审批模式
     *
     * @author wangweijun
     * @since 2024/9/11 11:46
     */
    private void continuousApproveModeProcess(String tenantId, TaskInstance nextTaskInstance,
                                              String roleId, String approverId,
                                              String nextRoleId, String nextApproverId,
                                              ContinuousApproveMode continuousApproveMode) {

        TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder.build();

        // 获取该流程实例下已经完成审批和即将要审批的审批人
        List<TaskApprove> taskApproves = taskApproveExecutor.findByTWorkflowInstanceId(tenantId, nextTaskInstance.getWorkflowInstanceId(), null, null);

        // 下一个审批人的审批情况（下一个审批人在之前的审批记录中是否存在，并且是通过审核的）
        Optional<TaskApprove> nextApproverIdApproveOptional = taskApproves.stream()
                .filter(approve -> nextApproverId.equals(approve.getApproverId()) && ApproveStatus.APPROVED == approve.getStatus()).findAny();

        switch (continuousApproveMode) {
            case APPROVE_FIRST:
                // 下个审批人已经存在审批的节点，则自动审批
                if (nextApproverIdApproveOptional.isPresent()) {
                    this.approve(tenantId, nextTaskInstance.getId(), nextRoleId, nextApproverId, WorkflowConstants.AUTO_APPROVER_COMMENT);
                }
                break;
            case APPROVE_CONTINUOUS:
                // 下个审批人已经存在审批的节点，且已审批的节点的审批人和下一个节点的审批人是同一个人，则自动审批
                if (nextApproverIdApproveOptional.isPresent() && approverId.equals(nextApproverId)) {
                    this.approve(tenantId, nextTaskInstance.getId(), nextRoleId, nextApproverId, WorkflowConstants.AUTO_APPROVER_COMMENT);
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
     * @param roleId         角色 ID
     * @param userId         用户 ID
     * @param comment        审批意见
     */
    @Override
    public void approve(String tenantId, Integer taskInstanceId, String roleId, String userId, String comment) {
        JdbcTemplateHelper jdbcTemplateHelper = this.context.getJdbcTemplateHelper();
        jdbcTemplateHelper.executeInTransaction(() -> {

            // 检查审批意见是否必填
            checkIfRequiredComment(taskInstanceId, comment);

            // 修改当前 TaskInstance 的 approved_count 数量
            TaskInstanceExecutor taskInstanceExecutor = taskInstanceExecutorBuilder.build();
            TaskInstance taskInstance = taskInstanceExecutor.getById(taskInstanceId);

            TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder.build();
            TaskApprove taskApprove;
            // 自动审批的情况
            if (WorkflowConstants.AUTO_APPROVER.equals(userId)) {
                // 创建一个 approver
                taskApprove = TaskApproveBuilder.builder()
                        .tenantId(tenantId)
                        .workflowInstanceId(taskInstance.getWorkflowInstanceId())
                        .taskInstanceId(taskInstanceId)
                        .approverId(userId)
                        .active(ActiveStatus.INACTIVE)
                        .status(ApproveStatus.APPROVED)
                        .comment(comment)
                        .build();
                taskApproveExecutor.save(taskApprove);
            }
            // 非自动审批的情况
            else {
                taskApprove = taskApproveExecutor.getByTaskInstanceIdAndRoleIdAndApproverId(tenantId, taskInstanceId, ActiveStatus.ACTIVE, roleId, userId);
                // 用户审批的情况
                if (ApproverIdType.ROLE != taskApprove.getApproverIdType()) {
                    // 更新 TaskApprove 的 active、status 和 comment
                    taskApprove.setActive(ActiveStatus.INACTIVE);
                    taskApprove.setStatus(ApproveStatus.APPROVED);
                    taskApprove.setComment(comment);
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
                roleApproveGotoNext = this.processApproveRoleApprove(tenantId, nodeDefinition, taskInstance, taskApprove, roleId, userId, comment);
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
                        otherActiveTaskApproves.forEach(otherActiveTaskApprove -> {
                            otherActiveTaskApprove.convertToApproveStatusApproved(comment);
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

            // 没有下一级审批节点，且当前审批节点全部审批完成，则表示流程已经结束
            if (nextTaskInstances == null && taskInstance.isCompleted()) {
                // 创建结束节点实例
                NodeDefinition endNodeDefinition = nodeDefinitionExecutor.getEndNode(tenantId, nodeDefinition.getWorkflowDefinitionId());
                taskInstanceExecutor = taskInstanceExecutorBuilder.newInstance()
                        .tenantId(tenantId)
                        .workflowInstanceId(taskInstance.getWorkflowInstanceId())
                        .nodeDefinitionId(endNodeDefinition.getId())
                        .roleApprove(endNodeDefinition.isRoleApprove())
                        .status(NodeStatus.COMPLETED)
                        .build();
                taskInstanceExecutor.save();
                // 记录流程日志（审批结束）
                recordLogs(tenantId, taskInstance.getWorkflowInstanceId(), taskInstance.getId(), endNodeDefinition.getName(), TaskHistoryMessage.INSTANCE_ENDED);
            }
        });
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
                if (curTaskRoleApproveRecord == null || ApproveStatus.IN_PROGRESS != curTaskRoleApproveRecord.getStatus()) {
                    throw new WorkflowException("角色用户审批记录不存在或状态不正确");
                }
                curTaskRoleApproveRecord.setComment(comment);
                curTaskRoleApproveRecord.setStatus(ApproveStatus.APPROVED);
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
                        taskRoleApproveRecord.setStatus(ApproveStatus.IN_PROGRESS);
                        taskRoleApproveRecordExecutor.updateById(taskRoleApproveRecord);
                    });
                });
            }
            // 角色用户审批类型为：ALL
            else if (RoleUserApproveType.ALL == roleUserApproveType) {
                // 查找当前角色用户审批记录，并修改为：APPROVED
                TaskRoleApproveRecord curTaskRoleApproveRecord = taskRoleApproveRecordExecutor.getByTaskInstanceIdAndRoleIdAndUserId(tenantId, taskInstanceId, roleId, userId);
                if (curTaskRoleApproveRecord == null || ApproveStatus.IN_PROGRESS != curTaskRoleApproveRecord.getStatus()) {
                    throw new WorkflowException("角色用户审批记录不存在或状态不正确");
                }
                curTaskRoleApproveRecord.setComment(comment);
                curTaskRoleApproveRecord.setStatus(ApproveStatus.APPROVED);
                taskRoleApproveRecordExecutor.updateById(curTaskRoleApproveRecord);
            }

            // 审批人数 +1
            taskInstance.setApprovedCount(taskInstance.getApprovedCount() + 1);

            // 获取所有角色审批记录中，审批通过的用户，并按角色进行分组
            List<TaskRoleApproveRecord> approvedRecords = findApprovedTaskRoleApproveRecords(tenantId, taskInstanceId);

            // 获取所有角色审批用户
            List<NodeRoleAssignment> nodeRoleAssignments = nodeRoleAssignmentExecutor.findByNodeDefinitionId(tenantId, nodeDefinition.getId());
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
                // 获取 nodeRoleAssignmentsByRole 中，审批人员数量最少的角色
                Optional<Map.Entry<String, List<NodeRoleAssignment>>> minEntry = nodeRoleAssignmentsByRoles.entrySet().stream().min(Comparator.comparingInt(e -> e.getValue().size()));
                if (minEntry.isPresent()) {
                    // 获取最少的审批人员
                    List<NodeRoleAssignment> minValue = minEntry.get().getValue();
                    // 如果审批人员数量最少的只有 1 个
                    if (minValue.size() == 1) {
                        roleApproveGotoNext = true;
                    }
                }

                // 表示还没有角色完全审批完成
                if (!roleApproveGotoNext) {
                    // 查询该角色下有多少角色用户
                    List<NodeRoleAssignment> nodeRoleAssignmentsByRole = nodeRoleAssignmentsByRoles.get(roleId);
                    // 查询该角色下已经完成审核的用户
                    List<NodeRoleAssignment> approvedNodeRoleAssignments = roleApprovedMap.get(roleId);
                    // 如果数量相等则表示该角色下所有用户均完成了审核
                    if (nodeRoleAssignmentsByRole.size() == approvedNodeRoleAssignments.size()) {
                        roleApproveGotoNext = true;
                    }
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
                        List<TaskRoleApproveRecord> unApprovedRecords = taskRoleApproveRecords.stream().filter(i -> ApproveStatus.IN_PROGRESS == i.getStatus() || ApproveStatus.SUSPEND == i.getStatus()).toList();
                        // 将所有 IN_PROGRESS 的审批记录设置为 SKIPPED
                        unApprovedRecords.forEach(unApprovedRecord -> {
                            unApprovedRecord.setStatus(ApproveStatus.SKIPPED);
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
            if (curTaskRoleApproveRecord == null || ApproveStatus.IN_PROGRESS != curTaskRoleApproveRecord.getStatus()) {
                throw new WorkflowException("角色用户审批记录不存在或状态不正确");
            }
            curTaskRoleApproveRecord.setComment(comment);
            curTaskRoleApproveRecord.setStatus(ApproveStatus.APPROVED);
            taskRoleApproveRecordExecutor.updateById(curTaskRoleApproveRecord);

            // 当前角色下其他用户修改为：SKIPPED
            List<TaskRoleApproveRecord> nextTaskRoleApproveRecords = taskRoleApproveRecordExecutor.findByTaskInstanceIdAndRoleIdAndStatus(tenantId, taskInstanceId, roleId, ApproveStatus.IN_PROGRESS);
            for (TaskRoleApproveRecord nextTaskRoleApproveRecord : nextTaskRoleApproveRecords) {
                nextTaskRoleApproveRecord.setStatus(ApproveStatus.SKIPPED);
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
                List<NodeRoleAssignment> nodeRoleAssignments = nodeRoleAssignmentExecutor.findByNodeDefinitionId(tenantId, nodeDefinition.getId());
                // 从 nodeRoleAssignments 去除当前的 roleId，然后找到 roleSeq 最小的角色 ID
                nodeRoleAssignments.stream().filter(i -> !i.getRoleId().equals(roleId))
                        .filter(i -> i.getRoleSeq() > curNodeRoleAssignment.getRoleSeq())
                        .min(Comparator.comparingInt(NodeRoleAssignment::getRoleSeq))
                        .flatMap(i -> Optional.of(i.getRoleId())).ifPresent(nextRoleId -> {
                            // 获取下一个角色的所有角色用户的审批记录
                            List<TaskRoleApproveRecord> nextRoleTaskRoleApproveRecords = taskRoleApproveRecordExecutor.findByTaskInstanceIdAndRoleId(tenantId, taskInstanceId, nextRoleId);
                            // 将下一个角色的用户审批记录设置为：IN_PROGRESS
                            for (TaskRoleApproveRecord nextRoleTaskRoleApproveRecord : nextRoleTaskRoleApproveRecords) {
                                nextRoleTaskRoleApproveRecord.setStatus(ApproveStatus.IN_PROGRESS);
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
                if (ApproveStatus.APPROVED == i.getStatus()) {
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
            if (curTaskRoleApproveRecord == null || ApproveStatus.IN_PROGRESS != curTaskRoleApproveRecord.getStatus()) {
                throw new WorkflowException("角色用户审批记录不存在或状态不正确");
            }
            curTaskRoleApproveRecord.setComment(comment);
            curTaskRoleApproveRecord.setStatus(ApproveStatus.APPROVED);
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
                    List<NodeRoleAssignment> allNodeRoleAssignments = nodeRoleAssignmentExecutor.findByNodeDefinitionId(tenantId, nodeDefinition.getId());
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
                    .filter(i -> ApproveStatus.APPROVED == i.getStatus() || ApproveStatus.ABANDONED == i.getStatus()).toList();

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
            List<NodeRoleAssignment> nodeRoleAssignmentsByRole = findNodeRoleAssignmentsByRoleId(tenantId, nodeDefinition.getId(), roleId);

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
            if (curTaskRoleApproveRecord == null || ApproveStatus.IN_PROGRESS != curTaskRoleApproveRecord.getStatus()) {
                throw new WorkflowException("角色用户审批记录不存在或状态不正确");
            }
            curTaskRoleApproveRecord.setComment(comment);
            curTaskRoleApproveRecord.setStatus(ApproveStatus.APPROVED);
            taskRoleApproveRecordExecutor.updateById(curTaskRoleApproveRecord);

            // 查找下一个角色用户审批记录
            List<NodeRoleAssignment> nextNodeRoleAssignments = new ArrayList<>();
            List<TaskRoleApproveRecord> nextTaskRoleApproveRecords = taskRoleApproveRecordExecutor.findByTaskInstanceIdAndStatus(tenantId, taskInstanceId, ApproveStatus.SUSPEND);
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
                    taskRoleApproveRecord.setStatus(ApproveStatus.IN_PROGRESS);
                    taskRoleApproveRecordExecutor.updateById(taskRoleApproveRecord);
                });
            });

            // 审批人数 +1
            taskInstance.setApprovedCount(taskInstance.getApprovedCount() + 1);

            // 获取所有角色审批记录中，审批通过的用户，并按角色进行分组
            List<TaskRoleApproveRecord> taskRoleApproveRecords = taskRoleApproveRecordExecutor.findByTaskInstanceId(tenantId, taskInstanceId);
            List<TaskRoleApproveRecord> approvedRecords = taskRoleApproveRecords.stream().filter(i -> ApproveStatus.APPROVED == i.getStatus() || ApproveStatus.ABANDONED == i.getStatus()).toList();

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
            List<NodeRoleAssignment> nodeRoleAssignmentsByRole = findNodeRoleAssignmentsByRoleId(tenantId, nodeDefinition.getId(), roleId);

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
            if (curTaskRoleApproveRecord == null || ApproveStatus.IN_PROGRESS != curTaskRoleApproveRecord.getStatus()) {
                throw new WorkflowException("角色用户审批记录不存在或状态不正确");
            }
            curTaskRoleApproveRecord.setComment(comment);
            curTaskRoleApproveRecord.setStatus(ApproveStatus.APPROVED);
            taskRoleApproveRecordExecutor.updateById(curTaskRoleApproveRecord);

            // 审批人数 +1
            taskInstance.setApprovedCount(taskInstance.getApprovedCount() + 1);

            // 获取所有角色审批记录，将剩余的审批记录设置为 SKIPPED
            List<TaskRoleApproveRecord> taskRoleApproveRecords = taskRoleApproveRecordExecutor.findByTaskInstanceId(tenantId, taskInstanceId);
            taskRoleApproveRecords.stream().filter(i -> !curTaskRoleApproveRecord.getId().equals(i.getId())).filter(i -> ApproveStatus.IN_PROGRESS == i.getStatus())
                    .forEach(otherTaskRoleApproveRecord -> {
                        otherTaskRoleApproveRecord.setStatus(ApproveStatus.SKIPPED);
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
        return taskRoleApproveRecords.stream().filter(i -> ApproveStatus.APPROVED == i.getStatus() || ApproveStatus.ABANDONED == i.getStatus()).toList();
    }

    /**
     * 根据角色 ID 获取所有角色用户
     *
     * @param tenantId         租户 ID
     * @param nodeDefinitionId 节点定义 ID
     * @param roleId           角色 ID
     *
     * @return List<NodeRoleAssignment>
     *
     * @author wangweijun
     * @since 2024/9/19 10:56
     */
    private List<NodeRoleAssignment> findNodeRoleAssignmentsByRoleId(String tenantId, Integer nodeDefinitionId, String roleId) {
        // 获取所有角色审批用户，并按角色分组
        NodeRoleAssignmentExecutor nodeRoleAssignmentExecutor = nodeRoleAssignmentExecutorBuilder.build();
        List<NodeRoleAssignment> nodeRoleAssignments = nodeRoleAssignmentExecutor.findByNodeDefinitionId(tenantId, nodeDefinitionId);
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
        return new ArrayList<>(nodeRoleAssignmentExecutor.findByNodeDefinitionId(tenantId, nodeDefinitionId));
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
                this.processRedoRoleApprove(tenantId, nodeDefinition, currTaskInstance, currTaskApprove, roleId, userId, comment);
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
                        if (nextNodeDefinition.isDynamicAssignment()) {
                            NodeAssignmentExecutor nodeAssignmentExecutor = nodeAssignmentExecutorBuilder.build();
                            List<NodeAssignment> dynamicApprovers = nodeAssignmentExecutor.findByNodeDefinitionId(tenantId, nextNodeDefinition.getId());
                            for (int i = 0; i < dynamicApprovers.size(); i++) {
                                NodeAssignment dynamicApprover = dynamicApprovers.get(i);
                                dynamicApprover.setApproverId(WorkflowConstants.DYNAMIC_ASSIGNMENT_APPROVER_VALUE.formatted(i));
                                nodeAssignmentExecutor.updateById(dynamicApprover);
                            }
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
        NodeRoleAssignment nodeRoleAssignment = nodeRoleAssignmentExecutor.getByNodeDefinitionIdAndRoleIdAndApproverId(tenantId, nodeDefinitionId, roleId, userId);
        if (nodeRoleAssignment == null) {
            throw new WorkflowException("未查询到相关角色用户，请确认是否审批人是否正确");
        }

        // 获取所有的角色审批人
        List<NodeRoleAssignment> allNodeRoleAssignments = nodeRoleAssignmentExecutor.findByNodeDefinitionId(tenantId, nodeDefinitionId);
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
                if (ApproveStatus.APPROVED == roleApproveRecord.getStatus() || ApproveStatus.ABANDONED == roleApproveRecord.getStatus()) {
                    Integer nodeRoleAssignmentId = roleApproveRecord.getNodeRoleAssignmentId();
                    nodeRoleAssignments.add(nodeRoleAssignmentExecutor.getById(nodeRoleAssignmentId));
                }
            }

            // 按 userSeq 倒序，并获取第一个审批用户
            nodeRoleAssignments.sort(Comparator.comparingInt(NodeRoleAssignment::getUserSeq).reversed());
            NodeRoleAssignment curNodeRoleAssignment = nodeRoleAssignments.get(0);

            // 找到已经完成审批，且和当前用户一致的记录
            taskRoleApproveRecord = taskRoleApproveRecordsByRole.stream()
                    .filter(i -> ApproveStatus.APPROVED.equals(i.getStatus()) || ApproveStatus.ABANDONED.equals(i.getStatus()))
                    .filter(i -> {
                        Integer nodeRoleAssignmentId = i.getNodeRoleAssignmentId();
                        return nodeRoleAssignmentId.equals(curNodeRoleAssignment.getId());
                    }).findAny().orElse(null);
        }
        // 判断撤回条件是否满足：其他情况
        else {
            // 获取审批状态为审批通过，且创建时间最近的一条记录
            taskRoleApproveRecord = taskRoleApproveRecordsByRole.stream()
                    .filter(i -> ApproveStatus.APPROVED.equals(i.getStatus()) || ApproveStatus.ABANDONED.equals(i.getStatus()))
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
                    if (ApproveStatus.APPROVED == approveRecord.getStatus()) {
                        approveRecord.setComment(comment);
                    }
                    // 更新为：IN_PROGRESS
                    approveRecord.setStatus(ApproveStatus.IN_PROGRESS);
                    taskRoleApproveRecordExecutor.updateById(approveRecord);
                }
            }
            // 角色审批为 ANY，角色用户审批为 ALL
            else if (RoleUserApproveType.ALL == roleUserApproveType) {
                // 获取当前角色下已经审批的记录
                List<TaskRoleApproveRecord> approvedRecords = taskRoleApproveRecordsByRole.stream()
                        .filter(i -> ApproveStatus.APPROVED == i.getStatus() || ApproveStatus.ABANDONED == i.getStatus()).toList();

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
                        if (ApproveStatus.SKIPPED == otherRoleTaskRoleApproveRecord.getStatus()) {
                            otherRoleTaskRoleApproveRecord.setStatus(ApproveStatus.IN_PROGRESS);
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
                curTaskRoleApproveRecord.setStatus(ApproveStatus.IN_PROGRESS);
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
                            if (ApproveStatus.SKIPPED == firstApproveRecord.getStatus()) {
                                firstApproveRecord.setStatus(ApproveStatus.IN_PROGRESS);
                                taskRoleApproveRecordExecutor.updateById(firstApproveRecord);
                                // 剩下的节点全部设置为：SUSPEND
                                otherTaskRoleApproveRecords.stream().filter(i -> !i.getNodeRoleAssignmentId().equals(firstNodeRoleAssignment.getId())).forEach(i -> {
                                    i.setStatus(ApproveStatus.SUSPEND);
                                    taskRoleApproveRecordExecutor.updateById(i);
                                });
                            }
                            // 第一个审批节点是：APPROVED，则将下一个 SKIPPED 状态的审批节点设置为：IN_PROGRESS，剩下的设置为：SUSPEND
                            else if (ApproveStatus.APPROVED == firstApproveRecord.getStatus() && otherNodeRoleAssignments.size() > 1) {
                                // 下一个 SKIPPED 状态的审批节点设置为：IN_PROGRESS
                                Optional<TaskRoleApproveRecord> firstSkippedOptional = otherTaskRoleApproveRecords.stream().filter(i -> ApproveStatus.SKIPPED == i.getStatus()).findFirst();
                                if (firstSkippedOptional.isPresent()) {
                                    // 下一个 SKIPPED 状态的审批节点设置为：IN_PROGRESS
                                    TaskRoleApproveRecord firstSkippedApproveRecord = firstSkippedOptional.get();
                                    firstSkippedApproveRecord.setStatus(ApproveStatus.IN_PROGRESS);
                                    taskRoleApproveRecordExecutor.updateById(firstSkippedApproveRecord);
                                    // 剩下的设置为：SUSPEND
                                    otherTaskRoleApproveRecords.stream()
                                            // 过滤掉第一个 SKIPPED 状态的节点
                                            .filter(i -> !firstSkippedApproveRecord.getId().equals(i.getId()))
                                            // 获取其他所有 SKIPPED 状态的节点
                                            .filter(i -> ApproveStatus.SKIPPED == i.getStatus())
                                            // 剩下的设置为：SUSPEND
                                            .forEach(i -> {
                                                i.setStatus(ApproveStatus.SUSPEND);
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
                    others.forEach(other -> taskRoleApproveRecordsByRole.stream().filter(i -> i.getNodeRoleAssignmentId().equals(other.getId()) && ApproveStatus.SUSPEND != i.getStatus())
                            .findFirst()
                            .ifPresent(approveRecord -> {
                                approveRecord.setStatus(ApproveStatus.SUSPEND);
                                taskRoleApproveRecordExecutor.updateById(approveRecord);
                            }));
                }

                // 将所有 SKIPPED 状态的审批记录修改为：SUSPEND
                List<TaskRoleApproveRecord> skippedTaskRoleApproveRecords = taskRoleApproveRecordExecutor.findByTaskInstanceIdAndStatus(tenantId, taskInstanceId, ApproveStatus.SKIPPED);
                for (TaskRoleApproveRecord skippedTaskRoleApproveRecord : skippedTaskRoleApproveRecords) {
                    skippedTaskRoleApproveRecord.setStatus(ApproveStatus.IN_PROGRESS);
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
                    if (ApproveStatus.APPROVED == approveRecord.getStatus()) {
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
                List<TaskRoleApproveRecord> approvedRecords = taskRoleApproveRecordsByRole.stream().filter(i -> ApproveStatus.APPROVED == i.getStatus() || ApproveStatus.ABANDONED == i.getStatus()).toList();

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
                                        if (ApproveStatus.APPROVED == nextTaskRoleApproveRecord.getStatus() || ApproveStatus.ABANDONED == nextTaskRoleApproveRecord.getStatus()) {
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
                            otherTaskRoleApproveRecord.setStatus(ApproveStatus.SUSPEND);
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
                        .filter(i -> ApproveStatus.IN_PROGRESS == i.getStatus())
                        .findFirst()
                        .ifPresent(ameTaskInstanceApproveRecord -> {
                            ameTaskInstanceApproveRecord.setStatus(ApproveStatus.SUSPEND);
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

            // 检查审批意见是否必填
            checkIfRequiredComment(taskInstanceId, comment);

            // 更新 TaskApprove 的 comment，并将状态设置为：已驳回
            TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder.build();
            TaskApprove taskApprove = taskApproveExecutor.getByTaskInstanceIdAndRoleIdAndApproverId(tenantId, taskInstanceId, ActiveStatus.ACTIVE, roleId, userId);
            taskApprove.setActive(ActiveStatus.INACTIVE);
            taskApprove.setStatus(ApproveStatus.REJECTED);
            taskApprove.setComment(comment);
            taskApproveExecutor.updateById(taskApprove);

            // 找到所有未进行审批的审批人节点，并将其状态设置为：已失效
            List<TaskApprove> activeTaskApproves = taskApproveExecutor.findByTaskInstanceId(tenantId, taskInstanceId, ActiveStatus.ACTIVE);
            activeTaskApproves.forEach(activeTaskApprove -> {
                activeTaskApprove.convertToApproveStatusSkipped();
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

            // 判断是否属于角色审批
            if (nodeDefinition.isRoleApprove()) {
                this.processRejectRoleApprove(tenantId, nodeDefinition, taskInstance, taskApprove, roleId, userId, comment);
            }

            // 找到流程定义
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
                .filter(i -> ApproveStatus.SUSPEND == i.getStatus() || ApproveStatus.IN_PROGRESS == i.getStatus())
                // 设置为 SKIPPED
                .forEach(taskRoleApproveRecord -> {
                    taskRoleApproveRecord.setStatus(ApproveStatus.SKIPPED);
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

            // 检查审批意见是否必填
            checkIfRequiredComment(taskInstanceId, comment);

            TaskInstanceExecutor taskInstanceExecutor = taskInstanceExecutorBuilder.build();
            NodeDefinitionExecutor nodeDefinitionExecutor = nodeDefinitionExecutorBuilder.build();
            TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder.build();

            // 任务实例
            TaskInstance taskInstance = taskInstanceExecutor.getById(taskInstanceId);

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
                this.processAbandonRoleApprove(tenantId, nodeDefinition, taskInstance, taskApprove, roleId, userId, comment);
            }
            // 非角色审批
            else {
                // 获取当前审批任务，并修改当前审批任务的 approved_count 数量
                taskInstance.setApprovedCount(taskInstance.getApprovedCount() + 1);
                taskInstanceExecutor.updateById(taskInstance);

                // 更新 TaskApprove 的 comment，并将状态设置为：已弃权
                taskApprove.setActive(ActiveStatus.INACTIVE);
                taskApprove.setStatus(ApproveStatus.ABANDONED);
                taskApprove.setComment(comment);
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
        List<NodeRoleAssignment> allNodeRoleAssignments = nodeRoleAssignmentExecutor.findByNodeDefinitionId(tenantId, nodeDefinitionId);
        // 当前角色用户
        NodeRoleAssignment curNodeRoleAssignment = nodeRoleAssignmentExecutor.getByNodeDefinitionIdAndRoleIdAndApproverId(tenantId, nodeDefinitionId, roleId, userId);

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
                List<TaskRoleApproveRecord> abandonedApproveRecordsByRole = taskRoleApproveRecordsByRole.stream().filter(i -> ApproveStatus.ABANDONED == i.getStatus()).toList();
                if (abandonedApproveRecordsByRole.size() == nodeRoleAssignmentsByRole.size() - 1) {
                    throw new WorkflowException("最后一个审批人无法放弃审批");
                }

                // 获取当前的审批记录
                TaskRoleApproveRecord curTaskRoleApproveRecord = taskRoleApproveRecordExecutor.getByTaskInstanceIdAndRoleIdAndUserId(tenantId, taskInstanceId, roleId, userId);
                curTaskRoleApproveRecord.setComment(comment);
                curTaskRoleApproveRecord.setStatus(ApproveStatus.ABANDONED);
                taskRoleApproveRecordExecutor.updateById(curTaskRoleApproveRecord);
            }
        }
        // 角色审批类型为：ALL 或 SEQ
        else if (RoleApproveType.ALL == roleApproveType || RoleApproveType.SEQ == roleApproveType) {
            // 角色用户审批类型为：ANY
            if (RoleUserApproveType.ANY == roleUserApproveType) {
                // 获取该角色下已经放弃审批的审批记录
                List<TaskRoleApproveRecord> abandonedApproveRecordsByRole = taskRoleApproveRecordsByRole.stream().filter(i -> ApproveStatus.ABANDONED == i.getStatus()).toList();
                if (abandonedApproveRecordsByRole.size() == nodeRoleAssignmentsByRole.size() - 1) {
                    throw new WorkflowException("最后一个审批人无法放弃审批");
                }

                // 获取当前的审批记录
                TaskRoleApproveRecord curTaskRoleApproveRecord = taskRoleApproveRecordExecutor.getByTaskInstanceIdAndRoleIdAndUserId(tenantId, taskInstanceId, roleId, userId);
                curTaskRoleApproveRecord.setComment(comment);
                curTaskRoleApproveRecord.setStatus(ApproveStatus.ABANDONED);
                taskRoleApproveRecordExecutor.updateById(curTaskRoleApproveRecord);
            }
            // 角色用户审批类型为：ALL
            else if (RoleUserApproveType.ALL == roleUserApproveType) {
                // 获取已经放弃审批的审批记录
                List<TaskRoleApproveRecord> abandonedApproveRecords = allTaskRoleApproveRecords.stream().filter(i -> ApproveStatus.ABANDONED == i.getStatus()).toList();
                if (abandonedApproveRecords.size() == allNodeRoleAssignments.size() - 1) {
                    throw new WorkflowException("最后一个审批人无法放弃审批");
                }

                // 获取当前的审批记录
                TaskRoleApproveRecord curTaskRoleApproveRecord = taskRoleApproveRecordExecutor.getByTaskInstanceIdAndRoleIdAndUserId(tenantId, taskInstanceId, roleId, userId);
                curTaskRoleApproveRecord.setComment(comment);
                curTaskRoleApproveRecord.setStatus(ApproveStatus.ABANDONED);
                taskRoleApproveRecordExecutor.updateById(curTaskRoleApproveRecord);
            }
        }

        // 角色用户审批类型为：SEQ
        if (RoleApproveType.SEQ == roleApproveType || RoleUserApproveType.SEQ == roleUserApproveType) {

            // 角色审批类型为：ANY
            if (RoleApproveType.ANY == roleApproveType) {
                // 获取该角色下已经放弃审批的审批记录
                List<TaskRoleApproveRecord> abandonedApproveRecordsByRole = taskRoleApproveRecordsByRole.stream().filter(i -> ApproveStatus.ABANDONED == i.getStatus()).toList();
                if (abandonedApproveRecordsByRole.size() == nodeRoleAssignmentsByRole.size() - 1) {
                    throw new WorkflowException("最后一个审批人无法放弃审批");
                }
            }

            // 获取当前的审批记录
            TaskRoleApproveRecord curTaskRoleApproveRecord = taskRoleApproveRecordExecutor.getByTaskInstanceIdAndRoleIdAndUserId(tenantId, taskInstanceId, roleId, userId);
            curTaskRoleApproveRecord.setComment(comment);
            curTaskRoleApproveRecord.setStatus(ApproveStatus.ABANDONED);
            taskRoleApproveRecordExecutor.updateById(curTaskRoleApproveRecord);

            // 查询 userSeq 大于当前角色用户的角色用户
            NodeRoleAssignment nodeRoleAssignment = nodeRoleAssignmentsByRole.stream().filter(i -> i.getRoleId().equals(curNodeRoleAssignment.getRoleId()))
                    .filter(i -> i.getUserSeq() > curNodeRoleAssignment.getUserSeq()).min(Comparator.comparingInt(NodeRoleAssignment::getUserSeq)).orElse(null);
            // 将最近的一个 SUSPEND 的审批记录设置为：IN_PROGRESS
            if (nodeRoleAssignment != null) {
                taskRoleApproveRecordsByRole.stream().filter(i -> i.getId().equals(nodeRoleAssignment.getId())).filter(i -> ApproveStatus.SUSPEND == i.getStatus())
                        .findFirst().ifPresent(taskRoleApproveRecord -> {
                            taskRoleApproveRecord.setStatus(ApproveStatus.IN_PROGRESS);
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
                                    otherTaskRoleApproveRecord.setStatus(ApproveStatus.IN_PROGRESS);
                                    taskRoleApproveRecordExecutor.updateById(otherTaskRoleApproveRecord);
                                });
                    }
                }

            }
        }

        // 获取当前任务下所有角色的审批记录
        List<TaskRoleApproveRecord> taskRoleApproveRecordByRole = taskRoleApproveRecordExecutor.findByTaskInstanceIdAndRoleId(tenantId, taskInstanceId, roleId);
        // 同角色下已经通过审批的用户
        List<TaskRoleApproveRecord> approvedApproveRecordsByRole = taskRoleApproveRecordByRole.stream().filter(i -> ApproveStatus.ABANDONED == i.getStatus() || ApproveStatus.APPROVED == i.getStatus()).toList();
        // 同角色下全部放弃审批的用户
        List<TaskRoleApproveRecord> abandonedApproveRecordsByRole = approvedApproveRecordsByRole.stream().filter(i -> ApproveStatus.ABANDONED == i.getStatus()).toList();

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
        // 查看流程状态
        WorkflowInstanceExecutor workflowInstanceExecutor = workflowInstanceExecutorBuilder.build();
        WorkflowInstance workflowInstance = workflowInstanceExecutor.getById(workflowInstanceId);
        if (WorkflowStatus.IN_PROGRESS != workflowInstance.getStatus()) {
            throw new WorkflowException("流程实例已结束: %s", workflowInstance.getStatus().getDesc());
        }

        // 获取该流程下所有的审批人
        TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder.build();
        List<TaskApprove> taskApproves = taskApproveExecutor.findByTWorkflowInstanceId(tenantId, workflowInstanceId, null, null);
        // 查看是否已经有人参与了审批
        TaskApprove taskApprove = taskApproves.stream().filter(i -> ApproveStatus.IN_PROGRESS != i.getStatus()).findFirst().orElse(null);
        if (taskApprove != null) {
            throw new WorkflowException("流程实例已被审批: %s", taskApprove.getApproverId());
        }

        // 未审批的审批人节点：设置为已失效
        List<TaskApprove> inProgressApproves = taskApproves.stream().filter(i -> ApproveStatus.IN_PROGRESS == i.getStatus()).toList();
        inProgressApproves.forEach(inProgressApprove -> {
            inProgressApprove.convertToApproveStatusSkipped();
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
     * 保存角色审批记录
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     * @param nodeDefinitionId   节点定义 ID
     * @param taskInstanceId     任务实例 ID
     * @param taskApproveId      审批实例 ID
     * @param roleId             角色 ID
     * @param userId             用户 ID
     * @param comment            审批意见
     * @param approveStatus      审批状态
     *
     * @author wangweijun
     * @since 2024/9/14 16:24
     */
    private TaskRoleApproveRecord saveTaskRoleApproveRecord(String tenantId, Integer workflowInstanceId, Integer nodeDefinitionId, Integer taskInstanceId, Integer taskApproveId, String roleId, String userId, String comment, ApproveStatus approveStatus) {
        NodeRoleAssignmentExecutor nodeRoleAssignmentExecutor = nodeRoleAssignmentExecutorBuilder.build();
        NodeRoleAssignment nodeRoleAssignment = nodeRoleAssignmentExecutor.getByNodeDefinitionIdAndRoleIdAndApproverId(tenantId, nodeDefinitionId, roleId, userId);
        if (nodeRoleAssignment == null) {
            throw new WorkflowException("角色审批人未定义");
        }
        TaskRoleApproveRecordExecutor taskRoleApproveRecordExecutor = taskRoleApproveRecordExecutorBuilder.build();
        TaskRoleApproveRecord taskRoleApproveRecord = TaskRoleApproveRecordBuilder.builder()
                .tenantId(tenantId)
                .workflowInstanceId(workflowInstanceId)
                .taskInstanceId(taskInstanceId)
                .taskApproveId(taskApproveId)
                .nodeRoleAssignmentId(nodeRoleAssignment.getId())
                .comment(comment)
                .status(approveStatus)
                .build();
        return taskRoleApproveRecordExecutor.save(taskRoleApproveRecord);
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
        return this.getWorkflowInstanceApproveRecords(tenantId, workflowInstanceId, null, null);
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
    @Override
    public WorkflowInstanceApproveRecords getWorkflowInstanceApproveRecords(String tenantId, Integer workflowInstanceId, List<String> curRoleIds, String curUserId) {
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

        // 节点审批人
        NodeAssignmentExecutor nodeAssignmentExecutor = nodeAssignmentExecutorBuilder.build();
        List<NodeAssignment> nodeAssignments = nodeAssignmentExecutor.findByWorkflowInstanceId(tenantId, workflowInstanceId);

        // 节点定义与节点审批人的对应关系
        Map<NodeDefinition, NodeAssignment> nodeDefAndNodeAssignmentMap = new HashMap<>();

        List<Pair<NodeDefinition, TaskInstance>> nodeDefAndTasks = new ArrayList<>();
        for (NodeDefinition nodeDefinition : nodeDefinitions) {
            TaskInstance taskInstance = taskInstanceExecutor.getByWorkflowInstanceIdAndNodeDefinitionId(tenantId, workflowInstanceId, nodeDefinition.getId());
            nodeDefAndTasks.add(Pair.of(nodeDefinition, taskInstance));

            // 封装：节点定义与节点审批人的对应关系
            Optional<NodeAssignment> nodeAssignmentOptional = nodeAssignments.stream().filter(nodeAssignment -> nodeDefinition.getId().equals(nodeAssignment.getNodeDefinitionId())).findFirst();
            nodeAssignmentOptional.ifPresent(assignment -> nodeDefAndNodeAssignmentMap.put(nodeDefinition, assignment));
        }

        // 审批人
        TaskApproveExecutor taskApproveExecutor = taskApproveExecutorBuilder.build();
        List<TaskApprove> taskApproves = taskApproveExecutor.findByTWorkflowInstanceId(tenantId, workflowInstanceId);


        // 审批记录与角色审批记录对应表
        TaskRoleApproveRecordExecutor taskRoleApproveRecordExecutor = taskRoleApproveRecordExecutorBuilder.build();
        Map<TaskApprove, List<TaskRoleApproveRecord>> taskApproveAndRoleApproveRecordsMap = new HashMap<>();
        // 角色审批记录与角色用户对应表
        NodeRoleAssignmentExecutor nodeRoleAssignmentExecutor = nodeRoleAssignmentExecutorBuilder.build();
        Map<TaskRoleApproveRecord, NodeRoleAssignment> taskRoleRecordAndNodeRoleAssignmentMap = new HashMap<>();

        for (TaskApprove taskApprove : taskApproves) {
            Integer taskInstanceId = taskApprove.getTaskInstanceId();
            TaskInstance taskInstance = taskInstanceExecutor.getById(taskInstanceId);
            Integer nodeDefinitionId = taskInstance.getNodeDefinitionId();
            NodeDefinition nodeDefinition = nodeDefinitionExecutor.getById(nodeDefinitionId);
            List<TaskRoleApproveRecord> taskRoleApproveRecords = null;
            if (nodeDefinition.isRoleApprove()) {
                taskRoleApproveRecords = taskRoleApproveRecordExecutor.findByTaskInstanceIdAndRoleId(tenantId, taskInstanceId, taskApprove.getApproverId());
                taskRoleApproveRecords.forEach(taskRoleApproveRecord -> {
                    NodeRoleAssignment nodeRoleAssignment = nodeRoleAssignmentExecutor.getById(taskRoleApproveRecord.getNodeRoleAssignmentId());
                    taskRoleRecordAndNodeRoleAssignmentMap.put(taskRoleApproveRecord, nodeRoleAssignment);
                });
            }
            taskApproveAndRoleApproveRecordsMap.put(taskApprove, taskRoleApproveRecords);
        }

        // 返回 WorkflowInstanceApproveRecords
        return WorkflowInstanceApproveRecords.of(workflowDefinition, workflowInstance, nodeDefAndTasks, taskApproves, nodeDefAndNodeAssignmentMap, taskApproveAndRoleApproveRecordsMap, taskRoleRecordAndNodeRoleAssignmentMap, nodeAssignments, curRoleIds, curUserId);
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
        return this.findWorkflowInstanceApproveRecords(tenantId, workflowDefinitionId, List.of(), null);
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
     * @since 2024/9/12 13:42
     */
    @Override
    public List<WorkflowInstanceApproveRecords> findWorkflowInstanceApproveRecords(String tenantId, Integer workflowDefinitionId, List<String> curRoleIds, String curUserId) {
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
            WorkflowInstanceApproveRecords records = this.getWorkflowInstanceApproveRecords(tenantId, workflowInstance.getId(), curRoleIds, curUserId);
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
                nodeAssignment.setApproverId(approver.getId());
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
                    .eq(NodeAssignment::getApproverId, sourceApproverId)
                    .eq(NodeAssignment::getState, 1)
                    .build();
            Page<NodeAssignment> nodeAssignmentPage = nodeAssignmentExecutor.find(query);
            List<NodeAssignment> nodeAssignments = nodeAssignmentPage.getRecords();
            if (CollectionUtils.isNotEmpty(nodeAssignments)) {
                for (NodeAssignment nodeAssignment : nodeAssignments) {
                    // 设置为目标值
                    nodeAssignment.setApproverId(targetApproverId);
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
