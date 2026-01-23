package io.github.thebesteric.framework.agile.plugins.workflow.service.impl;

import io.github.thebesteric.framework.agile.core.domain.page.PagingResponse;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.builder.Query;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.builder.QueryBuilderWrapper;
import io.github.thebesteric.framework.agile.plugins.database.core.jdbc.JdbcTemplateHelper;
import io.github.thebesteric.framework.agile.plugins.workflow.config.AgileWorkflowContext;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.repository.WorkflowRepositoryExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.repository.WorkflowRepositoryExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowDefinition;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowInstance;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowRepository;
import io.github.thebesteric.framework.agile.plugins.workflow.service.AbstractRepositoryService;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * 仓库 Service implementation
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-14 10:32:13
 */
public class RepositoryServiceImpl extends AbstractRepositoryService {

    private final WorkflowRepositoryExecutorBuilder workflowRepositoryExecutorBuilder;

    public RepositoryServiceImpl(AgileWorkflowContext context) {
        super(context);
        JdbcTemplate jdbcTemplate = context.getJdbcTemplateHelper().getJdbcTemplate();
        workflowRepositoryExecutorBuilder = WorkflowRepositoryExecutorBuilder.builder(jdbcTemplate);
    }

    /**
     * 添加附件
     *
     * @param attachment 附件
     *
     * @author wangweijun
     * @since 2024/7/15 13:49
     */
    @Override
    public void addAttachment(WorkflowRepository attachment) {
        JdbcTemplateHelper jdbcTemplateHelper = this.context.getJdbcTemplateHelper();
        jdbcTemplateHelper.executeInTransaction(() -> {
            WorkflowRepositoryExecutor executor = workflowRepositoryExecutorBuilder.build();
            executor.save(attachment);
        });
    }

    /**
     * 添加附件
     *
     * @param attachments 附件
     *
     * @author wangweijun
     * @since 2024/7/15 13:49
     */
    @Override
    public void addAttachments(List<WorkflowRepository> attachments) {
        attachments.forEach(this::addAttachment);
    }

    /**
     * 删除附件
     *
     * @param workflowInstance 流程实例
     *
     * @author wangweijun
     * @since 2024/7/15 13:49
     */
    @Override
    public Integer clearAttachment(WorkflowInstance workflowInstance) {
        return this.clearAttachmentByWorkflowInstanceId(workflowInstance.getTenantId(), workflowInstance.getId());
    }

    /**
     * 删除附件
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     *
     * @author wangweijun
     * @since 2024/7/15 16:57
     */
    @Override
    public Integer clearAttachmentByWorkflowInstanceId(String tenantId, Integer workflowInstanceId) {
        JdbcTemplateHelper jdbcTemplateHelper = this.context.getJdbcTemplateHelper();
        return jdbcTemplateHelper.executeInTransaction(() -> {
            Query query = QueryBuilderWrapper.createLambda(WorkflowRepository.class)
                    .eq(WorkflowRepository::getTenantId, tenantId)
                    .eq(WorkflowRepository::getWorkflowInstanceId, workflowInstanceId)
                    .build();
            return workflowRepositoryExecutorBuilder.build().delete(query);
        });
    }

    /**
     * 删除附件
     *
     * @param workflowDefinition 流程定义
     *
     * @author wangweijun
     * @since 2024/7/15 13:49
     */
    @Override
    public Integer clearAttachment(WorkflowDefinition workflowDefinition) {
        return this.clearAttachmentByWorkflowDefinitionId(workflowDefinition.getTenantId(), workflowDefinition.getId());
    }

    /**
     * 删除附件
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     *
     * @author wangweijun
     * @since 2024/7/15 16:57
     */
    @Override
    public Integer clearAttachmentByWorkflowDefinitionId(String tenantId, Integer workflowDefinitionId) {
        return workflowRepositoryExecutorBuilder.build().clearAttachmentByWorkflowDefinitionId(tenantId, workflowDefinitionId);
    }

