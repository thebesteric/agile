package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.assignment;

import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.NodeRoleAssignment;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * NodeRoleAssignmentBuilder
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-19 18:29:15
 */
public class NodeRoleAssignmentBuilder extends AbstractBuilder<NodeRoleAssignment> {

    private final NodeRoleAssignment nodeRoleUserAssignment;

    public static final AtomicInteger userSeq = new AtomicInteger(1);
    public static final AtomicInteger roleSeq = new AtomicInteger(1);

    private NodeRoleAssignmentBuilder(NodeRoleAssignment nodeRoleUserAssignment) {
        this.nodeRoleUserAssignment = nodeRoleUserAssignment;
    }

    public static NodeRoleAssignmentBuilder builder(String tenantId, Integer nodeDefinitionId) {
        NodeRoleAssignmentBuilder builder = new NodeRoleAssignmentBuilder(new NodeRoleAssignment());
        builder.nodeRoleUserAssignment.setTenantId(tenantId);
        builder.nodeRoleUserAssignment.setNodeDefinitionId(nodeDefinitionId);
        return builder;
    }

    public NodeRoleAssignmentBuilder userId(Integer seq, String userId, String userDesc) {
        this.nodeRoleUserAssignment.setUserId(userId);
        this.nodeRoleUserAssignment.setUserDesc(userDesc);
        this.nodeRoleUserAssignment.setUserSeq(seq);
        return this;
    }

    public NodeRoleAssignmentBuilder roleId(Integer seq, String roleId, String roleDesc) {
        this.nodeRoleUserAssignment.setRoleId(roleId);
        this.nodeRoleUserAssignment.setRoleDesc(roleDesc);
        this.nodeRoleUserAssignment.setRoleSeq(seq);
        return this;
    }

    public NodeRoleAssignment build() {
        return super.build(this.nodeRoleUserAssignment);
    }

    public static void resetUserSeq() {
        userSeq.set(1);
    }

    public static void resetRoleSeq() {
        roleSeq.set(1);
    }

    public static void resetSeq() {
        userSeq.set(1);
        roleSeq.set(1);
    }
}
