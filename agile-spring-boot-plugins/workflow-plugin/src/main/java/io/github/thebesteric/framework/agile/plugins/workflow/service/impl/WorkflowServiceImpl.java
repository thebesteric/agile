package io.github.thebesteric.framework.agile.plugins.workflow.service.impl;

import cn.hutool.core.collection.CollUtil;
import io.github.thebesteric.framework.agile.commons.util.LoggerPrinter;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.Page;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.OrderByOperator;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.builder.Query;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.builder.QueryBuilderWrapper;
import io.github.thebesteric.framework.agile.plugins.database.core.jdbc.JdbcTemplateHelper;
import io.github.thebesteric.framework.agile.plugins.workflow.config.AgileWorkflowContext;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.*;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.Approver;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.RoleApprover;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.assignment.*;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.definition.NodeDefinitionExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.definition.NodeDefinitionExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.history.NodeDefinitionHistoryBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.history.NodeDefinitionHistoryExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.history.NodeDefinitionHistoryExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.relation.NodeRelationBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.relation.NodeRelationExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.relation.NodeRelationExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.definition.WorkflowDefinitionExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.definition.WorkflowDefinitionExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.instance.WorkflowInstanceExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.instance.WorkflowInstanceExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.*;
import io.github.thebesteric.framework.agile.plugins.workflow.exception.WorkflowException;
import io.github.thebesteric.framework.agile.plugins.workflow.exception.WorkflowInstanceInProgressException;
import io.github.thebesteric.framework.agile.plugins.workflow.service.AbstractWorkflowService;
import io.github.thebesteric.framework.agile.plugins.workflow.service.DeploymentService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 流程 Service implementation
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-13 21:23:21
 */
@Slf4j
public class WorkflowServiceImpl extends AbstractWorkflowService {

    private final NodeDefinitionExecutorBuilder nodeDefinitionExecutorBuilder;
    private final NodeRelationExecutorBuilder nodeRelationExecutorBuilder;
    private final NodeAssignmentExecutorBuilder nodeAssignmentExecutorBuilder;
    private final NodeDefinitionHistoryExecutorBuilder nodeDefinitionHistoryExecutorBuilder;
    private final NodeRoleAssignmentExecutorBuilder nodeRoleAssignmentExecutorBuilder;
    private final WorkflowInstanceExecutorBuilder workflowInstanceExecutorBuilder;
    private final WorkflowDefinitionExecutorBuilder workflowDefinitionExecutorBuilder;
    private final DeploymentService deploymentService;

    public WorkflowServiceImpl(AgileWorkflowContext context) {
        super(context);
        JdbcTemplate jdbcTemplate = context.getJdbcTemplateHelper().getJdbcTemplate();
        nodeDefinitionExecutorBuilder = NodeDefinitionExecutorBuilder.builder(jdbcTemplate);
        nodeRelationExecutorBuilder = NodeRelationExecutorBuilder.builder(jdbcTemplate);
        nodeAssignmentExecutorBuilder = NodeAssignmentExecutorBuilder.builder(jdbcTemplate);
        nodeDefinitionHistoryExecutorBuilder = NodeDefinitionHistoryExecutorBuilder.builder(jdbcTemplate);
        nodeRoleAssignmentExecutorBuilder = NodeRoleAssignmentExecutorBuilder.builder(jdbcTemplate);
        workflowInstanceExecutorBuilder = WorkflowInstanceExecutorBuilder.builder(jdbcTemplate);
        workflowDefinitionExecutorBuilder = WorkflowDefinitionExecutorBuilder.builder(jdbcTemplate);
        deploymentService = new DeploymentServiceImpl(context);
    }

    /**
     * 获取流程定义
     *
     * @param tenantId 流程定义 ID
     * @param key      流程定义 KEY
     *
     * @return WorkflowDefinition
     */
    @Override
    public WorkflowDefinition getWorkflowDefinition(String tenantId, String key) {
        return workflowDefinitionExecutorBuilder.build().getByTenantAndKey(tenantId, key);
    }

