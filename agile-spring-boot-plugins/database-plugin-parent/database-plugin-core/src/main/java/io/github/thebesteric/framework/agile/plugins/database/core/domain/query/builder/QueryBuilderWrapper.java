package io.github.thebesteric.framework.agile.plugins.database.core.domain.query.builder;

import io.github.thebesteric.framework.agile.commons.util.LoggerPrinter;
import io.github.thebesteric.framework.agile.commons.util.MapWrapper;
import io.github.thebesteric.framework.agile.core.domain.Pair;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.ColumnDomain;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.EntityClassDomain;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.*;
import io.github.thebesteric.framework.agile.plugins.database.core.exception.QueryParseException;
import lombok.SneakyThrows;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * QueryWrapper
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-01 20:44:32
 */
public class QueryBuilderWrapper {

    private static final LoggerPrinter loggerPrinter = LoggerPrinter.newInstance();

    private static final String QUERY_PARSE_ERROR_MESSAGE = "Failed to extract field name";


    public static class Builder<T> {
        private final Class<T> clazz;
        private final Field[] fields;
        private final EntityClassDomain entityClassDomain;
        private final List<ColumnDomain> columnDomains;

        private final List<QueryParam> queryParams;
        private final List<OrderByParam> orderByParams;
        private Pager pager;
        private final boolean toUnderline;

        public Builder(Class<T> clazz, boolean toUnderline) {
            this.clazz = clazz;
            this.fields = clazz.getDeclaredFields();
            this.entityClassDomain = EntityClassDomain.of(this.clazz);
            this.columnDomains = Arrays.stream(this.fields).map(field -> ColumnDomain.of(this.entityClassDomain.getTableName(), field)).toList();
            this.queryParams = new ArrayList<>();
            this.orderByParams = new ArrayList<>();
            this.toUnderline = toUnderline;
        }

        public QueryBuilderWrapper.Builder<T> addQueryParam(String key, QueryOperator queryOperator, Object value) {
            queryParams.add(QueryParam.of(key, queryOperator, value, toUnderline));
            return this;
        }

        public QueryBuilderWrapper.Builder<T> addQueryParam(MapWrapper.SFunction<T, ?> getter, QueryOperator queryOperator, Object value) {
            try {
                String fieldName = getFieldName(getter);
                queryParams.add(QueryParam.of(fieldName, queryOperator, value, toUnderline));
            } catch (Exception e) {
                throw new QueryParseException(QUERY_PARSE_ERROR_MESSAGE, e);
            }
            return this;
        }

        public QueryBuilderWrapper.Builder<T> addOrderByParam(String key, OrderByOperator orderByOperator) {
            orderByParams.add(OrderByParam.of(key, orderByOperator, toUnderline));
            return this;
        }

        public QueryBuilderWrapper.Builder<T> addOrderByParam(MapWrapper.SFunction<T, ?> getter, OrderByOperator orderByOperator) {
            try {
                String fieldName = getFieldName(getter);
                orderByParams.add(OrderByParam.of(fieldName, orderByOperator, toUnderline));
            } catch (Exception e) {
                throw new QueryParseException(QUERY_PARSE_ERROR_MESSAGE, e);
            }
            return this;
        }

        public QueryBuilderWrapper.Builder<T> eq(String key, Object value) {
            return this.eq(true, key, value);
        }

        public QueryBuilderWrapper.Builder<T> eq(MapWrapper.SFunction<T, ?> getter, Object value) {
            return this.eq(true, getter, value);
        }

        public QueryBuilderWrapper.Builder<T> eq(boolean flag, String key, Object value) {
            if (flag) {
                queryParams.add(QueryParam.of(key, QueryOperator.EQUAL, value, toUnderline));
            }
            return this;
        }

        public QueryBuilderWrapper.Builder<T> eq(boolean flag, MapWrapper.SFunction<T, ?> getter, Object value) {
            return tryWith(() -> {
                String fieldName = getFieldName(getter);
                this.eq(flag, fieldName, value);
            });
        }

        public QueryBuilderWrapper.Builder<T> ne(String key, Object value) {
            return this.ne(true, key, value);
        }

