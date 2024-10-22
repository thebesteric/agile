package io.github.thebesteric.framework.agile.plugins.workflow.helper.service;

import io.github.thebesteric.framework.agile.plugins.database.core.domain.Page;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.Pager;
import io.github.thebesteric.framework.agile.plugins.workflow.WorkflowEngine;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.definition.WorkflowDefinitionBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.response.WorkflowDefinitionFlowSchema;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowDefinition;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowDefinitionHistory;
import io.github.thebesteric.framework.agile.plugins.workflow.helper.AbstractServiceHelper;
import io.github.thebesteric.framework.agile.plugins.workflow.service.DeploymentService;

import java.util.List;

/**
 * 流程部署帮助类
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-08 14:48:30
 */
public class DeploymentServiceHelper extends AbstractServiceHelper {

    private final DeploymentService deploymentService;

    public DeploymentServiceHelper(WorkflowEngine workflowEngine) {
        super(workflowEngine);
        this.deploymentService = workflowEngine.getDeploymentService();
    }

    /**
     * 部署流程
     *
     * @param workflowDefinitionBuilder workflowDefinitionBuilder
     *
     * @return WorkflowDefinition
     *
     * @author wangweijun
     * @since 2024/7/8 14:39
     */
    public WorkflowDefinition deploy(WorkflowDefinitionBuilder workflowDefinitionBuilder) {
        WorkflowDefinition workflowDefinition = workflowDefinitionBuilder.build();
        return this.deploymentService.create(workflowDefinition);
    }

    /**
     * 部署流程
     *
     * @param tenantId 租户 ID
     * @param name     流程名称
     * @param key      流程唯一标识
     * @param type     流程类型
     * @param desc     流氓描述
     *
     * @return WorkflowDefinition
     *
     * @author wangweijun
     * @since 2024/7/8 14:40
     */
    public WorkflowDefinition deploy(String tenantId, String name, String key, String type, String desc) {
        WorkflowDefinitionBuilder workflowDefinitionBuilder = WorkflowDefinitionBuilder.builder()
                .tenantId(tenantId).name(name).key(key).type(type).desc(desc);
        return this.deploy(workflowDefinitionBuilder);
    }

    /**
     * 部署流程
     *
     * @param tenantId 租户 ID
     * @param name     流程名称
     * @param key      流程唯一标识
     * @param type     流程类型
     *
     * @return WorkflowDefinition
     *
     * @author wangweijun
     * @since 2024/7/8 14:40
     */
    public WorkflowDefinition deploy(String tenantId, String name, String key, String type) {
        return this.deploy(tenantId, name, key, type, null);
    }

    /**
     * 部署流程
     *
     * @param tenantId 租户 ID
     * @param name     流程名称
     * @param key      流程唯一标识
     *
     * @return WorkflowDefinition
     *
     * @author wangweijun
     * @since 2024/7/8 14:40
     */
    public WorkflowDefinition deploy(String tenantId, String name, String key) {
        return this.deploy(tenantId, name, key, null);
    }


    /**
     * 获取流程定义
     *
     * @param tenantId 租户 ID
     * @param key      流程唯一标识
     *
     * @return WorkflowDefinition
     *
     * @author wangweijun
     * @since 2024/7/8 16:26
     */
    public WorkflowDefinition getByKey(String tenantId, String key) {
        return this.deploymentService.getByKey(tenantId, key);
    }

    /**
     * 获取流程定义
     *
     * @param tenantId 租户 ID
     * @param id       id
     *
     * @return WorkflowDefinition
     *
     * @author wangweijun
     * @since 2024/9/10 17:23
     */
    public WorkflowDefinition getById(String tenantId, Integer id) {
        return this.deploymentService.getById(tenantId, id);
    }

    /**
     * 获取流程定义
     *
     * @param tenantId 租户 ID
     *
     * @return WorkflowDefinition
     *
     * @author wangweijun
     * @since 2024/7/8 16:26
     */
    public List<WorkflowDefinition> list(String tenantId) {
        return this.deploymentService.find(tenantId);
    }

    /**
     * 获取流程定义
     *
     * @param tenantId 租户 ID
     * @param page     当前页
     * @param pageSize 每页显示数量
     *
     * @return WorkflowDefinition
     *
     * @author wangweijun
     * @since 2024/7/8 16:26
     */
    public Page<WorkflowDefinition> page(String tenantId, Integer page, Integer pageSize) {
        Pager pager = Pager.of(page, pageSize);
        return this.deploymentService.find(tenantId, pager);
    }

