package io.github.thebesteric.framework.agile.plugins.workflow.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.WorkflowConstants;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.NodeAssignment;
import lombok.Data;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serial;
import java.io.Serializable;

/**
 * 审批人信息
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-09-03 15:34:34
 */
@Data
public class Approver implements Serializable {
    @Serial
    private static final long serialVersionUID = -3246995224601408891L;

    /** 审批人唯一标识 */
    private String id;
    /** 审批人名称 */
    private String name;
    /** 审批人描述 */
    private String desc;

    /** 是否是角色 */
    private boolean roleType = false;

    public static Approver of(String id, String name, String desc, boolean roleType) {
        Approver approver = new Approver();
        approver.id = id;
        approver.name = name;
        approver.desc = desc;
        approver.roleType = roleType;
        return approver;
    }

    public static Approver of(String id, String name, String desc) {
        return Approver.of(id, name, desc, false);
    }

    public static Approver of(String id, String name) {
        return Approver.of(id, name, null);
    }

    public static Approver of(String id) {
        return Approver.of(id, null, null);
    }

    public static Approver of(NodeAssignment nodeAssignment, boolean roleType) {
        return Approver.of(nodeAssignment.getApproverId(), nodeAssignment.getApproverName(), nodeAssignment.getApproverDesc(), roleType);
    }

    /**
     * 是否未设置指定审批人
     *
     * @return boolean
     *
     * @author wangweijun
     * @since 2024/9/9 13:40
     */
    @JsonIgnore
    public boolean isUnSettingAssignmentApprover() {
        return this.id.startsWith(WorkflowConstants.DYNAMIC_ASSIGNMENT_APPROVER_VALUE_PREFIX) && this.id.endsWith(WorkflowConstants.DYNAMIC_ASSIGNMENT_APPROVER_VALUE_SUFFIX);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Approver approver = (Approver) o;
        return new EqualsBuilder().append(id, approver.id).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id).toHashCode();
    }
}
