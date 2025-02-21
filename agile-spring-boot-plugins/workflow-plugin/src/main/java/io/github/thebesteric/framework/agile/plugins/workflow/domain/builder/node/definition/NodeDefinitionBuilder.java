package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.definition;

import io.github.thebesteric.framework.agile.plugins.workflow.constant.*;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.Approver;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.Conditions;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.RoleApprover;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.NodeDefinition;
import io.github.thebesteric.framework.agile.plugins.workflow.exception.WorkflowException;
import org.springframework.util.Assert;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * NodeDefinitionBuilder
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-18 15:38:59
 */
public class NodeDefinitionBuilder extends AbstractBuilder<NodeDefinition> {

    private final NodeDefinition nodeDefinition;

    private NodeDefinitionBuilder(NodeDefinition nodeDefinition) {
        this.nodeDefinition = nodeDefinition;
    }

    public static NodeDefinitionBuilder builderNode(String tenantId, Integer workflowDefinitionId, NodeType nodeType, double sequence) {
        NodeDefinitionBuilder builder = new NodeDefinitionBuilder(new NodeDefinition());
        builder.nodeDefinition.setTenantId(tenantId);
        builder.nodeDefinition.setWorkflowDefinitionId(workflowDefinitionId);
        builder.nodeDefinition.setNodeType(nodeType);
        builder.nodeDefinition.setSequence(sequence);
        return builder;
    }

    public static NodeDefinitionBuilder builderStartNode(String tenantId, Integer workflowDefinitionId) {
        return builderNode(tenantId, workflowDefinitionId, NodeType.START, Integer.MIN_VALUE);
    }

    public static NodeDefinitionBuilder builderEndNode(String tenantId, Integer workflowDefinitionId) {
        return builderNode(tenantId, workflowDefinitionId, NodeType.END, Integer.MAX_VALUE);
    }

    public static NodeDefinitionBuilder builderTaskNode(String tenantId, Integer workflowDefinitionId, double sequence) {
        return builderNode(tenantId, workflowDefinitionId, NodeType.TASK, sequence);
    }

    public static NodeDefinitionBuilder builderTaskNode(String tenantId, Integer workflowDefinitionId) {
        return builderTaskNode(tenantId, workflowDefinitionId, 0);
    }

    public NodeDefinitionBuilder name(String name) {
        this.nodeDefinition.setName(name);
        return this;
    }

    public NodeDefinitionBuilder nodeType(NodeType nodeType) {
        this.nodeDefinition.setNodeType(nodeType);
        return this;
    }

    public NodeDefinitionBuilder approveType(ApproveType approveType) {
        this.nodeDefinition.setApproveType(approveType);
        return this;
    }

    public NodeDefinitionBuilder desc(String desc) {
        this.nodeDefinition.setDesc(desc);
        return this;
    }

    public NodeDefinitionBuilder conditions(Conditions conditions) {
        this.nodeDefinition.setConditions(conditions);
        return this;
    }

    public NodeDefinitionBuilder approverId(String approverId) {
        return approver(Approver.of(approverId));
    }

    public NodeDefinitionBuilder approverIds(Set<String> approverIds) {
        Assert.notEmpty(approverIds, "approverIds cannot be empty");
        Set<Approver> approvers = approverIds.stream().map(Approver::of).collect(Collectors.toSet());
        return this.approvers(approvers);
    }

    public NodeDefinitionBuilder approver(Approver approver) {
        // 判断是否是动态指定审批人
        if (this.nodeDefinition.isDynamic()) {
            this.nodeDefinition.setDynamic(false);
            Set<Approver> approvers = this.nodeDefinition.getApprovers();
            Set<Approver> dynamicAssignmentApprovers = approvers.stream().filter(i -> i.getId().startsWith(WorkflowConstants.DYNAMIC_ASSIGNMENT_APPROVER_VALUE_PREFIX)).collect(Collectors.toSet());
            // 删除所有动态指定的审批人
            if (!dynamicAssignmentApprovers.isEmpty()) {
                approvers.removeAll(dynamicAssignmentApprovers);
            }
        }
        this.nodeDefinition.getApprovers().add(approver);
        return this;
    }

    public NodeDefinitionBuilder approvers(Set<Approver> approvers) {
        Assert.notEmpty(approvers, "approvers cannot be empty");
        // 判断是否是动态指定审批人
        if (this.nodeDefinition.isDynamic()) {
            this.nodeDefinition.setDynamic(false);
        }
        this.nodeDefinition.setApprovers(new LinkedHashSet<>(approvers));
        return this;
    }

    public NodeDefinitionBuilder dynamicAssignmentApproversNum(int approverNum) {
        this.nodeDefinition.getApprovers().clear();
        // 未指定动态审批人数量
        if (approverNum < 0) {
            approverNum = -1;
            this.nodeDefinition.getApprovers().add(Approver.of(WorkflowConstants.DYNAMIC_ASSIGNMENT_APPROVER_VALUE.formatted(approverNum)));
        }
        // 指定了动态审批人的数量
        else if (approverNum >= 1) {
            for (int i = 0; i < approverNum; i++) {
                this.nodeDefinition.getApprovers().add(Approver.of(WorkflowConstants.DYNAMIC_ASSIGNMENT_APPROVER_VALUE.formatted(i)));
            }
        }
        if (approverNum != 0) {
            this.nodeDefinition.setDynamic(true);
            this.nodeDefinition.setDynamicAssignmentNum(approverNum);
        }
        // approverNum = 0 则表示是空审批节点，需要将流程定义的 allowEmptyAutoApprove 设置为 true，表示允许空节点审批
        return this;
    }

    public NodeDefinitionBuilder roleApprove(boolean roleApprove) {
        this.nodeDefinition.setRoleApprove(roleApprove);
        return this;
    }

    public NodeDefinitionBuilder roleApproveType(RoleApproveType roleApproveType) {
        this.nodeDefinition.setRoleApproveType(roleApproveType);
        return this;
    }

    public NodeDefinitionBuilder roleUserApproveType(RoleUserApproveType roleUserApproveType) {
        this.nodeDefinition.setRoleUserApproveType(roleUserApproveType);
        return this;
    }

    public NodeDefinitionBuilder roleApprover(RoleApprover roleApprover) {
        this.nodeDefinition.getRoleApprovers().add(roleApprover);
        return this;
    }

    public NodeDefinitionBuilder roleApprovers(Set<RoleApprover> roleApprovers) {
        this.nodeDefinition.setRoleApprovers(roleApprovers);
        return this;
    }

    public NodeDefinitionBuilder roleApprovers(List<Set<RoleApprover>> roleApprovers) {
        Set<RoleApprover> sets = new LinkedHashSet<>();
        for (Set<RoleApprover> roleApproverSet : roleApprovers) {
            sets.addAll(roleApproverSet);
        }
        this.nodeDefinition.setRoleApprovers(sets);
        return this;
    }

    public NodeDefinition build() {
        String tenantId = this.nodeDefinition.getTenantId();
        Integer workflowDefinitionId = this.nodeDefinition.getWorkflowDefinitionId();
        NodeType nodeType = this.nodeDefinition.getNodeType();
        if (tenantId == null || workflowDefinitionId == null || nodeType == null) {
            throw new WorkflowException("tenantId, workflowDefinitionId, nodeType cannot be null");
        }
        return super.build(this.nodeDefinition);
    }
}