    /**
     * 获取流程定义
     *
     * @param query 查询参数
     *
     * @return List<WorkflowDefinition>
     */
    @Override
    public Page<WorkflowDefinition> findWorkflowDefinitions(Query query) {
        return workflowDefinitionExecutorBuilder.build().find(query);
    }

    /**
     * 获取所有流程实例（分页）
     *
     * @param query 查询条件
     *
     * @return List<WorkflowInstance>
     */
    @Override
    public Page<WorkflowInstance> findWorkflowInstances(Query query) {
        return workflowInstanceExecutorBuilder.build().find(query);
    }

    /**
     * 创建节点定义
     *
     * @param nodeDefinition 节点定义
     */
    @Override
    public NodeDefinition createNode(NodeDefinition nodeDefinition) {
        JdbcTemplateHelper jdbcTemplateHelper = this.context.getJdbcTemplateHelper();
        return jdbcTemplateHelper.executeInTransaction(() -> {
            // 校验节点是否合法
            if (NodeType.TASK == nodeDefinition.getNodeType()) {
                if (nodeDefinition.isRoleApprove() && CollectionUtils.isEmpty(nodeDefinition.getRoleApprovers())) {
                    throw new WorkflowException("角色审批节点: 角色审批用户不能为空");
                }
                if (nodeDefinition.isUserApprove() && CollectionUtils.isEmpty(nodeDefinition.getApprovers())) {
                    throw new WorkflowException("用户审批节点: 审批用户不能为空");
                }
                if (nodeDefinition.getSequence() >= Integer.MAX_VALUE || nodeDefinition.getSequence() <= Integer.MIN_VALUE) {
                    throw new WorkflowException("节点更新修改失败: 节点顺序不能大于等于 %s 或小于等于 %s", Integer.MAX_VALUE, Integer.MIN_VALUE);
                }
            }
            // 查找所属流程定义
            WorkflowDefinitionExecutor workflowDefinitionExecutor = workflowDefinitionExecutorBuilder.build();
            WorkflowDefinition workflowDefinition = workflowDefinitionExecutor.getById(nodeDefinition.getWorkflowDefinitionId());
            // 检查当前流程定义是否有正在进行的实例
            throwExceptionWhenWorkflowDefinitionHasInProcessInstances(nodeDefinition.getTenantId(), workflowDefinition.getId());
            // 将流程定义设置为未发布
            if (workflowDefinition.isPublished()) {
                this.deploymentService.unPublish(workflowDefinition);
            }
            // 保存
            NodeDefinitionExecutor executor = nodeDefinitionExecutorBuilder.nodeDefinition(nodeDefinition).build();
            executor.save();
            // 记录日志
            this.recordNodeDefinitionHistory(nodeDefinition.getTenantId(), nodeDefinition.getId(), DMLOperator.INSERT, null, nodeDefinition, "节点创建");
            return nodeDefinition;
        });
    }

    /**
     * 插入节点定义
     *
     * @param nodeDefinition       节点定义
     * @param prevNodeDefinitionId 上一个节点定义 ID
     * @param nextNodeDefinitionId 下一个节点定义 ID
     *
     * @return NodeDefinition
     *
     * @author wangweijun
     * @since 2024/9/6 16:48
     */
    @Override
    public NodeDefinition insertNode(NodeDefinition nodeDefinition, Integer prevNodeDefinitionId, Integer nextNodeDefinitionId) {
        NodeDefinitionExecutor executor = nodeDefinitionExecutorBuilder.nodeDefinition(nodeDefinition).build();
        // 查找前置节点
        NodeDefinition prevNodeDefinition = executor.getById(nodeDefinition.getTenantId(), prevNodeDefinitionId);
        // 查找后置节点
        NodeDefinition nextNodeDefinition = executor.getById(nodeDefinition.getTenantId(), nextNodeDefinitionId);
        // 计算 sequence
        double sequence = (prevNodeDefinition.getSequence() + nextNodeDefinition.getSequence()) / 2;
        nodeDefinition.setSequence(sequence);
        // 创建节点
        return this.createNode(nodeDefinition);
    }

