package io.github.thebesteric.framework.agile.plugins.workflow.domain.response;

import io.github.thebesteric.framework.agile.plugins.workflow.constant.TaskHistoryMessage;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.TaskHistory;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowDefinition;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowInstance;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 实例审批记录响应
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-11 15:48:38
 */
@Data
public class TaskHistoryResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = -5646814701512110017L;

    // 主键
    protected Integer id;

    // 流程定义 ID
    private Integer workflowDefinitionId;

    // 流程定义 KEY
    private String workflowDefinitionKey;

    // 流程定义名称
    private String workflowDefinitionName;

    // 流程实例 ID
    private Integer workflowInstanceId;

    // 流程实例描述
    private String workflowInstanceDesc;

    // 租户 ID
    private String tenantId;

    // 任务实例 ID
    private Integer taskInstanceId;

    // 日志标题
    private String title;

    // 日志信息
    private TaskHistoryMessage message;

    // 创建人
    protected String createdBy;

    // 创建时间
    protected Date createdAt;

    public static TaskHistoryResponse of(WorkflowDefinition workflowDefinition, WorkflowInstance workflowInstance, TaskHistory taskHistory) {
        TaskHistoryResponse response = new TaskHistoryResponse();

        // 封装 WorkflowDefinition
        response.workflowDefinitionId = workflowDefinition.getId();
        response.workflowDefinitionKey = workflowDefinition.getKey();
        response.workflowDefinitionName = workflowDefinition.getName();

        // 封装 WorkflowInstance
        response.workflowInstanceId = workflowInstance.getId();
        response.workflowInstanceDesc = workflowInstance.getDesc();

        // 封装 TaskHistory
        response.id = taskHistory.getId();
        response.tenantId = taskHistory.getTenantId();
        response.taskInstanceId = taskHistory.getTaskInstanceId();
        response.title = taskHistory.getTitle();
        response.message = taskHistory.getMessage();
        response.createdBy = taskHistory.getCreatedBy();
        response.createdAt = taskHistory.getCreatedAt();

        return response;
    }
}
