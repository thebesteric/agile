package io.github.thebesteric.framework.agile.plugins.workflow.domain;

import io.github.thebesteric.framework.agile.plugins.workflow.entity.NodeAssignment;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.NodeRoleAssignment;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.TaskReassignRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 转派人信息
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-11-07 15:32:57
 */
@Data
public class Inviter implements Serializable {
    @Serial
    private static final long serialVersionUID = 4421963956820738071L;

    @Schema(description = "审批角色唯一标识")
    private String roleId;
    @Schema(description = "审批角色名称")
    private String roleName;
    @Schema(description = "审批角色描述")
    private String roleDesc;
    @Schema(description = "审批角色排序")
    private Integer roleSeq;

    @Schema(description = "审批人唯一标识")
    private String userId;
    @Schema(description = "审批人名称")
    private String userName;
    @Schema(description = "审批人描述")
    private String userDesc;
    @Schema(description = "审批人排序")
    private Integer userSeq;

    public static Inviter of(NodeRoleAssignment nodeRoleAssignment) {
        return Inviter.of(nodeRoleAssignment.getRoleId(), nodeRoleAssignment.getRoleName(), nodeRoleAssignment.getRoleDesc(), nodeRoleAssignment.getRoleSeq(),
                nodeRoleAssignment.getUserId(), nodeRoleAssignment.getUserName(), nodeRoleAssignment.getUserDesc(), nodeRoleAssignment.getUserSeq());
    }

    public static Inviter of(NodeAssignment nodeAssignment) {
        return Inviter.of(nodeAssignment.getApproverId(), nodeAssignment.getApproverName(), nodeAssignment.getApproverDesc(), nodeAssignment.getApproverSeq());
    }

    public static Inviter of(TaskReassignRecord taskReassignRecord) {
        return Inviter.of(taskReassignRecord.getToRoleId(), taskReassignRecord.getToRoleName(), taskReassignRecord.getToRoleDesc(), taskReassignRecord.getToRoleSeq(),
                taskReassignRecord.getToUserId(), taskReassignRecord.getToUserName(), taskReassignRecord.getToUserDesc(), taskReassignRecord.getToUserSeq());
    }

    public static Inviter of(String userId, String userName) {
        return Inviter.of(userId, userName, null, null);
    }

    public static Inviter of(String userId, String userName, Integer userSeq) {
        return Inviter.of(userId, userName, null, userSeq);
    }

    public static Inviter of(String userId, String userName, String userDesc, Integer userSeq) {
        return Inviter.of(null, null, null, null, userId, userName, userDesc, userSeq);
    }

    public static Inviter of(String roleId, String roleName, Integer roleSeq, String userId, String userName, Integer userSeq) {
        return Inviter.of(roleId, roleName, null, roleSeq, userId, userName, null, userSeq);
    }

    public static Inviter of(String roleId, String roleName, String roleDesc, Integer roleSeq, String userId, String userName, String userDesc, Integer userSeq) {
        Inviter inviter = new Inviter();
        inviter.roleId = roleId;
        inviter.roleName = roleName;
        inviter.roleDesc = roleDesc;
        inviter.roleSeq = roleSeq;
        inviter.userId = userId;
        inviter.userName = userName;
        inviter.userDesc = userDesc;
        inviter.userSeq = userSeq;
        return inviter;
    }


}
