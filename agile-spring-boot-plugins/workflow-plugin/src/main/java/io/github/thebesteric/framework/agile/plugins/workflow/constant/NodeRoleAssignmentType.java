package io.github.thebesteric.framework.agile.plugins.workflow.constant;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.github.thebesteric.framework.agile.core.domain.BaseEnum;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum NodeRoleAssignmentType implements BaseEnum {

    NORMAL(1, "正常", "正常角色用户"),
    REASSIGN(2, "转派", "转派角色用户");

    @JsonValue
    @EnumValue
    private final Integer code;
    private final String name;
    private final String desc;

    NodeRoleAssignmentType(Integer code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    @JsonCreator
    public static NodeRoleAssignmentType of(Integer code) {
        return Arrays.stream(NodeRoleAssignmentType.values()).filter(i -> ObjectUtil.equals(i.getCode(), code)).findFirst().orElse(null);
    }

}
