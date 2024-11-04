package io.github.thebesteric.framework.agile.plugins.workflow.domain.response;

import io.github.thebesteric.framework.agile.plugins.workflow.constant.*;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.Approver;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.Conditions;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.RoleApprover;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Stream;

/**
 * 流程定义流程图
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-09-29 18:28:40
 */
@Data
public class WorkflowDefinitionFlowSchema implements Serializable {
    @Serial
    private static final long serialVersionUID = 8622244089008695940L;

    /** 流程定义ID */
    private Integer id;
    /** 租户 ID */
    private String tenantId;
    /** 流程标识 */
    private String key;
    /** 流程名称 */
    private String name;
    /** 流程类型（用于类型分类） */
    private String type = "default";
    /** 连续审批方式：默认每个节点都需要审批 */
    private Map<String, String> continuousApproveMode;
    /** 审批人为空时，是否允许自动审批 */
    private boolean allowEmptyAutoApprove = false;
    /** 是否允许撤回 */
    private boolean allowRedo = true;
    /** 是否必须填写审批意见 */
    private boolean requiredComment = false;
    /** 发布状态 */
    private Map<String, String> publish;
    /** 发布日期 */
    private Date publishedAt;
    /** 为空时的审批人 */
    private Approver whenEmptyApprover;
    /** 创建日期 */
    private Date createdAt;

    /** 流程定义 */
    private List<List<NodeDefinitionResponse>> nodeDefinitionResponses = new ArrayList<>();

    /**
     * 封装
     *
     * @param workflowDefinition  流程定义
     * @param nodeDefinitions     节点定义集合
     * @param nodeRelations       节点关系集合
     * @param nodeAssignments     审批人列表
     * @param nodeRoleAssignments 角色审批人列表
     *
     * @return WorkflowDefinitionFlowSchema
     *
     * @author wangweijun
     * @since 2024/9/30 15:22
     */
    public static WorkflowDefinitionFlowSchema of(WorkflowDefinition workflowDefinition,
                                                  List<NodeDefinition> nodeDefinitions, List<NodeRelation> nodeRelations,
                                                  List<NodeAssignment> nodeAssignments, List<NodeRoleAssignment> nodeRoleAssignments) {
        WorkflowDefinitionFlowSchema flowSchema = new WorkflowDefinitionFlowSchema();
        flowSchema.id = workflowDefinition.getId();
        flowSchema.tenantId = workflowDefinition.getTenantId();
        flowSchema.key = workflowDefinition.getKey();
        flowSchema.name = workflowDefinition.getName();
        flowSchema.type = workflowDefinition.getType();
        flowSchema.continuousApproveMode = workflowDefinition.getContinuousApproveMode().toMap();
        flowSchema.allowEmptyAutoApprove = workflowDefinition.isAllowEmptyAutoApprove();
        flowSchema.allowRedo = workflowDefinition.isAllowRedo();
        flowSchema.requiredComment = workflowDefinition.isRequiredComment();
        flowSchema.publish = workflowDefinition.getPublish().toMap();
        flowSchema.publishedAt = workflowDefinition.getPublishedAt();
        flowSchema.whenEmptyApprover = workflowDefinition.getWhenEmptyApprover();
        flowSchema.createdAt = workflowDefinition.getCreatedAt();

        NodeDefinition startNode = nodeDefinitions.stream().filter(nodeDefinition -> nodeDefinition.getNodeType() == NodeType.START).findFirst().orElseThrow();
        List<NodeDefinition> taskNodes = nodeDefinitions.stream().filter(nodeDefinition -> nodeDefinition.getNodeType() == NodeType.TASK).toList();
        NodeDefinition endNode = nodeDefinitions.stream().filter(nodeDefinition -> nodeDefinition.getNodeType() == NodeType.END).findFirst().orElseThrow();


        // 添加开始节点
        flowSchema.nodeDefinitionResponses.add(List.of(NodeDefinitionResponse.of(startNode, null, null)));

        // 添加第一级任务节点
        List<NodeDefinition> nextNodeDefinitions = nodeRelations.stream()
                .filter(i -> i.getFromNodeId().equals(startNode.getId())).map(NodeRelation::getToNodeId)
                .map(nodeId -> taskNodes.stream().filter(n -> NodeType.END != n.getNodeType()).filter(n -> n.getId().equals(nodeId)).findFirst().orElse(null))
                .toList();

        flowSchema.nodeDefinitionResponses.add(nextNodeDefinitions.stream().map(nodeDefinition -> {
            List<NodeAssignment> nodeAssignmentsByNodeDefinition = new ArrayList<>();
            List<NodeRoleAssignment> nodeRoleAssignmentsByNodeDefinition = new ArrayList<>();
            if (nodeDefinition.isRoleApprove()) {
                nodeRoleAssignmentsByNodeDefinition = nodeRoleAssignments.stream().filter(i -> i.getNodeDefinitionId().equals(nodeDefinition.getId())).toList();
            } else {
                nodeAssignmentsByNodeDefinition = nodeAssignments.stream().filter(i -> i.getNodeDefinitionId().equals(nodeDefinition.getId())).toList();
            }
            return NodeDefinitionResponse.of(nodeDefinition, nodeAssignmentsByNodeDefinition, nodeRoleAssignmentsByNodeDefinition);
        }).toList());

        // 添加剩余任务节点
        while (!nextNodeDefinitions.isEmpty()) {
            Set<Integer> nextNodeDefinitionIds = new LinkedHashSet<>();
            for (NodeDefinition nextNodeDefinition : nextNodeDefinitions) {
                Integer nodeDefinitionId = nextNodeDefinition.getId();
                List<Integer> ids = nodeRelations.stream().filter(i -> i.getFromNodeId().equals(nodeDefinitionId)).filter(i -> !Objects.equals(i.getToNodeId(), endNode.getId()))
                        .flatMap(i -> Stream.of(i.getToNodeId())).toList();
                if (!ids.isEmpty()) {
                    nextNodeDefinitionIds.addAll(ids);
                }
            }
            nextNodeDefinitions = taskNodes.stream().filter(i -> nextNodeDefinitionIds.contains(i.getId())).toList();
            if (!nextNodeDefinitions.isEmpty()) {
                flowSchema.nodeDefinitionResponses.add(nextNodeDefinitions.stream().map(nodeDefinition -> {
                    List<NodeAssignment> nodeAssignmentsByNodeDefinition = new ArrayList<>();
                    List<NodeRoleAssignment> nodeRoleAssignmentsByNodeDefinition = new ArrayList<>();
                    if (nodeDefinition.isRoleApprove()) {
                        nodeRoleAssignmentsByNodeDefinition = nodeRoleAssignments.stream().filter(i -> i.getNodeDefinitionId().equals(nodeDefinition.getId())).toList();
                    } else {
                        nodeAssignmentsByNodeDefinition = nodeAssignments.stream().filter(i -> i.getNodeDefinitionId().equals(nodeDefinition.getId())).toList();
                    }
                    return NodeDefinitionResponse.of(nodeDefinition, nodeAssignmentsByNodeDefinition, nodeRoleAssignmentsByNodeDefinition);
                }).toList());
            }
        }

        // 添加结束节点
        flowSchema.nodeDefinitionResponses.add(List.of(NodeDefinitionResponse.of(endNode, null, null)));

        return flowSchema;
    }

