package io.github.thebesteric.framework.agile.plugins.workflow.constant;

import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;

/**
 * 逻辑运算符
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-13 11:37:21
 */
@Getter
public enum LogicOperator {
    AND("and"),
    OR("or"),
    NOT("not");

    @JsonValue
    private final String code;

    LogicOperator(String code) {
        this.code = code;
    }

    @JsonCreator
    public static LogicOperator getByCode(String code) {
        return Arrays.stream(LogicOperator.values()).filter(e -> ObjectUtil.equals(e.getCode(), code)).findFirst().orElse(null);
    }
}
