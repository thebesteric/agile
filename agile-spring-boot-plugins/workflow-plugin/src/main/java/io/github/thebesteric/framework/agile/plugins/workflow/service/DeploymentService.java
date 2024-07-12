package io.github.thebesteric.framework.agile.plugins.workflow.service;

import io.github.thebesteric.framework.agile.plugins.database.core.domain.Page;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.Pager;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowDefinition;

import java.util.List;

/**
 * 部署 Service
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-14 10:32:13
 */
public interface DeploymentService {

    /**
     * 创建流程定义
     *
     * @param workflowDefinition 流程定义
     *
     * @return WorkflowDefinition
     *
     * @author wangweijun
     * @since 2024/6/17 15:20
     */
    WorkflowDefinition create(WorkflowDefinition workflowDefinition);

    /**
     * 删除流程定义
     *
     * @param tenantId 租户
     * @param key      key
     *
     * @author wangweijun
     * @since 2024/6/17 15:26
     */
    void delete(String tenantId, String key);

    /**
     * 获取流程定义
     *
     * @param tenantId 租户
     * @param key      key
     *
     * @return WorkflowDefinition
     *
     * @author wangweijun
     * @since 2024/6/17 15:26
     */
    WorkflowDefinition get(String tenantId, String key);

    /**
     * 获取流程定义列表
     *
     * @param tenantId 租户
     *
     * @return WorkflowDefinition
     *
     * @author wangweijun
     * @since 2024/6/17 15:26
     */
    List<WorkflowDefinition> find(String tenantId);


    /**
     * 获取流程定义列表（分页）
     *
     * @param tenantId 租户
     *
     * @return WorkflowDefinition
     *
     * @author wangweijun
     * @since 2024/6/17 15:26
     */
    Page<WorkflowDefinition> find(String tenantId, Pager pager);

    /**
     * 禁用
     *
     * @param tenantId 租户
     * @param key      key
     *
     * @author wangweijun
     * @since 2024/6/18 14:08
     */
    void disable(String tenantId, String key);

    /**
     * 启用
     *
     * @param tenantId 租户
     * @param key      key
     *
     * @author wangweijun
     * @since 2024/6/18 14:08
     */
    void enable(String tenantId, String key);

    /**
     * 更新
     *
     * @param workflowDefinition 需要更新的流程定义
     *
     * @author wangweijun
     * @since 2024/6/18 14:18
     */
    void update(WorkflowDefinition workflowDefinition);

}
