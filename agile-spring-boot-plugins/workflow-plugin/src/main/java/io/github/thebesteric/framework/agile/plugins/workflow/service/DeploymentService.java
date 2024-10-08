package io.github.thebesteric.framework.agile.plugins.workflow.service;

import io.github.thebesteric.framework.agile.plugins.database.core.domain.Page;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.Pager;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.response.WorkflowDefinitionFlowSchema;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowDefinition;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowDefinitionHistory;

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
    WorkflowDefinition getByKey(String tenantId, String key);

    /**
     * 获取流程定义
     *
     * @param tenantId 租户
     * @param id       id
     *
     * @return WorkflowDefinition
     *
     * @author wangweijun
     * @since 2024/6/17 15:26
     */
    WorkflowDefinition getById(String tenantId, Integer id);

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
     * @param tenantId 租户 ID
     * @param key      key
     *
     * @author wangweijun
     * @since 2024/6/18 14:08
     */
    void disable(String tenantId, String key);

    /**
     * 启用
     *
     * @param tenantId 租户 ID
     * @param key      key
     *
     * @author wangweijun
     * @since 2024/6/18 14:08
     */
    void enable(String tenantId, String key);

    /**
     * 发布
     *
     * @param workflowDefinition 流程定义
     *
     * @author wangweijun
     * @since 2024/10/8 15:14
     */
    void publish(WorkflowDefinition workflowDefinition);

    /**
     * 取消发布
     *
     * @param workflowDefinition 流程定义
     *
     * @author wangweijun
     * @since 2024/10/8 15:14
     */
    void unPublish(WorkflowDefinition workflowDefinition);

    /**
     * 更新
     *
     * @param workflowDefinition 需要更新的流程定义
     *
     * @author wangweijun
     * @since 2024/6/18 14:18
     */
    void update(WorkflowDefinition workflowDefinition);

    /**
     * 更新
     *
     * @param workflowDefinition 需要更新的流程定义
     * @param desc               更新描述
     *
     * @author wangweijun
     * @since 2024/10/08 15:35
     */
    void update(WorkflowDefinition workflowDefinition, String desc);

    /**
     * 获取流程定义流程图
     *
     * @param tenantId           租户 ID
     * @param workflowDefinition 流程定义
     *
     * @return WorkflowDefinitionFlowSchema
     *
     * @author wangweijun
     * @since 2024/9/29 18:29
     */
    WorkflowDefinitionFlowSchema getWorkflowDefinitionFlowSchema(String tenantId, WorkflowDefinition workflowDefinition);

    /**
     * 根据流程定义 key 获取流程定义历史记录列表（分页）
     *
     * @param tenantId              租户 ID
     * @param workflowDefinitionKey 流程定义 key
     * @param page                  当前页
     * @param pageSize              每页显示数量
     *
     * @return List<WorkflowDefinitionHistory>
     *
     * @author wangweijun
     * @since 2024/10/8 13:15
     */
    Page<WorkflowDefinitionHistory> findHistoriesByWorkflowDefinitionKey(String tenantId, String workflowDefinitionKey, Integer page, Integer pageSize);

    /**
     * 根据流程定义 ID 获取流程定义历史记录列表（分页）
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     * @param page                 当前页
     * @param pageSize             每页显示数量
     *
     * @return List<WorkflowDefinitionHistory>
     *
     * @author wangweijun
     * @since 2024/10/8 13:15
     */
    Page<WorkflowDefinitionHistory> findHistoriesByWorkflowDefinitionId(String tenantId, Integer workflowDefinitionId, Integer page, Integer pageSize);

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
    Page<WorkflowDefinitionHistory> findHistories(String tenantId, Integer page, Integer pageSize);

    /**
     * 获取流程定义历史记录
     *
     * @param tenantId                    租户 ID
     * @param workflowDefinitionHistoryId 流程定义历史记录 ID
     *
     * @return WorkflowDefinitionHistory
     *
     * @author wangweijun
     * @since 2024/10/8 16:03
     */
    WorkflowDefinitionHistory getHistory(String tenantId, Integer workflowDefinitionHistoryId);
}
