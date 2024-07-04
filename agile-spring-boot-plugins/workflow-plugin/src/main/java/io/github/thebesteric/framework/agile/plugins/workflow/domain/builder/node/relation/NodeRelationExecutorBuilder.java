package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.relation;

import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.NodeRelation;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * NodeRelationExecutorBuilder
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-19 10:59:29
 */
public class NodeRelationExecutorBuilder extends AbstractExecutorBuilder<NodeRelation> {

    private final NodeRelationExecutor nodeRelationExecutor;

    private NodeRelationExecutorBuilder(JdbcTemplate jdbcTemplate) {
        this.nodeRelationExecutor = new NodeRelationExecutor(jdbcTemplate);
    }

    public static NodeRelationExecutorBuilder builder(JdbcTemplate jdbcTemplate) {
        return new NodeRelationExecutorBuilder(jdbcTemplate);
    }

    public NodeRelationExecutorBuilder tenantId(String tenantId) {
        nodeRelationExecutor.getNodeRelation().setTenantId(tenantId);
        return this;
    }

    public NodeRelationExecutorBuilder id(Integer id) {
        nodeRelationExecutor.getNodeRelation().setId(id);
        return this;
    }

    public NodeRelationExecutorBuilder nodeRelation(NodeRelation nodeRelation) {
        nodeRelationExecutor.setNodeRelation(nodeRelation);
        return this;
    }

    public NodeRelationExecutor build() {
        super.setDefaultEntityAttrs(nodeRelationExecutor.getNodeRelation());
        return nodeRelationExecutor;
    }
}