    /**
     * 更新流程定义
     *
     * @param workflowDefinition 流程定义
     *
     * @author wangweijun
     * @since 2024/7/11 19:54
     */
    public void update(WorkflowDefinition workflowDefinition) {
        this.deploymentService.update(workflowDefinition);
    }

    /**
     * 禁用流程定义
     *
     * @param tenantId 租户 ID
     * @param key      流程唯一标识
     */
    public void disable(String tenantId, String key) {
        this.deploymentService.disable(tenantId, key);
    }

    /**
     * 禁用流程定义
     *
     * @param tenantId 租户 ID
     * @param id       流程 ID
     */
    public void disable(String tenantId, Integer id) {
        WorkflowDefinition workflowDefinition = getById(tenantId, id);
        this.disable(tenantId, workflowDefinition.getKey());
    }

    /**
     * 启用流程定义
     *
     * @param tenantId 租户 ID
     * @param key      流程唯一标识
     */
    public void enable(String tenantId, String key) {
        this.deploymentService.enable(tenantId, key);
    }

    /**
     * 启用流程定义
     *
     * @param tenantId 租户 ID
     * @param id       流程 ID
     */
    public void enable(String tenantId, Integer id) {
        WorkflowDefinition workflowDefinition = getById(tenantId, id);
        this.enable(tenantId, workflowDefinition.getKey());
    }

    /**
     * 获取流程定义纲要
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     *
     * @return WorkflowDefinitionFlowSchema
     *
     * @author wangweijun
     * @since 2024/9/29 18:31
     */
    public WorkflowDefinitionFlowSchema getWorkflowDefinitionFlowSchema(String tenantId, Integer workflowDefinitionId) {
        WorkflowDefinition workflowDefinition = getById(tenantId, workflowDefinitionId);
        return this.deploymentService.getWorkflowDefinitionFlowSchema(tenantId, workflowDefinition);
    }

    /**
     * 获取流程定义流程图
     *
     * @param tenantId 租户 ID
     * @param key      流程唯一标识
     *
     * @return WorkflowDefinitionFlowSchema
     *
     * @author wangweijun
     * @since 2024/9/29 18:31
     */
    public WorkflowDefinitionFlowSchema getWorkflowDefinitionFlowSchema(String tenantId, String key) {
        WorkflowDefinition workflowDefinition = getByKey(tenantId, key);
        return this.deploymentService.getWorkflowDefinitionFlowSchema(tenantId, workflowDefinition);
    }

    /**
     * 根据流程定义 key 获取流程定义历史记录列表（分页）
     *
     * @param tenantId              租户 ID
     * @param workflowDefinitionKey 流程定义 key
     * @param page                  当前页
     * @param pageSize              每页显示数量
     *
     * @return Page<WorkflowDefinitionHistory>
     *
     * @author wangweijun
     * @since 2024/10/8 13:15
     */
    public Page<WorkflowDefinitionHistory> findHistoriesByWorkflowDefinitionKey(String tenantId, String workflowDefinitionKey, Integer page, Integer pageSize) {
        return this.deploymentService.findHistoriesByWorkflowDefinitionKey(tenantId, workflowDefinitionKey, page, pageSize);
    }

    /**
     * 根据流程定义 ID 获取流程定义历史记录列表（分页）
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     * @param page                 当前页
     * @param pageSize             每页显示数量
     *
     * @return Page<WorkflowDefinitionHistory>
     *
     * @author wangweijun
     * @since 2024/10/8 13:15
     */
    public Page<WorkflowDefinitionHistory> findHistoriesByWorkflowDefinitionId(String tenantId, Integer workflowDefinitionId, Integer page, Integer pageSize) {
        return this.deploymentService.findHistoriesByWorkflowDefinitionId(tenantId, workflowDefinitionId, page, pageSize);
    }

    /**
     * 获取所有流程定义历史记录列表（分页）
     *
     * @param tenantId 租户 ID
     * @param page     当前页
     * @param pageSize 每页显示数量
     *
     * @return Page<WorkflowDefinitionHistory>
     *
     * @author wangweijun
     * @since 2024/10/8 13:41
     */
    public Page<WorkflowDefinitionHistory> findHistories(String tenantId, Integer page, Integer pageSize) {
        return this.deploymentService.findHistories(tenantId, page, pageSize);
    }
}
