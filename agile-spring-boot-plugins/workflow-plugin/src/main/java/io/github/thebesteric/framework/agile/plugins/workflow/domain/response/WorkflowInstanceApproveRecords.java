package io.github.thebesteric.framework.agile.plugins.workflow.domain.response;

import io.github.thebesteric.framework.agile.core.domain.Pair;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.ApproveStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.NodeType;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 流程实例审批记录
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-09-12 12:06:48
 */
@Data
public class WorkflowInstanceApproveRecords {
    /** 流程实例 ID */
    private Integer id;
    /** 流程定义 */
    private WorkflowDefinitionResponse workflowDefinition;
    /** 流程节点 */
    private List<NodeDefinitionResponse> nodeDefinitionResponses;
    /** 创建时间 */
    private Date createdAt;

    public static WorkflowInstanceApproveRecords of(WorkflowDefinition workflowDefinition, WorkflowInstance workflowInstance,
                                                    List<Pair<NodeDefinition, TaskInstance>> nodeDefAndTasks,
                                                    List<TaskApprove> taskApproves, List<NodeAssignment> nodeAssignments,
                                                    String currentUserId) {

        WorkflowInstanceApproveRecords records = new WorkflowInstanceApproveRecords();
        records.setId(workflowInstance.getId());
        records.setCreatedAt(workflowInstance.getCreatedAt());

        // 封装流程定义
        WorkflowDefinitionResponse workflowDefinitionResponse = new WorkflowDefinitionResponse();
        workflowDefinitionResponse.setId(workflowDefinition.getId());
        workflowDefinitionResponse.setTenantId(workflowDefinition.getTenantId());
        workflowDefinitionResponse.setName(workflowDefinition.getName());
        workflowDefinitionResponse.setCreatedAt(workflowDefinition.getCreatedAt());
        records.setWorkflowDefinition(workflowDefinitionResponse);

        List<NodeDefinitionResponse> nodeDefinitionResponses = new ArrayList<>();
        for (Pair<NodeDefinition, TaskInstance> nodeDefAndTask : nodeDefAndTasks) {
            NodeDefinition nodeDefinition = nodeDefAndTask.getKey();
            TaskInstance taskInstance = nodeDefAndTask.getValue();

            // 封装任务实例
            TaskInstanceResponse taskInstanceResponse = null;
            if (taskInstance != null) {
                taskInstanceResponse = new TaskInstanceResponse();
                taskInstanceResponse.setId(taskInstance.getId());
                taskInstanceResponse.setCreatedAt(taskInstance.getCreatedAt());
            }

            // 封装节点定义
            NodeDefinitionResponse nodeDefinitionResponse = new NodeDefinitionResponse();
            nodeDefinitionResponse.setId(nodeDefinition.getId());
            nodeDefinitionResponse.setName(nodeDefinition.getName());
            nodeDefinitionResponse.setNodeType(nodeDefinition.getNodeType());
            nodeDefinitionResponse.setCreatedAt(nodeDefinition.getCreatedAt());
            nodeDefinitionResponse.setTaskInstanceResponse(taskInstanceResponse);

            // 封装审批人
            List<NodeAssignment> currNodeAssignments = nodeAssignments.stream().filter(nodeAssignment -> nodeDefinition.getId().equals(nodeAssignment.getNodeDefinitionId())).toList();
            List<NodeAssignmentResponse> nodeAssignmentResponses = currNodeAssignments.stream().map(NodeAssignmentResponse::of).toList();
            nodeDefinitionResponse.setNodeAssignmentResponses(nodeAssignmentResponses);

            // 封装审批记录
            if (taskInstance != null) {
                List<TaskApprove> currTaskApproves = taskApproves.stream().filter(approve -> taskInstance.getId().equals(approve.getTaskInstanceId())).toList();
                if (!currTaskApproves.isEmpty()) {
                    List<TaskApproveResponse> taskApproveResponses = currTaskApproves.stream().map(taskApprove -> {
                        return TaskApproveResponse.of(taskApprove, currentUserId);
                    }).toList();
                    taskInstanceResponse.setTaskApproveResponses(taskApproveResponses);
                }
            }

            nodeDefinitionResponses.add(nodeDefinitionResponse);
        }
        records.setNodeDefinitionResponses(nodeDefinitionResponses);

        return records;
    }

    @Data
    public static class WorkflowDefinitionResponse {
        /** 流程定义 ID */
        private Integer id;
        /** 租户 ID */
        private String tenantId;
        /** 流程名称 */
        private String name;
        /** 创建时间 */
        private Date createdAt;
    }

    @Data
    public static class NodeDefinitionResponse {
        /** 节点定义 ID */
        private Integer id;
        /** 节点名称 */
        private String name;
        /** 节点类型 */
        private NodeType nodeType;
        /** 节点实例 */
        private TaskInstanceResponse taskInstanceResponse;
        /** 审批人 */
        private List<NodeAssignmentResponse> nodeAssignmentResponses;
        /** 创建时间 */
        private Date createdAt;
    }

    @Data
    public static class NodeAssignmentResponse {
        /** 审批人 ID */
        private Integer id;
        /** 审批人 */
        private String approverId;
        /** 审批顺序 */
        private Integer approverSeq;
        /** 创建时间 */
        private Date createdAt;

        public static NodeAssignmentResponse of(NodeAssignment nodeAssignment) {
            NodeAssignmentResponse response = new NodeAssignmentResponse();
            response.id = nodeAssignment.getId();
            response.approverId = nodeAssignment.getApproverId();
            response.approverSeq = nodeAssignment.getApproverSeq();
            response.createdAt = nodeAssignment.getCreatedAt();
            return response;
        }
    }

    @Data
    public static class TaskInstanceResponse {
        /** 节点实例 ID */
        private Integer id;
        /** 审批节点 */
        private List<TaskApproveResponse> taskApproveResponses;
        /** 创建时间 */
        private Date createdAt;
    }

    @Data
    public static class TaskApproveResponse {
        /** 审核人 */
        private String approver;
        /** 审核意见 */
        private String comment;
        /** 审核结果 */
        private ApproveStatus approveStatus;
        /** 创建时间 */
        private Date createdAt;
        /** 是否为当前用户 */
        private boolean isSelf;

        public static TaskApproveResponse of(TaskApprove taskApprove, String currentUserId) {
            TaskApproveResponse response = new TaskApproveResponse();
            response.approver = taskApprove.getApproverId();
            response.comment = taskApprove.getComment();
            response.approveStatus = taskApprove.getStatus();
            response.createdAt = taskApprove.getCreatedAt();
            response.setSelf(currentUserId != null && currentUserId.equals(taskApprove.getApproverId()));
            return response;
        }
    }

}
