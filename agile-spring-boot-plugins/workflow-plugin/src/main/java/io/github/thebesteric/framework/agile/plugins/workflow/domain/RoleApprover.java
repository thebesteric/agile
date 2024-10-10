package io.github.thebesteric.framework.agile.plugins.workflow.domain;

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

    /** 角色唯一标识 */
    private String roleId;
    /** 角色描述 */
    private String roleDesc;
    /** 审批人唯一标识 */
    private String userId;
    /** 审批人描述 */
    private String userDesc;

    public static RoleApprover of(String roleId, String roleDesc, Approver approver) {
        RoleApprover roleApprover = new RoleApprover();
        roleApprover.roleId = roleId;
        roleApprover.roleDesc = roleDesc;
        roleApprover.userId = approver.getId();
        roleApprover.userDesc = approver.getDesc();
        return roleApprover;
    }

    public static RoleApprover of(String roleId, Approver approver) {
        return RoleApprover.of(roleId, null, approver);
    }

    public static Set<RoleApprover> of(String roleId, Set<Approver> approvers) {
        return RoleApprover.of(roleId, null, approvers);
    }

    public static Set<RoleApprover> of(String roleId, String roleDesc, Set<Approver> approvers) {
        Set<RoleApprover> roleApprovers = new LinkedHashSet<>();
        for (Approver approver : approvers) {
            roleApprovers.add(RoleApprover.of(roleId, roleDesc, approver));
        }
        return roleApprovers;
    }

    public static Set<RoleApprover> of(Map<String, Set<Approver>> multiRoleApprovers) {
        Set<RoleApprover> roleApprovers = new HashSet<>();
        for (Map.Entry<String, Set<Approver>> entry : multiRoleApprovers.entrySet()) {
            String roleId = entry.getKey();
            Set<Approver> approvers = entry.getValue();
            roleApprovers.addAll(RoleApprover.of(roleId, approvers));
        }
        return roleApprovers;
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