    /**
     * 更新节点定义
     *
     * @param nodeDefinition 节点定义
     */
    @Override
    public void updateNode(NodeDefinition nodeDefinition) {
        JdbcTemplateHelper jdbcTemplateHelper = this.context.getJdbcTemplateHelper();
        jdbcTemplateHelper.executeInTransaction(() -> {
            // 校验节点是否合法
            if (NodeType.TASK == nodeDefinition.getNodeType()) {
                if (nodeDefinition.isRoleApprove() && CollectionUtils.isEmpty(nodeDefinition.getRoleApprovers())) {
                    throw new WorkflowException("角色审批节点: 角色审批用户不能为空");
                }
                if (nodeDefinition.isUserApprove() && CollectionUtils.isEmpty(nodeDefinition.getApprovers())) {
                    throw new WorkflowException("用户审批节点: 审批用户不能为空");
                }
            }

            if (NodeType.START == nodeDefinition.getNodeType() || NodeType.END == nodeDefinition.getNodeType()) {
                throw new WorkflowException("节点更新修改失败: 无法修改开始节点或结束节点");
            }

            if (nodeDefinition.getSequence() >= Integer.MAX_VALUE || nodeDefinition.getSequence() <= Integer.MIN_VALUE) {
                throw new WorkflowException("节点更新修改失败: 节点顺序不能大于等于 %s 或小于等于 %s", Integer.MAX_VALUE, Integer.MIN_VALUE);
            }

            // 记录之前的节点定义
            NodeDefinition beforeNodeDefinition = new NodeDefinition();
            BeanUtils.copyProperties(nodeDefinition, beforeNodeDefinition);

            // 租户 ID
            String tenantId = nodeDefinition.getTenantId();

            WorkflowDefinitionExecutor workflowDefinitionExecutor = workflowDefinitionExecutorBuilder.build();
            NodeAssignmentExecutor nodeAssignmentExecutor = nodeAssignmentExecutorBuilder.build();
            NodeDefinitionExecutor nodeDefinitionExecutor = nodeDefinitionExecutorBuilder.build();

            // 查找所属流程定义
            WorkflowDefinition workflowDefinition = workflowDefinitionExecutor.getById(nodeDefinition.getWorkflowDefinitionId());

            // 检查当前流程定义是否有正在进行的实例
            throwExceptionWhenWorkflowDefinitionHasInProcessInstances(tenantId, workflowDefinition.getId());

            // 获取原有的节点定义
            NodeDefinition oldNodeDefinition = nodeDefinitionExecutor.getById(nodeDefinition.getId());

            // 获取原有的审批人
            List<NodeAssignment> oldNodeAssignments = nodeAssignmentExecutor.findByNodeDefinitionId(tenantId, oldNodeDefinition.getId());
            Set<Approver> oldApprovers = oldNodeAssignments.stream().map(assignment -> Approver.of(assignment.getApproverId(), assignment.getDesc())).collect(Collectors.toSet());

            // 审批人不同
            if (!nodeDefinition.getApprovers().containsAll(oldApprovers)) {
                // 删除原有的审批人
                nodeAssignmentExecutor.deleteByNodeDefinitionId(tenantId, nodeDefinition.getId());
                // 重新添加审批人
                NodeAssignmentBuilder nodeAssignmentBuilder = NodeAssignmentBuilder.builder(tenantId, nodeDefinition.getId());
                ApproverIdType approverIdType = nodeDefinition.isRoleApprove() ? ApproverIdType.ROLE : ApproverIdType.USER;
                ApproveType approveType = nodeDefinition.getApproveType();
                for (Approver approver : nodeDefinition.getApprovers()) {
                    NodeAssignment nodeAssignment = nodeAssignmentBuilder.approverId(approverIdType, approveType, approver.getId(), approver.getDesc()).build();
                    NodeAssignmentExecutor assignmentExecutor = nodeAssignmentExecutorBuilder.nodeAssignment(nodeAssignment).build();
                    assignmentExecutor.save();
                }
                nodeAssignmentBuilder.resetSeq();
            }

            // 更新节点定义
            nodeDefinitionExecutor.updateById(nodeDefinition);

            // 将流程定义设置为未发布
            if (workflowDefinition.isPublished()) {
                this.deploymentService.unPublish(workflowDefinition);
            }

            // 记录日志
            this.recordNodeDefinitionHistory(beforeNodeDefinition.getTenantId(), beforeNodeDefinition.getId(), DMLOperator.UPDATE, beforeNodeDefinition, nodeDefinition, "节点更新");
        });
    }

