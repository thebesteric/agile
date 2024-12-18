package io.github.thebesteric.framework.agile.plugins.workflow.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 被转派人信息
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-11-07 15:32:57
 */
@Data
public class Invitee implements Serializable {
    @Serial
    private static final long serialVersionUID = -3846539328354530647L;

    @Schema(description = "审批角色唯一标识")
    private String roleId;
    @Schema(description = "审批角色名称")
    private String roleName;
    @Schema(description = "审批角色描述")
    private String roleDesc;

    @Schema(description = "审批人唯一标识")
    private String userId;
    @Schema(description = "审批人名称")
    private String userName;
    @Schema(description = "审批人描述")
    private String userDesc;

    public static Invitee of(String userId, String userName) {
        return Invitee.of(userId, userName, null);
    }

    public static Invitee of(String userId, String userName, String userDesc) {
        return Invitee.of(null, null, null, userId, userName, userDesc);
    }

    public static Invitee of(String roleId, String roleName, String userId, String userName) {
        return Invitee.of(roleId, roleName, null, userId, userName, null);
    }

    public static Invitee of(String roleId, String roleName, String roleDesc, String userId, String userName, String userDesc) {
        Invitee reassigner = new Invitee();
        reassigner.roleId = roleId;
        reassigner.roleName = roleName;
        reassigner.roleDesc = roleDesc;
        reassigner.userId = userId;
        reassigner.userName = userName;
        reassigner.userDesc = userDesc;
        return reassigner;
    }


}
