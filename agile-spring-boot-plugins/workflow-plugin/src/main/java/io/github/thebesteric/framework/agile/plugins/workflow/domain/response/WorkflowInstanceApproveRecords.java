package io.github.thebesteric.framework.agile.plugins.workflow.domain.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.thebesteric.framework.agile.core.domain.Pair;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.BusinessInfo;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.Conditions;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.RequestCondition;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.RequestConditions;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * 流程实例审批记录
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-09-12 12:06:48
 */
@Data
public class WorkflowInstanceApproveRecords implements Serializable {
    @Serial
    private static final long serialVersionUID = 4516285379098113700L;

    @Schema(description = "流程实例")
    private WorkflowInstanceResponse workflowInstance;
    @Schema(description = "流程定义")
    private WorkflowDefinitionResponse workflowDefinition;
    @Schema(description = "流程节点")
    private List<NodeDefinitionResponse> nodeDefinitionResponses;

    /**
     * 组装
     *
     * @param workflowDefinition                     流程定义
     * @param workflowInstance                       流程实例
     * @param nodeDefAndTasks                        节点定义与流程实例对应关系
     * @param taskApproves                           审批任务
     * @param taskApproveAndNodeAssignmentMap        审批任务与节点审批人的对应关系
     * @param taskApproveAndTaskReassignRecordMap    审批任务与委派人的对应关系
     * @param taskApproveAndRoleApproveRecordsMap    审批任务与角色审批记录对应关系
     * @param taskRoleRecordAndNodeRoleAssignmentMap 角色审批记录与角色用户对应关系
     * @param nodeDefAndDynamicNodeAssignmentMap     节点定义动态节点审批人的对应关系
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
                                                    Map<TaskApprove, NodeAssignment> taskApproveAndNodeAssignmentMap,
                                                    Map<TaskApprove, TaskReassignRecord> taskApproveAndTaskReassignRecordMap,
                                                    Map<TaskApprove, List<TaskRoleApproveRecord>> taskApproveAndRoleApproveRecordsMap,
                                                    Map<TaskRoleApproveRecord, NodeRoleAssignment> taskRoleRecordAndNodeRoleAssignmentMap,
                                                    Map<NodeDefinition, List<TaskDynamicAssignment>> nodeDefAndDynamicNodeAssignmentMap,
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
                                NodeAssignment nodeAssignment = taskApproveAndNodeAssignmentMap.get(taskApprove);
                                TaskReassignRecord taskReassignRecord = taskApproveAndTaskReassignRecordMap.get(taskApprove);
                                List<TaskRoleApproveRecord> taskRoleApproveRecords = taskApproveAndRoleApproveRecordsMap.get(taskApprove);
                                return TaskApproveResponse.of(nodeDefinition, taskApprove, nodeAssignment, taskReassignRecord, taskRoleApproveRecords, taskRoleRecordAndNodeRoleAssignmentMap, curRoleIds, curUserId);
                            })
                            .sorted(Comparator.comparing(TaskApproveResponse::getApproverSeq, Comparator.nullsLast(Comparator.naturalOrder()))
                                    .thenComparing(TaskApproveResponse::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                            .toList();
                    // 封装任务实例
                    taskInstanceResponse = TaskInstanceResponse.of(taskInstance, taskApproveResponses);
                }
            }

            // 封装审批人
            List<NodeAssignment> currNodeAssignments = nodeAssignments.stream().filter(nodeAssignment -> nodeDefinition.getId().equals(nodeAssignment.getNodeDefinitionId())).toList();
            List<NodeAssignmentResponse> nodeAssignmentsResponse = currNodeAssignments.stream().map(NodeAssignmentResponse::of).toList();

            // 获取动态审批人
            List<TaskDynamicAssignment> taskDynamicAssignments = nodeDefAndDynamicNodeAssignmentMap.get(nodeDefinition);
            List<TaskDynamicAssignmentResponse> taskDynamicAssignmentsResponse = taskDynamicAssignments.stream().map(TaskDynamicAssignmentResponse::of).toList();

            // 封装节点定义
            NodeDefinitionResponse nodeDefinitionResponse = NodeDefinitionResponse.of(nodeDefinition, taskInstanceResponse, nodeAssignmentsResponse, taskDynamicAssignmentsResponse);
            nodeDefinitionResponses.add(nodeDefinitionResponse);
        }
        records.setNodeDefinitionResponses(nodeDefinitionResponses);

        return records;
    }

    @Data
    public static class WorkflowInstanceResponse implements Serializable {
        @Serial
        private static final long serialVersionUID = 8027852454113775015L;

        @Schema(description = "流程实例 ID")
        private Integer id;
        @Schema(description = "租户 ID")
        private String tenantId;
        @Schema(description = "流程定义 ID")
        private Integer workflowDefinitionId;
        @Schema(description = "流程发起人 ID")
        private String requesterId;
        @Schema(description = "流程发起人名称")
        private String requesterName;
        @Schema(description = "流程发起人描述")
        private String requesterDesc;
        @Schema(description = "业务信息")
        private BusinessInfo businessInfo;
        @Schema(description = "请求条件")
        private List<RequestCondition> requestConditions = new ArrayList<>();
        @Schema(description = "流程状态")
        private Map<String, String> status;
        @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "创建时间")
        private Date createdAt;
        @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "修改时间")
        private Date updatedAt;

        public static WorkflowInstanceResponse of(WorkflowInstance workflowInstance) {
            WorkflowInstanceResponse response = new WorkflowInstanceResponse();
            response.id = workflowInstance.getId();
            response.tenantId = workflowInstance.getTenantId();
            response.workflowDefinitionId = workflowInstance.getWorkflowDefinitionId();
            response.requesterId = workflowInstance.getRequesterId();
            response.requesterName = workflowInstance.getRequesterName();
            response.requesterDesc = workflowInstance.getRequesterDesc();
            response.businessInfo = workflowInstance.getBusinessInfo();
            RequestConditions requestConditions = workflowInstance.getRequestConditions();
            if (requestConditions != null) {
                response.requestConditions = requestConditions.getRequestConditions();
            }
            response.status = workflowInstance.getStatus().toMap();
            response.createdAt = workflowInstance.getCreatedAt();
            response.updatedAt = workflowInstance.getUpdatedAt();
            return response;
        }
    }

    @Data
    public static class WorkflowDefinitionResponse implements Serializable {
        @Serial
        private static final long serialVersionUID = 6449586499506204689L;

        @Schema(description = "流程定义 ID")
        private Integer id;
        @Schema(description = "租户 ID")
        private String tenantId;
        @Schema(description = "流程标识")
        private String key;
        @Schema(description = "流程名称")
        private String name;
        @Schema(description = "流程类型（用于类型分类）")
        private String type;
        @Schema(description = "连续审批方式：默认每个节点都需要审批")
        private Map<String, String> continuousApproveMode;
        @Schema(description = "没有条件节点符合时的处理策略: 默认抛出异常")
        private Map<String, String> conditionNotMatchedAnyStrategy;
        @Schema(description = "审批人为空时，是否允许自动审批")
        private boolean allowEmptyAutoApprove = false;
        @Schema(description = "是否允许撤回")
        private boolean allowRedo = true;
        @Schema(description = "是否必须填写审批意见")
        private boolean requiredComment = false;
        @Schema(description = "发布状态")
        private Map<String, String> publish;
        @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "创建时间")
        private Date createdAt;
        @Schema(description = "修改时间")
        @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
        private Date updatedAt;

        public static WorkflowDefinitionResponse of(WorkflowDefinition workflowDefinition) {
            WorkflowDefinitionResponse response = new WorkflowDefinitionResponse();
            response.id = workflowDefinition.getId();
            response.tenantId = workflowDefinition.getTenantId();
            response.key = workflowDefinition.getKey();
            response.name = workflowDefinition.getName();
            response.type = workflowDefinition.getType();
            response.continuousApproveMode = workflowDefinition.getContinuousApproveMode().toMap();
            response.conditionNotMatchedAnyStrategy = workflowDefinition.getConditionNotMatchedAnyStrategy().toMap();
            response.allowEmptyAutoApprove = workflowDefinition.isAllowEmptyAutoApprove();
            response.allowRedo = workflowDefinition.isAllowRedo();
            response.requiredComment = workflowDefinition.isRequiredComment();
            response.publish = workflowDefinition.getPublish().toMap();
            response.createdAt = workflowDefinition.getCreatedAt();
            response.updatedAt = workflowDefinition.getUpdatedAt();
            return response;
        }
    }

    @Data
    public static class NodeDefinitionResponse implements Serializable {
        @Serial
        private static final long serialVersionUID = -8280161498012703611L;

        @Schema(description = "节点定义 ID")
        private Integer id;
        @Schema(description = "节点名称")
        private String name;
        @Schema(description = "节点类型")
        private Map<String, String> nodeType;
        @Schema(description = "是否是动态指定审批节点")
        private boolean dynamic = false;
        @Schema(description = "动态审批人数量")
        private Integer dynamicAssignmentNum = 0;
        @Schema(description = "条件定义")
        private Conditions conditions;
        @Schema(description = "排序")
        private Double sequence;
        @Schema(description = "是否是角色审批节点")
        private boolean roleApprove = false;
        @Schema(description = "角色用户审批类型")
        private Map<String, String> roleUserApproveType;
        @Schema(description = "角色审批类型")
        private Map<String, String> roleApproveType;
        @Schema(description = "节点实例")
        private TaskInstanceResponse taskInstanceResponse;
        @Schema(description = "审批人")
        private List<NodeAssignmentResponse> nodeAssignmentsResponse;
        @Schema(description = "动态审批人")
        private List<TaskDynamicAssignmentResponse> taskDynamicAssignmentsResponse;
        @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "创建时间")
        private Date createdAt;
        @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "修改时间")
        private Date updatedAt;

        public static NodeDefinitionResponse of(NodeDefinition nodeDefinition, TaskInstanceResponse taskInstanceResponse,
                                                List<NodeAssignmentResponse> nodeAssignmentsResponse,
                                                List<TaskDynamicAssignmentResponse> taskDynamicAssignmentsResponse) {
            NodeDefinitionResponse response = new NodeDefinitionResponse();
            response.id = nodeDefinition.getId();
            response.name = nodeDefinition.getName();
            response.nodeType = nodeDefinition.getNodeType().toMap();
            response.dynamic = nodeDefinition.isDynamic();
            response.dynamicAssignmentNum = nodeDefinition.getDynamicAssignmentNum();
            response.conditions = nodeDefinition.getConditions();
            response.sequence = nodeDefinition.getSequence();
            response.roleApprove = nodeDefinition.isRoleApprove();
            response.roleUserApproveType = nodeDefinition.getRoleUserApproveType().toMap();
            response.roleApproveType = nodeDefinition.getRoleApproveType().toMap();
            response.taskInstanceResponse = taskInstanceResponse;
            response.nodeAssignmentsResponse = nodeAssignmentsResponse;
            response.taskDynamicAssignmentsResponse = taskDynamicAssignmentsResponse;
            response.createdAt = nodeDefinition.getCreatedAt();
            response.updatedAt = nodeDefinition.getUpdatedAt();
            return response;
        }
    }

    @Data
    public static class NodeAssignmentResponse implements Serializable {
        @Serial
        private static final long serialVersionUID = -725062203728064396L;

        @Schema(description = "用户任务关联 ID")
        private Integer id;
        @Schema(description = "审批人 ID")
        private String approverId;
        @Schema(description = "审批人名称")
        private String approverName;
        @Schema(description = "审批人描述")
        private String approverDesc;
        @Schema(description = "审批顺序")
        private Integer approverSeq;
        @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "创建时间")
        private Date createdAt;
        @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "修改时间")
        private Date updatedAt;

        public static NodeAssignmentResponse of(NodeAssignment nodeAssignment) {
            NodeAssignmentResponse response = new NodeAssignmentResponse();
            response.id = nodeAssignment.getId();
            response.approverId = nodeAssignment.getApproverId();
            response.approverName = nodeAssignment.getApproverName();
            response.approverDesc = nodeAssignment.getApproverDesc();
            response.approverSeq = nodeAssignment.getApproverSeq();
            response.createdAt = nodeAssignment.getCreatedAt();
            response.updatedAt = nodeAssignment.getUpdatedAt();
            return response;
        }
    }

    @Data
    public static class TaskDynamicAssignmentResponse implements Serializable {
        @Serial
        private static final long serialVersionUID = 6895044462882392326L;

        @Schema(description = "动态审批人 ID")
        private Integer id;
        @Schema(description = "节点定义 ID")
        private Integer nodeDefinitionId;
        @Schema(description = "任务实例 ID")
        private Integer taskInstanceId;
        @Schema(description = "审批人 ID")
        private String approverId;
        @Schema(description = "审批人名称")
        private String approverName;
        @Schema(description = "审批人描述")
        private String approverDesc;
        @Schema(description = "审批顺序")
        private Integer approverSeq;
        @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "创建时间")
        private Date createdAt;
        @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "修改时间")
        private Date updatedAt;

        public static TaskDynamicAssignmentResponse of(TaskDynamicAssignment taskDynamicAssignment) {
            TaskDynamicAssignmentResponse response = new TaskDynamicAssignmentResponse();
            response.id = taskDynamicAssignment.getId();
            response.nodeDefinitionId = taskDynamicAssignment.getNodeDefinitionId();
            response.taskInstanceId = taskDynamicAssignment.getTaskInstanceId();
            response.approverId = taskDynamicAssignment.getApproverId();
            response.approverName = taskDynamicAssignment.getApproverName();
            response.approverDesc = taskDynamicAssignment.getApproverDesc();
            response.approverSeq = taskDynamicAssignment.getApproverSeq();
            response.createdAt = taskDynamicAssignment.getCreatedAt();
            response.updatedAt = taskDynamicAssignment.getUpdatedAt();
            return response;
        }
    }

    @Data
    public static class TaskInstanceResponse implements Serializable {
        @Serial
        private static final long serialVersionUID = 706922113015404374L;

        @Schema(description = "节点实例 ID")
        private Integer id;
        @Schema(description = "审批人数")
        private Integer approvedCount;
        @Schema(description = "应审批总人数")
        private Integer totalCount;
        @Schema(description = "审核结果")
        private Map<String, String> status;
        @Schema(description = "审批节点")
        private List<TaskApproveResponse> taskApproveResponses;
        @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "创建时间")
        private Date createdAt;
        @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "修改时间")
        private Date updatedAt;

        public static TaskInstanceResponse of(TaskInstance taskInstance, List<TaskApproveResponse> taskApproveResponses) {
            // taskApproveResponses 先按 approverSeq 排序，再按 createdAt 排序
            taskApproveResponses = new ArrayList<>(taskApproveResponses);
            taskApproveResponses.sort(
                    Comparator.comparing(TaskApproveResponse::getApproverSeq, Comparator.nullsLast(Comparator.naturalOrder()))
                            .thenComparing(TaskApproveResponse::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
            );
            TaskInstanceResponse response = new TaskInstanceResponse();
            response.id = taskInstance.getId();
            response.approvedCount = taskInstance.getApprovedCount();
            response.totalCount = taskInstance.getTotalCount();
            response.status = taskInstance.getStatus().toMap();
            response.taskApproveResponses = taskApproveResponses;
            response.createdAt = taskInstance.getCreatedAt();
            response.updatedAt = taskInstance.getUpdatedAt();
            return response;
        }
    }

    @Data
    public static class TaskApproveResponse implements Serializable {
        @Serial
        private static final long serialVersionUID = 7710466011285696446L;

        @Schema(description = "审批记录 ID")
        private Integer taskApproveId;
        @Schema(description = "审核人 ID（根据 roleApprove 来对应 NodeAssignment 或 NodeRoleAssignment 中的 ID）")
        private String userId;
        @Schema(description = "审批人 ID")
        private String approverId;
        @Schema(description = "审批人姓名")
        private String approverName;
        @Schema(description = "审批人顺序")
        private Integer approverSeq;
        @Schema(description = "审批人描述")
        private String approverDesc;
        @Schema(description = "审批意见")
        private String comment;
        @Schema(description = "审核结果")
        private Map<String, String> approveStatus;
        @Schema(description = "被委派的任务实例审批记录 ID")
        private Integer reassignedTaskApproveId;
        @Schema(description = "是否时角色审批")
        private boolean roleApprove;
        @Schema(description = "角色审批人列表")
        private List<TaskRoleApproveRecordResponse> taskRoleApproveRecordResponses;
        @Schema(description = "是否为当前用户")
        private boolean isSelf;
        @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "创建时间")
        private Date createdAt;
        @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "修改时间")
        private Date updatedAt;

        public static TaskApproveResponse of(NodeDefinition nodeDefinition, TaskApprove taskApprove, NodeAssignment nodeAssignment, TaskReassignRecord taskReassignRecord,
                                             List<TaskRoleApproveRecord> taskRoleApproveRecords, Map<TaskRoleApproveRecord, NodeRoleAssignment> taskRoleRecordAndNodeRoleAssignmentMap,
                                             List<String> curRoleIds, String curUserId) {
            TaskApproveResponse response = new TaskApproveResponse();
            response.taskApproveId = taskApprove.getId();
            response.approverId = taskApprove.getApproverId();
            if (nodeAssignment != null) {
                // 用户审批（取的是用户定义表 ID），角色审批（取的是角色 ID）
                response.userId = nodeDefinition.isUserApprove() ? nodeAssignment.getId().toString() : taskApprove.getApproverId();
                response.approverName = nodeAssignment.getApproverName();
                response.approverSeq = nodeAssignment.getApproverSeq();
                response.approverDesc = nodeAssignment.getApproverDesc();
            }

            // 表示是用户级别的审批委派
            if (taskReassignRecord != null && nodeDefinition.isUserApprove()) {
                response.userId = taskReassignRecord.getId().toString();
                response.approverName = taskReassignRecord.getToUserName();
                response.approverSeq = taskReassignRecord.getToUserSeq();
                response.approverDesc = taskReassignRecord.getToUserDesc();
            }

            response.comment = taskApprove.getComment();
            response.approveStatus = taskApprove.getStatus().toMap();
            response.reassignedTaskApproveId = taskApprove.getReassignedTaskApproveId();
            response.createdAt = taskApprove.getCreatedAt();
            response.updatedAt = taskApprove.getUpdatedAt();
            response.isSelf = nodeDefinition.isUserApprove() && curUserId != null && curUserId.equals(taskApprove.getApproverId());
            response.setRoleApprove(nodeDefinition.isRoleApprove());
            if (taskRoleApproveRecords != null) {
                response.taskRoleApproveRecordResponses = taskRoleApproveRecords.stream()
                        .map(taskRoleApproveRecord -> {
                            NodeRoleAssignment nodeRoleAssignment = taskRoleRecordAndNodeRoleAssignmentMap.get(taskRoleApproveRecord);
                            return TaskRoleApproveRecordResponse.of(taskRoleApproveRecord, nodeRoleAssignment, curRoleIds, curUserId);
                        })
                        .sorted(Comparator.comparing(TaskRoleApproveRecordResponse::getRoleSeq, Comparator.nullsLast(Comparator.naturalOrder()))
                                .thenComparing(TaskRoleApproveRecordResponse::getUserSeq, Comparator.nullsLast(Comparator.naturalOrder())))
                        .toList();
            }
            return response;
        }
    }

    @Data
    public static class TaskRoleApproveRecordResponse implements Serializable {
        @Serial
        private static final long serialVersionUID = 332412407655337100L;

        @Schema(description = "角色审批记录 ID")
        private Integer taskRoleApproveRecordId;
        @Schema(description = "角色用户 ID")
        private Integer nodeRoleAssignmentId;
        @Schema(description = "角色 ID")
        private String roleId;
        @Schema(description = "角色名称")
        private String roleName;
        @Schema(description = "角色顺序")
        private Integer roleSeq;
        @Schema(description = "角色描述")
        private String roleDesc;
        @Schema(description = "审核人 ID")
        private String userId;
        @Schema(description = "审核人名称")
        private String userName;
        @Schema(description = "审核人顺序")
        private Integer userSeq;
        @Schema(description = "审核人描述")
        private String userDesc;
        @Schema(description = "审核意见")
        private String comment;
        @Schema(description = "审批结果")
        private Map<String, String> roleApproveStatus;
        @Schema(description = "被委派的角色任务实例审批记录 ID")
        private Integer reassignedTaskRoleApproveRecordId;
        @Schema(description = "是否为当前用户")
        private boolean isSelf;
        @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "创建时间")
        private Date createdAt;
        @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "修改时间")
        private Date updatedAt;

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
            response.roleApproveStatus = taskRoleApproveRecord.getStatus().toMap();
            response.reassignedTaskRoleApproveRecordId = taskRoleApproveRecord.getReassignedTaskRoleApproveRecordId();
            response.createdAt = taskRoleApproveRecord.getCreatedAt();
            response.updatedAt = taskRoleApproveRecord.getUpdatedAt();
            response.isSelf = curUserId != null && curUserId.equals(nodeRoleAssignment.getUserId()) && curRoleIds != null && curRoleIds.contains(nodeRoleAssignment.getRoleId());
            return response;
        }
    }

}
