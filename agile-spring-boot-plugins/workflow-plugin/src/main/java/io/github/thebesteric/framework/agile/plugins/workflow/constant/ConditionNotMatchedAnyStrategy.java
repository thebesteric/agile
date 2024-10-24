package io.github.thebesteric.framework.agile.plugins.workflow.constant;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.github.thebesteric.framework.agile.core.domain.BaseEnum;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum ConditionNotMatchedAnyStrategy implements BaseEnum {


    PROCESS_APPROVED(1, "流程直接同意"),
    PROCESS_REJECTED(2, "流程直接拒绝"),
    PROCESS_CONTINUE_TO_NEXT(3, "流程跳转到下一个节点"),
    PROCESS_THROW_EXCEPTION(9, "直接抛出异常");



    @JsonValue
    @EnumValue
    private final Integer code;
    private final String desc;

    ConditionNotMatchedAnyStrategy(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @JsonCreator
    public static ConditionNotMatchedAnyStrategy of(Integer code) {
        return Arrays.stream(values()).filter(i -> ObjectUtil.equals(i.getCode(), code)).findFirst().orElse(null);
    }
}
