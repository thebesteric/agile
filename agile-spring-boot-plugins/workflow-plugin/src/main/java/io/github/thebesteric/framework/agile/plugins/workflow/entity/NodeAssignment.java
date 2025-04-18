package io.github.thebesteric.framework.agile.plugins.workflow.entity;

import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityClass;
import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityColumn;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.ApproverIdType;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.WorkflowConstants;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.base.BaseEntity;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.beans.BeanUtils;

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
@ToString(callSuper = true)
@Data
@EntityClass(value = "awf_node_assignment", comment = "节点用户定义表")
public class NodeAssignment extends BaseEntity {
    @Serial
    private static final long serialVersionUID = -7417403195737955414L;

    @EntityColumn(name = "tenant_id", length = 32, nullable = false, comment = "租户 ID")
    private String tenantId;

    @EntityColumn(name = "node_def_id", nullable = false, comment = "节点定义 ID")
    private Integer nodeDefinitionId;

    @EntityColumn(name = "approver_id", length = 32, nullable = false, comment = "审批人 ID")
    private String approverId;

    @EntityColumn(name = "approver_name", length = 64, comment = "审批人名称")
    private String approverName;

    @EntityColumn(name = "approver_seq", type = EntityColumn.Type.SMALL_INT, comment = "审批顺序")
    private Integer approverSeq;

    @EntityColumn(name = "approver_desc", comment = "审批人描述")
    private String approverDesc;

    @EntityColumn(name = "approver_id_type", type = EntityColumn.Type.TINY_INT, nullable = false, comment = "审批人 ID 类型")
    private ApproverIdType approverIdType = ApproverIdType.USER;

    public static NodeAssignment of(ResultSet rs) throws SQLException {
        NodeAssignment nodeAssignment = new NodeAssignment();
        nodeAssignment.setTenantId(rs.getString("tenant_id"));
        nodeAssignment.setNodeDefinitionId(rs.getInt("node_def_id"));
        nodeAssignment.setApproverId(rs.getString("approver_id"));
        nodeAssignment.setApproverName(rs.getString("approver_name"));
        nodeAssignment.setApproverDesc(rs.getString("approver_desc"));
        // 解决 rs.getInt("xxx") null 值会返回 0 的问题
        Object approverSeqObject = rs.getObject("approver_seq");
        if (approverSeqObject != null) {
            nodeAssignment.setApproverSeq((Integer) approverSeqObject);
        }
        nodeAssignment.setApproverIdType(ApproverIdType.of(rs.getInt("approver_id_type")));
        return of(nodeAssignment, rs);
    }

    public static NodeAssignment copyOf(NodeAssignment source) {
        NodeAssignment target = new NodeAssignment();
        BeanUtils.copyProperties(source, target, IGNORE_COPY_FIELD_NAMES);
        return target;
    }

    /**
     * 获取动态审批人数量
     *
     * @return Integer
     *
     * @author wangweijun
     * @since 2025/1/17 11:56
     */
    public Integer getDynamicApproverNum() {
        if (approverId.startsWith(WorkflowConstants.DYNAMIC_ASSIGNMENT_APPROVER_VALUE_PREFIX)) {
            String nextPart = this.approverId.split(":")[1];
            return Integer.valueOf(nextPart.substring(0, nextPart.length() - 1));
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeAssignment that = (NodeAssignment) o;
        return new EqualsBuilder().appendSuper(super.equals(o)).append(id, that.id).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).appendSuper(super.hashCode()).append(id).toHashCode();
    }
}
