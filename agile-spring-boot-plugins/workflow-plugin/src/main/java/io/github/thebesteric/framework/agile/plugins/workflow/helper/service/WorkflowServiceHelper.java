package io.github.thebesteric.framework.agile.plugins.workflow.helper.service;

import io.github.thebesteric.framework.agile.plugins.workflow.WorkflowEngine;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.ApproveType;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.NodeType;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.Conditions;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.definition.NodeDefinitionBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.NodeDefinition;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowDefinition;
import io.github.thebesteric.framework.agile.plugins.workflow.helper.AbstractServiceHelper;
import io.github.thebesteric.framework.agile.plugins.workflow.service.WorkflowService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 流程定义帮助类
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-08 14:59:17
 */
public class WorkflowServiceHelper extends AbstractServiceHelper {

    private final WorkflowService workflowService;

    public WorkflowServiceHelper(WorkflowEngine workflowEngine) {
        super(workflowEngine);
        this.workflowService = workflowEngine.getWorkflowService();
    }

    /**
     * 创建流程节点
     *
     * @param nodeDefinitionBuilder 节点定义创建器
     *
     * @return NodeDefinition
     *
     * @author wangweijun
     * @since 2024/7/8 15:59
     */
    public NodeDefinition createNode(NodeDefinitionBuilder nodeDefinitionBuilder) {
        NodeDefinition nodeDefinition = nodeDefinitionBuilder.build();
        nodeDefinition = this.workflowService.createNode(nodeDefinition);
        return nodeDefinition;
    }

    /**
     * 创建流程节点
     *
     * @param workflowDefinition 流程定义
     * @param name               节点名称
     * @param sequence           节点排序
     * @param nodeType           节点类型
     * @param approveType        审批类型
     * @param approverIds        审批人
     * @param conditions         审批条件
     * @param desc               节点描述
     *
     * @return NodeDefinition
     *
     * @author wangweijun
     * @since 2024/7/8 16:01
     */
    public NodeDefinition createNode(WorkflowDefinition workflowDefinition, String name, Integer sequence,
                                     NodeType nodeType, ApproveType approveType, List<String> approverIds, Conditions conditions, String desc) {
        String tenantId = workflowDefinition.getTenantId();
        Integer workflowDefinitionId = workflowDefinition.getId();
        NodeDefinitionBuilder nodeDefinitionBuilder = NodeDefinitionBuilder.builderNode(tenantId, workflowDefinitionId, nodeType, sequence)
                .name(name).approveType(approveType).approverIds(approverIds).conditions(conditions).desc(desc);
        return this.createNode(nodeDefinitionBuilder);
    }

    /**
     * 创建流程节点
     *
     * @param nodeDefinitionBuilder 节点定义创建器
     *
     * @return NodeDefinition
     *
     * @author wangweijun
     * @since 2024/7/8 15:59
     */
    public NodeDefinition createTaskNode(NodeDefinitionBuilder nodeDefinitionBuilder) {
        return this.createNode(nodeDefinitionBuilder.nodeType(NodeType.TASK));
    }


    /**
     * 创建流程节点
     *
     * @param workflowDefinition 流程定义
     * @param name               节点名称
     * @param sequence           节点排序
     * @param approveType        审批类型
     * @param approverIds        审批人
     * @param conditions         审批条件
     * @param desc               节点描述
     *
     * @return NodeDefinition
     *
     * @author wangweijun
     * @since 2024/7/8 16:01
     */
    public NodeDefinition createTaskNode(WorkflowDefinition workflowDefinition, String name, Integer sequence,
                                         ApproveType approveType, List<String> approverIds, Conditions conditions, String desc) {
        return this.createNode(workflowDefinition, name, sequence, NodeType.TASK, approveType, approverIds, conditions, desc);
    }

    /**
     * 创建流程节点
     *
     * @param workflowDefinition 流程定义
     * @param name               节点名称
     * @param sequence           节点排序
     * @param approveType        审批类型
     * @param approverIds        审批人
     * @param conditions         审批条件
     *
     * @return NodeDefinition
     *
     * @author wangweijun
     * @since 2024/7/8 16:01
     */
    public NodeDefinition createTaskNode(WorkflowDefinition workflowDefinition, String name, Integer sequence,
                                         ApproveType approveType, List<String> approverIds, Conditions conditions) {
        return this.createTaskNode(workflowDefinition, name, sequence, approveType, approverIds, conditions, null);
    }

