package io.github.thebesteric.framework.agile.plugins.workflow.constant;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.github.thebesteric.framework.agile.core.domain.BaseEnum;
import lombok.Getter;

import java.util.Arrays;

/**
 * 审批类型
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-11 21:28:54
 */
@Getter
public enum ApproveType implements BaseEnum {
    ANY(1, "或签", "表示审批人中，只需要其中一个完成审批"),
    ALL(2, "会签", "表示审批人中，所有人都需要完成审批"),
    SEQ(3, "顺序签", "表示审批人中，所有人按照顺序依次完成审批");

    @JsonValue
    @EnumValue
    private final Integer code;
    private final String name;
    private final String desc;

    ApproveType(Integer code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    @JsonCreator
    public static ApproveType of(Integer code) {
        return Arrays.stream(ApproveType.values()).filter(i -> ObjectUtil.equals(i.getCode(), code)).findFirst().orElse(null);
    }
}
