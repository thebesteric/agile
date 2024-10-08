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
 * 流程定义历史记录
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-09-30 17:42:57
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@EntityClass(value = "awf_wf_history", comment = "工作流历史记录")
public class WorkflowDefinitionHistory extends BaseEntity {
    @Serial
    private static final long serialVersionUID = -5033662513587365393L;

    @EntityColumn(name = "tenant_id", length = 32, nullable = false, comment = "租户 ID")
    private String tenantId;

    @EntityColumn(name = "wf_def_id", nullable = false, comment = "流程定义 ID")
    private Integer workflowDefinitionId;

    @EntityColumn(type = EntityColumn.Type.TINY_INT, nullable = false, comment = "操作类型")
    private DMLOperator dmlOperator;

    @EntityColumn(name = "before_obj", type = EntityColumn.Type.JSON, comment = "修改前的流程定义")
    private WorkflowDefinition beforeObj;

    @EntityColumn(name = "current_obj", type = EntityColumn.Type.JSON, comment = "修改后的流程定义")
    private WorkflowDefinition currentObj;

    public static WorkflowDefinitionHistory of(ResultSet rs) throws SQLException {
        WorkflowDefinitionHistory history = new WorkflowDefinitionHistory();
        history.setTenantId(rs.getString("tenant_id"));
        history.setWorkflowDefinitionId(rs.getInt("wf_def_id"));
        history.setDmlOperator(DMLOperator.of(rs.getInt("dml_operator")));
        String beforeStr = rs.getString("before_obj");
        if (CharSequenceUtil.isNotEmpty(beforeStr)) {
            history.setBeforeObj(JSONUtil.toBean(beforeStr, WorkflowDefinition.class));
        }
        String afterStr = rs.getString("current_obj");
        if (CharSequenceUtil.isNotEmpty(afterStr)) {
            history.setCurrentObj(JSONUtil.toBean(afterStr, WorkflowDefinition.class));
        }
        return of(history, rs);
    }

}