    /**
     * 创建流程节点
     *
     * @param workflowDefinition 流程定义
     * @param name               节点名称
     * @param sequence           节点排序
     * @param approveType        审批类型
     * @param approverIds        审批人
     * @param desc               节点描述
     *
     * @return NodeDefinition
     *
     * @author wangweijun
     * @since 2024/7/8 16:01
     */
    public NodeDefinition createTaskNode(WorkflowDefinition workflowDefinition, String name, Integer sequence,
                                         ApproveType approveType, List<String> approverIds, String desc) {
        return this.createTaskNode(workflowDefinition, name, sequence, approveType, approverIds, null, desc);
    }

    /**
     * 创建流程节点
     *
     * @param workflowDefinition 流程定义
     * @param name               节点名称
     * @param sequence           节点排序
     * @param approveType        审批类型
     * @param approverIds        审批人
     *
     * @return NodeDefinition
     *
     * @author wangweijun
     * @since 2024/7/8 16:01
     */
    public NodeDefinition createTaskNode(WorkflowDefinition workflowDefinition, String name, Integer sequence,
                                         ApproveType approveType, List<String> approverIds) {
        return this.createTaskNode(workflowDefinition, name, sequence, approveType, approverIds, null, null);
    }

    /**
     * 创建流程节点
     *
     * @param workflowDefinition 流程定义
     * @param name               节点名称
     * @param sequence           节点排序
     * @param approveType        审批类型
     * @param approverId         审批人
     * @param conditions         审批条件
     * @param desc               节点描述
     *
     * @return NodeDefinition
     *
     * @author wangweijun
     * @since 2024/7/8 16:01
     */
    public NodeDefinition createTaskNode(WorkflowDefinition workflowDefinition, String name, Integer sequence,
                                         ApproveType approveType, String approverId, Conditions conditions, String desc) {
        return this.createTaskNode(workflowDefinition, name, sequence, approveType, List.of(approverId), conditions, desc);
    }

    /**
     * 创建流程节点
     *
     * @param workflowDefinition 流程定义
     * @param name               节点名称
     * @param sequence           节点排序
     * @param approveType        审批类型
     * @param approverId         审批人
     * @param conditions         审批条件
     *
     * @return NodeDefinition
     *
     * @author wangweijun
     * @since 2024/7/8 16:01
     */
    public NodeDefinition createTaskNode(WorkflowDefinition workflowDefinition, String name, Integer sequence,
                                         ApproveType approveType, String approverId, Conditions conditions) {
        return this.createTaskNode(workflowDefinition, name, sequence, approveType, List.of(approverId), conditions);
    }

    /**
     * 创建流程节点
     *
     * @param workflowDefinition 流程定义
     * @param name               节点名称
     * @param sequence           节点排序
     * @param approveType        审批类型
     * @param approverId         审批人
     * @param desc               节点描述
     *
     * @return NodeDefinition
     *
     * @author wangweijun
     * @since 2024/7/8 16:01
     */
    public NodeDefinition createTaskNode(WorkflowDefinition workflowDefinition, String name, Integer sequence,
                                         ApproveType approveType, String approverId, String desc) {
        return this.createTaskNode(workflowDefinition, name, sequence, approveType, approverId, null, desc);
    }

    /**
     * 创建流程节点
     *
     * @param workflowDefinition 流程定义
     * @param name               节点名称
     * @param sequence           节点排序
     * @param approveType        审批类型
     * @param approverId         审批人
     *
     * @return NodeDefinition
     *
     * @author wangweijun
     * @since 2024/7/8 16:01
     */
    public NodeDefinition createTaskNode(WorkflowDefinition workflowDefinition, String name, Integer sequence,
                                         ApproveType approveType, String approverId) {
        return this.createTaskNode(workflowDefinition, name, sequence, approveType, approverId, null, null);
    }

    /**
     * 创建流程开始节点
     *
     * @param workflowDefinition 流程定义
     * @param name               节点名称
     *
     * @return NodeDefinition
     *
     * @author wangweijun
     * @since 2024/7/8 16:01
     */
    public NodeDefinition createStartNode(WorkflowDefinition workflowDefinition, String name) {
        return this.createStartNode(workflowDefinition, name, null);
    }

    /**
     * 创建流程开始节点
     *
     * @param workflowDefinition 流程定义
     * @param name               节点名称
     * @param desc               节点描述
     *
     * @return NodeDefinition
     *
     * @author wangweijun
     * @since 2024/7/8 16:01
     */
    public NodeDefinition createStartNode(WorkflowDefinition workflowDefinition, String name, String desc) {
        String tenantId = workflowDefinition.getTenantId();
        Integer workflowDefinitionId = workflowDefinition.getId();
        NodeDefinitionBuilder nodeDefinitionBuilder = NodeDefinitionBuilder.builderStartNode(tenantId, workflowDefinitionId).name(name).desc(desc);
        return this.createNode(nodeDefinitionBuilder);
    }

