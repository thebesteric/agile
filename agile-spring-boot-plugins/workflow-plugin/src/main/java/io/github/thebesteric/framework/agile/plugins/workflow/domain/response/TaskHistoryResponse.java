package io.github.thebesteric.framework.agile.plugins.workflow.domain.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.TaskHistoryMessage;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.TaskHistory;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowDefinition;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowInstance;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "ID")
    protected Integer id;
    @Schema(description = "流程定义 ID")
    private Integer workflowDefinitionId;
    @Schema(description = "流程定义 KEY")
    private String workflowDefinitionKey;
    @Schema(description = "流程定义名称")
    private String workflowDefinitionName;
    @Schema(description = "流程实例 ID")
    private Integer workflowInstanceId;
    @Schema(description = "流程实例描述")
    private String workflowInstanceDesc;
    @Schema(description = "租户 ID")
    private String tenantId;
    @Schema(description = "任务实例 ID")
    private Integer taskInstanceId;
    @Schema(description = "日志标题")
    private String title;
    @Schema(description = "日志信息")
    private TaskHistoryMessage message;
    @Schema(description = "创建人")
    protected String createdBy;
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
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
