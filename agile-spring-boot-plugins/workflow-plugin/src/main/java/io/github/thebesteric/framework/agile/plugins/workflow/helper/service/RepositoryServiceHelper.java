package io.github.thebesteric.framework.agile.plugins.workflow.helper.service;

import io.github.thebesteric.framework.agile.plugins.database.core.domain.Page;
import io.github.thebesteric.framework.agile.plugins.workflow.WorkflowEngine;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.repository.WorkflowRepositoryBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.TaskInstance;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowDefinition;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowInstance;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowRepository;
import io.github.thebesteric.framework.agile.plugins.workflow.helper.AbstractServiceHelper;
import io.github.thebesteric.framework.agile.plugins.workflow.service.RepositoryService;

import java.util.List;

/**
 * 附件帮助类
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-15 14:20:41
 */
public class RepositoryServiceHelper extends AbstractServiceHelper {

    private final RepositoryService repositoryService;

    public RepositoryServiceHelper(WorkflowEngine workflowEngine) {
        super(workflowEngine);
        this.repositoryService = workflowEngine.getRepositoryService();
    }

    /**
     * 添加附件
     *
     * @param workflowInstance  流程实例
     * @param attachmentId      附件 ID
     * @param attachmentName    附件名称
     * @param attachmentSuffix  附件后缀
     * @param attachmentUri     附件 URI
     * @param attachmentContent 附件内容
     */
    public void addAttachment(WorkflowInstance workflowInstance, String attachmentId, String attachmentName, String attachmentSuffix, String attachmentUri, byte[] attachmentContent) {
        WorkflowRepositoryBuilder builder = WorkflowRepositoryBuilder.builder(workflowInstance.getTenantId(), workflowInstance.getId())
                .attachmentId(attachmentId).attachmentName(attachmentName).attachmentSuffix(attachmentSuffix)
                .attachmentUri(attachmentUri).attachmentContent(attachmentContent);
        this.addAttachment(builder);
    }

    /**
     * 添加附件
     *
     * @param workflowInstance 流程实例
     * @param attachmentId     附件 ID
     * @param attachmentName   附件名称
     * @param attachmentSuffix 附件后缀
     * @param attachmentUri    附件 URI
     */
    public void addAttachment(WorkflowInstance workflowInstance, String attachmentId, String attachmentName, String attachmentSuffix, String attachmentUri) {
        this.addAttachment(workflowInstance, attachmentId, attachmentName, attachmentSuffix, attachmentUri, null);
    }

    /**
     * 添加附件
     *
     * @param workflowInstance  流程实例
     * @param attachmentId      附件 ID
     * @param attachmentName    附件名称
     * @param attachmentSuffix  附件后缀
     * @param attachmentContent 附件内容
     */
    public void addAttachment(WorkflowInstance workflowInstance, String attachmentId, String attachmentName, String attachmentSuffix, byte[] attachmentContent) {
        this.addAttachment(workflowInstance, attachmentId, attachmentName, attachmentSuffix, null, attachmentContent);
    }


    /**
     * 添加附件
     *
     * @param builder 附件构建器
     */
    public void addAttachment(WorkflowRepositoryBuilder builder) {
        this.addAttachment(builder.build());
    }

    /**
     * 添加附件
     *
     * @param attachment 附件
     */
    public void addAttachment(WorkflowRepository attachment) {
        this.repositoryService.addAttachment(attachment);
    }

    /**
     * 添加附件
     *
     * @param attachments 附件列表
     */
    public void addAttachments(List<WorkflowRepository> attachments) {
        attachments.forEach(this::addAttachment);
    }

    /**
     * 查询附件
     *
     * @param workflowInstance 流程实例
     * @param page             页码
     * @param pageSize         每页显示数量
     *
     * @return 附件列表
     */
    public Page<WorkflowRepository> findAttachments(WorkflowInstance workflowInstance, Integer page, Integer pageSize) {
        return this.repositoryService.findAttachments(workflowInstance, page, pageSize);
    }

    /**
     * 查询所有附件
     *
     * @param workflowInstance 流程实例
     *
     * @return 附件列表
     */
    public List<WorkflowRepository> findAttachments(WorkflowInstance workflowInstance) {
        return this.findAttachments(workflowInstance, 1, Integer.MAX_VALUE).getRecords();
    }

    /**
     * 查询附件
     *
     * @param workflowDefinition 流程定义
     * @param page               页码
     * @param pageSize           每页显示数量
     *
     * @return 附件列表
     */
    public Page<WorkflowRepository> findAttachments(WorkflowDefinition workflowDefinition, Integer page, Integer pageSize) {
        return this.repositoryService.findAttachments(workflowDefinition, page, pageSize);
    }

    /**
     * 查询所有附件
     *
     * @param workflowDefinition 流程定义
     *
     * @return 附件列表
     */
    public List<WorkflowRepository> findAttachments(WorkflowDefinition workflowDefinition) {
        return this.findAttachments(workflowDefinition, 1, Integer.MAX_VALUE).getRecords();
    }

