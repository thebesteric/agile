package io.github.thebesteric.framework.agile.plugins.database.core.domain.query;

import lombok.Data;

/**
 * QueryParam
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-01 19:08:54
 */
@Data
public class QueryParam {

    private String key;
    private QueryOperator queryOperator;
    private Object value;

    private boolean toUnderline;

    private QueryParam(String key, QueryOperator queryOperator, Object value, boolean toUnderline) {
        this.key = key;
        this.queryOperator = queryOperator;
        this.value = value;
        this.toUnderline = toUnderline;
    }

    public static QueryParam of(String key, QueryOperator queryOperator, Object value) {
        return of(key, queryOperator, value, true);
    }

    public static QueryParam of(String key, QueryOperator queryOperator, Object value, boolean toUnderline) {
        return new QueryParam(key, queryOperator, value, toUnderline);
    }
}
