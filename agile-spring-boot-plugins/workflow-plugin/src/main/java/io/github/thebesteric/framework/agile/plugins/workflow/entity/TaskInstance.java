package io.github.thebesteric.framework.agile.plugins.workflow.entity;

import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityClass;
import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityColumn;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.NodeStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serial;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 任务实例表
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-11 21:26:37
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@EntityClass(value = "awf_task_instance", comment = "任务实例表")
public class TaskInstance extends BaseEntity {
    @Serial
    private static final long serialVersionUID = -4054589633782074802L;

    @EntityColumn(name = "tenant_id", length = 32, nullable = false, comment = "租户 ID")
    private String tenantId;

    @EntityColumn(name = "wf_inst_id", nullable = false, comment = "流程实例 ID")
    private Integer workflowInstanceId;

    @EntityColumn(name = "node_def_id", nullable = false, comment = "节点定义 ID")
    private Integer nodeDefinitionId;

    @EntityColumn(type = EntityColumn.Type.TINY_INT, nullable = false, comment = "节点状态")
    private NodeStatus status;

    @EntityColumn(comment = "审批人数（已完成）")
    private Integer approvedCount;

    @EntityColumn(comment = "总需要审批的人数")
    private Integer totalCount;

    public static TaskInstance of(ResultSet rs) throws SQLException {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setTenantId(rs.getString("tenant_id"));
        taskInstance.setWorkflowInstanceId(rs.getInt("wf_inst_id"));
        taskInstance.setNodeDefinitionId(rs.getInt("node_def_id"));
        taskInstance.setStatus(NodeStatus.of(rs.getInt("status")));
        taskInstance.setApprovedCount(rs.getInt("approved_count"));
        taskInstance.setTotalCount(rs.getInt("total_count"));
        return of(taskInstance, rs);
    }
}
