package io.github.thebesteric.framework.agile.plugins.workflow.service;

import io.github.thebesteric.framework.agile.plugins.database.core.domain.Page;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.builder.Query;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.NodeDefinition;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowDefinition;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowInstance;

import java.util.List;

/**
 * 流程 Service
 *
 * @author wangweijun
 * @since 2024/6/13 21:23
 */
public interface WorkflowService {

    /**
     * 获取流程定义
     *
     * @param tenantId 租户 ID
     * @param key      key
     *
     * @return WorkflowDefinition
     *
     * @author wangweijun
     * @since 2024/7/2 17:14
     */
    WorkflowDefinition getWorkflowDefinition(String tenantId, String key);

    /**
     * 获取流程定义
     *
     * @param query 查询条件
     *
     * @return List<WorkflowDefinition>
     *
     * @author wangweijun
     * @since 2024/6/28 17:59
     */
    Page<WorkflowDefinition> findWorkflowDefinitions(Query query);

    /**
     * 获取流程实例
     *
     * @param query 查询条件
     *
     * @return List<WorkflowInstance>
     *
     * @author wangweijun
     * @since 2024/6/28 17:59
     */
    Page<WorkflowInstance> findWorkflowInstances(Query query);

    /**
     * 创建节点定义
     *
     * @param nodeDefinition 节点定义
     *
     * @return NodeDefinition
     *
     * @author wangweijun
     * @since 2024/6/18 16:52
     */
    NodeDefinition createNode(NodeDefinition nodeDefinition);

    /**
     * 获取节点定义
     *
     * @param tenantId         租户 ID
     * @param nodeDefinitionId 节点定义 ID
     *
     * @return NodeDefinition
     *
     * @author wangweijun
     * @since 2024/6/18 17:41
     */
    NodeDefinition getNode(String tenantId, Integer nodeDefinitionId);

    /**
     * 获取节点定义
     *
     * @param tenantId          租户 ID
     * @param nodeDefinitionIds 节点定义 IDs
     *
     * @return NodeDefinition
     *
     * @author wangweijun
     * @since 2024/6/18 17:41
     */
    List<NodeDefinition> getNodes(String tenantId, List<Integer> nodeDefinitionIds);

    /**
     * 获取节点定义
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     *
     * @return NodeDefinition
     *
     * @author wangweijun
     * @since 2024/6/18 17:41
     */
    List<NodeDefinition> getNodes(String tenantId, Integer workflowDefinitionId);

    /**
     * 获取开始节点定义
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     *
     * @return NodeDefinition
     *
     * @author wangweijun
     * @since 2024/6/18 17:41
     */
    NodeDefinition getStartNode(String tenantId, Integer workflowDefinitionId);

    /**
     * 获取结束节点定义
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     *
     * @return NodeDefinition
     *
     * @author wangweijun
     * @since 2024/6/18 17:41
     */
    NodeDefinition getEndNode(String tenantId, Integer workflowDefinitionId);

    /**
     * 获取任务节点定义集合
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     *
     * @return NodeDefinition
     *
     * @author wangweijun
     * @since 2024/6/18 17:41
     */
    List<NodeDefinition> findTaskNodes(String tenantId, Integer workflowDefinitionId);

    /**
     * 根据来自节点 ID 查找目标节点集合
     *
     * @param tenantId             租户 ID
     * @param fromNodeDefinitionId 来自节点 ID
     *
     * @return List<NodeDefinition>
     *
     * @author wangweijun
     * @since 2024/6/24 16:00
     */
    List<NodeDefinition> findToTaskNodesByFromNodeId(String tenantId, Integer fromNodeDefinitionId);

    /**
     * 更新节点定义
     *
     * @param nodeDefinition 节点定义
     *
     * @author wangweijun
     * @since 2024/6/18 16:52
     */
    void updateNode(NodeDefinition nodeDefinition);

    /**
     * 删除节点定义
     *
     * @param tenantId         租户 ID
     * @param nodeDefinitionId 节点定义 ID
     *
     * @author wangweijun
     * @since 2024/6/19 09:49
     */
    boolean deleteNode(String tenantId, Integer nodeDefinitionId);

    /**
     * 创建节点关系
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 节点定义 ID
     *
     * @author wangweijun
     * @since 2024/6/19 10:30
     */
    void createRelations(String tenantId, Integer workflowDefinitionId);
}
