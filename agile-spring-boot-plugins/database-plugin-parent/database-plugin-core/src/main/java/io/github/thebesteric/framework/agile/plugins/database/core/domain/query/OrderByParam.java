package io.github.thebesteric.framework.agile.plugins.database.core.domain.query;

import lombok.Data;

/**
 * OrderByParam
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-01 20:07:00
 */
@Data
public class OrderByParam {
    private String key;
    private OrderByOperator orderByOperator;
    private boolean toUnderline;

    private OrderByParam(String key, OrderByOperator orderByOperator, boolean toUnderline) {
        this.key = key;
        this.orderByOperator = orderByOperator;
        this.toUnderline = toUnderline;
    }

    public static OrderByParam of(String key, OrderByOperator orderByOperator) {
        return of(key, orderByOperator, true);
    }

    public static OrderByParam of(String key, OrderByOperator orderByOperator, boolean toUnderline) {
        return new OrderByParam(key, orderByOperator, toUnderline);
    }
}
