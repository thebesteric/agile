package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder;

import cn.hutool.core.text.CharSequenceUtil;
import io.github.thebesteric.framework.agile.commons.exception.AccessDeniedException;
import io.github.thebesteric.framework.agile.commons.exception.ExecuteErrorException;
import io.github.thebesteric.framework.agile.commons.util.*;
import io.github.thebesteric.framework.agile.core.domain.BaseEnum;
import io.github.thebesteric.framework.agile.core.domain.Pair;
import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityClass;
import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityColumn;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.ColumnDomain;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.Page;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.OrderByParam;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.Pager;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.QueryOperator;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.QueryParam;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.builder.Query;
import io.github.thebesteric.framework.agile.plugins.workflow.config.AgileWorkflowContext;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.base.BaseEntity;
import io.github.thebesteric.framework.agile.plugins.workflow.exception.WorkflowException;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.annotation.Transient;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AbstractExecutor
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-18 16:03:10
 */
@Slf4j
public abstract class AbstractExecutor<T extends BaseEntity> {

    @Transient
    protected final JdbcTemplate jdbcTemplate;

    private final Class<T> type;
    private final String tableName;

    @SuppressWarnings("unchecked")
    protected AbstractExecutor(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        Type superclass = this.getClass().getGenericSuperclass();
        if (superclass instanceof ParameterizedType parameterizedType) {
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            this.type = (Class<T>) actualTypeArguments[0];
            EntityClass entityClass = type.getAnnotation(EntityClass.class);
            tableName = entityClass.value();
        } else {
            throw new AccessDeniedException("AbstractExecutor must be generic");
        }
    }

    /**
     * 根据 ID 查询
     *
     * @param id ID
     *
     * @return entity
     *
     * @author wangweijun
     * @since 2024/6/25 10:20
     */
    @SuppressWarnings("unchecked")
    public T getById(Integer id) {
        Method ofMethod;
        try {
            ofMethod = type.getMethod("of", ResultSet.class);
        } catch (NoSuchMethodException e) {
            LoggerPrinter.error(log, e.getMessage(), e);
            return null;
        }
        final String selectSql = """
                SELECT * FROM %s WHERE `id` = ?
                """.formatted(tableName);
        Method finalOfMethod = ofMethod;
        return Try.of(() -> this.jdbcTemplate.queryForObject(selectSql, (rs, rowNum) -> {
            try {
                return (T) finalOfMethod.invoke(null, rs);
            } catch (Exception e) {
                throw new ExecuteErrorException(e.getMessage(), e);
            }
        }, id)).getOrNull();
    }

    /**
     * 更新实体类，注意：null 值不参与更新
     *
     * @param entity 实体类
     *
     * @author wangweijun
     * @since 2024/6/25 12:04
     */
    public void updateById(T entity) {
        entity.setUpdatedAt(new Date());
        entity.setVersion(entity.getVersion() + 1);
        if (entity.getUpdatedAt() == null) {
            entity.setUpdatedBy(AgileWorkflowContext.getCurrentUser());
        }
        // 获取实体类对应的字段和值
        Pair<List<ColumnDomain>, List<Object>> entityParams = getEntityParams(entity);

        List<ColumnDomain> columnDomains = entityParams.getKey();

        // 组装条件
        StringBuilder setClause = new StringBuilder();
        for (ColumnDomain columnDomain : columnDomains) {
            if (!setClause.isEmpty()) {
                setClause.append(", ");
            }
            setClause.append("`").append(columnDomain.getName()).append("`").append(" = ?");
        }

        // 拼接 SQL
        String updateSql = """
                UPDATE  %s SET %s WHERE id = ?
                """.formatted(tableName, setClause.toString());
        List<Object> args = entityParams.getValue();
        args.add(entity.getId());

        jdbcTemplate.update(updateSql, args.toArray());
    }