        public QueryBuilderWrapper.Builder<T> ne(MapWrapper.SFunction<T, ?> getter, Object value) {
            return this.ne(true, getter, value);
        }

        public QueryBuilderWrapper.Builder<T> ne(boolean flag, String key, Object value) {
            if (flag) {
                queryParams.add(QueryParam.of(key, QueryOperator.NOT_EQUAL, value, toUnderline));
            }
            return this;
        }

        public QueryBuilderWrapper.Builder<T> ne(boolean flag, MapWrapper.SFunction<T, ?> getter, Object value) {
            return tryWith(() -> {
                String fieldName = getFieldName(getter);
                this.ne(flag, fieldName, value);
            });
        }

        public QueryBuilderWrapper.Builder<T> gt(String key, Object value) {
            return this.gt(true, key, value);
        }

        public QueryBuilderWrapper.Builder<T> gt(MapWrapper.SFunction<T, ?> getter, Object value) {
            return this.gt(true, getter, value);
        }

        public QueryBuilderWrapper.Builder<T> gt(boolean flag, String key, Object value) {
            if (flag) {
                queryParams.add(QueryParam.of(key, QueryOperator.GREATER_THAN, value, toUnderline));
            }
            return this;
        }

        public QueryBuilderWrapper.Builder<T> gt(boolean flag, MapWrapper.SFunction<T, ?> getter, Object value) {
            return tryWith(() -> {
                String fieldName = getFieldName(getter);
                this.gt(flag, fieldName, value);
            });
        }

        public QueryBuilderWrapper.Builder<T> lt(String key, Object value) {
            return this.lt(true, key, value);
        }

        public QueryBuilderWrapper.Builder<T> lt(MapWrapper.SFunction<T, ?> getter, Object value) {
            return this.lt(true, getter, value);
        }

        public QueryBuilderWrapper.Builder<T> lt(boolean flag, String key, Object value) {
            if (flag) {
                queryParams.add(QueryParam.of(key, QueryOperator.LESS_THAN, value, toUnderline));
            }
            return this;
        }

        public QueryBuilderWrapper.Builder<T> lt(boolean flag, MapWrapper.SFunction<T, ?> getter, Object value) {
            return tryWith(() -> {
                String fieldName = getFieldName(getter);
                this.lt(flag, fieldName, value);
            });
        }

        public QueryBuilderWrapper.Builder<T> gte(String key, Object value) {
            return this.gte(true, key, value);
        }

        public QueryBuilderWrapper.Builder<T> gte(MapWrapper.SFunction<T, ?> getter, Object value) {
            return this.gte(true, getter, value);
        }

        public QueryBuilderWrapper.Builder<T> gte(boolean flag, String key, Object value) {
            if (flag) {
                queryParams.add(QueryParam.of(key, QueryOperator.GREATER_THAN_OR_EQUAL, value, toUnderline));
            }
            return this;
        }

        public QueryBuilderWrapper.Builder<T> gte(boolean flag, MapWrapper.SFunction<T, ?> getter, Object value) {
            return tryWith(() -> {
                String fieldName = getFieldName(getter);
                this.gte(flag, fieldName, value);
            });
        }

        public QueryBuilderWrapper.Builder<T> lte(String key, Object value) {
            return this.lte(true, key, value);
        }

        public QueryBuilderWrapper.Builder<T> lte(MapWrapper.SFunction<T, ?> getter, Object value) {
            return this.lte(true, getter, value);
        }

        public QueryBuilderWrapper.Builder<T> lte(boolean flag, String key, Object value) {
            if (flag) {
                queryParams.add(QueryParam.of(key, QueryOperator.LESS_THAN_OR_EQUAL, value, toUnderline));
            }
            return this;
        }

        public QueryBuilderWrapper.Builder<T> lte(boolean flag, MapWrapper.SFunction<T, ?> getter, Object value) {
            return tryWith(() -> {
                String fieldName = getFieldName(getter);
                this.lte(flag, fieldName, value);
            });
        }

        public QueryBuilderWrapper.Builder<T> like(String key, Object value) {
            return this.like(true, key, value);
        }