    /**
     * 删除节点定义
     *
     * @param tenantId         租户 ID
     * @param nodeDefinitionId 节点定义 ID
     */
    @Override
    public boolean deleteNode(String tenantId, Integer nodeDefinitionId) {
        JdbcTemplateHelper jdbcTemplateHelper = this.context.getJdbcTemplateHelper();
        return jdbcTemplateHelper.executeInTransaction(() -> {
            NodeDefinition nodeDefinition = getNode(tenantId, nodeDefinitionId);

            // 判断是否是开始或结束节点
            if (NodeType.START == nodeDefinition.getNodeType() || NodeType.END == nodeDefinition.getNodeType()) {
                throw new WorkflowException("节点删除失败: 无法删除开始节点或结束节点");
            }

            // 获取任务节点
            List<NodeDefinition> taskNodes = findTaskNodes(tenantId, nodeDefinition.getWorkflowDefinitionId());
            if (CollectionUtils.isEmpty(taskNodes)) {
                throw new WorkflowException("节点删除失败: 节点定义必须至少存在一个任务节点");
            }

            // 查找所属流程定义
            WorkflowDefinitionExecutor workflowDefinitionExecutor = workflowDefinitionExecutorBuilder.build();
            WorkflowDefinition workflowDefinition = workflowDefinitionExecutor.getById(nodeDefinition.getWorkflowDefinitionId());

            // 检查当前流程定义是否有正在进行的实例
            throwExceptionWhenWorkflowDefinitionHasInProcessInstances(tenantId, workflowDefinition.getId());

            // 将流程定义设置为未发布
            if (workflowDefinition.isPublished()) {
                this.deploymentService.unPublish(workflowDefinition);
            }

            NodeDefinitionExecutor executor = nodeDefinitionExecutorBuilder.tenantId(tenantId).id(nodeDefinitionId).build();
            boolean isDeleted = executor.deleteById();


            // 记录日志
            this.recordNodeDefinitionHistory(tenantId, nodeDefinitionId, DMLOperator.DELETE, nodeDefinition, null, "节点删除");

            return isDeleted;
        });
    }

    /**
     * 检查当前流程定义是否有正在进行的实例
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     *
     * @author wangweijun
     * @since 2024/9/30 17:29
     */
    private void throwExceptionWhenWorkflowDefinitionHasInProcessInstances(String tenantId, Integer workflowDefinitionId) {
        // 检查是否有正在进行的流程实例
        List<WorkflowInstance> inProcessWorkflowInstances = this.findWorkflowDefinitionHasInProcessInstances(tenantId, workflowDefinitionId);
        if (CollUtil.isNotEmpty(inProcessWorkflowInstances)) {
            throw new WorkflowInstanceInProgressException();
        }
    }