    /**
     * 保存实体类
     *
     * @param entity 实体类
     *
     * @author wangweijun
     * @since 2024/6/28 14:04
     */
    public T save(T entity) {
        entity.setUpdatedAt(null);
        entity.setUpdatedBy(null);
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(new Date());
        }
        if (entity.getCreatedBy() == null) {
            entity.setCreatedBy(AgileWorkflowContext.getCurrentUser());
        }
        // 获取实体类对应的字段和值
        Pair<List<ColumnDomain>, List<Object>> entityParams = getEntityParams(entity);

        List<ColumnDomain> columnDomains = entityParams.getKey();

        // 组装条件
        StringBuilder fieldColumns = new StringBuilder();
        for (ColumnDomain columnDomain : columnDomains) {
            if (!fieldColumns.isEmpty()) {
                fieldColumns.append(", ");
            }
            fieldColumns.append("`").append(columnDomain.getName()).append("`");
        }

        // 组装值
        List<Object> args = entityParams.getValue();
        String values = args.stream().map(arg -> {
            if (arg instanceof Boolean) {
                arg = (boolean) arg ? 1 : 0;
            }
            return "'" + arg.toString() + "'";
        }).collect(Collectors.joining(","));

        // 执行 SQL
        String insertSql = """
                INSERT INTO %s (%s) VALUES (%s)
                """.formatted(tableName, fieldColumns.toString(), values);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        int rowsAffected = 0;
        try {
            rowsAffected = this.jdbcTemplate.update(conn -> conn.prepareStatement(insertSql, new String[]{"id"}), keyHolder);
            if (rowsAffected > 0) {
                entity.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
            } else {
                throw new DataIntegrityViolationException("Failed to insert data, no rows affected.");
            }
        } catch (Exception ex) {
            throw new WorkflowException("Failed to insert data: " + ex.getMessage(), ex);
        }
        return entity;
    }

    /**
     * 保存或更新实体类
     *
     * @param entity 实体类
     *
     * @author wangweijun
     * @since 2024/6/28 14:11
     */
    public void saveOrUpdate(T entity) {
        if (entity.getId() != null) {
            updateById(entity);
        } else {
            save(entity);
        }
    }

    /**
     * 根据 ID 删除数据
     *
     * @param id
     *
     * @return 删除的行数
     *
     * @author wangweijun
     * @since 2024/7/3 11:22
     */
    public int deleteById(Integer id) {
        String deleteSql = """
                DELETE FROM %s WHERE id = ?
                """.formatted(tableName);
        return this.jdbcTemplate.update(deleteSql, id);
    }

    /**
     * 删除所有数据
     *
     * @return 删除的行数
     *
     * @author wangweijun
     * @since 2024/7/3 11:22
     */
    public int deleteAll() {
        String deleteSql = """
                DELETE FROM %s
                """.formatted(tableName);
        return this.jdbcTemplate.update(deleteSql);
    }

    /**
     * 根据查询参数删除数据
     *
     * @param query 查询参数
     *
     * @return 删除的行数
     *
     * @author wangweijun
     * @since 2024/7/3 11:22
     */
    public int delete(Query query) {
        String whereClause = getWhereClause(query.getQueryParams());
        String deleteSql = """
                DELETE FROM %s
                """.formatted(tableName);
        if (CharSequenceUtil.isNotEmpty(whereClause)) {
            deleteSql += " WHERE " + whereClause;
        }
        return this.jdbcTemplate.update(deleteSql);
    }

    /**
     * 根据查询参数查询实体类
     *
     * @param query 查询参数
     *
     * @return entity
     *
     * @author wangweijun
     * @since 2024/6/28 18:08
     */
    @SuppressWarnings("unchecked")
    public T get(Query query) {
        query.setPager(null);
        String selectSql = this.queryToSelectSql(query);
        return this.jdbcTemplate.query(selectSql, rs -> {
            try {
                if (!rs.next()) {
                    return null;
                }
                return (T) type.getMethod("of", ResultSet.class).invoke(null, rs);
            } catch (Exception e) {
                throw new ExecuteErrorException(e.getMessage(), e);
            }
        });
    }

    /**
     * 根据查询参数查询实体类
     *
     * @param query 查询参数
     *
     * @return List
     *
     * @author wangweijun
     * @since 2024/6/28 18:08
     */
    @SuppressWarnings("unchecked")
    public Page<T> find(Query query) {
        String selectSql = this.queryToSelectSql(query);
        List<T> records = this.jdbcTemplate.query(selectSql, (rs, rowNum) -> {
            try {
                return (T) type.getMethod("of", ResultSet.class).invoke(null, rs);
            } catch (Exception e) {
                throw new ExecuteErrorException(e.getMessage(), e);
            }
        });
        Pager pager = query.getPager();
        if (pager != null) {
            String countSql = this.queryToCountSql(query);
            Long count = this.jdbcTemplate.queryForObject(countSql, Long.class);
            return Page.of(pager.getPage(), pager.getPageSize(), count == null ? 0 : count, records);
        }
        return Page.of(records);
    }

    /**
     * 查询 Query 转换为 select 语句
     *
     * @param query 查询参数
     *
     * @return String
     *
     * @author wangweijun
     * @since 2024/7/2 17:36
     */
    private String queryToSelectSql(Query query) {
        String whereClause = getWhereClause(query.getQueryParams());
        String orderByClause = getOrderByClause(query.getOrderByParams());
        String selectSql = """
                SELECT * FROM %s t
                """.formatted(tableName);
        if (CharSequenceUtil.isNotEmpty(whereClause)) {
            selectSql += " WHERE " + whereClause;
        }
        if (CharSequenceUtil.isNotEmpty(orderByClause)) {
            selectSql += " ORDER BY " + orderByClause;
        }
        Pager pager = query.getPager();
        if (pager != null) {
            selectSql += " LIMIT %s OFFSET %s".formatted(pager.getPageSize(), pager.getOffset());
        }
        return selectSql;
    }

    private String queryToCountSql(Query query) {
        String whereClause = getWhereClause(query.getQueryParams());
        String orderByClause = getOrderByClause(query.getOrderByParams());
        String selectSql = """
                SELECT * FROM %s _inner
                """.formatted(tableName);
        if (CharSequenceUtil.isNotEmpty(whereClause)) {
            selectSql += " WHERE " + whereClause;
        }
        if (CharSequenceUtil.isNotEmpty(orderByClause)) {
            selectSql += " ORDER BY " + orderByClause;
        }
        return "SELECT COUNT(*) FROM (" + selectSql + ") _outer";
    }

    /**
     * 获取过滤条件
     *
     * @param queryParams 查询参数
     *
     * @return String
     *
     * @author wangweijun
     * @since 2024/7/1 19:59
     */
    @SuppressWarnings("unchecked")
    private String getWhereClause(List<QueryParam> queryParams) {
        StringBuilder whereClause = null;
        if (queryParams != null && !queryParams.isEmpty()) {
            whereClause = new StringBuilder();
            int i = 0;
            for (QueryParam param : queryParams) {
                i++;
                String fieldName = param.getKey();
                if (param.isToUnderline()) {
                    fieldName = StringUtils.camelToUnderline(fieldName);
                }
                // 操作符
                String operator = param.getQueryOperator().getOperator();
                if (QueryOperator.IN == param.getQueryOperator() || QueryOperator.NOT_IN == param.getQueryOperator()) {
                    List<Object> values = (List<Object>) param.getValue();
                    whereClause.append("`").append(fieldName).append("`")
                            .append(" ").append(operator).append(" ")
                            .append("(");
                    for (int j = 0; j < values.size(); j++) {
                        whereClause.append("'").append(values.get(j).toString()).append("'");
                        if (j != values.size() - 1) {
                            whereClause.append(",").append(" ");
                        }
                    }
                    whereClause.append(")").append(" ");
                } else if (QueryOperator.BETWEEN == param.getQueryOperator() || QueryOperator.NOT_BETWEEN == param.getQueryOperator()) {
                    Pair<Object, Object> pair = (Pair<Object, Object>) param.getValue();
                    whereClause.append("`").append(fieldName).append("`")
                            .append(" ").append(operator).append(" ")
                            .append(pair.getKey().toString()).append(" ")
                            .append("and").append(" ")
                            .append(pair.getValue().toString()).append(" ");

                } else {
                    String value = param.getValue().toString();
                    whereClause.append("`").append(fieldName).append("`")
                            .append(" ").append(operator).append(" ")
                            .append("'").append(value).append("'");
                }

                if (i != queryParams.size()) {
                    whereClause.append(" ").append("AND").append(" ");
                }
            }
        }
        return whereClause == null ? null : whereClause.toString();
    }

    /**
     * 获取过滤条件
     *
     * @param orderByParams 排序参数
     *
     * @return String
     *
     * @author wangweijun
     * @since 2024/7/1 19:59
     */
    private String getOrderByClause(List<OrderByParam> orderByParams) {
        StringBuilder orderByClause = null;
        if (orderByParams != null && !orderByParams.isEmpty()) {
            orderByClause = new StringBuilder();
            int i = 0;
            for (OrderByParam param : orderByParams) {
                i++;
                String fieldName = param.getKey();
                if (param.isToUnderline()) {
                    fieldName = StringUtils.camelToUnderline(fieldName);
                }
                String operator = param.getOrderByOperator().getOperator();
                orderByClause.append("`").append(fieldName).append("`")
                        .append(" ").append(operator);
                if (i != orderByParams.size()) {
                    orderByClause.append(" ").append(",").append(" ");
                }
            }
        }
        return orderByClause == null ? null : orderByClause.toString();
    }

    /**
     * 获取实体类对应的字段和值
     *
     * @param entity 实体类
     *
     * @return Pair
     *
     * @author wangweijun
     * @since 2024/6/26 15:39
     */
    private Pair<List<ColumnDomain>, List<Object>> getEntityParams(T entity) {

        // 用于存储设置的字段名和值
        Map<ColumnDomain, Object> updateParams = new HashMap<>();

        Class<?> clazz = entity.getClass();
        do {
            Field[] fields = clazz.getDeclaredFields();
            // 遍历所有字段，检查哪些字段的值不为空，并准备用于UPDATE的参数
            for (Field field : fields) {
                try {
                    ColumnDomain columnDomain = ColumnDomain.of(tableName, field);
                    if (ReflectUtils.isStatic(field) && ReflectUtils.isFinal(field) || !columnDomain.isExist()) {
                        continue;
                    }
                    field.setAccessible(true);
                    Object value = field.get(entity);
                    // 处理枚举
                    if (value != null) {
                        if (columnDomain.isPrimary()) {
                            continue;
                        }
                        // 处理 BaseEnum 类型
                        if (value instanceof BaseEnum baseEnum) {
                            value = baseEnum.getCode();
                        }
                        // 处理 JSON 类型
                        if (EntityColumn.Type.JSON == columnDomain.getType()) {
                            value = JsonUtils.toJson(value);
                        }
                        // 处理 Date 类型
                        if (value instanceof Date date) {
                            value = DateUtils.formatToDateTime(date);
                        }
                        updateParams.put(columnDomain, value);
                    }
                } catch (IllegalAccessException e) {
                    LoggerPrinter.error(log, e.getMessage(), e);
                }
            }
            // 递归寻找父类
            clazz = clazz.getSuperclass();
        } while (clazz != Object.class);

        // 字段对应的值
        List<Object> args = new ArrayList<>(updateParams.values());

        return Pair.of(new ArrayList<>(updateParams.keySet()), args);
    }

}
