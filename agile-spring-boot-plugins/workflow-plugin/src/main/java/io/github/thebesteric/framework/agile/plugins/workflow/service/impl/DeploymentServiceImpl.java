package io.github.thebesteric.framework.agile.plugins.workflow.service.impl;

import cn.hutool.core.collection.CollUtil;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.Page;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.Pager;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.builder.Query;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.builder.QueryBuilderWrapper;
import io.github.thebesteric.framework.agile.plugins.database.core.jdbc.JdbcTemplateHelper;
import io.github.thebesteric.framework.agile.plugins.workflow.config.AgileWorkflowContext;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.WorkflowStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.definition.WorkflowDefinitionExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.definition.WorkflowDefinitionExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.instance.WorkflowInstanceExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.instance.WorkflowInstanceExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowDefinition;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowInstance;
import io.github.thebesteric.framework.agile.plugins.workflow.exception.WorkflowInstanceInProgressException;
import io.github.thebesteric.framework.agile.plugins.workflow.service.AbstractDeploymentService;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * 部署 Service implementation
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-14 16:02:28
 */
public class DeploymentServiceImpl extends AbstractDeploymentService {

    private final WorkflowDefinitionExecutorBuilder workflowDefinitionExecutorBuilder;
    private final WorkflowInstanceExecutorBuilder workflowInstanceExecutorBuilder;

    public DeploymentServiceImpl(AgileWorkflowContext context) {
        super(context);
        JdbcTemplate jdbcTemplate = context.getJdbcTemplateHelper().getJdbcTemplate();
        this.workflowDefinitionExecutorBuilder = WorkflowDefinitionExecutorBuilder.builder(jdbcTemplate);
        this.workflowInstanceExecutorBuilder = WorkflowInstanceExecutorBuilder.builder(jdbcTemplate);
    }

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
    @Override
    public WorkflowDefinition create(WorkflowDefinition workflowDefinition) {
        JdbcTemplateHelper jdbcTemplateHelper = this.context.getJdbcTemplateHelper();
        return jdbcTemplateHelper.executeInTransaction(() -> {
            WorkflowDefinitionExecutor executor = this.workflowDefinitionExecutorBuilder.workflowDefinition(workflowDefinition).build();
            return executor.save();
        });
    }

    /**
     * 删除流程定义
     *
     * @param tenantId 租户
     * @param key      key
     *
     * @author wangweijun
     * @since 2024/6/17 15:27
     */
    @Override
    public void delete(String tenantId, String key) {
        WorkflowDefinitionExecutor executor = this.workflowDefinitionExecutorBuilder.tenantId(tenantId).key(key).build();
        executor.deleteByTenantAndKey();
    }

    /**
     * 获取流程定义
     *
     * @param tenantId 租户
     * @param key      key
     */
    @Override
    public WorkflowDefinition getByKey(String tenantId, String key) {
        WorkflowDefinitionExecutor executor = this.workflowDefinitionExecutorBuilder.tenantId(tenantId).key(key).build();
        return executor.getByTenantAndKey();
    }

    /**
     * 获取流程定义
     *
     * @param tenantId 租户
     * @param id       id
     */
    @Override
    public WorkflowDefinition getById(String tenantId, Integer id) {
        WorkflowDefinitionExecutor executor = this.workflowDefinitionExecutorBuilder.build();
        return executor.getById(id);
    }

    /**
     * 获取流程定义列表
     *
     * @param tenantId 租户
     */
    @Override
    public List<WorkflowDefinition> find(String tenantId) {
        WorkflowDefinitionExecutor executor = this.workflowDefinitionExecutorBuilder.tenantId(tenantId).build();
        return executor.findByTenantId();
    }

    /**
     * 获取流程定义列表（分页）
     *
     * @param tenantId 租户
     * @param pager    分页参数
     */
    @Override
    public Page<WorkflowDefinition> find(String tenantId, Pager pager) {
        WorkflowDefinitionExecutor executor = this.workflowDefinitionExecutorBuilder.build();
        Query query = QueryBuilderWrapper.createLambda(WorkflowDefinition.class)
                .eq(WorkflowDefinition::getTenantId, tenantId)
                .page(pager.getPage(), pager.getPageSize()).build();
        return executor.find(query);
    }

    /**
     * 禁用流程定义
     *
     * @param tenantId 租户
     * @param key      key
     */
    @Override
    public void disable(String tenantId, String key) {
        WorkflowDefinitionExecutor query = this.workflowDefinitionExecutorBuilder.tenantId(tenantId).key(key).build();
        query.disable();
    }

    /**
     * 启用流程定义
     *
     * @param tenantId 租户
     * @param key      key
     */
    @Override
    public void enable(String tenantId, String key) {
        WorkflowDefinitionExecutor query = this.workflowDefinitionExecutorBuilder.tenantId(tenantId).key(key).build();
        query.enable();
    }

    /**
     * 更新流程定义
     *
     * @param workflowDefinition 流程定义
     */
    @Override
    public void update(WorkflowDefinition workflowDefinition) {
        WorkflowInstanceExecutor workflowInstanceExecutor = this.workflowInstanceExecutorBuilder.build();
        Query query = QueryBuilderWrapper.createLambda(WorkflowInstance.class)
                .eq(WorkflowInstance::getTenantId, workflowDefinition.getTenantId())
                .eq(WorkflowInstance::getWorkflowDefinitionId, workflowDefinition.getId())
                .eq(WorkflowInstance::getStatus, WorkflowStatus.IN_PROGRESS.getCode())
                .eq(WorkflowInstance::getState, 1).build();
        Page<WorkflowInstance> page = workflowInstanceExecutor.find(query);
        if (CollUtil.isNotEmpty(page.getRecords())) {
            throw new WorkflowInstanceInProgressException();
        }
        this.workflowDefinitionExecutorBuilder.build().updateById(workflowDefinition);
    }

}
