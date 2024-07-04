package io.github.thebesteric.framework.agile.plugins.workflow.constant;

import lombok.Getter;

/**
 * 条件操作符
 *
 * @author wangweijun
 * @since 2024/6/13 11:32
 */
@Getter
public enum Operator {
    EQUAL("=="),
    NOT_EQUAL("!="),
    GREATER_THAN(">"),
    GREATER_THAN_AND_EQUAL(">="),
    LESS_THAN("<"),
    LESS_THAN_AND_EQUAL("<="),
    LIKE("%");

    private final String value;

    Operator(String value) {
        this.value = value;
    }
}
