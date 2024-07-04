package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.definition;

import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.NodeDefinition;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * NodeDefinitionExecBuilder
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-18 15:58:40
 */
public class NodeDefinitionExecutorBuilder extends AbstractExecutorBuilder<NodeDefinition> {

    private final NodeDefinitionExecutor nodeDefinitionExecutor;

    private NodeDefinitionExecutorBuilder(JdbcTemplate jdbcTemplate) {
        this.nodeDefinitionExecutor = new NodeDefinitionExecutor(jdbcTemplate);
    }

    public static NodeDefinitionExecutorBuilder builder(JdbcTemplate jdbcTemplate) {
        return new NodeDefinitionExecutorBuilder(jdbcTemplate);
    }

    public NodeDefinitionExecutorBuilder id(Integer id) {
        nodeDefinitionExecutor.getNodeDefinition().setId(id);
        return this;
    }

    public NodeDefinitionExecutorBuilder tenantId(String tenantId) {
        nodeDefinitionExecutor.getNodeDefinition().setTenantId(tenantId);
        return this;
    }

    public NodeDefinitionExecutorBuilder workflowDefinitionId(Integer workflowDefinitionId) {
        nodeDefinitionExecutor.getNodeDefinition().setWorkflowDefinitionId(workflowDefinitionId);
        return this;
    }

    public NodeDefinitionExecutorBuilder nodeDefinition(NodeDefinition nodeDefinition) {
        nodeDefinitionExecutor.setNodeDefinition(nodeDefinition);
        return this;
    }

    public NodeDefinitionExecutor build() {
        super.setDefaultEntityAttrs(nodeDefinitionExecutor.getNodeDefinition());
        return nodeDefinitionExecutor;
    }
}
