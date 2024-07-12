package io.github.thebesteric.framework.agile.plugins.workflow.helper.service;

import io.github.thebesteric.framework.agile.plugins.database.core.domain.Page;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.Pager;
import io.github.thebesteric.framework.agile.plugins.workflow.WorkflowEngine;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.definition.WorkflowDefinitionBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowDefinition;
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
    public WorkflowDefinition get(String tenantId, String key) {
        return this.deploymentService.get(tenantId, key);
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
     * 启用流程定义
     *
     * @param tenantId 租户 ID
     * @param key      流程唯一标识
     */
    public void enable(String tenantId, String key) {
        this.deploymentService.enable(tenantId, key);
    }

}
