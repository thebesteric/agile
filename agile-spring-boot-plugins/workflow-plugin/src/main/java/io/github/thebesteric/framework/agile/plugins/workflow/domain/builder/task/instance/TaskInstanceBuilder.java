package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.task.instance;

import io.github.thebesteric.framework.agile.commons.exception.InvalidParamsException;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.NodeStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.TaskInstance;

/**
 * TaskInstanceBuilder
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-24 12:01:23
 */
public class TaskInstanceBuilder extends AbstractBuilder<TaskInstance> {

    private final TaskInstance taskInstance;

    private TaskInstanceBuilder(TaskInstance taskInstance) {
        this.taskInstance = taskInstance;
    }

    public static TaskInstanceBuilder builder() {
        return new TaskInstanceBuilder(new TaskInstance());
    }

    public TaskInstanceBuilder tenantId(String tenantId) {
        this.taskInstance.setTenantId(tenantId);
        return this;
    }

    public TaskInstanceBuilder workflowDefinitionId(Integer workflowDefinitionId) {
        this.taskInstance.setWorkflowInstanceId(workflowDefinitionId);
        return this;
    }

    public TaskInstanceBuilder nodeDefinitionId(Integer nodeDefinitionId) {
        this.taskInstance.setNodeDefinitionId(nodeDefinitionId);
        return this;
    }

    public TaskInstanceBuilder status(NodeStatus status) {
        this.taskInstance.setStatus(status);
        return this;
    }

    public TaskInstanceBuilder approvedCount(Integer approvedCount) {
        this.taskInstance.setApprovedCount(approvedCount);
        return this;
    }

    public TaskInstanceBuilder totalCount(Integer totalCount) {
        this.taskInstance.setTotalCount(totalCount);
        return this;
    }

    public TaskInstance build() {
        String tenantId = this.taskInstance.getTenantId();
        Integer workflowInstanceId = this.taskInstance.getWorkflowInstanceId();
        Integer nodeDefinitionId = this.taskInstance.getNodeDefinitionId();
        NodeStatus status = this.taskInstance.getStatus();
        if (tenantId == null || workflowInstanceId == null || nodeDefinitionId == null || status == null) {
            throw new InvalidParamsException("tenantId, workflowInstanceId, key, name cannot be empty");
        }
        return super.build(this.taskInstance);
    }

}
