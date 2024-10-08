package io.github.thebesteric.framework.agile.plugins.workflow.constant;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.github.thebesteric.framework.agile.core.domain.BaseEnum;
import lombok.Getter;

import java.util.Arrays;

/**
 * DMLOperator
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-09-30 17:48:51
 */
@Getter
public enum DMLOperator implements BaseEnum {
    INSERT(1, "插入数据"),
    UPDATE(2, "更新数据"),
    DELETE(3, "删除数据");

    @JsonValue
    @EnumValue
    private final Integer code;
    private final String desc;

    DMLOperator(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @JsonCreator
    public static DMLOperator of(Integer code) {
        return Arrays.stream(DMLOperator.values()).filter(i -> ObjectUtil.equals(i.getCode(), code)).findFirst().orElse(null);
    }
}