    /**
     * 删除附件
     *
     * @param workflowInstance 流程实例
     * @param attachmentId     附件 ID
     *
     * @author wangweijun
     * @since 2024/7/15 16:57
     */
    @Override
    public Integer deleteAttachment(WorkflowInstance workflowInstance, Integer attachmentId) {
        return this.deleteAttachment(workflowInstance.getTenantId(), workflowInstance.getId(), attachmentId);
    }

    /**
     * 删除附件
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     * @param attachmentId       附件 ID
     *
     * @author wangweijun
     * @since 2024/7/15 15:57
     */
    @Override
    public Integer deleteAttachment(String tenantId, Integer workflowInstanceId, Integer attachmentId) {
        JdbcTemplateHelper jdbcTemplateHelper = this.context.getJdbcTemplateHelper();
        return jdbcTemplateHelper.executeInTransaction(() -> {
            Query query = QueryBuilderWrapper.createLambda(WorkflowRepository.class)
                    .eq(WorkflowRepository::getTenantId, tenantId)
                    .eq(WorkflowRepository::getWorkflowInstanceId, workflowInstanceId)
                    .eq(WorkflowRepository::getId, attachmentId)
                    .build();
            return workflowRepositoryExecutorBuilder.build().delete(query);
        });
    }

    /**
     * 查询附件
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     * @param page               页码
     * @param pageSize           页大小
     *
     * @author wangweijun
     * @since 2024/7/15 14:19
     */
    @Override
    public PagingResponse<WorkflowRepository> findAttachmentsByWorkflowInstanceId(String tenantId, Integer workflowInstanceId, Integer page, Integer pageSize) {
        WorkflowRepositoryExecutor executor = workflowRepositoryExecutorBuilder.build();
        return executor.findByWorkflowInstanceId(tenantId, workflowInstanceId, page, pageSize);
    }

    /**
     * 查询附件
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     * @param page                 页码
     * @param pageSize             页大小
     *
     * @author wangweijun
     * @since 2024/7/15 14:19
     */
    @Override
    public PagingResponse<WorkflowRepository> findAttachmentsByWorkflowDefinitionId(String tenantId, Integer workflowDefinitionId, Integer page, Integer pageSize) {
        WorkflowRepositoryExecutor executor = workflowRepositoryExecutorBuilder.build();
        return executor.findByWorkflowDefinitionId(tenantId, workflowDefinitionId, page, pageSize);
    }

    /**
     * 查询附件
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     * @param page           页码
     * @param pageSize       页大小
     *
     * @author wangweijun
     * @since 2024/7/15 14:19
     */
    @Override
    public PagingResponse<WorkflowRepository> findAttachmentsByTaskInstanceId(String tenantId, Integer taskInstanceId, Integer page, Integer pageSize) {
        WorkflowRepositoryExecutor executor = workflowRepositoryExecutorBuilder.build();
        return executor.findAttachmentsByTaskInstanceId(tenantId, taskInstanceId, page, pageSize);
    }

    /**
     * 查询附件
     *
     * @param workflowInstance 流程实例
     * @param page             页码
     * @param pageSize         页大小
     *
     * @author wangweijun
     * @since 2024/7/15 14:19
     */
    @Override
    public PagingResponse<WorkflowRepository> findAttachments(WorkflowInstance workflowInstance, Integer page, Integer pageSize) {
        return this.findAttachmentsByWorkflowInstanceId(workflowInstance.getTenantId(), workflowInstance.getId(), page, pageSize);
    }

    /**
     * 查询附件
     *
     * @param workflowDefinition 流程定义
     * @param page               页码
     * @param pageSize           页大小
     *
     * @author wangweijun
     * @since 2024/7/15 14:19
     */
    @Override
    public PagingResponse<WorkflowRepository> findAttachments(WorkflowDefinition workflowDefinition, Integer page, Integer pageSize) {
        return this.findAttachmentsByWorkflowDefinitionId(workflowDefinition.getTenantId(), workflowDefinition.getId(), page, pageSize);
    }
}
