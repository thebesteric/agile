package io.github.thebesteric.framework.agile.plugins.workflow.constant;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.github.thebesteric.framework.agile.core.domain.BaseEnum;
import lombok.Getter;

import java.util.Arrays;

/**
 * 审批人 ID 类型
 *
 * @author wangweijun
 * @since 2024/9/13 14:31
 */
@Getter
public enum ApproverIdType implements BaseEnum {

    USER(1, "用户"),
    ROLE(2, "角色");

    @JsonValue
    @EnumValue
    private final Integer code;
    private final String desc;

    ApproverIdType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @JsonCreator
    public static ApproverIdType of(Integer code) {
        return Arrays.stream(ApproverIdType.values()).filter(i -> ObjectUtil.equals(i.getCode(), code)).findFirst().orElse(null);
    }
}
