package io.github.thebesteric.framework.agile.plugins.workflow.domain;

import io.github.thebesteric.framework.agile.plugins.workflow.constant.Operator;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "字段名")
    private String key;
    @Schema(description = "值")
    private String value;
    @Schema(description = "操作符")
    private Operator operator;
    @Schema(description = "描述")
    private String desc;

    public static Condition of(String key, String value, Operator operator) {
        return of(key, value, operator, "%s %s %s".formatted(key, operator.getValue(), value));
    }

    public static Condition of(String key, String value, Operator operator, String desc) {
        Condition condition = new Condition();
        condition.setKey(key);
        condition.setValue(value);
        condition.setOperator(operator);
        condition.setDesc(desc);
        return condition;
    }
}
