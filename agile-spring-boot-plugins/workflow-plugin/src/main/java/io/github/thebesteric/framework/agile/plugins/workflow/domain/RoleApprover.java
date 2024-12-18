package io.github.thebesteric.framework.agile.plugins.workflow.domain;

import io.github.thebesteric.framework.agile.plugins.workflow.entity.NodeRoleAssignment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 角色审批人信息
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-09-12 20:44:17
 */
@Data
public class RoleApprover implements Serializable {
    @Serial
    private static final long serialVersionUID = -461153416109482175L;

    @Schema(description = "角色唯一标识")
    private String roleId;
    @Schema(description = "角色名称")
    private String roleName;
    @Schema(description = "角色描述")
    private String roleDesc;
    @Schema(description = "审批人唯一标识")
    private String userId;
    @Schema(description = "审批人名称")
    private String userName;
    @Schema(description = "审批人描述")
    private String userDesc;

    public static RoleApprover of(NodeRoleAssignment nodeRoleAssignment) {
        RoleApprover roleApprover = new RoleApprover();
        roleApprover.roleId = nodeRoleAssignment.getRoleId();
        roleApprover.roleName = nodeRoleAssignment.getRoleName();
        roleApprover.roleDesc = nodeRoleAssignment.getRoleDesc();
        roleApprover.userId = nodeRoleAssignment.getUserId();
        roleApprover.userName = nodeRoleAssignment.getUserName();
        roleApprover.userDesc = nodeRoleAssignment.getUserDesc();
        return roleApprover;
    }

    public static RoleApprover of(String roleId, String roleName, String roleDesc, Approver approver) {
        RoleApprover roleApprover = new RoleApprover();
        roleApprover.roleId = roleId;
        roleApprover.roleName = roleName;
        roleApprover.roleDesc = roleDesc;
        roleApprover.userId = approver.getId();
        roleApprover.userName = approver.getName();
        roleApprover.userDesc = approver.getDesc();
        return roleApprover;
    }

    public static RoleApprover of(String roleId, String roleName, Approver approver) {
        return RoleApprover.of(roleId, roleName, null, approver);
    }

    public static RoleApprover of(String roleId, Approver approver) {
        return RoleApprover.of(roleId, null, null, approver);
    }

    public static Set<RoleApprover> of(String roleId, String roleName, String roleDesc, Set<Approver> approvers) {
        Set<RoleApprover> roleApprovers = new LinkedHashSet<>();
        for (Approver approver : approvers) {
            roleApprovers.add(RoleApprover.of(roleId, roleName, roleDesc, approver));
        }
        return roleApprovers;
    }

    public static RoleApprover of(ApproverRole approverRole, Approver approver) {
        return RoleApprover.of(approverRole.id, approverRole.name, approverRole.desc, approver);
    }

    public static Set<RoleApprover> of(String roleId, String roleName, Set<Approver> approvers) {
        return RoleApprover.of(roleId, roleName, null, approvers);
    }

    public static Set<RoleApprover> of(String roleId, Set<Approver> approvers) {
        return RoleApprover.of(roleId, null, approvers);
    }

    public static Set<RoleApprover> of(ApproverRole approverRole, Set<Approver> approvers) {
        return RoleApprover.of(approverRole.id, approverRole.name, approverRole.desc, approvers);
    }

    public static Set<RoleApprover> of(Map<ApproverRole, Set<Approver>> multiRoleApprovers) {
        Set<RoleApprover> roleApprovers = new HashSet<>();
        for (Map.Entry<ApproverRole, Set<Approver>> entry : multiRoleApprovers.entrySet()) {
            ApproverRole approverRole = entry.getKey();
            Set<Approver> approvers = entry.getValue();
            roleApprovers.addAll(RoleApprover.of(approverRole.id, approverRole.name, approverRole.desc, approvers));
        }
        return roleApprovers;
    }

    @Data
    public static class ApproverRole {
        private String id;
        private String name;
        private String desc;

        public static ApproverRole of(String id, String name, String desc) {
            ApproverRole approverRole = new ApproverRole();
            approverRole.id = id;
            approverRole.name = name;
            approverRole.desc = desc;
            return approverRole;
        }

        public static ApproverRole of(String id, String name) {
            return ApproverRole.of(id, name, null);
        }

        public static ApproverRole of(String id) {
            return ApproverRole.of(id, null, null);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleApprover that = (RoleApprover) o;
        return new EqualsBuilder().append(roleId, that.roleId).append(userId, that.userId).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(roleId).append(userId).toHashCode();
    }

}
