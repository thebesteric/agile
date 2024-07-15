package io.github.thebesteric.framework.agile.plugins.workflow.service;

import io.github.thebesteric.framework.agile.plugins.database.core.domain.Page;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowDefinition;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowInstance;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowRepository;

import java.util.List;

/**
 * 仓库 Service
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-14 10:32:13
 */
public interface RepositoryService {

    /**
     * 添加附件
     *
     * @param attachment 附件
     *
     * @author wangweijun
     * @since 2024/7/15 13:48
     */
    void addAttachment(WorkflowRepository attachment);

    /**
     * 添加附件
     *
     * @param attachments 附件
     *
     * @author wangweijun
     * @since 2024/7/15 14:10
     */
    void addAttachments(List<WorkflowRepository> attachments);

    /**
     * 清空附件
     *
     * @param workflowInstance 流程实例
     *
     * @return 影响的行数
     *
     * @author wangweijun
     * @since 2024/7/15 14:11
     */
    Integer clearAttachment(WorkflowInstance workflowInstance);

    /**
     * 清空附件
     *
     * @param workflowDefinition 流程定义
     *
     * @return 影响的行数
     *
     * @author wangweijun
     * @since 2024/7/15 14:11
     */
    Integer clearAttachment(WorkflowDefinition workflowDefinition);

    /**
     * 清空附件
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     *
     * @return 影响的行数
     *
     * @author wangweijun
     * @since 2024/7/15 14:11
     */
    Integer clearAttachmentByWorkflowInstanceId(String tenantId, Integer workflowInstanceId);

    /**
     * 清空附件
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     *
     * @return 影响的行数
     *
     * @author wangweijun
     * @since 2024/7/15 14:11
     */
    Integer clearAttachmentByWorkflowDefinitionId(String tenantId, Integer workflowDefinitionId);

    /**
     * 删除附件
     *
     * @param workflowInstance 流程实例
     * @param attachmentId     附件 ID
     *
     * @return 影响的行数
     *
     * @author wangweijun
     * @since 2024/7/15 14:11
     */
    Integer deleteAttachment(WorkflowInstance workflowInstance, Integer attachmentId);

    /**
     * 删除附件
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     * @param attachmentId       附件 ID
     *
     * @return 影响的行数
     *
     * @author wangweijun
     * @since 2024/7/15 14:11
     */
    Integer deleteAttachment(String tenantId, Integer workflowInstanceId, Integer attachmentId);

    /**
     * 查询附件
     *
     * @param tenantId           流程实例
     * @param workflowInstanceId 流程实例 ID
     * @param page               页码
     * @param pageSize           页大小
     *
     * @return 附件列表
     *
     * @author wangweijun
     * @since 2024/7/15 14:11
     */
    Page<WorkflowRepository> findAttachmentsByWorkflowInstanceId(String tenantId, Integer workflowInstanceId, Integer page, Integer pageSize);

    /**
     * 查询附件
     *
     * @param tenantId             流程实例
     * @param workflowDefinitionId 流程定义 ID
     * @param page                 页码
     * @param pageSize             页大小
     *
     * @return 附件列表
     *
     * @author wangweijun
     * @since 2024/7/15 14:11
     */
    Page<WorkflowRepository> findAttachmentsByWorkflowDefinitionId(String tenantId, Integer workflowDefinitionId, Integer page, Integer pageSize);

    /**
     * 查询附件
     *
     * @param tenantId       流程实例
     * @param taskInstanceId 任务实例 ID
     * @param page           页码
     * @param pageSize       页大小
     *
     * @return 附件列表
     *
     * @author wangweijun
     * @since 2024/7/15 14:11
     */
    Page<WorkflowRepository> findAttachmentsByTaskInstanceId(String tenantId, Integer taskInstanceId, Integer page, Integer pageSize);

    /**
     * 查询附件
     *
     * @param workflowInstance 流程实例
     * @param page             页码
     * @param pageSize         页大小
     *
     * @return 附件列表
     *
     * @author wangweijun
     * @since 2024/7/15 14:11
     */
    Page<WorkflowRepository> findAttachments(WorkflowInstance workflowInstance, Integer page, Integer pageSize);

    /**
     * 查询附件
     *
     * @param workflowDefinition 流程定义
     * @param page               页码
     * @param pageSize           页大小
     *
     * @return 附件列表
     *
     * @author wangweijun
     * @since 2024/7/15 14:11
     */
    Page<WorkflowRepository> findAttachments(WorkflowDefinition workflowDefinition, Integer page, Integer pageSize);
}
