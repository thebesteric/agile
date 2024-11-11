package io.github.thebesteric.framework.agile.plugins.workflow.entity;

import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityClass;
import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityColumn;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.NodeRoleAssignmentType;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.Invitee;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.Inviter;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.base.BaseEntity;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serial;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 角色用户任务关联表
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-09-12 20:08:47
 */
@ToString(callSuper = true)
@Data
@EntityClass(value = "awf_node_role_assignment", comment = "节点角色用户定义表")
public class NodeRoleAssignment extends BaseEntity {
    @Serial
    private static final long serialVersionUID = -1812264740243827643L;

    @EntityColumn(name = "tenant_id", length = 32, nullable = false, comment = "租户 ID")
    private String tenantId;

    @EntityColumn(name="node_def_id", nullable = false, comment = "节点定义 ID")
    private Integer nodeDefinitionId;

    @EntityColumn(name = "role_id", length = 32, nullable = false, comment = "角色 ID")
    private String roleId;

    @EntityColumn(name = "role_name", length = 64, comment = "角色名称")
    private String roleName;

    @EntityColumn(name = "role_seq", type = EntityColumn.Type.SMALL_INT, comment = "角色审批顺序")
    private Integer roleSeq;

    @EntityColumn(name = "role_desc", comment = "角色描述")
    private String roleDesc;

    @EntityColumn(name = "user_id", length = 32, nullable = false, comment = "用户 ID")
    private String userId;

    @EntityColumn(name = "user_name", length = 64, comment = "用户 ID")
    private String userName;

    @EntityColumn(name = "user_seq", type = EntityColumn.Type.SMALL_INT, comment = "角色用户审批顺序")
    private Integer userSeq;

    @EntityColumn(name = "user_desc", comment = "用户描述")
    private String userDesc;

    @EntityColumn(type = EntityColumn.Type.TINY_INT, nullable = false, comment = "角色用户类型")
    private NodeRoleAssignmentType type = NodeRoleAssignmentType.NORMAL;

    public static NodeRoleAssignment reassignFrom(NodeDefinition nodeDefinition, TaskApprove taskApprove, Inviter inviter, Invitee invitee) {
        NodeRoleAssignment nodeRoleAssignment = new NodeRoleAssignment();
        nodeRoleAssignment.setTenantId(taskApprove.getTenantId());
        nodeRoleAssignment.setNodeDefinitionId(nodeDefinition.getId());
        nodeRoleAssignment.setRoleId(invitee.getRoleId());
        nodeRoleAssignment.setRoleName(invitee.getRoleName());
        nodeRoleAssignment.setRoleDesc(invitee.getRoleDesc());
        nodeRoleAssignment.setRoleSeq(inviter.getRoleSeq());
        nodeRoleAssignment.setUserId(invitee.getUserId());
        nodeRoleAssignment.setUserName(invitee.getUserName());
        nodeRoleAssignment.setUserDesc(invitee.getUserDesc());
        nodeRoleAssignment.setUserSeq(inviter.getUserSeq());
        nodeRoleAssignment.setType(NodeRoleAssignmentType.REASSIGN);
        return nodeRoleAssignment;
    }

    public static NodeRoleAssignment of(ResultSet rs) throws SQLException {
        NodeRoleAssignment nodeRoleAssignment = new NodeRoleAssignment();
        nodeRoleAssignment.setTenantId(rs.getString("tenant_id"));
        nodeRoleAssignment.setNodeDefinitionId(rs.getInt("node_def_id"));
        nodeRoleAssignment.setRoleId(rs.getString("role_id"));
        nodeRoleAssignment.setRoleName(rs.getString("role_name"));
        nodeRoleAssignment.setRoleDesc(rs.getString("role_desc"));
        nodeRoleAssignment.setUserId(rs.getString("user_id"));
        nodeRoleAssignment.setUserName(rs.getString("user_name"));
        nodeRoleAssignment.setUserDesc(rs.getString("user_desc"));
        nodeRoleAssignment.setType(NodeRoleAssignmentType.of(rs.getInt("type")));
        // 解决 rs.getInt("xxx") null 值会返回 0 的问题
        Object userSeqObject = rs.getObject("user_seq");
        if (userSeqObject != null) {
            nodeRoleAssignment.setUserSeq((Integer) userSeqObject);
        }
        Object roleSeqObject = rs.getObject("role_seq");
        if (roleSeqObject != null) {
            nodeRoleAssignment.setRoleSeq((Integer) roleSeqObject);
        }
        return of(nodeRoleAssignment, rs);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeRoleAssignment that = (NodeRoleAssignment) o;
        return new EqualsBuilder().appendSuper(super.equals(o)).append(tenantId, that.tenantId).append(nodeDefinitionId, that.nodeDefinitionId).append(roleId, that.roleId).append(userId, that.userId).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).appendSuper(super.hashCode()).append(tenantId).append(nodeDefinitionId).append(roleId).append(userId).toHashCode();
    }
}