    /**
     * 获取正在进行的流程实例
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     *
     * @return List<WorkflowInstance>
     *
     * @author wangweijun
     * @since 2024/10/8 11:51
     */
    private List<WorkflowInstance> findWorkflowDefinitionHasInProcessInstances(String tenantId, Integer workflowDefinitionId) {
        // 检查是否有正在进行的流程实例
        WorkflowInstanceExecutor workflowInstanceExecutor = this.workflowInstanceExecutorBuilder.build();
        Query query = QueryBuilderWrapper.createLambda(WorkflowInstance.class)
                .eq(WorkflowInstance::getTenantId, tenantId)
                .eq(WorkflowInstance::getWorkflowDefinitionId, workflowDefinitionId)
                .eq(WorkflowInstance::getStatus, WorkflowStatus.IN_PROGRESS.getCode())
                .eq(WorkflowInstance::getState, 1).build();
        Page<WorkflowInstance> page = workflowInstanceExecutor.find(query);
        return page.getRecords();
    }

    /**
     * 获取节点定义
     *
     * @param tenantId         租户 ID
     * @param nodeDefinitionId 节点定义 ID
     */
    @Override
    public NodeDefinition getNode(String tenantId, Integer nodeDefinitionId) {
        // 获取节点定义
        NodeDefinitionExecutor nodeDefinitionExecutor = nodeDefinitionExecutorBuilder.build();
        return nodeDefinitionExecutor.getById(tenantId, nodeDefinitionId);
    }

    /**
     * 获取节点定义
     *
     * @param tenantId          租户 ID
     * @param nodeDefinitionIds 节点定义 IDs
     */
    @Override
    public List<NodeDefinition> getNodes(String tenantId, List<Integer> nodeDefinitionIds) {
        List<NodeDefinition> nodeDefinitions = new ArrayList<>();
        for (Integer nodeDefinitionId : nodeDefinitionIds) {
            NodeDefinition nodeDefinition = this.getNode(tenantId, nodeDefinitionId);
            nodeDefinitions.add(nodeDefinition);
        }
        return nodeDefinitions;
    }

    /**
     * 获取开始节点
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     */
    @Override
    public NodeDefinition getStartNode(String tenantId, Integer workflowDefinitionId) {
        NodeDefinitionExecutor executor = nodeDefinitionExecutorBuilder.tenantId(tenantId).workflowDefinitionId(workflowDefinitionId).build();
        return executor.getStartNode();
    }

    /**
     * 获取结束节点
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     */
    @Override
    public NodeDefinition getEndNode(String tenantId, Integer workflowDefinitionId) {
        NodeDefinitionExecutor executor = nodeDefinitionExecutorBuilder.tenantId(tenantId).workflowDefinitionId(workflowDefinitionId).build();
        return executor.getEndNode();
    }

    /**
     * 获取任务节点集合
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     */
    @Override
    public List<NodeDefinition> findTaskNodes(String tenantId, Integer workflowDefinitionId) {
        NodeDefinitionExecutor executor = nodeDefinitionExecutorBuilder.tenantId(tenantId).workflowDefinitionId(workflowDefinitionId).build();
        return executor.findTaskNodes();
    }

    /**
     * 获取节点定义集合
     *
     * @param tenantId             租户 ID
     * @param fromNodeDefinitionId 起始节点定义 ID
     */
    @Override
    public List<NodeDefinition> findToTaskNodesByFromNodeId(String tenantId, Integer fromNodeDefinitionId) {
        NodeDefinitionExecutor executor = nodeDefinitionExecutorBuilder.build();
        return executor.findToTaskNodesByFromNodeId(tenantId, fromNodeDefinitionId);
    }

    /**
     * 获取节点定义集合
     *
     * @param workflowDefinitionId 流程定义 ID
     */
    @Override
    public List<NodeDefinition> getNodes(String tenantId, Integer workflowDefinitionId) {
        NodeDefinitionExecutor executor = nodeDefinitionExecutorBuilder.tenantId(tenantId).workflowDefinitionId(workflowDefinitionId).build();
        return executor.findByWorkflowDefinitionId();
    }

