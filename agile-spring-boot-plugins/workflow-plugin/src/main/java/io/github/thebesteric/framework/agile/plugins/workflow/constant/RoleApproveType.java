package io.github.thebesteric.framework.agile.plugins.workflow.constant;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.github.thebesteric.framework.agile.core.domain.BaseEnum;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum RoleApproveType implements BaseEnum {

    ANY(1, "或签", "表示角多个角色中，其中一个角色审核通过即可"),
    ALL(2, "会签", "表示角多个角色中，每个角色都需要完成审批"),
    SEQ(3, "顺签", "表示角多个角色中，每个角色按照顺序依次完成审批");

    @JsonValue
    @EnumValue
    private final Integer code;
    private final String name;
    private final String desc;

    RoleApproveType(Integer code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    @JsonCreator
    public static RoleApproveType of(Integer code) {
        return Arrays.stream(RoleApproveType.values()).filter(i -> ObjectUtil.equals(i.getCode(), code)).findFirst().orElse(null);
    }

}
