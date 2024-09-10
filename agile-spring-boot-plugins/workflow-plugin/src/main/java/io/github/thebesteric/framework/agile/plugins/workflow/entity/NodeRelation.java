package io.github.thebesteric.framework.agile.plugins.workflow.entity;

import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityClass;
import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityColumn;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.ActiveStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serial;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 节点关系表
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-11 21:14:06
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@EntityClass(value = "awf_node_relation", comment = "节点定义表")
public class NodeRelation extends BaseEntity {
    @Serial
    private static final long serialVersionUID = -840332836046120858L;

    @EntityColumn(name = "tenant_id", length = 32, nullable = false, comment = "租户 ID")
    private String tenantId;

    @EntityColumn(name="wf_def_id", nullable = false, comment = "流程定义 ID")
    private Integer workflowDefinitionId;

    @EntityColumn(name = "from_node_id", nullable = false, comment = "来自节点 ID")
    private Integer fromNodeId;

    @EntityColumn(name = "to_node_id", nullable = false, comment = "目标节点 ID")
    private Integer toNodeId;

    @EntityColumn(nullable = false, length = 12, precision = 2, comment = "节点顺序")
    private Double sequence;

    @EntityColumn(type = EntityColumn.Type.TINY_INT, nullable = false, comment = "活动状态")
    private ActiveStatus active = ActiveStatus.ACTIVE;

    public static NodeRelation of(ResultSet rs) throws SQLException {
        NodeRelation nodeRelation = new NodeRelation();
        nodeRelation.setTenantId(rs.getString("tenant_id"));
        nodeRelation.setWorkflowDefinitionId(rs.getInt("wf_def_id"));
        nodeRelation.setWorkflowDefinitionId(rs.getInt("wf_def_id"));
        nodeRelation.setFromNodeId(rs.getInt("from_node_id"));
        nodeRelation.setToNodeId(rs.getInt("to_node_id"));
        nodeRelation.setSequence(rs.getDouble("sequence"));
        nodeRelation.setActive(ActiveStatus.of(rs.getInt("active")));
        return of(nodeRelation, rs);
    }
}