    /**
     * 创建节点关系
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 节点定义 ID
     */
    @Override
    public void createRelations(String tenantId, Integer workflowDefinitionId) {
        JdbcTemplateHelper jdbcTemplateHelper = this.context.getJdbcTemplateHelper();
        jdbcTemplateHelper.executeInTransaction(() -> {

            // 获取流程定义
            WorkflowDefinitionExecutor workflowDefinitionExecutor = workflowDefinitionExecutorBuilder.build();
            WorkflowDefinition workflowDefinition = workflowDefinitionExecutor.getById(workflowDefinitionId);

            // 记录流程定义变更之前的状态
            WorkflowDefinition beforeObj = new WorkflowDefinition();
            BeanUtils.copyProperties(workflowDefinition, beforeObj);

            // 查看是否已经发布
            if (workflowDefinition.isPublished()) {
                LoggerPrinter.debug(log, "流程定义已发布，无需重复发布: {}", workflowDefinitionId);
                return;
            }

            // 删除原有节点定义
            this.inactiveRelations(tenantId, workflowDefinitionId);

            // 获取开始节点、任务节点、结束节点
            NodeDefinition startNode = this.getStartNode(tenantId, workflowDefinitionId);
            List<NodeDefinition> taskNodes = this.findTaskNodes(tenantId, workflowDefinitionId);
            NodeDefinition endNode = this.getEndNode(tenantId, workflowDefinitionId);

            if (startNode == null || endNode == null) {
                throw new WorkflowException("未查询到开始节点或结束节点，请确认是否配置了开始节点和结束节点");
            }

            if (CollectionUtils.isEmpty(taskNodes)) {
                throw new WorkflowException("未查询到任何任务节点，请确认是否配置了任务节点");
            }

            // taskNodes 按 sequence 分组
            Map<Double, List<NodeDefinition>> taskNodesGroups = taskNodes.stream().collect(Collectors.groupingBy(NodeDefinition::getSequence));
            // 创建一个 TreeMap 来保证 sequence 的顺序在 map 中是按照升序排列的
            TreeMap<Double, List<NodeDefinition>> sortedTaskNodesGroups = new TreeMap<>(taskNodesGroups);

            // 顺序节点
            List<NodeDefinition> fromNodes = List.of(startNode);
            for (Map.Entry<Double, List<NodeDefinition>> entry : sortedTaskNodesGroups.entrySet()) {
                Double sequence = entry.getKey();
                List<NodeDefinition> nodeDefinitions = entry.getValue();
                this.createRelations(tenantId, workflowDefinitionId, fromNodes, nodeDefinitions, sequence);
                fromNodes = nodeDefinitions;
            }

            // 逆向节点
            double sequence = 1;
            for (NodeDefinition taskNode : taskNodes) {
                NodeRelation rejectNodeRelation = NodeRelationBuilder.builder(tenantId, workflowDefinitionId).fromNode(taskNode).toNode(endNode).sequence(sequence).build();
                this.createRelation(rejectNodeRelation);
                sequence++;
            }

            // 将流程定义设置为发布状态
            this.deploymentService.publish(workflowDefinition);

        }, true);
    }


    /**
     * 创建节点关系
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     * @param fromNodes            来自节点
     * @param toNodes              去往节点
     * @param sequence             节点顺序
     *
     * @author wangweijun
     * @since 2024/10/8 14:33
     */
    private void createRelations(String tenantId, Integer workflowDefinitionId, List<NodeDefinition> fromNodes, List<NodeDefinition> toNodes, Double sequence) {
        for (NodeDefinition fromNode : fromNodes) {
            for (NodeDefinition toNode : toNodes) {
                NodeRelation rejectNodeRelation = NodeRelationBuilder.builder(tenantId, workflowDefinitionId).fromNode(fromNode).toNode(toNode).sequence(sequence).build();
                this.createRelation(rejectNodeRelation);
            }
        }
    }

    /**
     * 创建节点关系
     *
     * @param nodeRelation 节点关系
     *
     * @author wangweijun
     * @since 2024/10/8 14:33
     */
    private void createRelation(NodeRelation nodeRelation) {
        NodeRelationExecutor executor = nodeRelationExecutorBuilder.nodeRelation(nodeRelation).build();
        executor.save();
    }

