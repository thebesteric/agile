package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.assignment;

import io.github.thebesteric.framework.agile.plugins.workflow.constant.ApproveType;
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

    public NodeAssignmentBuilder userId(ApproveType approveType, String userId) {
        this.nodeAssignment.setUserId(userId);
        if (ApproveType.SEQ == approveType) {
            this.nodeAssignment.setUserSeq(seq.getAndIncrement());
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
