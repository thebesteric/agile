package io.github.thebesteric.framework.agile.plugins.workflow.helper.service;

import io.github.thebesteric.framework.agile.plugins.database.core.domain.Page;
import io.github.thebesteric.framework.agile.plugins.workflow.WorkflowEngine;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.NodeType;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.Approver;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.RoleApprover;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.definition.NodeDefinitionBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.NodeAssignment;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.NodeDefinition;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.NodeDefinitionHistory;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowDefinition;
import io.github.thebesteric.framework.agile.plugins.workflow.helper.AbstractServiceHelper;
import io.github.thebesteric.framework.agile.plugins.workflow.service.WorkflowService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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
     * 创建流程开始节点
     *
     * @param nodeDefinitionBuilder 流程定义
     *
     * @return NodeDefinition
     *
     * @author wangweijun
     * @since 2024/10/08 20:39
     */
    public NodeDefinition createStartNode(NodeDefinitionBuilder nodeDefinitionBuilder) {
        return this.createNode(nodeDefinitionBuilder.nodeType(NodeType.START));
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
     * 创建流程开始节点
     *
     * @param nodeDefinitionBuilder 流程定义
     *
     * @return NodeDefinition
     *
     * @author wangweijun
     * @since 2024/10/08 20:39
     */
    public NodeDefinition createEndNode(NodeDefinitionBuilder nodeDefinitionBuilder) {
        return this.createNode(nodeDefinitionBuilder.nodeType(NodeType.END));
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
     * 获取节点定义集合
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
     * 获取节点定义集合
     *
     * @param tenantId              租户 ID
     * @param workflowDefinitionKey 流程定义 Key
     *
     * @return List
     *
     * @author wangweijun
     * @since 2024/7/8 16:01
     */
    public List<NodeDefinition> getNodes(String tenantId, String workflowDefinitionKey) {
        WorkflowDefinition workflowDefinition = this.workflowService.getWorkflowDefinition(tenantId, workflowDefinitionKey);
        return this.workflowService.getNodes(tenantId, workflowDefinition.getId());
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
     * @param tenantId              租户 ID
     * @param workflowDefinitionKey 流程定义 KEY
     *
     * @return NodeDefinition
     *
     * @author wangweijun
     * @since 2024/10/24 10:09
     */
    public NodeDefinition getStartNode(String tenantId, String workflowDefinitionKey) {
        WorkflowDefinition workflowDefinition = this.workflowService.getWorkflowDefinition(tenantId, workflowDefinitionKey);
        return this.getStartNode(workflowDefinition);
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
     * @param tenantId              租户 ID
     * @param workflowDefinitionKey 流程定义 KEY
     *
     * @return NodeDefinition
     *
     * @author wangweijun
     * @since 2024/7/12 12:45
     */
    public NodeDefinition getEndNode(String tenantId, String workflowDefinitionKey) {
        WorkflowDefinition workflowDefinition = this.workflowService.getWorkflowDefinition(tenantId, workflowDefinitionKey);
        return this.getEndNode(workflowDefinition);
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
     * 获取所有任务节点
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     *
     * @return List<NodeDefinition>
     *
     * @author wangweijun
     * @since 2024/9/9 13:13
     */
    public List<NodeDefinition> findTaskNodes(String tenantId, Integer workflowDefinitionId) {
        List<NodeDefinition> nodes = this.workflowService.getNodes(tenantId, workflowDefinitionId);
        return nodes.stream().filter(node -> node.getNodeType() == NodeType.TASK).toList();
    }

    /**
     * 获取所有任务节点
     *
     * @param tenantId              租户 ID
     * @param workflowDefinitionKey 流程定义 Key
     *
     * @return List<NodeDefinition>
     *
     * @author wangweijun
     * @since 2024/9/9 13:13
     */
    public List<NodeDefinition> findTaskNodes(String tenantId, String workflowDefinitionKey) {
        WorkflowDefinition workflowDefinition = this.workflowService.getWorkflowDefinition(tenantId, workflowDefinitionKey);
        return this.findTaskNodes(tenantId, workflowDefinition.getId());
    }

    /**
     * 获取第一个任务节点
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     *
     * @return List<NodeDefinition>
     *
     * @author wangweijun
     * @since 2024/9/9 13:13
     */
    public NodeDefinition getFirstTaskNode(String tenantId, Integer workflowDefinitionId) {
        List<NodeDefinition> taskNodes = findTaskNodes(tenantId, workflowDefinitionId);
        return taskNodes.stream().min(Comparator.comparingDouble(NodeDefinition::getSequence)).orElse(null);
    }

    /**
     * 获取第一个任务节点
     *
     * @param tenantId              租户 ID
     * @param workflowDefinitionKey 流程定义 Key
     *
     * @return List<NodeDefinition>
     *
     * @author wangweijun
     * @since 2024/9/9 13:13
     */
    public NodeDefinition getFirstTaskNode(String tenantId, String workflowDefinitionKey) {
        WorkflowDefinition workflowDefinition = this.workflowService.getWorkflowDefinition(tenantId, workflowDefinitionKey);
        return this.getFirstTaskNode(tenantId, workflowDefinition.getId());
    }

    /**
     * 获取最后一个任务节点
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     *
     * @return List<NodeDefinition>
     *
     * @author wangweijun
     * @since 2024/9/9 13:13
     */
    public NodeDefinition getLastTaskNode(String tenantId, Integer workflowDefinitionId) {
        List<NodeDefinition> taskNodes = findTaskNodes(tenantId, workflowDefinitionId);
        return taskNodes.stream().max(Comparator.comparingDouble(NodeDefinition::getSequence)).orElse(null);
    }

    /**
     * 获取最后一个任务节点
     *
     * @param tenantId              租户 ID
     * @param workflowDefinitionKey 流程定义 Key
     *
     * @return List<NodeDefinition>
     *
     * @author wangweijun
     * @since 2024/9/9 13:13
     */
    public NodeDefinition getLastTaskNode(String tenantId, String workflowDefinitionKey) {
        WorkflowDefinition workflowDefinition = this.workflowService.getWorkflowDefinition(tenantId, workflowDefinitionKey);
        return this.getLastTaskNode(tenantId, workflowDefinition.getId());
    }

    /**
     * 更新节点定义
     *
     * @param tenantId         租户 ID
     * @param nodeDefinitionId 节点定义 ID
     *
     * @author wangweijun
     * @since 2024/9/6 15:08
     */
    public void updateNode(String tenantId, Integer nodeDefinitionId) {
        NodeDefinition nodeDefinition = getNode(tenantId, nodeDefinitionId);
        this.updateNode(nodeDefinition);
    }

    /**
     * 更新节点定义
     *
     * @param nodeDefinition 节点定义
     *
     * @author wangweijun
     * @since 2024/7/11 21:32
     */
    public void updateNode(NodeDefinition nodeDefinition) {
        this.workflowService.updateNode(nodeDefinition);
    }

    /**
     * 删除节点定义
     *
     * @param tenantId         租户 ID
     * @param nodeDefinitionId 节点定义 ID
     *
     * @author wangweijun
     * @since 2024/9/6 15:13
     */
    public void deleteNode(String tenantId, Integer nodeDefinitionId) {
        this.workflowService.deleteNode(tenantId, nodeDefinitionId);
    }

    /**
     * 删除节点定义
     *
     * @param nodeDefinition 节点定义
     *
     * @author wangweijun
     * @since 2024/9/6 15:13
     */
    public void deleteNode(NodeDefinition nodeDefinition) {
        this.deleteNode(nodeDefinition.getTenantId(), nodeDefinition.getId());
    }

    /**
     * 插入节点定义
     *
     * @param nodeDefinition     节点定义
     * @param prevNodeDefinition 上一个节点定义
     * @param nextNodeDefinition 下一个节点定义
     *
     * @return NodeDefinition
     *
     * @author wangweijun
     * @since 2024/9/6 16:48
     */
    public NodeDefinition insertNode(NodeDefinition nodeDefinition, NodeDefinition prevNodeDefinition, NodeDefinition nextNodeDefinition) {
        return this.workflowService.insertNode(nodeDefinition, prevNodeDefinition.getId(), nextNodeDefinition.getId());
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
    public NodeDefinition insertNode(NodeDefinition nodeDefinition, Integer prevNodeDefinitionId, Integer nextNodeDefinitionId) {
        return this.workflowService.insertNode(nodeDefinition, prevNodeDefinitionId, nextNodeDefinitionId);
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

    /**
     * 发布流程（创建节点关系）
     *
     * @param tenantId              租户 ID
     * @param workflowDefinitionKey 流程定义 Key
     *
     * @author wangweijun
     * @since 2024/7/8 16:14
     */
    public void publish(String tenantId, String workflowDefinitionKey) {
        WorkflowDefinition workflowDefinition = this.workflowService.getWorkflowDefinition(tenantId, workflowDefinitionKey);
        Integer workflowDefinitionId = workflowDefinition.getId();
        this.publish(tenantId, workflowDefinitionId);
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
    public NodeDefinitionHistory getNodeDefinitionHistory(String tenantId, Integer nodeDefinitionHistoryId) {
        return this.workflowService.getNodeDefinitionHistory(tenantId, nodeDefinitionHistoryId);
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
    public Page<NodeDefinitionHistory> findNodeHistoriesByNodeDefinitionId(String tenantId, Integer nodeDefinitionId, Integer page, Integer pageSize) {
        return this.workflowService.findNodeHistoriesByNodeDefinitionId(tenantId, nodeDefinitionId, page, pageSize);
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
    public Page<NodeDefinitionHistory> findNodeHistoriesByWorkflowDefinitionId(String tenantId, Integer workflowDefinitionId, Integer page, Integer pageSize) {
        return this.workflowService.findNodeHistoriesByWorkflowDefinitionId(tenantId, workflowDefinitionId, page, pageSize);
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
    public Page<NodeDefinitionHistory> findNodeHistoriesByTenantId(String tenantId, Integer page, Integer pageSize) {
        return this.workflowService.findNodeHistoriesByTenantId(tenantId, page, pageSize);
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
    public List<NodeAssignment> findNodeAssignments(String tenantId, Integer nodeDefinitionId) {
        return this.workflowService.findNodeAssignments(tenantId, nodeDefinitionId);
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
    public List<Approver> findApprovers(String tenantId, Integer nodeDefinitionId) {
        return this.workflowService.findApprovers(tenantId, nodeDefinitionId);
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
    public List<RoleApprover> findRoleApprovers(String tenantId, Integer nodeDefinitionId) {
        return this.workflowService.findRoleApprovers(tenantId, nodeDefinitionId);
    }

}