    /**
     * 使节点关系失效
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     *
     * @author wangweijun
     * @since 2024/10/8 14:35
     */
    private void inactiveRelations(String tenantId, Integer workflowDefinitionId) {
        NodeRelationExecutor executor = nodeRelationExecutorBuilder.build();
        executor.inactiveByWorkflowDefinitionId(tenantId, workflowDefinitionId);
    }

    /**
     * 记录日志
     *
     * @param tenantId         租户 ID
     * @param nodeDefinitionId 节点定义 ID
     * @param dmlOperator      操作
     * @param beforeObj        之前数据
     * @param currentObj       当前数据
     *
     * @author wangweijun
     * @since 2024/10/8 10:15
     */
    private void recordNodeDefinitionHistory(String tenantId, Integer nodeDefinitionId, DMLOperator dmlOperator, NodeDefinition beforeObj, NodeDefinition currentObj, String desc) {
        NodeDefinitionHistory history = NodeDefinitionHistoryBuilder.builder()
                .tenantId(tenantId)
                .nodeDefinitionId(nodeDefinitionId)
                .dmlOperator(dmlOperator)
                .beforeObj(beforeObj)
                .currentObj(currentObj)
                .desc(desc)
                .build();
        NodeDefinitionHistoryExecutor historyExecutor = this.nodeDefinitionHistoryExecutorBuilder.build();
        historyExecutor.save(history);
    }

    /**
     * 获取节点定义历史记录
     *
     * @param tenantId                租户 ID
     * @param nodeDefinitionHistoryId 节点定义历史记录 ID
     *
     * @return NodeDefinitionHistory
     *
     * @author wangweijun
     * @since 2024/10/8 16:07
     */
    @Override
    public NodeDefinitionHistory getNodeDefinitionHistory(String tenantId, Integer nodeDefinitionHistoryId) {
        NodeDefinitionHistoryExecutor executor = this.nodeDefinitionHistoryExecutorBuilder.build();
        Query query = QueryBuilderWrapper.createLambda(NodeDefinitionHistory.class)
                .eq(NodeDefinitionHistory::getTenantId, tenantId)
                .eq(NodeDefinitionHistory::getId, nodeDefinitionHistoryId)
                .build();
        return executor.get(query);
    }

    /**
     * 根据节点定义 ID 查找节点历史记录（分页）
     *
     * @param tenantId         租户 ID
     * @param nodeDefinitionId 节点定义 ID
     * @param page             当前页
     * @param pageSize         每页显示数量
     *
     * @return Page<NodeDefinitionHistory>
     *
     * @author wangweijun
     * @since 2024/10/8 16:00
     */
    @Override
    public Page<NodeDefinitionHistory> findNodeHistoriesByNodeDefinitionId(String tenantId, Integer nodeDefinitionId, Integer page, Integer pageSize) {
        NodeDefinitionHistoryExecutor executor = this.nodeDefinitionHistoryExecutorBuilder.build();
        Query query = QueryBuilderWrapper.createLambda(NodeDefinitionHistory.class)
                .eq(NodeDefinitionHistory::getTenantId, tenantId)
                .eq(NodeDefinitionHistory::getNodeDefinitionId, nodeDefinitionId)
                .orderBy(NodeDefinitionHistory::getId, OrderByOperator.DESC)
                .page(page, pageSize)
                .build();
        return executor.find(query);
    }

    /**
     * 根据流程定义 ID 查找节点历史记录（分页）
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     * @param page                 当前页
     * @param pageSize             每页显示数量
     *
     * @return Page<NodeDefinitionHistory>
     *
     * @author wangweijun
     * @since 2024/10/8 16:00
     */
    @Override
    public Page<NodeDefinitionHistory> findNodeHistoriesByWorkflowDefinitionId(String tenantId, Integer workflowDefinitionId, Integer page, Integer pageSize) {
        NodeDefinitionHistoryExecutor executor = this.nodeDefinitionHistoryExecutorBuilder.build();
        return executor.findNodeHistoriesByWorkflowDefinitionId(tenantId, workflowDefinitionId, page, pageSize);
    }

