package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.assignment;

import io.github.thebesteric.framework.agile.plugins.workflow.constant.ApproveType;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.ApproverIdType;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.RoleApproveType;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.NodeAssignment;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * NodeAssignmentBuilder
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-19 18:29:15
 */
public class NodeAssignmentBuilder extends AbstractBuilder<NodeAssignment> {

    private final NodeAssignment nodeAssignment;

    private final AtomicInteger seq = new AtomicInteger(1);

    private NodeAssignmentBuilder(NodeAssignment nodeAssignment) {
        this.nodeAssignment = nodeAssignment;
    }

    public static NodeAssignmentBuilder builder(String tenantId, Integer nodeDefinitionId) {
        NodeAssignmentBuilder builder = new NodeAssignmentBuilder(new NodeAssignment());
        builder.nodeAssignment.setTenantId(tenantId);
        builder.nodeAssignment.setNodeDefinitionId(nodeDefinitionId);
        return builder;
    }

    public NodeAssignmentBuilder approverInfo(ApproverIdType approverIdType, ApproveType approveType, RoleApproveType roleApproveType, String approverId, String approverName, String approverDesc) {
        this.nodeAssignment.setApproverIdType(approverIdType);
        this.nodeAssignment.setApproverId(approverId);
        this.nodeAssignment.setApproverName(approverName);
        this.nodeAssignment.setApproverDesc(approverDesc);
        if (ApproveType.SEQ == approveType || RoleApproveType.SEQ == roleApproveType) {
            this.nodeAssignment.setApproverSeq(seq.getAndIncrement());
        }
        return this;
    }

    public NodeAssignment build() {
        return super.build(this.nodeAssignment);
    }

    public void resetSeq() {
        this.seq.set(1);
    }
}
