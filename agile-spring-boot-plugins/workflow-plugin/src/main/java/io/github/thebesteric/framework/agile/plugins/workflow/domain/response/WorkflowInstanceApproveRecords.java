package io.github.thebesteric.framework.agile.plugins.workflow.domain.response;

import io.github.thebesteric.framework.agile.core.domain.Pair;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.ApproveStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.RequestConditions;
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
    private WorkflowInstanceResponse workflowInstance;
    /** 流程定义 */
    private WorkflowDefinitionResponse workflowDefinition;
    /** 流程节点 */
    private List<NodeDefinitionResponse> nodeDefinitionResponses;

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
     * @param curRoleIds                             当前角色 IDs
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
                                                    List<String> curRoleIds, String curUserId) {

        WorkflowInstanceApproveRecords records = new WorkflowInstanceApproveRecords();
        // 封装流程实例
        WorkflowInstanceResponse workflowInstanceResponse = WorkflowInstanceResponse.of(workflowInstance);
        records.setWorkflowInstance(workflowInstanceResponse);

        // 封装流程定义
        WorkflowDefinitionResponse workflowDefinitionResponse = WorkflowDefinitionResponse.of(workflowDefinition);
        records.setWorkflowDefinition(workflowDefinitionResponse);

        List<NodeDefinitionResponse> nodeDefinitionResponses = new ArrayList<>();
        for (Pair<NodeDefinition, TaskInstance> nodeDefAndTask : nodeDefAndTasks) {
            NodeDefinition nodeDefinition = nodeDefAndTask.getKey();
            TaskInstance taskInstance = nodeDefAndTask.getValue();

            // 封装任务实例
            TaskInstanceResponse taskInstanceResponse = null;

            // 封装审批记录
            if (taskInstance != null) {
                List<TaskApprove> currTaskApproves = taskApproves.stream().filter(approve -> taskInstance.getId().equals(approve.getTaskInstanceId())).toList();
                if (!currTaskApproves.isEmpty()) {
                    List<TaskApproveResponse> taskApproveResponses = currTaskApproves.stream().map(taskApprove -> {
                        NodeAssignment nodeAssignment = nodeDefAndNodeAssignmentMap.get(nodeDefinition);
                        List<TaskRoleApproveRecord> taskRoleApproveRecords = taskApproveAndRoleApproveRecordsMap.get(taskApprove);
                        return TaskApproveResponse.of(nodeDefinition, taskApprove, nodeAssignment, taskRoleApproveRecords, taskRoleRecordAndNodeRoleAssignmentMap, curRoleIds, curUserId);
                    }).toList();
                    // 封装任务实例
                    taskInstanceResponse = TaskInstanceResponse.of(taskInstance, taskApproveResponses);
                }
            }

            // 封装审批人
            List<NodeAssignment> currNodeAssignments = nodeAssignments.stream().filter(nodeAssignment -> nodeDefinition.getId().equals(nodeAssignment.getNodeDefinitionId())).toList();
            List<NodeAssignmentResponse> nodeAssignmentResponses = currNodeAssignments.stream().map(NodeAssignmentResponse::of).toList();

            // 封装节点定义
            NodeDefinitionResponse nodeDefinitionResponse = NodeDefinitionResponse.of(nodeDefinition, taskInstanceResponse, nodeAssignmentResponses);
            nodeDefinitionResponses.add(nodeDefinitionResponse);
        }
        records.setNodeDefinitionResponses(nodeDefinitionResponses);

        return records;
    }

    @Data
    public static class WorkflowInstanceResponse {
        /** 流程实例 ID */
        private Integer id;
        /** 租户 ID */
        private String tenantId;
        /** 流程定义 ID */
        private Integer workflowDefinitionId;
        /** 流程发起人 */
        private String requesterId;
        /** 业务类型 */
        private String businessType;
        /** 业务标识 */
        private String businessId;
        /** 请求条件 */
        private RequestConditions requestConditions;
        /** 流程状态 */
        private Map<String, Object> status;
        /** 创建时间 */
        private Date createdAt;

        public static WorkflowInstanceResponse of(WorkflowInstance workflowInstance) {
            WorkflowInstanceResponse response = new WorkflowInstanceResponse();
            response.id = workflowInstance.getId();
            response.tenantId = workflowInstance.getTenantId();
            response.workflowDefinitionId = workflowInstance.getWorkflowDefinitionId();
            response.requesterId = workflowInstance.getRequesterId();
            response.businessType = workflowInstance.getBusinessType();
            response.businessId = workflowInstance.getBusinessId();
            response.requestConditions = workflowInstance.getRequestConditions();
            response.status = workflowInstance.getStatus().toMap();
            response.createdAt = workflowInstance.getCreatedAt();
            return response;
        }
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

        public static WorkflowDefinitionResponse of(WorkflowDefinition workflowDefinition) {
            WorkflowDefinitionResponse response = new WorkflowDefinitionResponse();
            response.id = workflowDefinition.getId();
            response.tenantId = workflowDefinition.getTenantId();
            response.name = workflowDefinition.getName();
            response.createdAt = workflowDefinition.getCreatedAt();
            return response;
        }
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

        public static NodeDefinitionResponse of(NodeDefinition nodeDefinition, TaskInstanceResponse taskInstanceResponse, List<NodeAssignmentResponse> nodeAssignmentResponses) {
            NodeDefinitionResponse response = new NodeDefinitionResponse();
            response.id = nodeDefinition.getId();
            response.name = nodeDefinition.getName();
            response.nodeType = nodeDefinition.getNodeType().toMap();
            response.taskInstanceResponse = taskInstanceResponse;
            response.nodeAssignmentResponses = nodeAssignmentResponses;
            response.createdAt = nodeDefinition.getCreatedAt();
            return response;
        }
    }

    @Data
    public static class NodeAssignmentResponse {
        /** 审批人 ID */
        private Integer id;
        /** 审批人 */
        private String approverId;
        /** 审批人名称 */
        private String approverName;
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
            response.approverName = nodeAssignment.getApproverName();
            response.approverDesc = nodeAssignment.getApproverDesc();
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

        public static TaskInstanceResponse of(TaskInstance taskInstance, List<TaskApproveResponse> taskApproveResponses) {
            TaskInstanceResponse response = new TaskInstanceResponse();
            response.id = taskInstance.getId();
            response.approvedCount = taskInstance.getApprovedCount();
            response.totalCount = taskInstance.getTotalCount();
            response.taskApproveResponses = taskApproveResponses;
            response.createdAt = taskInstance.getCreatedAt();
            return response;
        }
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

        public static TaskApproveResponse of(NodeDefinition nodeDefinition, TaskApprove taskApprove, NodeAssignment nodeAssignment,
                                             List<TaskRoleApproveRecord> taskRoleApproveRecords, Map<TaskRoleApproveRecord, NodeRoleAssignment> taskRoleRecordAndNodeRoleAssignmentMap,
                                             List<String> curRoleIds, String curUserId) {
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
                    return TaskRoleApproveRecordResponse.of(taskRoleApproveRecord, nodeRoleAssignment, curRoleIds, curUserId);
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
        /** 角色名称 */
        private String roleName;
        /** 角色顺序 */
        private Integer roleSeq;
        /** 角色描述 */
        private String roleDesc;
        /** 审核人 ID */
        private String userId;
        /** 审核人名称 */
        private String userName;
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

        public static TaskRoleApproveRecordResponse of(TaskRoleApproveRecord taskRoleApproveRecord, NodeRoleAssignment nodeRoleAssignment, List<String> curRoleIds, String curUserId) {
            TaskRoleApproveRecordResponse response = new TaskRoleApproveRecordResponse();
            response.taskRoleApproveRecordId = taskRoleApproveRecord.getId();
            response.nodeRoleAssignmentId = nodeRoleAssignment.getId();
            response.roleId = nodeRoleAssignment.getRoleId();
            response.roleName = nodeRoleAssignment.getRoleName();
            response.roleSeq = nodeRoleAssignment.getRoleSeq();
            response.roleDesc = nodeRoleAssignment.getRoleDesc();
            response.userId = nodeRoleAssignment.getUserId();
            response.userName = nodeRoleAssignment.getUserName();
            response.userSeq = nodeRoleAssignment.getUserSeq();
            response.userDesc = nodeRoleAssignment.getUserDesc();
            response.comment = taskRoleApproveRecord.getComment();
            response.approveStatus = taskRoleApproveRecord.getStatus();
            response.createdAt = taskRoleApproveRecord.getCreatedAt();
            response.isSelf = curUserId != null && curUserId.equals(nodeRoleAssignment.getUserId()) && curRoleIds != null && curRoleIds.contains(nodeRoleAssignment.getRoleId());
            return response;
        }
    }

}
