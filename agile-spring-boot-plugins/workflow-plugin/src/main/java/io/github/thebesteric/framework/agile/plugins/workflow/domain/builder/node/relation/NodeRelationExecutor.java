package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.relation;

import io.github.thebesteric.framework.agile.commons.exception.DataExistsException;
import io.github.thebesteric.framework.agile.plugins.workflow.config.AgileWorkflowContext;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.NodeRelation;
import io.vavr.control.Try;
import lombok.Getter;
import lombok.Setter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * NodeRelationExecutor
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-19 11:00:17
 */
@Getter
@Setter
public class NodeRelationExecutor extends AbstractExecutor<NodeRelation> {

    private NodeRelation nodeRelation;

    public NodeRelationExecutor(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
        this.nodeRelation = new NodeRelation();
    }

    public NodeRelation save() {
        // 检查是否有已存在的节点定义
        NodeRelation existsNodeRelation = this.getByWorkflowDefinitionIdAndFromNodeAndToNode();
        if (existsNodeRelation != null) {
            throw new DataExistsException("NodeRelation already exists");
        }
        return super.save(nodeRelation);
    }

    /**
     * 根据流程定义 ID、来自节点 ID、目标节点 ID 查询节点关系
     *
     * @return NodeRelation
     *
     * @author wangweijun
     * @since 2024/6/24 15:41
     */
    public NodeRelation getByWorkflowDefinitionIdAndFromNodeAndToNode() {
        String tenantId = nodeRelation.getTenantId();
        Integer workflowDefinitionId = nodeRelation.getWorkflowDefinitionId();
        Integer fromNodeId = nodeRelation.getFromNodeId();
        Integer toNodeId = nodeRelation.getToNodeId();
        return getByWorkflowDefinitionIdAndFromNodeAndToNode(tenantId, workflowDefinitionId, fromNodeId, toNodeId);
    }

    /**
     * 根据流程定义 ID、来自节点 ID、目标节点 ID 查询节点关系
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     * @param fromNodeId           来自节点 ID
     * @param toNodeId             目标节点 ID
     *
     * @return NodeRelation
     *
     * @author wangweijun
     * @since 2024/6/24 15:41
     */
    public NodeRelation getByWorkflowDefinitionIdAndFromNodeAndToNode(String tenantId, Integer workflowDefinitionId, Integer fromNodeId, Integer toNodeId) {
        final String selectSql = """
                SELECT * FROM awf_node_relation WHERE `active` = 1 AND `tenant_id` = ? AND `wf_def_id` = ? AND `from_node_id` = ? AND `to_node_id` = ? AND `state` = 1
                """;
        return Try.of(() -> this.jdbcTemplate.queryForObject(selectSql, (rs, rowNum) -> NodeRelation.of(rs), tenantId, workflowDefinitionId, fromNodeId, toNodeId)).getOrNull();
    }

    /**
     * 根据来自节点 ID 查询节点关系（存在多个的情况是，有条件参数）
     *
     * @param tenantId   租户 ID
     * @param fromNodeId 来自节点 ID
     *
     * @return List<NodeRelation>
     *
     * @author wangweijun
     * @since 2024/6/25 14:12
     */
    public List<NodeRelation> findByFromNodeId(String tenantId, Integer fromNodeId) {
        final String selectSql = """
                SELECT * FROM awf_node_relation WHERE `active` = 1 AND `tenant_id` = ? AND `from_node_id` = ?
                """;
        RowMapper<NodeRelation> rowMapper = (ResultSet rs, int rowNum) -> NodeRelation.of(rs);
        return this.jdbcTemplate.query(selectSql, rowMapper, tenantId, fromNodeId).stream().toList();
    }

    /**
     * 根据来自节点 ID 查询节点关系（存在多个的情况是，有条件参数）
     *
     * @param tenantId    租户 ID
     * @param fromNodeIds 来自节点 IDs
     *
     * @return List<NodeRelation>
     *
     * @author wangweijun
     * @since 2024/6/25 14:12
     */
    public List<NodeRelation> findByFromNodeIds(String tenantId, List<Integer> fromNodeIds) {
        final String selectSql = """
                SELECT * FROM awf_node_relation WHERE `active` = 1 AND `tenant_id` = ? AND find_in_set(`from_node_id`, ?)
                """;
        RowMapper<NodeRelation> rowMapper = (ResultSet rs, int rowNum) -> NodeRelation.of(rs);
        String ids = fromNodeIds.stream().map(Object::toString).collect(Collectors.joining(","));
        return this.jdbcTemplate.query(selectSql, rowMapper, tenantId, ids).stream().toList();
    }

    /**
     * 根据目标节点 ID 查询节点关系
     *
     * @param tenantId 租户 ID
     * @param toNodeId 来自节点 ID
     *
     * @return List<NodeRelation>
     *
     * @author wangweijun
     * @since 2024/6/25 14:12
     */
    public List<NodeRelation> findByToNodeId(String tenantId, Integer toNodeId) {
        final String selectSql = """
                SELECT * FROM awf_node_relation WHERE `active` = 1 AND `to_node_id` = ?
                """;
        RowMapper<NodeRelation> rowMapper = (ResultSet rs, int rowNum) -> NodeRelation.of(rs);
        return this.jdbcTemplate.query(selectSql, rowMapper, tenantId, toNodeId).stream().toList();
    }

    /**
     * 根据流程定义 ID 将节点关系设置为无效
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     *
     * @author wangweijun
     * @since 2024/7/3 14:55
     */
    public void inactiveByWorkflowDefinitionId(String tenantId, Integer workflowDefinitionId) {
        final String updateSql = """
                UPDATE awf_node_relation set `active` = 0, `version` = `version` + 1, `updated_at` = ?, `updated_by` = ? WHERE `tenant_id` = ? AND `wf_def_id` = ?
                """;
        jdbcTemplate.update(updateSql, new Date(), AgileWorkflowContext.getCurrentUser(), tenantId, workflowDefinitionId);
    }
}
