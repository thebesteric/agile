package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.repository;

import io.github.thebesteric.framework.agile.commons.exception.InvalidParamsException;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowRepository;

/**
 * WorkflowRepositoryBuilder
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-15 11:52:58
 */
public class WorkflowRepositoryBuilder extends AbstractBuilder<WorkflowRepository> {

    private final WorkflowRepository workflowRepository;

    private WorkflowRepositoryBuilder(WorkflowRepository workflowRepository) {
        this.workflowRepository = workflowRepository;
    }

    public static WorkflowRepositoryBuilder builder(String tenantId, Integer workflowInstanceId) {
        WorkflowRepositoryBuilder builder = new WorkflowRepositoryBuilder(new WorkflowRepository());
        builder.workflowRepository.setTenantId(tenantId);
        builder.workflowRepository.setWorkflowInstanceId(workflowInstanceId);
        return builder;
    }

    public WorkflowRepositoryBuilder attachmentId(String attachmentId) {
        this.workflowRepository.setAttachmentId(attachmentId);
        return this;
    }

    public WorkflowRepositoryBuilder attachmentName(String attachmentName) {
        this.workflowRepository.setAttachmentName(attachmentName);
        return this;
    }

    public WorkflowRepositoryBuilder attachmentSuffix(String attachmentSuffix) {
        this.workflowRepository.setAttachmentSuffix(attachmentSuffix);
        return this;
    }

    public WorkflowRepositoryBuilder attachmentUri(String attachmentUri) {
        this.workflowRepository.setAttachmentUri(attachmentUri);
        return this;
    }

    public WorkflowRepositoryBuilder attachmentContent(byte[] attachmentContent) {
        this.workflowRepository.setAttachmentContent(attachmentContent);
        return this;
    }

    public WorkflowRepository build() {
        String attachmentId = this.workflowRepository.getAttachmentId();
        if (attachmentId == null) {
            throw new InvalidParamsException("attachmentId cannot be empty");
        }
        return super.build(this.workflowRepository);
    }
}
