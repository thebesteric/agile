package io.github.thebesteric.framework.agile.plugins.database.core.domain.query.builder;

import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.*;

import java.util.ArrayList;
import java.util.List;

/**
 * QueryParamsBuilder
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-01 19:11:17
 */
public class QueryBuilder {

    private final List<QueryParam> queryParams;
    private final List<OrderByParam> orderByParams;
    private Pager pager;


    private final boolean toUnderline;

    private QueryBuilder(List<QueryParam> queryParams, List<OrderByParam> orderByParams, boolean toUnderline) {
        this.queryParams = queryParams;
        this.orderByParams = orderByParams;
        this.toUnderline = toUnderline;
    }

    public static QueryBuilder create() {
        return create(true);
    }

    public static QueryBuilder create(boolean toUnderline) {
        return new QueryBuilder(new ArrayList<>(), new ArrayList<>(), toUnderline);
    }

    public QueryBuilder eq(String key, Object value) {
        queryParams.add(QueryParam.of(key, QueryOperator.EQUAL, value, toUnderline));
        return this;
    }

    public QueryBuilder ne(String key, Object value) {
        queryParams.add(QueryParam.of(key, QueryOperator.NOT_EQUAL, value, toUnderline));
        return this;
    }

    public QueryBuilder gt(String key, Object value) {
        queryParams.add(QueryParam.of(key, QueryOperator.GREATER_THAN, value, toUnderline));
        return this;
    }

    public QueryBuilder lt(String key, Object value) {
        queryParams.add(QueryParam.of(key, QueryOperator.LESS_THAN, value, toUnderline));
        return this;
    }

    public QueryBuilder gte(String key, Object value) {
        queryParams.add(QueryParam.of(key, QueryOperator.GREATER_THAN_OR_EQUAL, value, toUnderline));
        return this;
    }

    public QueryBuilder lte(String key, Object value) {
        queryParams.add(QueryParam.of(key, QueryOperator.LESS_THAN_OR_EQUAL, value, toUnderline));
        return this;
    }

    public QueryBuilder like(String key, Object value) {
        queryParams.add(QueryParam.of(key, QueryOperator.LIKE, value, toUnderline));
        return this;
    }

    public QueryBuilder notLike(String key, Object value) {
        queryParams.add(QueryParam.of(key, QueryOperator.NOT_LIKE, value, toUnderline));
        return this;
    }

    public QueryBuilder in(String key, Object value) {
        queryParams.add(QueryParam.of(key, QueryOperator.IN, value, toUnderline));
        return this;
    }

    public QueryBuilder notIn(String key, Object value) {
        queryParams.add(QueryParam.of(key, QueryOperator.NOT_IN, value, toUnderline));
        return this;
    }

    public QueryBuilder between(String key, Object value) {
        queryParams.add(QueryParam.of(key, QueryOperator.BETWEEN, value, toUnderline));
        return this;
    }

    public QueryBuilder notBetween(String key, Object value) {
        queryParams.add(QueryParam.of(key, QueryOperator.NOT_BETWEEN, value, toUnderline));
        return this;
    }

    public QueryBuilder orderBy(String key) {
        return this.orderBy(key, OrderByOperator.DESC);
    }

    public QueryBuilder orderBy(String key, OrderByOperator orderByOperator) {
        orderByParams.add(OrderByParam.of(key, orderByOperator, toUnderline));
        return this;
    }

    public QueryBuilder page(Integer page, Integer pageSize) {
        pager = Pager.of(page, pageSize);
        return this;
    }

    public Query build() {
        return Query.of(queryParams, orderByParams, pager);
    }

}
