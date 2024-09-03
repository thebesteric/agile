package io.github.thebesteric.framework.agile.plugins.workflow.entity;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityClass;
import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityColumn;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.ApproveType;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.NodeType;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.Approver;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.Conditions;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.Transient;

import java.io.Serial;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 节点定义表
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-11 21:13:45
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@EntityClass(value = "awf_node_definition", comment = "节点定义表")
public class NodeDefinition extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1079297069865230679L;

    @EntityColumn(name = "tenant_id", length = 32, nullable = false, comment = "租户 ID")
    private String tenantId;

    @EntityColumn(name = "wf_def_id", nullable = false, comment = "流程定义 ID")
    private Integer workflowDefinitionId;

    @EntityColumn(length = 32, nullable = false, comment = "流程名称")
    private String name;

    @EntityColumn(type = EntityColumn.Type.TINY_INT, nullable = false, comment = "节点类型")
    private NodeType nodeType;

    @EntityColumn(type = EntityColumn.Type.TINY_INT, nullable = false, comment = "审批类型")
    private ApproveType approveType = ApproveType.ANY;

    @EntityColumn(type = EntityColumn.Type.JSON, comment = "条件定义")
    private Conditions conditions;

    @EntityColumn(nullable = false, comment = "排序")
    private Integer sequence;

    /** 审批人，存储在 NodeAssignment 表中 */
    @Transient
    private Set<Approver> approvers = new LinkedHashSet<>();

    public static NodeDefinition of(ResultSet rs) throws SQLException {
        NodeDefinition nodeDefinition = new NodeDefinition();
        nodeDefinition.setTenantId(rs.getString("tenant_id"));
        nodeDefinition.setWorkflowDefinitionId(rs.getInt("wf_def_id"));
        nodeDefinition.setName(rs.getString("name"));
        nodeDefinition.setNodeType(NodeType.of(rs.getInt("node_type")));
        nodeDefinition.setApproveType(ApproveType.of(rs.getInt("approve_type")));
        String conditionStr = rs.getString("conditions");
        if (CharSequenceUtil.isNotEmpty(conditionStr)) {
            nodeDefinition.setConditions(JSONUtil.toBean(conditionStr, Conditions.class));
        }
        nodeDefinition.setSequence(rs.getInt("sequence"));
        return of(nodeDefinition, rs);
    }
}