    /**
     * 查询附件
     *
     * @param taskInstance 任务实例
     * @param page         页码
     * @param pageSize     每页显示数量
     *
     * @return 附件列表
     */
    public Page<WorkflowRepository> findAttachments(TaskInstance taskInstance, Integer page, Integer pageSize) {
        return this.findAttachmentsByWorkflowInstanceId(taskInstance.getTenantId(), taskInstance.getWorkflowInstanceId(), page, pageSize);
    }

    /**
     * 查询附件
     *
     * @param taskInstance 任务实例
     *
     * @return 附件列表
     */
    public List<WorkflowRepository> findAttachments(TaskInstance taskInstance) {
        return this.findAttachmentsByWorkflowInstanceId(taskInstance.getTenantId(), taskInstance.getWorkflowInstanceId(), 1, Integer.MAX_VALUE).getRecords();
    }

    /**
     * 查询附件
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     * @param page               页码
     * @param pageSize           每页显示数量
     *
     * @return 附件列表
     */
    public Page<WorkflowRepository> findAttachmentsByWorkflowInstanceId(String tenantId, Integer workflowInstanceId, Integer page, Integer pageSize) {
        return this.repositoryService.findAttachmentsByWorkflowInstanceId(tenantId, workflowInstanceId, page, pageSize);
    }

    /**
     * 查询附件
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     *
     * @return 附件列表
     */
    public List<WorkflowRepository> findAttachmentsByWorkflowInstanceId(String tenantId, Integer workflowInstanceId) {
        return this.findAttachmentsByWorkflowInstanceId(tenantId, workflowInstanceId, 1, Integer.MAX_VALUE).getRecords();
    }

    /**
     * 查询附件
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     * @param page                 页码
     * @param pageSize             每页显示数量
     *
     * @return 附件列表
     */
    public Page<WorkflowRepository> findAttachmentsByWorkflowDefinitionId(String tenantId, Integer workflowDefinitionId, Integer page, Integer pageSize) {
        return this.repositoryService.findAttachmentsByWorkflowDefinitionId(tenantId, workflowDefinitionId, page, pageSize);
    }

    /**
     * 查询附件
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     *
     * @return 附件列表
     */
    public List<WorkflowRepository> findAttachmentsByWorkflowDefinitionId(String tenantId, Integer workflowDefinitionId) {
        return this.findAttachmentsByWorkflowDefinitionId(tenantId, workflowDefinitionId, 1, Integer.MAX_VALUE).getRecords();
    }

    /**
     * 查询附件
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     * @param page           页码
     * @param pageSize       每页显示数量
     *
     * @return 附件列表
     */
    public Page<WorkflowRepository> findAttachmentsByTaskInstanceId(String tenantId, Integer taskInstanceId, Integer page, Integer pageSize) {
        return this.repositoryService.findAttachmentsByTaskInstanceId(tenantId, taskInstanceId, page, pageSize);
    }

    /**
     * 查询附件
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     *
     * @return 附件列表
     */
    public List<WorkflowRepository> findAttachmentsByTaskInstanceId(String tenantId, Integer taskInstanceId) {
        return this.findAttachmentsByTaskInstanceId(tenantId, taskInstanceId, 1, Integer.MAX_VALUE).getRecords();
    }

    /**
     * 删除附件
     *
     * @param workflowInstance 流程实例
     * @param attachmentId     附件 ID
     *
     * @return 影响的行数
     */
    public Integer deleteAttachment(WorkflowInstance workflowInstance, Integer attachmentId) {
        return this.repositoryService.deleteAttachment(workflowInstance, attachmentId);
    }

    /**
     * 清空附件
     *
     * @param workflowInstance 流程实例
     *
     * @return 影响的行数
     */
    public Integer clearAttachmentsByWorkflowInstance(WorkflowInstance workflowInstance) {
        return this.repositoryService.clearAttachment(workflowInstance);
    }

    /**
     * 清空附件
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     *
     * @return 影响的行数
     */
    public Integer clearAttachmentsByWorkflowInstance(String tenantId, Integer workflowInstanceId) {
        return this.repositoryService.clearAttachmentByWorkflowInstanceId(tenantId, workflowInstanceId);
    }

    /**
     * 清空附件
     *
     * @param workflowDefinition 流程定义
     *
     * @return 影响的行数
     */
    public Integer clearAttachmentsByWorkflowDefinition(WorkflowDefinition workflowDefinition) {
        return this.repositoryService.clearAttachment(workflowDefinition);
    }

    /**
     * 清空附件
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     *
     * @return 影响的行数
     */
    public Integer clearAttachmentsByWorkflowDefinition(String tenantId, Integer workflowDefinitionId) {
        return this.repositoryService.clearAttachmentByWorkflowDefinitionId(tenantId, workflowDefinitionId);
    }
}