    @Data
    public static class NodeDefinitionResponse implements Serializable{
        @Serial
        private static final long serialVersionUID = -2416352782055701117L;

        /** 节点定义ID */
        private Integer id;
        /** 流程名称 */
        private String name;
        /** 节点类型 */
        private Map<String, String> nodeType;
        /** 审批类型 */
        private Map<String, String> approveType;
        /** 条件定义 */
        private Conditions conditions;
        /** 排序 */
        private Double sequence;
        /** 是否是动态指定审批人 */
        private boolean dynamic = false;
        /** 是否是动态指定审批人 */
        private boolean roleApprove = false;
        /** 是否是动态指定审批人 */
        private Map<String, String> roleUserApproveType;
        /** 角色审批类型 */
        private Map<String, String> roleApproveType;
        /** 审批人 */
        private Set<Approver> approvers = new LinkedHashSet<>();
        /** 角色审批人 */
        private Set<RoleApprover> roleApprovers = new LinkedHashSet<>();

        /** 审批人对应关系 */
        private List<NodeAssignmentResponse> nodeAssignmentResponses = new ArrayList<>();
        /** 角色审批人对应关系 */
        private List<NodeRoleAssignmentResponse> nodeRoleAssignmentResponses = new ArrayList<>();

        public static NodeDefinitionResponse of(NodeDefinition nodeDefinition, List<NodeAssignment> nodeAssignments, List<NodeRoleAssignment> nodeRoleAssignments) {
            NodeDefinitionResponse nodeDefinitionResponse = new NodeDefinitionResponse();
            nodeDefinitionResponse.id = nodeDefinition.getId();
            nodeDefinitionResponse.name = nodeDefinition.getName();
            nodeDefinitionResponse.nodeType = nodeDefinition.getNodeType().toMap();
            nodeDefinitionResponse.approveType = nodeDefinition.getApproveType().toMap();
            nodeDefinitionResponse.conditions = nodeDefinition.getConditions();
            nodeDefinitionResponse.sequence = nodeDefinition.getSequence();
            nodeDefinitionResponse.dynamic = nodeDefinition.isDynamic();
            nodeDefinitionResponse.roleApprove = nodeDefinition.isRoleApprove();
            nodeDefinitionResponse.roleUserApproveType = nodeDefinition.getRoleUserApproveType().toMap();
            nodeDefinitionResponse.roleApproveType = nodeDefinition.getRoleApproveType().toMap();
            nodeDefinitionResponse.roleApprovers = nodeDefinition.getRoleApprovers();
            nodeDefinitionResponse.approvers = nodeDefinition.getApprovers();
            if (nodeRoleAssignments != null) {
                nodeDefinitionResponse.nodeRoleAssignmentResponses = nodeRoleAssignments.stream().flatMap(i -> Stream.of(NodeRoleAssignmentResponse.of(i))).toList();
            }
            if (nodeAssignments != null) {
                nodeDefinitionResponse.nodeAssignmentResponses = nodeAssignments.stream().flatMap(i -> Stream.of(NodeAssignmentResponse.of(i))).toList();
            }
            return nodeDefinitionResponse;
        }
    }

