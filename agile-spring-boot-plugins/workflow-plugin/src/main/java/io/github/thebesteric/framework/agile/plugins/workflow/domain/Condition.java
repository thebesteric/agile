package io.github.thebesteric.framework.agile.plugins.workflow.domain;

import io.github.thebesteric.framework.agile.plugins.workflow.constant.Operator;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 条件
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-13 11:11:19
 */
@Data
public class Condition implements Serializable {
    @Serial
    private static final long serialVersionUID = 5707313372537270971L;

    private String key;
    private String value;
    private Operator operator;

    public static Condition of(String key, String value, Operator operator) {
        Condition condition = new Condition();
        condition.setKey(key);
        condition.setValue(value);
        condition.setOperator(operator);
        return condition;
    }
}
