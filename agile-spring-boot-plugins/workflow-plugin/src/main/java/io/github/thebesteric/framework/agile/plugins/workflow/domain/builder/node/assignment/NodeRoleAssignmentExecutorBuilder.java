package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.assignment;

import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.NodeRoleAssignment;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * NodeAssignmentExecutorBuilder
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-18 15:58:40
 */
public class NodeRoleAssignmentExecutorBuilder extends AbstractExecutorBuilder<NodeRoleAssignment> {

    private final NodeRoleAssignmentExecutor nodeRoleAssignmentExecutor;

    private NodeRoleAssignmentExecutorBuilder(JdbcTemplate jdbcTemplate) {
        this.nodeRoleAssignmentExecutor = new NodeRoleAssignmentExecutor(jdbcTemplate);
    }

    public static NodeRoleAssignmentExecutorBuilder builder(JdbcTemplate jdbcTemplate) {
        return new NodeRoleAssignmentExecutorBuilder(jdbcTemplate);
    }

    public NodeRoleAssignmentExecutorBuilder nodeRoleUserAssignment(NodeRoleAssignment nodeRoleUserAssignment) {
        nodeRoleAssignmentExecutor.setNodeRoleAssignment(nodeRoleUserAssignment);
        return this;
    }

    public NodeRoleAssignmentExecutorBuilder tenantId(String tenantId) {
        nodeRoleAssignmentExecutor.getNodeRoleAssignment().setTenantId(tenantId);
        return this;
    }

    public NodeRoleAssignmentExecutorBuilder userId(String userId) {
        nodeRoleAssignmentExecutor.getNodeRoleAssignment().setUserId(userId);
        return this;
    }

    public NodeRoleAssignmentExecutorBuilder nodeDefinitionId(Integer nodeDefinitionId) {
        nodeRoleAssignmentExecutor.getNodeRoleAssignment().setNodeDefinitionId(nodeDefinitionId);
        return this;
    }

    public NodeRoleAssignmentExecutor build() {
        super.setDefaultEntityAttrs(nodeRoleAssignmentExecutor.getNodeRoleAssignment());
        return nodeRoleAssignmentExecutor;
    }
}