    /**
     * 根据租户 ID 查找节点历史记录（分页）
     *
     * @param tenantId 租户 ID
     * @param page     当前页
     * @param pageSize 每页显示数量
     *
     * @return Page<NodeDefinitionHistory>
     *
     * @author wangweijun
     * @since 2024/10/8 15:59
     */
    @Override
    public Page<NodeDefinitionHistory> findNodeHistoriesByTenantId(String tenantId, Integer page, Integer pageSize) {
        NodeDefinitionHistoryExecutor executor = this.nodeDefinitionHistoryExecutorBuilder.build();
        Query query = QueryBuilderWrapper.createLambda(NodeDefinitionHistory.class)
                .eq(NodeDefinitionHistory::getTenantId, tenantId)
                .orderBy(NodeDefinitionHistory::getId, OrderByOperator.DESC)
                .page(page, pageSize)
                .build();
        return executor.find(query);
    }

    /**
     * 获取用户任务关联记录
     *
     * @param tenantId         租户 ID
     * @param nodeDefinitionId 节点定义 ID
     *
     * @return Set<NodeAssignment>
     *
     * @author wangweijun
     * @since 2024/10/10 21:20
     */
    @Override
    public List<NodeAssignment> findNodeAssignments(String tenantId, Integer nodeDefinitionId) {
        NodeAssignmentExecutor nodeAssignmentExecutor = nodeAssignmentExecutorBuilder.build();
        return nodeAssignmentExecutor.findByNodeDefinitionId(tenantId, nodeDefinitionId);
    }

    /**
     * 查询节点的审批用户
     *
     * @param tenantId         租户 ID
     * @param nodeDefinitionId 节点定义 ID
     *
     * @return List<Approver>
     *
     * @author wangweijun
     * @since 2024/10/10 21:31
     */
    @Override
    public List<Approver> findApprovers(String tenantId, Integer nodeDefinitionId) {
        NodeDefinition nodeDefinition = this.getNode(tenantId, nodeDefinitionId);
        List<NodeAssignment> nodeAssignments = this.findNodeAssignments(tenantId, nodeDefinitionId);
        return nodeAssignments.stream().map(nodeAssignment -> {
            Approver approver = Approver.of(nodeAssignment.getApproverId(), nodeAssignment.getDesc());
            approver.setRoleType(nodeDefinition.isRoleApprove());
            return approver;
        }).toList();
    }

    /**
     * 查询角色节点的审批用户
     *
     * @param tenantId         租户 ID
     * @param nodeDefinitionId 节点定义 ID
     *
     * @return List<RoleApprover>
     *
     * @author wangweijun
     * @since 2024/10/10 21:31
     */
    @Override
    public List<RoleApprover> findRoleApprovers(String tenantId, Integer nodeDefinitionId) {
        NodeDefinition nodeDefinition = this.getNode(tenantId, nodeDefinitionId);
        if (nodeDefinition.isUserApprove()) {
            return Collections.emptyList();
        }
        NodeRoleAssignmentExecutor nodeRoleAssignmentExecutor = this.nodeRoleAssignmentExecutorBuilder.build();
        List<NodeAssignment> nodeAssignments = this.findNodeAssignments(tenantId, nodeDefinitionId);
        return nodeAssignments.stream().map(nodeAssignment -> {
                    String roleId = nodeAssignment.getApproverId();
                    return nodeRoleAssignmentExecutor.findByNodeDefinitionIdRoleId(tenantId, nodeDefinitionId, roleId);
                })
                .flatMap(List::stream)
                .map(nodeRoleAssignment -> RoleApprover.of(nodeRoleAssignment.getRoleId(), nodeRoleAssignment.getDesc(), Approver.of(nodeRoleAssignment.getUserId(), nodeRoleAssignment.getUserDesc())))
                .toList();
    }
}
