package io.github.thebesteric.framework.agile.core.domain;

import lombok.Data;

/**
 * 单值对象
 *
 * @author wangweijun
 * @since 2024/8/2 17:46
 */
@Data
public class SingleValue {
    private Object value;

    private SingleValue() {
        super();
    }

    public static SingleValue of(Object value) {
        SingleValue singleValue = new SingleValue();
        singleValue.value = value;
        return singleValue;
    }
}