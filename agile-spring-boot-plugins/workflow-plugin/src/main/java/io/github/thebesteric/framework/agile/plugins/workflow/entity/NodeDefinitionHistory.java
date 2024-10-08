package io.github.thebesteric.framework.agile.plugins.workflow.entity;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityClass;
import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityColumn;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.DMLOperator;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serial;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * NodeDefinitionHistory
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-10-08 12:01:33
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@EntityClass(value = "awf_node_definition_history", comment = "节点定义历史记录")
public class NodeDefinitionHistory extends BaseEntity {
    @Serial
    private static final long serialVersionUID = -4297734273149296023L;

    @EntityColumn(name = "tenant_id", length = 32, nullable = false, comment = "租户 ID")
    private String tenantId;

    @EntityColumn(name = "node_def_id", nullable = false, comment = "节点定义 ID")
    private Integer nodeDefinitionId;

    @EntityColumn(type = EntityColumn.Type.TINY_INT, nullable = false, comment = "操作类型")
    private DMLOperator dmlOperator;

    @EntityColumn(name = "before_obj", type = EntityColumn.Type.JSON, comment = "修改前的流程定义")
    private NodeDefinition beforeObj;

    @EntityColumn(name = "current_obj", type = EntityColumn.Type.JSON, comment = "修改后的流程定义")
    private NodeDefinition currentObj;

    public static NodeDefinitionHistory of(ResultSet rs) throws SQLException {
        NodeDefinitionHistory history = new NodeDefinitionHistory();
        history.setTenantId(rs.getString("tenant_id"));
        history.setNodeDefinitionId(rs.getInt("node_def_id"));
        history.setDmlOperator(DMLOperator.of(rs.getInt("dml_operator")));
        String beforeStr = rs.getString("before_obj");
        if (CharSequenceUtil.isNotEmpty(beforeStr)) {
            history.setBeforeObj(JSONUtil.toBean(beforeStr, NodeDefinition.class));
        }
        String afterStr = rs.getString("current_obj");
        if (CharSequenceUtil.isNotEmpty(afterStr)) {
            history.setCurrentObj(JSONUtil.toBean(afterStr, NodeDefinition.class));
        }
        return of(history, rs);
    }
}
