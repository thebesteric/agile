package io.github.thebesteric.framework.agile.plugins.workflow.service.impl;

import cn.hutool.core.collection.CollUtil;
import io.github.thebesteric.framework.agile.commons.util.LoggerPrinter;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.Page;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.builder.Query;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.builder.QueryBuilderWrapper;
import io.github.thebesteric.framework.agile.plugins.database.core.jdbc.JdbcTemplateHelper;
import io.github.thebesteric.framework.agile.plugins.workflow.config.AgileWorkflowContext;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.*;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.Approver;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.assignment.NodeAssignmentBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.assignment.NodeAssignmentExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.assignment.NodeAssignmentExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.definition.NodeDefinitionExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.definition.NodeDefinitionExecutorBuilder;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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
    private final WorkflowInstanceExecutorBuilder workflowInstanceExecutorBuilder;
    private final WorkflowDefinitionExecutorBuilder workflowDefinitionExecutorBuilder;

    public WorkflowServiceImpl(AgileWorkflowContext context) {
        super(context);
        JdbcTemplate jdbcTemplate = context.getJdbcTemplateHelper().getJdbcTemplate();
        nodeDefinitionExecutorBuilder = NodeDefinitionExecutorBuilder.builder(jdbcTemplate);
        nodeRelationExecutorBuilder = NodeRelationExecutorBuilder.builder(jdbcTemplate);
        nodeAssignmentExecutorBuilder = NodeAssignmentExecutorBuilder.builder(jdbcTemplate);
        workflowInstanceExecutorBuilder = WorkflowInstanceExecutorBuilder.builder(jdbcTemplate);
        workflowDefinitionExecutorBuilder = WorkflowDefinitionExecutorBuilder.builder(jdbcTemplate);
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
        NodeDefinitionExecutor executor = nodeDefinitionExecutorBuilder.nodeDefinition(nodeDefinition).build();

        // 查找所属流程定义
        WorkflowDefinitionExecutor workflowDefinitionExecutor = workflowDefinitionExecutorBuilder.build();
        WorkflowDefinition workflowDefinition = workflowDefinitionExecutor.getById(nodeDefinition.getWorkflowDefinitionId());

        // 将流程定义设置为未发布
        if (workflowDefinition.isPublished()) {
            workflowDefinition.setPublish(PublishStatus.UNPUBLISHED);
            workflowDefinitionExecutor.updateById(workflowDefinition);
        }

        return executor.save();
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
        return createNode(nodeDefinition);
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
            if (NodeType.START == nodeDefinition.getNodeType() || NodeType.END == nodeDefinition.getNodeType()) {
                throw new WorkflowException("节点更新修改失败: 无法修改开始节点或结束节点");
            }

            if (nodeDefinition.getSequence() >= Integer.MAX_VALUE || nodeDefinition.getSequence() <= Integer.MIN_VALUE) {
                throw new WorkflowException("节点更新修改失败: 节点顺序不能大于等于 %s 或小于等于 %s", Integer.MAX_VALUE, Integer.MIN_VALUE);
            }

            // 租户 ID
            String tenantId = nodeDefinition.getTenantId();

            WorkflowDefinitionExecutor workflowDefinitionExecutor = workflowDefinitionExecutorBuilder.build();
            NodeAssignmentExecutor nodeAssignmentExecutor = nodeAssignmentExecutorBuilder.build();
            NodeDefinitionExecutor nodeDefinitionExecutor = nodeDefinitionExecutorBuilder.build();

            // 查找所属流程定义
            WorkflowDefinition workflowDefinition = workflowDefinitionExecutor.getById(nodeDefinition.getWorkflowDefinitionId());
            Integer workflowDefinitionId = workflowDefinition.getId();

            // 查找所属流程实例，是否正在进行中
            WorkflowInstanceExecutor workflowInstanceExecutor = workflowInstanceExecutorBuilder.build();
            Query query = QueryBuilderWrapper.createLambda(WorkflowInstance.class)
                    .eq(WorkflowInstance::getTenantId, tenantId)
                    .eq(WorkflowInstance::getWorkflowDefinitionId, workflowDefinitionId)
                    .eq(WorkflowInstance::getStatus, WorkflowStatus.IN_PROGRESS.getCode())
                    .eq(WorkflowInstance::getState, 1).build();
            Page<WorkflowInstance> page = workflowInstanceExecutor.find(query);
            if (CollUtil.isNotEmpty(page.getRecords())) {
                throw new WorkflowInstanceInProgressException();
            }

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
                workflowDefinition.setPublish(PublishStatus.UNPUBLISHED);
                workflowDefinitionExecutor.updateById(workflowDefinition);
            }
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
        NodeDefinition nodeDefinition = getNode(tenantId, nodeDefinitionId);

        // 判断是否是开始或结束节点
        if (NodeType.START == nodeDefinition.getNodeType() || NodeType.END == nodeDefinition.getNodeType()) {
            throw new WorkflowException("节点删除失败: 无法删除开始节点或结束节点");
        }

        // 获取任务节点
        List<NodeDefinition> taskNodes = findTaskNodes(tenantId, nodeDefinition.getWorkflowDefinitionId());
        if (taskNodes.size() <= 1) {
            throw new WorkflowException("节点删除失败: 节点定义必须至少存在一个任务节点");
        }

        // 查找所属流程定义
        WorkflowDefinitionExecutor workflowDefinitionExecutor = workflowDefinitionExecutorBuilder.build();
        WorkflowDefinition workflowDefinition = workflowDefinitionExecutor.getById(nodeDefinition.getWorkflowDefinitionId());

        // 将流程定义设置为未发布
        if (workflowDefinition.isPublished()) {
            workflowDefinition.setPublish(PublishStatus.UNPUBLISHED);
            workflowDefinitionExecutor.updateById(workflowDefinition);
        }

        NodeDefinitionExecutor executor = nodeDefinitionExecutorBuilder.tenantId(tenantId).id(nodeDefinitionId).build();
        return executor.deleteById();
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
            workflowDefinition.setPublish(PublishStatus.PUBLISHED);
            workflowDefinitionExecutor.updateById(workflowDefinition);

        }, true);
    }

    private void createRelations(String tenantId, Integer workflowDefinitionId, List<NodeDefinition> fromNodes, List<NodeDefinition> toNodes, Double sequence) {
        for (NodeDefinition fromNode : fromNodes) {
            for (NodeDefinition toNode : toNodes) {
                NodeRelation rejectNodeRelation = NodeRelationBuilder.builder(tenantId, workflowDefinitionId).fromNode(fromNode).toNode(toNode).sequence(sequence).build();
                this.createRelation(rejectNodeRelation);
            }
        }
    }

    private void createRelation(NodeRelation nodeRelation) {
        NodeRelationExecutor executor = nodeRelationExecutorBuilder.nodeRelation(nodeRelation).build();
        executor.save();
    }

    private void inactiveRelations(String tenantId, Integer workflowDefinitionId) {
        NodeRelationExecutor executor = nodeRelationExecutorBuilder.build();
        executor.inactiveByWorkflowDefinitionId(tenantId, workflowDefinitionId);
    }
}
