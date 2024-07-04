package io.github.thebesteric.framework.agile.plugins.database.core.domain.query.builder;

import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.OrderByParam;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.Pager;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.QueryParam;
import lombok.Data;

import java.util.List;

/**
 * Query
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-01 20:13:27
 */
@Data
public class Query {
    private List<QueryParam> queryParams;
    private List<OrderByParam> orderByParams;
    private Pager pager;

    private Query(List<QueryParam> queryParams, List<OrderByParam> orderByParams, Pager pager) {
        this.queryParams = queryParams;
        this.orderByParams = orderByParams;
        this.pager = pager;
    }

    public static Query of(List<QueryParam> queryParams, List<OrderByParam> orderByParams, Pager pager) {
        return new Query(queryParams, orderByParams, pager);
    }

    public static Query of(List<QueryParam> queryParams, List<OrderByParam> orderByParams) {
        return of(queryParams, orderByParams, null);
    }

    public static Query of(List<QueryParam> queryParams) {
        return of(queryParams, null);
    }

}
