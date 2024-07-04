package io.github.thebesteric.framework.agile.plugins.workflow.entity;

import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityClass;
import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityColumn;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serial;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 用户任务关联表
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-12 17:14:53
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@EntityClass(value = "awf_node_assignment", comment = "用户任务关联表")
public class NodeAssignment extends BaseEntity {
    @Serial
    private static final long serialVersionUID = -7417403195737955414L;

    @EntityColumn(name = "tenant_id", length = 32, nullable = false, comment = "租户 ID")
    private String tenantId;

    @EntityColumn(name="node_def_id", nullable = false, comment = "节点定义 ID")
    private Integer nodeDefinitionId;

    @EntityColumn(length = 32, nullable = false, comment = "用户 ID")
    private String userId;

    public static NodeAssignment of(ResultSet rs) throws SQLException {
        NodeAssignment nodeAssignment = new NodeAssignment();
        nodeAssignment.setTenantId(rs.getString("tenant_id"));
        nodeAssignment.setNodeDefinitionId(rs.getInt("node_def_id"));
        nodeAssignment.setUserId(rs.getString("user_id"));
        return of(nodeAssignment, rs);
    }
}