        public QueryBuilderWrapper.Builder<T> like(MapWrapper.SFunction<T, ?> getter, Object value) {
            return this.like(true, getter, value);
        }

        public QueryBuilderWrapper.Builder<T> like(boolean flag, String key, Object value) {
            if (flag) {
                queryParams.add(QueryParam.of(key, QueryOperator.LIKE, value, toUnderline));
            }
            return this;
        }

        public QueryBuilderWrapper.Builder<T> like(boolean flag, MapWrapper.SFunction<T, ?> getter, Object value) {
            return tryWith(() -> {
                String fieldName = getFieldName(getter);
                this.like(flag, fieldName, value);
            });
        }

        public QueryBuilderWrapper.Builder<T> notLike(String key, Object value) {
            return this.notLike(true, key, value);
        }

        public QueryBuilderWrapper.Builder<T> notLike(MapWrapper.SFunction<T, ?> getter, Object value) {
            return this.notLike(true, getter, value);
        }

        public QueryBuilderWrapper.Builder<T> notLike(boolean flag, String key, Object value) {
            if (flag) {
                queryParams.add(QueryParam.of(key, QueryOperator.NOT_LIKE, value, toUnderline));
            }
            return this;
        }

        public QueryBuilderWrapper.Builder<T> notLike(boolean flag, MapWrapper.SFunction<T, ?> getter, Object value) {
            return tryWith(() -> {
                String fieldName = getFieldName(getter);
                this.notLike(flag, fieldName, value);
            });
        }

        public QueryBuilderWrapper.Builder<T> in(String key, List<Object> values) {
            return this.in(true, key, values);
        }

        public QueryBuilderWrapper.Builder<T> in(MapWrapper.SFunction<T, ?> getter, List<Object> values) {
            return this.in(true, getter, values);
        }

        public QueryBuilderWrapper.Builder<T> in(boolean flag, String key, List<Object> values) {
            if (flag) {
                queryParams.add(QueryParam.of(key, QueryOperator.IN, values, toUnderline));
            }
            return this;
        }

        public QueryBuilderWrapper.Builder<T> in(boolean flag, MapWrapper.SFunction<T, ?> getter, List<Object> values) {
            return tryWith(() -> {
                String fieldName = getFieldName(getter);
                this.in(flag, fieldName, values);
            });
        }

        public QueryBuilderWrapper.Builder<T> notIn(String key, List<Object> values) {
            return this.notIn(true, key, values);
        }

        public QueryBuilderWrapper.Builder<T> notIn(MapWrapper.SFunction<T, ?> getter, List<Object> values) {
            return this.notIn(true, getter, values);
        }

        public QueryBuilderWrapper.Builder<T> notIn(boolean flag, String key, List<Object> values) {
            if (flag) {
                queryParams.add(QueryParam.of(key, QueryOperator.NOT_IN, values, toUnderline));
            }
            return this;
        }

        public QueryBuilderWrapper.Builder<T> notIn(boolean flag, MapWrapper.SFunction<T, ?> getter, List<Object> values) {
            return tryWith(() -> {
                String fieldName = getFieldName(getter);
                this.notIn(flag, fieldName, values);
            });
        }

        public QueryBuilderWrapper.Builder<T> between(String key, Object left, Object right) {
            return this.between(true, key, left, right);
        }

        public QueryBuilderWrapper.Builder<T> between(MapWrapper.SFunction<T, ?> getter, Object left, Object right) {
            return this.between(true, getter, left, right);
        }

        public QueryBuilderWrapper.Builder<T> between(boolean flag, String key, Object left, Object right) {
            if (flag) {
                Pair<Object, Object> value = Pair.of(left, right);
                queryParams.add(QueryParam.of(key, QueryOperator.BETWEEN, value, toUnderline));
            }
            return this;
        }

        public QueryBuilderWrapper.Builder<T> between(boolean flag, MapWrapper.SFunction<T, ?> getter, Object left, Object right) {
            return tryWith(() -> {
                String fieldName = getFieldName(getter);
                this.between(flag, fieldName, left, right);
            });
        }