    @Data
    public static class NodeAssignmentResponse implements Serializable {
        @Serial
        private static final long serialVersionUID = -7225604582591287563L;

        /** ID */
        private Integer id;
        /** 审批人 ID */
        private String approverId;
        /** 审批人名称 */
        private String approverName;
        /** 审批顺序 */
        private Integer approverSeq;
        /** 审批人描述 */
        private String approverDesc;
        /** 审批人 ID 类型 */
        private ApproverIdType approverIdType;

        private static NodeAssignmentResponse of(NodeAssignment nodeAssignment) {
            NodeAssignmentResponse nodeAssignmentResponse = new NodeAssignmentResponse();
            nodeAssignmentResponse.id = nodeAssignment.getId();
            nodeAssignmentResponse.approverId = nodeAssignment.getApproverId();
            nodeAssignmentResponse.approverName = nodeAssignment.getApproverName();
            nodeAssignmentResponse.approverSeq = nodeAssignment.getApproverSeq();
            nodeAssignmentResponse.approverDesc = nodeAssignment.getApproverDesc();
            nodeAssignmentResponse.approverIdType = nodeAssignment.getApproverIdType();
            return nodeAssignmentResponse;
        }
    }

    @Data
    public static class NodeRoleAssignmentResponse implements Serializable {
        @Serial
        private static final long serialVersionUID = -8716668936512338775L;

        /** ID */
        private Integer id;
        /** 角色 ID */
        private String roleId;
        /** 角色名称 */
        private String roleName;
        /** 角色审批顺序 */
        private Integer roleSeq;
        /** 角色描述 */
        private String roleDesc;
        /** 用户 ID */
        private String userId;
        /** 用户名称 */
        private String userName;
        /** 用户 ID */
        private Integer userSeq;
        /** 用户描述 */
        private String userDesc;

        private static NodeRoleAssignmentResponse of(NodeRoleAssignment nodeRoleAssignment) {
            NodeRoleAssignmentResponse nodeRoleAssignmentResponse = new NodeRoleAssignmentResponse();
            nodeRoleAssignmentResponse.id = nodeRoleAssignment.getId();
            nodeRoleAssignmentResponse.roleId = nodeRoleAssignment.getRoleId();
            nodeRoleAssignmentResponse.roleName = nodeRoleAssignment.getRoleName();
            nodeRoleAssignmentResponse.roleSeq = nodeRoleAssignment.getRoleSeq();
            nodeRoleAssignmentResponse.roleDesc = nodeRoleAssignment.getRoleDesc();
            nodeRoleAssignmentResponse.userId = nodeRoleAssignment.getUserId();
            nodeRoleAssignmentResponse.userName = nodeRoleAssignment.getUserName();
            nodeRoleAssignmentResponse.userSeq = nodeRoleAssignment.getUserSeq();
            nodeRoleAssignmentResponse.userDesc = nodeRoleAssignment.getUserDesc();
            return nodeRoleAssignmentResponse;
        }
    }

}
