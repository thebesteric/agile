package io.github.thebesteric.framework.agile.plugins.workflow.service.impl;

import cn.hutool.core.collection.CollUtil;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.Page;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.Pager;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.builder.Query;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.builder.QueryBuilderWrapper;
import io.github.thebesteric.framework.agile.plugins.database.core.jdbc.JdbcTemplateHelper;
import io.github.thebesteric.framework.agile.plugins.workflow.config.AgileWorkflowContext;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.DMLOperator;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.WorkflowStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.assignment.NodeAssignmentExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.assignment.NodeAssignmentExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.assignment.NodeRoleAssignmentExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.assignment.NodeRoleAssignmentExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.definition.NodeDefinitionExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.definition.NodeDefinitionExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.relation.NodeRelationExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.relation.NodeRelationExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.definition.WorkflowDefinitionExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.definition.WorkflowDefinitionExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.definition.history.WorkflowDefinitionHistoryBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.definition.history.WorkflowDefinitionHistoryExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.definition.history.WorkflowDefinitionHistoryExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.instance.WorkflowInstanceExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.instance.WorkflowInstanceExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.response.WorkflowDefinitionFlowSchema;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.*;
import io.github.thebesteric.framework.agile.plugins.workflow.exception.WorkflowInstanceInProgressException;
import io.github.thebesteric.framework.agile.plugins.workflow.service.AbstractDeploymentService;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Collections;
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
    private final WorkflowDefinitionHistoryExecutorBuilder workflowDefinitionHistoryExecutorBuilder;
    private final NodeDefinitionExecutorBuilder nodeDefinitionExecutorBuilder;
    private final NodeRelationExecutorBuilder nodeRelationExecutorBuilder;
    private final NodeAssignmentExecutorBuilder nodeAssignmentExecutorBuilder;
    private final NodeRoleAssignmentExecutorBuilder nodeRoleAssignmentExecutorBuilder;

    public DeploymentServiceImpl(AgileWorkflowContext context) {
        super(context);
        JdbcTemplate jdbcTemplate = context.getJdbcTemplateHelper().getJdbcTemplate();
        this.workflowDefinitionExecutorBuilder = WorkflowDefinitionExecutorBuilder.builder(jdbcTemplate);
        this.workflowInstanceExecutorBuilder = WorkflowInstanceExecutorBuilder.builder(jdbcTemplate);
        this.nodeDefinitionExecutorBuilder = NodeDefinitionExecutorBuilder.builder(jdbcTemplate);
        this.nodeRelationExecutorBuilder = NodeRelationExecutorBuilder.builder(jdbcTemplate);
        this.nodeAssignmentExecutorBuilder = NodeAssignmentExecutorBuilder.builder(jdbcTemplate);
        this.nodeRoleAssignmentExecutorBuilder = NodeRoleAssignmentExecutorBuilder.builder(jdbcTemplate);
        this.workflowDefinitionHistoryExecutorBuilder = WorkflowDefinitionHistoryExecutorBuilder.builder(jdbcTemplate);
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
            executor.save();
            // 记录日志
            this.recordWorkflowDefinitionHistory(workflowDefinition.getTenantId(), workflowDefinition.getId(), DMLOperator.INSERT, null, workflowDefinition);
            return workflowDefinition;
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
        WorkflowDefinitionExecutor executor = this.workflowDefinitionExecutorBuilder.build();
        Query query = QueryBuilderWrapper.createLambda(WorkflowDefinition.class)
                .eq(WorkflowDefinition::getTenantId, tenantId)
                .eq(WorkflowDefinition::getKey, key).build();
        WorkflowDefinition workflowDefinition = executor.get(query);
        // 检查是否有正在进行的流程实例
        this.throwExceptionWhenWorkflowDefinitionHasInProcessInstances(workflowDefinition);
        // 删除流程定义
        executor.delete(workflowDefinition);
        // 记录日志
        this.recordWorkflowDefinitionHistory(tenantId, workflowDefinition.getId(), DMLOperator.DELETE, workflowDefinition, null);
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
        WorkflowDefinitionExecutor executor = this.workflowDefinitionExecutorBuilder.build();
        Query query = QueryBuilderWrapper.createLambda(WorkflowDefinition.class)
                .eq(WorkflowDefinition::getTenantId, tenantId)
                .eq(WorkflowDefinition::getKey, key).build();
        WorkflowDefinition workflowDefinition = executor.get(query);
        if (workflowDefinition.getState() == 0) {
            return;
        }
        workflowDefinition.setState(0);
        this.update(workflowDefinition);

    }

    /**
     * 启用流程定义
     *
     * @param tenantId 租户
     * @param key      key
     */
    @Override
    public void enable(String tenantId, String key) {
        WorkflowDefinitionExecutor executor = this.workflowDefinitionExecutorBuilder.build();
        Query query = QueryBuilderWrapper.createLambda(WorkflowDefinition.class)
                .eq(WorkflowDefinition::getTenantId, tenantId)
                .eq(WorkflowDefinition::getKey, key).build();
        WorkflowDefinition workflowDefinition = executor.get(query);
        if (workflowDefinition.getState() == 1) {
            return;
        }
        workflowDefinition.setState(1);
        this.update(workflowDefinition);
    }

    /**
     * 更新流程定义
     *
     * @param workflowDefinition 流程定义
     */
    @Override
    public void update(WorkflowDefinition workflowDefinition) {
        // 检查是否有正在进行的流程实例
        this.throwExceptionWhenWorkflowDefinitionHasInProcessInstances(workflowDefinition);

        // 更新流程定义
        WorkflowDefinitionExecutor executor = this.workflowDefinitionExecutorBuilder.build();
        WorkflowDefinition beforeObj = executor.getById(workflowDefinition.getId());
        executor.updateById(workflowDefinition);

        // 记录日志
        this.recordWorkflowDefinitionHistory(workflowDefinition.getTenantId(), workflowDefinition.getId(), DMLOperator.UPDATE, beforeObj, workflowDefinition);
    }

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
    @Override
    public WorkflowDefinitionFlowSchema getWorkflowDefinitionFlowSchema(String tenantId, WorkflowDefinition workflowDefinition) {
        // 获取节点定义
        NodeDefinitionExecutor nodeDefinitionExecutor = this.nodeDefinitionExecutorBuilder.build();
        List<NodeDefinition> nodeDefinitions = nodeDefinitionExecutor.findByWorkflowDefinitionId(tenantId, workflowDefinition.getId());
        // 获取节点关系
        NodeRelationExecutor nodeRelationExecutor = this.nodeRelationExecutorBuilder.build();
        List<NodeRelation> nodeRelations = nodeRelationExecutor.findByWorkflowDefinitionId(tenantId, workflowDefinition.getId());
        // 获取审批人
        NodeAssignmentExecutor nodeAssignmentExecutor = this.nodeAssignmentExecutorBuilder.build();
        List<NodeAssignment> nodeAssignments = nodeAssignmentExecutor.findByWorkflowDefinitionId(tenantId, workflowDefinition.getId());
        // 获取角色审批人
        NodeRoleAssignmentExecutor nodeRoleAssignmentExecutor = this.nodeRoleAssignmentExecutorBuilder.build();
        List<NodeRoleAssignment> nodeRoleAssignments = nodeRoleAssignmentExecutor.findByWorkflowDefinitionId(tenantId, workflowDefinition.getId());

        // 封装
        return WorkflowDefinitionFlowSchema.of(workflowDefinition, nodeDefinitions, nodeRelations, nodeAssignments, nodeRoleAssignments);
    }

    /**
     * 记录日志
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     * @param dmlOperator          操作
     * @param beforeObj            之前数据
     * @param currentObj           当前数据
     *
     * @author wangweijun
     * @since 2024/10/8 10:15
     */
    private void recordWorkflowDefinitionHistory(String tenantId, Integer workflowDefinitionId, DMLOperator dmlOperator, WorkflowDefinition beforeObj, WorkflowDefinition currentObj) {
        WorkflowDefinitionHistory history = WorkflowDefinitionHistoryBuilder.builder()
                .tenantId(tenantId)
                .workflowDefinitionId(workflowDefinitionId)
                .dmlOperator(dmlOperator)
                .beforeObj(beforeObj)
                .currentObj(currentObj)
                .build();
        WorkflowDefinitionHistoryExecutor historyExecutor = workflowDefinitionHistoryExecutorBuilder.build();
        historyExecutor.save(history);
    }

    /**
     * 如果有正在进行的流程实例则抛出异常
     *
     * @param workflowDefinition 流程定义
     *
     * @author wangweijun
     * @since 2024/10/8 11:38
     */
    private void throwExceptionWhenWorkflowDefinitionHasInProcessInstances(WorkflowDefinition workflowDefinition) {
        // 检查是否有正在进行的流程实例
        List<WorkflowInstance> workflowInstances = this.findWorkflowDefinitionHasInProcessInstances(workflowDefinition);
        if (CollUtil.isNotEmpty(workflowInstances)) {
            throw new WorkflowInstanceInProgressException();
        }
    }

    /**
     * 获取正在进行的流程实例
     *
     * @param workflowDefinition 流程定义
     *
     * @return List<WorkflowInstance>
     *
     * @author wangweijun
     * @since 2024/10/8 11:51
     */
    private List<WorkflowInstance> findWorkflowDefinitionHasInProcessInstances(WorkflowDefinition workflowDefinition) {
        // 检查是否有正在进行的流程实例
        WorkflowInstanceExecutor workflowInstanceExecutor = this.workflowInstanceExecutorBuilder.build();
        Query query = QueryBuilderWrapper.createLambda(WorkflowInstance.class)
                .eq(WorkflowInstance::getTenantId, workflowDefinition.getTenantId())
                .eq(WorkflowInstance::getWorkflowDefinitionId, workflowDefinition.getId())
                .eq(WorkflowInstance::getStatus, WorkflowStatus.IN_PROGRESS.getCode())
                .eq(WorkflowInstance::getState, 1).build();
        Page<WorkflowInstance> page = workflowInstanceExecutor.find(query);
        return page.getRecords();
    }

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
    @Override
    public Page<WorkflowDefinitionHistory> findHistoriesByWorkflowDefinitionKey(String tenantId, String workflowDefinitionKey, Integer page, Integer pageSize) {
        WorkflowDefinitionExecutor executor = this.workflowDefinitionExecutorBuilder.build();
        Query query = QueryBuilderWrapper.createLambda(WorkflowDefinition.class)
                .eq(WorkflowDefinition::getTenantId, tenantId)
                .eq(WorkflowDefinition::getKey, workflowDefinitionKey)
                .build();
        WorkflowDefinition workflowDefinition = executor.get(query);
        if (workflowDefinition == null) {
            return Page.of(Collections.emptyList());
        }
        return this.findHistoriesByWorkflowDefinitionId(tenantId, workflowDefinition.getId(), page, pageSize);
    }

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
    @Override
    public Page<WorkflowDefinitionHistory> findHistoriesByWorkflowDefinitionId(String tenantId, Integer workflowDefinitionId, Integer page, Integer pageSize) {
        WorkflowDefinitionHistoryExecutor executor = workflowDefinitionHistoryExecutorBuilder.build();
        Query query = QueryBuilderWrapper.createLambda(WorkflowDefinitionHistory.class)
                .eq(WorkflowDefinitionHistory::getTenantId, tenantId)
                .eq(WorkflowDefinitionHistory::getWorkflowDefinitionId, workflowDefinitionId)
                .page(page, pageSize)
                .build();
        return executor.find(query);
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
    @Override
    public Page<WorkflowDefinitionHistory> findHistories(String tenantId, Integer page, Integer pageSize) {
        WorkflowDefinitionHistoryExecutor executor = workflowDefinitionHistoryExecutorBuilder.build();
        Query query = QueryBuilderWrapper.createLambda(WorkflowDefinitionHistory.class)
                .eq(WorkflowDefinitionHistory::getTenantId, tenantId)
                .page(page, pageSize)
                .build();
        return executor.find(query);
    }
}