    /**
     * 创建流程结束节点
     *
     * @param workflowDefinition 流程定义
     * @param name               节点名称
     *
     * @return NodeDefinition
     *
     * @author wangweijun
     * @since 2024/7/8 16:01
     */
    public NodeDefinition createEndNode(WorkflowDefinition workflowDefinition, String name) {
        return this.createEndNode(workflowDefinition, name, null);
    }

    /**
     * 创建流程结束节点
     *
     * @param workflowDefinition 流程定义
     * @param name               节点名称
     * @param desc               节点描述
     *
     * @return NodeDefinition
     *
     * @author wangweijun
     * @since 2024/7/8 16:01
     */
    public NodeDefinition createEndNode(WorkflowDefinition workflowDefinition, String name, String desc) {
        String tenantId = workflowDefinition.getTenantId();
        Integer workflowDefinitionId = workflowDefinition.getId();
        NodeDefinitionBuilder nodeDefinitionBuilder = NodeDefinitionBuilder.builderEndNode(tenantId, workflowDefinitionId).name(name).desc(desc);
        return this.createNode(nodeDefinitionBuilder);
    }

    /**
     * 获取节点定义
     *
     * @param tenantId         租户 ID
     * @param nodeDefinitionId 节点定义 ID
     *
     * @return NodeDefinition
     *
     * @author wangweijun
     * @since 2024/7/8 16:01
     */
    public NodeDefinition getNode(String tenantId, Integer nodeDefinitionId) {
        return this.workflowService.getNode(tenantId, nodeDefinitionId);
    }

    /**
     * 获取节点定义
     *
     * @param tenantId          租户 ID
     * @param nodeDefinitionIds 节点定义 IDs
     *
     * @return List
     *
     * @author wangweijun
     * @since 2024/7/8 16:01
     */
    public List<NodeDefinition> getNodes(String tenantId, Integer nodeDefinitionId, Integer... nodeDefinitionIds) {
        List<Integer> ids = new ArrayList<>();
        ids.add(nodeDefinitionId);
        if (nodeDefinitionIds != null && nodeDefinitionIds.length > 0) {
            ids.addAll(Arrays.asList(nodeDefinitionIds));
        }
        return this.workflowService.getNodes(tenantId, ids);
    }

    /**
     * 获取开始节点
     *
     * @param workflowDefinition 节点定义
     *
     * @return NodeDefinition
     *
     * @author wangweijun
     * @since 2024/7/12 12:11
     */
    public NodeDefinition getStartNode(WorkflowDefinition workflowDefinition) {
        return this.getStartNode(workflowDefinition.getTenantId(), workflowDefinition.getId());
    }

    /**
     * 获取开始节点
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     *
     * @return NodeDefinition
     *
     * @author wangweijun
     * @since 2024/7/12 12:11
     */
    public NodeDefinition getStartNode(String tenantId, Integer workflowDefinitionId) {
        return this.workflowService.getStartNode(tenantId, workflowDefinitionId);
    }

    /**
     * 获取结束节点
     *
     * @param workflowDefinition 流程定义
     *
     * @return NodeDefinition
     *
     * @author wangweijun
     * @since 2024/7/12 12:45
     */
    public NodeDefinition getEndNode(WorkflowDefinition workflowDefinition) {
        return this.getEndNode(workflowDefinition.getTenantId(), workflowDefinition.getId());
    }

    /**
     * 获取结束节点
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     *
     * @return NodeDefinition
     *
     * @author wangweijun
     * @since 2024/7/12 12:45
     */
    public NodeDefinition getEndNode(String tenantId, Integer workflowDefinitionId) {
        return this.workflowService.getEndNode(tenantId, workflowDefinitionId);
    }

    /**
     * 更新节点定义
     *
     * @param nodeDefinition 节点定义
     *
     * @author wangweijun
     * @since 2024/7/11 21:32
     */
    public void update(NodeDefinition nodeDefinition) {
        this.workflowService.updateNode(nodeDefinition);
    }

    /**
     * 发布流程（创建节点关系）
     *
     * @param workflowDefinition 流程定义
     *
     * @author wangweijun
     * @since 2024/7/8 16:14
     */
    public void publish(WorkflowDefinition workflowDefinition) {
        String tenantId = workflowDefinition.getTenantId();
        Integer workflowDefinitionId = workflowDefinition.getId();
        this.publish(tenantId, workflowDefinitionId);
    }

    /**
     * 发布流程（创建节点关系）
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     *
     * @author wangweijun
     * @since 2024/7/8 16:14
     */
    public void publish(String tenantId, Integer workflowDefinitionId) {
        this.workflowService.createRelations(tenantId, workflowDefinitionId);
    }

}