        public QueryBuilderWrapper.Builder<T> notBetween(String key, Object left, Object right) {
            return this.notBetween(true, key, left, right);
        }

        public QueryBuilderWrapper.Builder<T> notBetween(MapWrapper.SFunction<T, ?> getter, Object left, Object right) {
            return this.notBetween(true, getter, left, right);
        }

        public QueryBuilderWrapper.Builder<T> notBetween(boolean flag, String key, Object left, Object right) {
            if (flag) {
                Pair<Object, Object> value = Pair.of(left, right);
                queryParams.add(QueryParam.of(key, QueryOperator.NOT_BETWEEN, value, toUnderline));
            }
            return this;
        }

        public QueryBuilderWrapper.Builder<T> notBetween(boolean flag, MapWrapper.SFunction<T, ?> getter, Object left, Object right) {
            return tryWith(() -> {
                String fieldName = getFieldName(getter);
                this.notBetween(flag, fieldName, left, right);
            });
        }

        public QueryBuilderWrapper.Builder<T> orderBy(String key) {
            return this.orderBy(key, OrderByOperator.DESC);
        }

        public QueryBuilderWrapper.Builder<T> orderBy(MapWrapper.SFunction<T, ?> getter) {
            return this.orderBy(getter, OrderByOperator.DESC);
        }

        public QueryBuilderWrapper.Builder<T> orderBy(String key, OrderByOperator orderByOperator) {
            return this.orderBy(true, key, orderByOperator);
        }

        public QueryBuilderWrapper.Builder<T> orderBy(MapWrapper.SFunction<T, ?> getter, OrderByOperator orderByOperator) {
            return this.orderBy(true, getter, orderByOperator);
        }

        public QueryBuilderWrapper.Builder<T> orderBy(boolean flag, String key, OrderByOperator orderByOperator) {
            if (flag) {
                orderByParams.add(OrderByParam.of(key, orderByOperator, toUnderline));
            }
            return this;
        }

        public QueryBuilderWrapper.Builder<T> orderBy(boolean flag, MapWrapper.SFunction<T, ?> getter, OrderByOperator orderByOperator) {
            return tryWith(() -> {
                String fieldName = getFieldName(getter);
                this.orderBy(flag, fieldName, orderByOperator);
            });
        }

        public QueryBuilderWrapper.Builder<T> page(Integer page, Integer pageSize) {
            pager = Pager.of(page, pageSize);
            return this;
        }


        private QueryBuilderWrapper.Builder<T> tryWith(Runnable runnable) {
            try {
                runnable.run();
            } catch (Exception e) {
                loggerPrinter.error(QUERY_PARSE_ERROR_MESSAGE, e.getMessage(), e);
                throw new QueryParseException(QUERY_PARSE_ERROR_MESSAGE, e);
            }
            return this;
        }

        @SneakyThrows
        private String getFieldName(MapWrapper.SFunction<T, ?> getter) {
            Method method = getter.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(true);
            SerializedLambda serializedLambda = (SerializedLambda) method.invoke(getter);
            String implMethodName = serializedLambda.getImplMethodName();
            String fieldName = implMethodName.startsWith("get") ? implMethodName.substring(3) : implMethodName.substring(2);
            String lowCaseFieldName = Character.toLowerCase(fieldName.charAt(0)) + fieldName.substring(1);
            ColumnDomain columnDomain = columnDomains.stream().filter(c -> lowCaseFieldName.equals(c.getFieldName())).findFirst().orElse(null);
            if (columnDomain != null) {
                return columnDomain.getName();
            }
            return lowCaseFieldName;
        }

        public Query build() {
            return Query.of(queryParams, orderByParams, pager);
        }
    }

    public static <T> QueryBuilderWrapper.Builder<T> createLambda(Class<T> clazz) {
        return createLambda(clazz, true);
    }

    public static <T> QueryBuilderWrapper.Builder<T> createLambda(Class<T> clazz, boolean toUnderline) {
        return new Builder<>(clazz, toUnderline);
    }


    @FunctionalInterface
    public interface SFunction<T, R> extends Function<T, R>, Serializable {
    }
}
