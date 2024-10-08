package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.history;

import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractExecutorBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.NodeDefinitionHistory;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * NodeDefinitionHistoryExecutorBuilder
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-10-08 14:26:39
 */
public class NodeDefinitionHistoryExecutorBuilder extends AbstractExecutorBuilder<NodeDefinitionHistory> {

    private final NodeDefinitionHistoryExecutor nodeDefinitionHistoryExecutor;

    public NodeDefinitionHistoryExecutorBuilder(JdbcTemplate jdbcTemplate) {
        this.nodeDefinitionHistoryExecutor = new NodeDefinitionHistoryExecutor(jdbcTemplate);
    }

    public static NodeDefinitionHistoryExecutorBuilder builder(JdbcTemplate jdbcTemplate) {
        return new NodeDefinitionHistoryExecutorBuilder(jdbcTemplate);
    }

    public NodeDefinitionHistoryExecutor build() {
        super.setDefaultEntityAttrs(this.nodeDefinitionHistoryExecutor.getNodeDefinitionHistory());
        return this.nodeDefinitionHistoryExecutor;
    }
}
