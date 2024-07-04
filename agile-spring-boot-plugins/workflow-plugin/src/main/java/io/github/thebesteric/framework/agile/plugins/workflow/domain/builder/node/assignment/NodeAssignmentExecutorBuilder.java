package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.assignment;

import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.NodeAssignment;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * NodeAssignmentExecutorBuilder
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-18 15:58:40
 */
public class NodeAssignmentExecutorBuilder extends AbstractExecutorBuilder<NodeAssignment> {

    private final NodeAssignmentExecutor nodeAssignmentExecutor;

    private NodeAssignmentExecutorBuilder(JdbcTemplate jdbcTemplate) {
        this.nodeAssignmentExecutor = new NodeAssignmentExecutor(jdbcTemplate);
    }

    public static NodeAssignmentExecutorBuilder builder(JdbcTemplate jdbcTemplate) {
        return new NodeAssignmentExecutorBuilder(jdbcTemplate);
    }

    public NodeAssignmentExecutorBuilder nodeAssignment(NodeAssignment nodeAssignment) {
        nodeAssignmentExecutor.setNodeAssignment(nodeAssignment);
        return this;
    }

    public NodeAssignmentExecutorBuilder tenantId(String tenantId) {
        nodeAssignmentExecutor.getNodeAssignment().setTenantId(tenantId);
        return this;
    }

    public NodeAssignmentExecutorBuilder userId(String userId) {
        nodeAssignmentExecutor.getNodeAssignment().setUserId(userId);
        return this;
    }

    public NodeAssignmentExecutorBuilder nodeDefinitionId(Integer nodeDefinitionId) {
        nodeAssignmentExecutor.getNodeAssignment().setNodeDefinitionId(nodeDefinitionId);
        return this;
    }

    public NodeAssignmentExecutor build() {
        super.setDefaultEntityAttrs(nodeAssignmentExecutor.getNodeAssignment());
        return nodeAssignmentExecutor;
    }
}
