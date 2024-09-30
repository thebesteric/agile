package io.github.thebesteric.framework.agile.plugins.workflow.domain.response;

import io.github.thebesteric.framework.agile.core.domain.Pair;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.ApproveStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
    private Integer workflowInstanceId;
    /** 流程定义 */
    private WorkflowDefinitionResponse workflowDefinition;
    /** 流程节点 */
    private List<NodeDefinitionResponse> nodeDefinitionResponses;
    /** 创建时间 */
    private Date createdAt;

    /**
     * 组装
     *
     * @param workflowDefinition                     流程定义
     * @param workflowInstance                       流程实例
     * @param nodeDefAndTasks                        节点定义与流程实例对应关系
     * @param taskApproves                           审批任务
     * @param nodeDefAndNodeAssignmentMap            节点定义与节点审批人的对应关系
     * @param taskApproveAndRoleApproveRecordsMap    审批任务与角色审批记录对应关系
     * @param taskRoleRecordAndNodeRoleAssignmentMap 角色审批记录与角色用户对应关系
     * @param nodeAssignments                        节点审批人
     * @param curRoleId                              当前角色 ID
     * @param curUserId                              当前用户 ID
     *
     * @return WorkflowInstanceApproveRecords
     *
     * @author wangweijun
     * @since 2024/9/29 10:36
     */
    public static WorkflowInstanceApproveRecords of(WorkflowDefinition workflowDefinition, WorkflowInstance workflowInstance,
                                                    List<Pair<NodeDefinition, TaskInstance>> nodeDefAndTasks,
                                                    List<TaskApprove> taskApproves,
                                                    Map<NodeDefinition, NodeAssignment> nodeDefAndNodeAssignmentMap,
                                                    Map<TaskApprove, List<TaskRoleApproveRecord>> taskApproveAndRoleApproveRecordsMap,
                                                    Map<TaskRoleApproveRecord, NodeRoleAssignment> taskRoleRecordAndNodeRoleAssignmentMap,
                                                    List<NodeAssignment> nodeAssignments,
                                                    String curRoleId, String curUserId) {

        WorkflowInstanceApproveRecords records = new WorkflowInstanceApproveRecords();
        records.setWorkflowInstanceId(workflowInstance.getId());
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
                taskInstanceResponse.setApprovedCount(taskInstance.getApprovedCount());
                taskInstanceResponse.setTotalCount(taskInstance.getTotalCount());
                taskInstanceResponse.setCreatedAt(taskInstance.getCreatedAt());
            }

            // 封装节点定义
            NodeDefinitionResponse nodeDefinitionResponse = new NodeDefinitionResponse();
            nodeDefinitionResponse.setId(nodeDefinition.getId());
            nodeDefinitionResponse.setName(nodeDefinition.getName());
            nodeDefinitionResponse.setNodeType(nodeDefinition.getNodeType().toMap());
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
                        NodeAssignment nodeAssignment = nodeDefAndNodeAssignmentMap.get(nodeDefinition);
                        List<TaskRoleApproveRecord> taskRoleApproveRecords = taskApproveAndRoleApproveRecordsMap.get(taskApprove);
                        return TaskApproveResponse.of(nodeDefinition, taskApprove, nodeAssignment, taskRoleApproveRecords, taskRoleRecordAndNodeRoleAssignmentMap, curRoleId, curUserId);
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
        private Map<String, Object> nodeType;
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
        /** 审批人备注 */
        private String approverDesc;
        /** 审批顺序 */
        private Integer approverSeq;
        /** 创建时间 */
        private Date createdAt;

        public static NodeAssignmentResponse of(NodeAssignment nodeAssignment) {
            NodeAssignmentResponse response = new NodeAssignmentResponse();
            response.id = nodeAssignment.getId();
            response.approverId = nodeAssignment.getApproverId();
            response.approverDesc = nodeAssignment.getDesc();
            response.approverSeq = nodeAssignment.getApproverSeq();
            response.createdAt = nodeAssignment.getCreatedAt();
            return response;
        }
    }

    @Data
    public static class TaskInstanceResponse {
        /** 节点实例 ID */
        private Integer id;
        /** 审批人数 */
        private Integer approvedCount;
        /** 应审批总人数 */
        private Integer totalCount;
        /** 审批节点 */
        private List<TaskApproveResponse> taskApproveResponses;
        /** 创建时间 */
        private Date createdAt;
    }

    @Data
    public static class TaskApproveResponse {
        /** 审核人 ID（根据 roleApprove 来对应 NodeAssignment 或 NodeRoleAssignment 中的 ID） */
        private String userId;
        /** 审批记录 ID */
        private String approverId;
        /** 审核意见 */
        private String comment;
        /** 审核结果 */
        private Map<String, Object> approveStatus;

        /** 是否时角色审批 */
        private boolean roleApprove;
        /** 角色审批人列表 */
        private List<TaskRoleApproveRecordResponse> taskRoleApproveRecordResponses;

        /** 是否为当前用户 */
        private boolean isSelf;
        /** 创建时间 */
        private Date createdAt;

        public static TaskApproveResponse of(NodeDefinition nodeDefinition, TaskApprove taskApprove, NodeAssignment nodeAssignment, List<TaskRoleApproveRecord> taskRoleApproveRecords, Map<TaskRoleApproveRecord, NodeRoleAssignment> taskRoleRecordAndNodeRoleAssignmentMap, String curRoleId, String curUserId) {
            TaskApproveResponse response = new TaskApproveResponse();
            if (nodeDefinition.isUserApprove()) {
                response.userId = nodeAssignment.getId().toString();
            } else {
                response.userId = taskApprove.getApproverId();
            }
            response.approverId = taskApprove.getApproverId();
            response.comment = taskApprove.getComment();
            response.approveStatus = taskApprove.getStatus().toMap();
            response.createdAt = taskApprove.getCreatedAt();
            response.isSelf = nodeDefinition.isUserApprove() && curUserId != null && curUserId.equals(taskApprove.getApproverId());
            response.setRoleApprove(nodeDefinition.isRoleApprove());
            if (taskRoleApproveRecords != null) {
                response.taskRoleApproveRecordResponses = taskRoleApproveRecords.stream().map(taskRoleApproveRecord -> {
                    NodeRoleAssignment nodeRoleAssignment = taskRoleRecordAndNodeRoleAssignmentMap.get(taskRoleApproveRecord);
                    return TaskRoleApproveRecordResponse.of(taskRoleApproveRecord, nodeRoleAssignment, curRoleId, curUserId);
                }).toList();
            }
            return response;
        }
    }

    @Data
    public static class TaskRoleApproveRecordResponse {
        /** 角色审批记录 ID */
        private Integer taskRoleApproveRecordId;
        /** 角色用户 ID */
        private Integer nodeRoleAssignmentId;
        /** 角色 ID */
        private String roleId;
        /** 角色顺序 */
        private Integer roleSeq;
        /** 角色描述 */
        private String roleDesc;
        /** 审核人 ID */
        private String userId;
        /** 审核人顺序 */
        private Integer userSeq;
        /** 审核人描述 */
        private String userDesc;
        /** 审核意见 */
        private String comment;
        /** 审核结果 */
        private ApproveStatus approveStatus;
        /** 是否为当前用户 */
        private boolean isSelf;
        /** 创建时间 */
        private Date createdAt;

        public static TaskRoleApproveRecordResponse of(TaskRoleApproveRecord taskRoleApproveRecord, NodeRoleAssignment nodeRoleAssignment, String curRoleId, String curUserId) {
            TaskRoleApproveRecordResponse response = new TaskRoleApproveRecordResponse();
            response.taskRoleApproveRecordId = taskRoleApproveRecord.getId();
            response.nodeRoleAssignmentId = nodeRoleAssignment.getId();
            response.roleId = nodeRoleAssignment.getRoleId();
            response.roleSeq = nodeRoleAssignment.getRoleSeq();
            response.roleDesc = nodeRoleAssignment.getRoleDesc();
            response.userId = nodeRoleAssignment.getUserId();
            response.userSeq = nodeRoleAssignment.getUserSeq();
            response.userDesc = nodeRoleAssignment.getUserDesc();
            response.comment = taskRoleApproveRecord.getComment();
            response.approveStatus = taskRoleApproveRecord.getStatus();
            response.createdAt = taskRoleApproveRecord.getCreatedAt();
            response.isSelf = curUserId != null && curUserId.equals(nodeRoleAssignment.getUserId()) && curRoleId != null && curRoleId.equals(nodeRoleAssignment.getRoleId());
            return response;
        }
    }

}
