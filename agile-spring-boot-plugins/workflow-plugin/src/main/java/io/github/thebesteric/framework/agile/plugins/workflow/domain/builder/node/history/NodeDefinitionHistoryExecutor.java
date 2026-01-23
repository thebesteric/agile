package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.history;

import io.github.thebesteric.framework.agile.core.domain.page.PagingResponse;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.NodeDefinitionHistory;
import lombok.Getter;
import lombok.Setter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;

/**
 * NodeDefinitionHistoryExecutor
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-10-08 14:26:39
 */
@Getter
@Setter
public class NodeDefinitionHistoryExecutor extends AbstractExecutor<NodeDefinitionHistory> {

    private NodeDefinitionHistory nodeDefinitionHistory;

    public NodeDefinitionHistoryExecutor(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
        this.nodeDefinitionHistory = new NodeDefinitionHistory();
    }

    /**
     * 根据流程定义 ID 查找节点历史记录（分页）
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     * @param page                 当前页
     * @param pageSize             每页显示数量
     *
     * @return PagingResponse
     *
     * @author wangweijun
     * @since 2024/10/8 16:22
     */
    public PagingResponse<NodeDefinitionHistory> findNodeHistoriesByWorkflowDefinitionId(String tenantId, Integer workflowDefinitionId, Integer page, Integer pageSize) {
        String selectSql = """
                SELECT t.* FROM awf_node_definition_history t
                LEFT JOIN awf_node_definition nd ON nd.id = t.node_def_id
                LEFT JOIN awf_wf_definition wfd ON wfd.id = nd.wf_def_id
                WHERE wfd.tenant_id = %s AND wfd.id = %s
                """.formatted(tenantId, workflowDefinitionId);

        final String countSql = "SELECT COUNT(*) FROM (" + selectSql + ") AS t";
        Integer count = this.jdbcTemplate.queryForObject(countSql, Integer.class);

        selectSql += " ORDER BY t.`id` DESC, t.`created_at` DESC LIMIT ? OFFSET ?";
        Integer offset = (page - 1) * pageSize;
        RowMapper<NodeDefinitionHistory> rowMapper = (rs, rowNum) -> NodeDefinitionHistory.of(rs);
        List<NodeDefinitionHistory> records = this.jdbcTemplate.query(selectSql, rowMapper, pageSize, offset);

        return PagingResponse.of(page, pageSize, count, records);
    }
}
