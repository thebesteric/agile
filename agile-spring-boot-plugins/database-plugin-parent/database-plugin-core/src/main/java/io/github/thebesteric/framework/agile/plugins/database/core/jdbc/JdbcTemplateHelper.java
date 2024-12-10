package io.github.thebesteric.framework.agile.plugins.database.core.jdbc;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.db.sql.SqlFormatter;
import io.github.thebesteric.framework.agile.commons.util.CollectionUtils;
import io.github.thebesteric.framework.agile.commons.util.LoggerPrinter;
import io.github.thebesteric.framework.agile.core.domain.Pair;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.ColumnDomain;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.EntityClassDomain;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.ReferenceDomain;
import io.vavr.control.Try;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JdbcTemplateHelper
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-14 13:36:57
 */
@Slf4j
@Getter
public class JdbcTemplateHelper {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformTransactionManager transactionManager;
    private final String jdbcUrl;
    private final String schema;

    // 表对应的外键集合
    private final Map<EntityClassDomain, List<ColumnDomain>> createdTableWithForeignKeys = new HashMap<>();

    public JdbcTemplateHelper(DataSource dataSource, PlatformTransactionManager transactionManager) throws SQLException {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.transactionManager = transactionManager;
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            jdbcUrl = metaData.getURL();
            schema = jdbcUrl.split("//")[1].split("/")[1].split("\\?")[0];
        }
    }

    public enum Operation {
        CREATE, ADD, DROP, UPDATE, DELETE, INSERT
    }

    /**
     * 获取元数据
     *
     * @param function 有返回值的函数
     *
     * @return R
     *
     * @author wangweijun
     * @since 2024/7/4 17:52
     */
    public <R> R getDatabaseMetaData(Function<DatabaseMetaData, R> function) throws SQLException {
        DataSource dataSource = jdbcTemplate.getDataSource();
        if (dataSource == null) {
            throw new SQLException("DataSource is null");
        }
        try (Connection connection = dataSource.getConnection()) {
            return function.apply(connection.getMetaData());
        }
    }

    /**
     * 获取元数据
     *
     * @param consumer 没有返回值的函数
     *
     * @author wangweijun
     * @since 2024/7/4 17:52
     */
    public void getDatabaseMetaData(Consumer<DatabaseMetaData> consumer) throws SQLException {
        DataSource dataSource = jdbcTemplate.getDataSource();
        if (dataSource == null) {
            throw new SQLException("DataSource is null");
        }
        try (Connection connection = dataSource.getConnection()) {
            consumer.accept(connection.getMetaData());
        }
    }

    /**
     * 获取数据库名称
     *
     * @return String
     *
     * @author wangweijun
     * @since 2024/12/10 11:25
     */
    public String getDatabaseName() {
        if (jdbcUrl == null || jdbcUrl.isEmpty()) {
            throw new IllegalArgumentException("JDBC URL cannot be null or empty");
        }
        Pattern pattern = Pattern.compile("jdbc:mysql://[^/]+/([^/?]+)(\\?.*)?");
        Matcher matcher = pattern.matcher(jdbcUrl);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException("Unsupported JDBC URL format: " + jdbcUrl);
    }

    /**
     * 在事务中执行（有返回值）默认不会创建新的事务
     *
     * @param supplier supplier
     *
     * @author wangweijun
     * @since 2024/6/24 14:55
     */
    public <T> T executeInTransaction(Supplier<T> supplier) {
        return this.executeInTransaction(supplier, false);
    }

    /**
     * 在事务中执行（有返回值）
     *
     * @param supplier  supplier
     * @param createNew 是否创建新的事务
     *
     * @author wangweijun
     * @since 2024/6/24 14:55
     */
    public <T> T executeInTransaction(Supplier<T> supplier, boolean createNew) {
        if (TransactionSynchronizationManager.isActualTransactionActive() && !createNew) {
            return supplier.get();
        } else {
            TransactionDefinition def = new DefaultTransactionDefinition();
            TransactionStatus status = transactionManager.getTransaction(def);
            return Try.of(supplier::get).onSuccess(result -> transactionManager.commit(status)).onFailure(e -> {
                LoggerPrinter.error(log, "Failed to execute: {}", e.getMessage(), e);
                transactionManager.rollback(status);
            }).get();
        }
    }

    /**
     * 在事务中执行（没有返回值）默认不会创建新的事务
     *
     * @param runnable runnable
     *
     * @author wangweijun
     * @since 2024/6/25 13:48
     */
    public void executeInTransaction(Runnable runnable) {
        this.executeInTransaction(runnable, false);
    }

    /**
     * 在事务中执行（没有返回值）
     *
     * @param runnable  runnable
     * @param createNew 是否创建新的事务
     *
     * @author wangweijun
     * @since 2024/6/25 13:48
     */
    public void executeInTransaction(Runnable runnable, boolean createNew) {
        if (TransactionSynchronizationManager.isActualTransactionActive() && !createNew) {
            runnable.run();
        } else {
            TransactionDefinition def = new DefaultTransactionDefinition();
            TransactionStatus status = transactionManager.getTransaction(def);
            Try.run(runnable::run).onSuccess(result -> transactionManager.commit(status)).onFailure(e -> {
                LoggerPrinter.error(log, "Failed to execute: {}", e.getMessage(), e);
                transactionManager.rollback(status);
            }).get();
        }
    }

    /**
     * 表是否存在
     *
     * @param tableName 表名
     *
     * @return boolean
     *
     * @author wangweijun
     * @since 2024/6/17 12:18
     */
    public boolean tableExists(String tableName) throws SQLException {
        String tableExistsSql = TableMetadataHelper.tableExistsSql(schema, tableName);
        Map<String, Object> result = executeSelect(tableExistsSql);
        return result != null && !result.isEmpty() && (Long) result.get("exists") != 0;
    }

    /**
     * 表是否存在
     *
     * @param entityClassDomain 实体类
     *
     * @return boolean
     *
     * @author wangweijun
     * @since 2024/6/17 12:18
     */
    public boolean tableExists(EntityClassDomain entityClassDomain) throws SQLException {
        return tableExists(entityClassDomain.getTableName());
    }

    /**
     * 表是否存在
     *
     * @param clazz 实体类
     *
     * @return boolean
     *
     * @author wangweijun
     * @since 2024/6/17 12:18
     */
    public boolean tableExists(Class<?> clazz) throws SQLException {
        EntityClassDomain entityClassDomain = EntityClassDomain.of(clazz);
        return tableExists(entityClassDomain);
    }

    /**
     * 添加列
     *
     * @param tableName 表名
     * @param field     字段名
     *
     * @return ColumnDomain
     *
     * @author wangweijun
     * @since 2024/7/4 17:21
     */
    public ColumnDomain addColumn(String tableName, Field field) throws SQLException {
        ColumnDomain columnDomain = ColumnDomain.of(tableName, field);
        return this.addColumn(columnDomain);
    }

    /**
     * 添加列
     *
     * @param columnDomain 列对象
     *
     * @return ColumnDomain
     *
     * @author wangweijun
     * @since 2024/7/4 17:21
     */
    public ColumnDomain addColumn(ColumnDomain columnDomain) throws SQLException {
        String tableName = columnDomain.getTableName();
        StringBuilder sb = new StringBuilder();
        sb.append("ALTER TABLE").append(" ").append("`").append(tableName).append("`").append(" ");
        sb.append("ADD").append(" ").append("`").append(columnDomain.getName()).append("`").append(" ").append(columnDomain.typeWithLength()).append(" ");

        if (columnDomain.getType().isSupportSign() && columnDomain.isUnsigned()) {
            sb.append("UNSIGNED").append(" ");
        }

        String defaultExpression = columnDomain.getDefaultExpression();
        if (CharSequenceUtil.isNotEmpty(defaultExpression)) {
            sb.append("DEFAULT").append(" ").append(defaultExpression).append(" ");
        }
        sb.append(columnDomain.isNullable() ? "NULL" : "NOT NULL").append(" ");

        String comment = columnDomain.getComment();
        if (CharSequenceUtil.isNotEmpty(comment)) {
            sb.append("COMMENT").append(" ").append("'").append(comment).append("'");
        }
        this.executeUpdate(sb.toString(), JdbcTemplateHelper.Operation.ADD);

        // 新建主键
        if (columnDomain.isPrimary()) {
            if (columnDomain.isAutoIncrement()) {
                this.createPrimaryKeyWithoutAutoIncrement(tableName, columnDomain.getName(), columnDomain.typeWithLength());
            } else {
                this.createPrimaryKey(tableName, columnDomain.getName());
            }
        }

        // 创建外键
        if (columnDomain.getReference() != null) {
            this.createForeignKey(tableName, columnDomain.getReference());
        }

        // 新建唯一约束
        if (columnDomain.isUnique()) {
            this.createUniqueIndex(tableName, columnDomain.getName());
        }

        // 判断索引
        if (columnDomain.isIndex()) {
            this.createIndex(tableName, columnDomain.getName());
        }

        return columnDomain;
    }

    /**
     * 删除列
     *
     * @param metaData     元数据
     * @param tableName    表名
     * @param deleteColumn 需要删除的列名
     *
     * @author wangweijun
     * @since 2024/7/4 17:22
     */
    public void deleteColumn(DatabaseMetaData metaData, String tableName, String deleteColumn) throws SQLException {
        // 判断是否有对应的外键
        Set<ReferenceDomain> foreignKeyDomains = TableMetadataHelper.foreignKeyDomains(metaData, tableName);
        for (ReferenceDomain foreignKeyDomain : foreignKeyDomains) {
            if (foreignKeyDomain.getColumn().equalsIgnoreCase(deleteColumn)) {
                // 删除外键
                this.dropForeignKey(tableName, foreignKeyDomain.getForeignKeyName());
            }
        }

        // 删除列
        StringBuilder sb = new StringBuilder();
        sb.append("ALTER TABLE").append(" ").append("`").append(tableName).append("`").append(" ");
        sb.append("DROP COLUMN").append(" ").append("`").append(deleteColumn).append("`");
        this.executeUpdate(sb.toString(), JdbcTemplateHelper.Operation.DELETE);
    }

    /**
     * 更新列
     *
     * @param tableName 表名
     * @param field     字段名
     *
     * @return ColumnDomain
     *
     * @author wangweijun
     * @since 2024/7/4 17:21
     */
    public ColumnDomain updateColumn(String tableName, Field field) throws SQLException {
        ColumnDomain columnDomain = ColumnDomain.of(tableName, field);
        return this.updateColumn(columnDomain);
    }

    /**
     * 更新列
     *
     * @param columnDomain 列对象
     *
     * @return ColumnDomain
     *
     * @author wangweijun
     * @since 2024/7/4 17:29
     */
    public ColumnDomain updateColumn(ColumnDomain columnDomain) throws SQLException {
        String tableName = columnDomain.getTableName();
        StringBuilder sb = new StringBuilder();
        sb.append("ALTER TABLE").append(" ").append("`").append(tableName).append("`").append(" ");

        String forUpdateColumn = columnDomain.getForUpdate();
        String columnName = columnDomain.getName();
        // 字段名称变更的情况
        if (CharSequenceUtil.isNotEmpty(forUpdateColumn)) {
            sb.append("CHANGE").append(" ").append("`").append(forUpdateColumn).append("`").append(" ").append("`").append(columnName).append("`").append(" ");
        }
        // 非字段名称变更的情况
        else {
            sb.append("MODIFY").append(" ").append("`").append(columnName).append("`").append(" ");
        }
        sb.append(columnDomain.typeWithLength()).append(" ");

        if (columnDomain.getType().isSupportSign() && columnDomain.isUnsigned()) {
            sb.append("UNSIGNED").append(" ");
        }

        String defaultExpression = columnDomain.getDefaultExpression();
        if (CharSequenceUtil.isNotEmpty(defaultExpression)) {
            sb.append("DEFAULT").append(" ").append(defaultExpression).append(" ");
        }

        sb.append(columnDomain.isNullable() ? "NULL" : "NOT NULL").append(" ");

        String comment = columnDomain.getComment();
        if (CharSequenceUtil.isNotEmpty(comment)) {
            sb.append("COMMENT").append(" ").append("'").append(comment).append("'");
        }
        this.executeUpdate(sb.toString(), JdbcTemplateHelper.Operation.UPDATE);

        return columnDomain;
    }

    /**
     * 创建表
     *
     * @param clazz 实体类
     *
     * @author wangweijun
     * @since 2024/6/14 13:50
     */
    public List<ColumnDomain> createTable(Class<?> clazz) throws SQLException {
        return this.createTable(EntityClassDomain.of(clazz), true, true);
    }

    /**
     * 创建表
     *
     * @param entityClassDomain 实体类
     *
     * @author wangweijun
     * @since 2024/6/14 13:50
     */
    public List<ColumnDomain> createTable(EntityClassDomain entityClassDomain) throws SQLException {
        return this.createTable(entityClassDomain, true, true);
    }

    /**
     * 创建表
     *
     * @param entityClassDomain 实体类
     * @param showSql           是否显示 SQL 语句
     *
     * @author wangweijun
     * @since 2024/6/14 13:50
     */
    public List<ColumnDomain> createTable(EntityClassDomain entityClassDomain, boolean showSql) throws SQLException {
        return this.createTable(entityClassDomain, showSql, true);
    }

    /**
     * 创建表
     *
     * @param entityClassDomain 实体类
     * @param showSql           是否显示 SQL 语句
     * @param formatSql         是否格式化 SQL 语句
     *
     * @author wangweijun
     * @since 2024/6/14 13:50
     */
    public List<ColumnDomain> createTable(EntityClassDomain entityClassDomain, boolean showSql, boolean formatSql) throws SQLException {
        String tableName = entityClassDomain.getTableName();

        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE `").append(tableName).append("` (");
        List<Field> fields = entityClassDomain.getEntityFields();

        List<ColumnDomain> columnDomains = new ArrayList<>();

        ColumnDomain primaryKey = null;
        List<ColumnDomain> uniqueColumnDomains = new ArrayList<>();
        Map<String, List<String>> uniqueGroups = new HashMap<>();
        List<ColumnDomain> indexColumnDomains = new ArrayList<>();
        Map<String, List<Pair<String, Integer>>> indexGroups = new HashMap<>();

        List<ColumnDomain> foreignKeys = new ArrayList<>();

        for (Field field : fields) {
            // 获取字段信息
            ColumnDomain columnDomain = ColumnDomain.of(tableName, field);
            columnDomains.add(columnDomain);

            // 主键判断
            if (columnDomain.isPrimary()) {
                if (primaryKey != null) {
                    throw new SQLException("Primary key is duplicate: %s".formatted(primaryKey));
                }
                primaryKey = columnDomain;
            }

            // 外键判断
            if (columnDomain.getReference() != null) {
                foreignKeys.add(columnDomain);
            }

            // 字段名
            sb.append("`").append(columnDomain.getName()).append("`");
            // 字段类型
            sb.append(" ").append(columnDomain.typeWithLength());
            // 符号
            if (columnDomain.getType().isSupportSign() && columnDomain.isUnsigned()) {
                sb.append(" ").append("UNSIGNED");
            }
            // 默认值
            String defaultExpression = columnDomain.getDefaultExpression();
            if (CharSequenceUtil.isNotEmpty(defaultExpression)) {
                sb.append(" ").append("DEFAULT ").append(defaultExpression);
            }
            // 自增
            if (columnDomain.isAutoIncrement()) {
                sb.append(" ").append("AUTO_INCREMENT");
            }
            // 非自增
            else {
                // 是否为空判断
                if (columnDomain.isNullable()) {
                    sb.append(" ").append("NULL");
                } else {
                    sb.append(" ").append("NOT NULL");
                }
            }
            // 注释
            String comment = columnDomain.getComment();
            if (CharSequenceUtil.isNotEmpty(comment)) {
                sb.append(" ").append("COMMENT").append(" ").append("'").append(comment).append("'");
            }
            sb.append(", ");

            // 判断唯一键
            if (columnDomain.isUnique()) {
                uniqueColumnDomains.add(columnDomain);
            }

            // 判断联合唯一键
            if (CharSequenceUtil.isNotEmpty(columnDomain.getUniqueGroup())) {
                List<String> columns = uniqueGroups.getOrDefault(columnDomain.getUniqueGroup(), new ArrayList<>());
                columns.add(columnDomain.getName());
                uniqueGroups.put(columnDomain.getUniqueGroup(), columns);
            }

            // 判断索引键
            if (columnDomain.isIndex()) {
                indexColumnDomains.add(columnDomain);
            }

            // 判断联合索引
            if (CharSequenceUtil.isNotEmpty(columnDomain.getIndexGroup())) {
                List<Pair<String, Integer>> columns = indexGroups.getOrDefault(columnDomain.getIndexGroup(), new ArrayList<>());
                columns.add(Pair.of(columnDomain.getName(), columnDomain.getIndexGroupSort()));
                indexGroups.put(columnDomain.getIndexGroup(), columns);
            }
        }

        // 主键
        if (primaryKey != null) {
            String name = primaryKey.getName();
            String primaryKeyName = ColumnDomain.generatePrimaryKeyName(tableName, primaryKey.getName());
            sb.append(String.format("CONSTRAINT %s PRIMARY KEY (`%s`)", primaryKeyName, name)).append(", ");
        }

        // 唯一键
        for (int i = 0; i < uniqueColumnDomains.size(); i++) {
            if (i == 0) {
                sb.append(" ");
            }
            ColumnDomain uniqueColumnDomain = uniqueColumnDomains.get(i);
            String uniqueName = uniqueColumnDomain.getName();
            String uniqueKeyName = ColumnDomain.generateUniqueKeyName(tableName, uniqueName);
            sb.append(String.format("CONSTRAINT %s UNIQUE (`%s`)", uniqueKeyName, uniqueName)).append(", ");
        }

        // 联合唯一键
        int uniqueGroupsLength = uniqueGroups.size();
        for (Map.Entry<String, List<String>> entry : uniqueGroups.entrySet()) {
            String uniqueGroupName = entry.getKey();
            List<String> keys = entry.getValue();
            StringBuilder uniqueKeys = new StringBuilder();
            for (int i = 0; i < keys.size(); i++) {
                uniqueKeys.append("`").append(keys.get(i)).append("`");
                if (i != keys.size() - 1) {
                    uniqueKeys.append(",").append(" ");
                }
            }
            sb.append(String.format("CONSTRAINT %s_uk_%s UNIQUE (%s)", tableName, uniqueGroupName, uniqueKeys));
            if (--uniqueGroupsLength != 0) {
                sb.append(", ");
            }
        }

        // 去除最后一个逗号
        String sql = sb.toString().trim();
        sb = new StringBuilder(sql);
        int length = sb.length();
        char lastChar = sb.charAt(length - 1);
        if (lastChar == ',') {
            sb.deleteCharAt(length - 1);
        }
        sb.append(") COMMENT ").append("'").append(entityClassDomain.getComment()).append("'").append(" ");
        sb.append("ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci");

        // 创建表
        this.executeUpdate(sb.toString(), Operation.CREATE, showSql, formatSql);

        // 主键设置为第一列
        if (primaryKey != null) {
            String firstColumnSql = "ALTER TABLE `%s` MODIFY COLUMN `%s` %s %s NOT NULL FIRST"
                    .formatted(tableName, primaryKey.getName(), primaryKey.typeWithLength(), primaryKey.isAutoIncrement() ? "AUTO_INCREMENT" : "");
            this.executeUpdate(firstColumnSql, Operation.UPDATE);
        }

        // 创建唯一索引（包含使用 @Unique 注解的类上的列）
        List<String> uniqueColumns = entityClassDomain.getOnClassUniqueColumns();
        uniqueColumns = new ArrayList<>(uniqueColumns);
        for (ColumnDomain uniqueColumnDomain : uniqueColumnDomains) {
            String columnName = uniqueColumnDomain.getName();
            if (uniqueColumns.contains(columnName)) {
                uniqueColumns.remove(columnName);
            }
        }
        for (String uniqueColumn : uniqueColumns) {
            // 创建唯一索引
            createUniqueIndex(tableName, uniqueColumn);
        }

        // 创建唯一索引组（使用 @UniqueGroup 注解的列）
        List<List<String>> uniqueGroupColumns = entityClassDomain.getOnClassUniqueGroupColumns();
        uniqueGroupColumns = new ArrayList<>(uniqueGroupColumns);
        for (Map.Entry<String, List<String>> entry : uniqueGroups.entrySet()) {
            List<String> groupColumns = entry.getValue();
            for (List<String> groupColumnsOnClass : uniqueGroupColumns) {
                if (CollectionUtils.isEquals(groupColumns, groupColumnsOnClass)) {
                    uniqueGroupColumns.remove(groupColumnsOnClass);
                    break;
                }
            }
        }
        for (List<String> groupColumns : uniqueGroupColumns) {
            // 创建唯一索引组
            createUniqueGroupIndex(tableName, groupColumns);
        }

        // 创建索引键（包含使用 @Index 注解的类上的列）
        List<String> indexColumns = entityClassDomain.getOnClassIndexColumns();
        indexColumns = new ArrayList<>(indexColumns);
        for (ColumnDomain indexColumnDomain : indexColumnDomains) {
            String columnName = indexColumnDomain.getName();
            if (indexColumns.contains(columnName)) {
                indexColumns.remove(columnName);
            }
        }
        for (String indexColumn : indexColumns) {
            // 创建普通索引
            createIndex(tableName, indexColumn);
        }

        // 创建普通索引
        for (Map.Entry<String, List<Pair<String, Integer>>> entry : indexGroups.entrySet()) {
            String indexGroupName = entry.getKey();
            List<Pair<String, Integer>> pairs = entry.getValue();
            // 排序
            pairs.sort(Comparator.comparingInt(Pair::getValue));
            List<String> indexNames = pairs.stream().map(Pair::getKey).toList();
            // 创建普通索引
            createIndex(tableName, indexGroupName, indexNames, true);
        }

        // 创建普通索引组（使用 @IndexGroup 注解的列）
        List<List<String>> indexGroupColumns = entityClassDomain.getOnClassIndexGroupColumns();
        indexGroupColumns = new ArrayList<>(indexGroupColumns);
        for (Map.Entry<String, List<Pair<String, Integer>>> entry : indexGroups.entrySet()) {
            List<Pair<String, Integer>> pairs = entry.getValue();
            // 排序
            pairs.sort(Comparator.comparingInt(Pair::getValue));
            List<String> indexNames = pairs.stream().map(Pair::getKey).toList();
            for (List<String> indexGroupColumnsOnClass : indexGroupColumns) {
                if (CollectionUtils.isStrictEquals(indexNames, indexGroupColumnsOnClass)) {
                    indexGroupColumns.remove(indexGroupColumnsOnClass);
                    break;
                }
            }
        }
        for (List<String> groupColumns : indexGroupColumns) {
            // 创建普通索引组
            createGroupIndex(tableName, groupColumns);
        }

        // 添加外键
        if (CollUtil.isNotEmpty(foreignKeys)) {
            this.createdTableWithForeignKeys.put(entityClassDomain, foreignKeys);
        }

        return columnDomains;
    }

    public void executeUpdate(final String sql, final Operation operation) throws SQLException {
        this.executeUpdate(sql, operation, true, true);
    }

    public void executeUpdate(final String sql, final Operation operation, boolean showSql) throws SQLException {
        this.executeUpdate(sql, operation, showSql, true);
    }

    public void executeUpdate(final String sql, final Operation operation, boolean showSql, boolean formatSql) throws SQLException {
        DataSource dataSource = this.jdbcTemplate.getDataSource();
        if (dataSource == null) {
            throw new SQLException("DataSource is null");
        }
        AtomicReference<Connection> connection = new AtomicReference<>();
        Try.of(() -> {
            connection.set(dataSource.getConnection());
            PreparedStatementCreator creator = conn -> conn.prepareStatement(sql);
            return creator.createPreparedStatement(connection.get()).executeUpdate();
        }).onFailure(e -> {
            // 更新或新建重复数据，忽略
            if (e instanceof SQLSyntaxErrorException && e.getMessage().startsWith("Duplicate key")) {
                LoggerPrinter.warn(log, e.getMessage() + ": {}", sql);
                return;
            }
            if (Operation.DELETE != operation) {
                LoggerPrinter.error(log, e.getMessage() + ": {}", sql, e);
            }
        }).andThen(result -> {
            if (showSql && result == 0) {
                if (formatSql) {
                    String formattedSql = SqlFormatter.format(sql);
                    LoggerPrinter.info(log, formattedSql);
                } else {
                    LoggerPrinter.info(log, sql);
                }
            }
        }).andFinallyTry(() -> connection.get().close());
    }

    public Map<String, Object> executeSelect(final String sql) throws SQLException {
        return this.executeSelect(sql, false, true);
    }

    public Map<String, Object> executeSelect(final String sql, boolean showSql) throws SQLException {
        return this.executeSelect(sql, showSql, true);
    }

    public Map<String, Object> executeSelect(final String sql, boolean showSql, boolean formatSql) throws SQLException {
        DataSource dataSource = this.jdbcTemplate.getDataSource();
        if (dataSource == null) {
            throw new SQLException("DataSource is null");
        }
        AtomicReference<Connection> connection = new AtomicReference<>();
        return Try.of(() -> {
            Map<String, Object> result = new LinkedHashMap<>();
            connection.set(dataSource.getConnection());
            PreparedStatementCreator creator = conn -> conn.prepareStatement(sql);
            ResultSet resultSet = creator.createPreparedStatement(connection.get()).executeQuery();
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnsCount = metaData.getColumnCount();
            // 封装 key
            List<String> keys = new ArrayList<>();
            for (int i = 1; i <= columnsCount; i++) {
                String columnName = metaData.getColumnName(i);
                keys.add(columnName);
            }
            // 封装 value
            while (resultSet.next()) {
                for (int i = 1; i <= columnsCount; i++) {
                    Object columnValue = resultSet.getObject(i);
                    result.put(keys.get(i - 1), columnValue);
                }
            }
            return result;
        }).onFailure(e -> LoggerPrinter.error(log, e.getMessage() + ": {}", sql, e)).andThen(result -> {
            if (showSql) {
                if (formatSql) {
                    String formattedSql = SqlFormatter.format(sql);
                    LoggerPrinter.info(log, formattedSql);
                } else {
                    LoggerPrinter.info(log, sql);
                }
            }
        }).andFinallyTry(() -> connection.get().close()).get();
    }

    public void createUniqueIndex(String tableName, String columnName) throws SQLException {
        String uniqueKeyName = ColumnDomain.generateUniqueKeyName(tableName, columnName);
        String uniqueIndexSql = String.format("ALTER TABLE `%s` ADD CONSTRAINT `%s` UNIQUE (`%s`)", tableName, uniqueKeyName, columnName);
        this.executeUpdate(uniqueIndexSql, Operation.CREATE);
    }

    public void createUniqueGroupIndex(String tableName, String uniqueGroupName, List<String> columnNames, boolean generateUniqueIndexName) throws SQLException {
        StringBuilder columnKeyNames = new StringBuilder();
        for (int i = 0; i < columnNames.size(); i++) {
            columnKeyNames.append("`").append(columnNames.get(i)).append("`");
            if (i != columnNames.size() - 1) {
                columnKeyNames.append(",").append(" ");
            }
        }
        String indexGroupNameKey = generateUniqueIndexName ? ColumnDomain.generateIndexKeyName(tableName, uniqueGroupName) : uniqueGroupName;
        String uniqueIndexSql = String.format("CREATE UNIQUE INDEX `%s` ON `%s` (%s);", indexGroupNameKey, tableName, columnKeyNames);
        this.executeUpdate(uniqueIndexSql, Operation.CREATE);
    }

    public void createUniqueGroupIndex(String tableName, List<String> columnNames) throws SQLException {
        String uniqueGroupName = tableName + ColumnDomain.UNIQUE_KEY_PREFIX_SUFFIX + String.join("_", columnNames);
        createUniqueGroupIndex(tableName, uniqueGroupName, columnNames, true);
    }

    public void dropUniqueIndex(String tableName, String name, boolean generateIndexName) throws SQLException {
        String uniqueKeyName = generateIndexName ? ColumnDomain.generateUniqueKeyName(tableName, name) : name;
        this.executeUpdate("ALTER TABLE `%s` DROP KEY `%s`".formatted(tableName, uniqueKeyName), Operation.DROP);
    }

    public void createIndex(String tableName, String columnName) throws SQLException {
        String indexNameKey = ColumnDomain.generateIndexKeyName(tableName, columnName);
        String indexSql = String.format("CREATE INDEX %s ON `%s` (`%s`)", indexNameKey, tableName, columnName);
        this.executeUpdate(indexSql, Operation.CREATE);
    }

    public void createIndex(String tableName, String indexGroupName, List<String> columnNames, boolean generateIndexName) throws SQLException {
        StringBuilder indexKeys = new StringBuilder();
        for (int i = 0; i < columnNames.size(); i++) {
            indexKeys.append("`").append(columnNames.get(i)).append("`");
            if (i != columnNames.size() - 1) {
                indexKeys.append(",").append(" ");
            }
        }
        String indexGroupNameKey = generateIndexName ? ColumnDomain.generateIndexKeyName(tableName, indexGroupName) : indexGroupName;
        String indexSql = String.format("CREATE INDEX %s ON `%s` (%s)", indexGroupNameKey, tableName, indexKeys);
        this.executeUpdate(indexSql, Operation.CREATE);
    }

    public void createGroupIndex(String tableName, List<String> columnNames) throws SQLException {
        String indexGroupName = String.join("_", columnNames);
        createGroupIndex(tableName, indexGroupName, columnNames, true);
    }

    public void createGroupIndex(String tableName, String indexGroupName, List<String> columnNames, boolean generateIndexName) throws SQLException {
        createIndex(tableName, indexGroupName, columnNames, generateIndexName);
    }

    public void dropIndex(String tableName, String name, boolean generateIndexName) throws SQLException {
        String indexNameKey = generateIndexName ? ColumnDomain.generateIndexKeyName(tableName, name) : name;
        this.executeUpdate("DROP INDEX `%s` on `%s`".formatted(indexNameKey, tableName), Operation.DROP);
    }

    public void updateIndex(Set<String> indexNames, EntityClassDomain entityClassDomain) throws SQLException {
        String tableName = entityClassDomain.getTableName();
        // 所有普通索引组
        Map<String, List<String>> indexGroupMap = entityClassDomain.indexGroups();
        // 所有普通索引（不包含普通索引组）
        Map<String, String> indices = entityClassDomain.indices();
        // 所有的索引
        Set<String> allIndexNames = new HashSet<>();

        // 合并索引
        allIndexNames.addAll(indexGroupMap.keySet());
        allIndexNames.addAll(indices.keySet());

        // 创建普通索引组
        for (Map.Entry<String, List<String>> entry : indexGroupMap.entrySet()) {
            String indexGroupKey = entry.getKey();
            if (!indexNames.contains(indexGroupKey)) {
                this.createGroupIndex(tableName, indexGroupKey, entry.getValue(), false);
            }
        }

        // 删除不需要的普通索引组
        for (String indexName : indexNames) {
            Set<String> indexGroupKeys = indexGroupMap.keySet();
            if (!indexGroupKeys.contains(indexName) && !allIndexNames.contains(indexName)) {
                this.dropIndex(tableName, indexName, false);
            }
        }
    }

    public void updateUniqueIndex(Set<String> inDatabaseUniqueIndexNames, EntityClassDomain entityClassDomain) throws SQLException {
        String tableName = entityClassDomain.getTableName();
        // 所有唯一索引组
        Map<String, List<String>> uniqueGroupMap = entityClassDomain.uniqueGroups();
        // 所有唯一索引（不包含唯一索引组）
        Map<String, String> uniques = entityClassDomain.uniques();
        // 所有唯一索引
        Set<String> allUniqueNames = new HashSet<>();

        // 合并唯一索引
        allUniqueNames.addAll(uniqueGroupMap.keySet());
        allUniqueNames.addAll(uniques.keySet());

        // 创建唯一索引组
        for (Map.Entry<String, List<String>> entry : uniqueGroupMap.entrySet()) {
            String uniqueGroupKey = entry.getKey();
            if (!inDatabaseUniqueIndexNames.contains(uniqueGroupKey)) {
                this.createUniqueGroupIndex(tableName, uniqueGroupKey, entry.getValue(), false);
            }
        }

        // 删除不需要的唯一索引组
        for (String uniqueIndexName : inDatabaseUniqueIndexNames) {
            Set<String> uniqueGroupKeys = uniqueGroupMap.keySet();
            if (!uniqueGroupKeys.contains(uniqueIndexName) && !allUniqueNames.contains(uniqueIndexName)) {
                this.dropUniqueIndex(tableName, uniqueIndexName, false);
            }
        }
    }

    public void createPrimaryKey(String tableName, String columnName) throws SQLException {
        String primaryKeyName = ColumnDomain.generatePrimaryKeyName(tableName, columnName);
        String sql = "ALTER TABLE `%s` ADD CONSTRAINT `%s` PRIMARY KEY (`%s`)".formatted(tableName, primaryKeyName, columnName);
        this.executeUpdate(sql, Operation.CREATE);
    }

    public void createPrimaryKeyWithoutAutoIncrement(String tableName, String columnName, String columnType) throws SQLException {
        this.createPrimaryKey(tableName, columnName);
        String sql = "ALTER TABLE `%s` MODIFY `%s` %s AUTO_INCREMENT".formatted(tableName, columnName, columnType);
        this.executeUpdate(sql, Operation.CREATE);
    }

    /**
     * 创建外键（新增表的时候会生效）
     *
     * @author wangweijun
     * @since 2024/6/20 15:05
     */
    public void createForeignKeyOnCreated() throws SQLException {
        for (Map.Entry<EntityClassDomain, List<ColumnDomain>> entry : this.createdTableWithForeignKeys.entrySet()) {
            EntityClassDomain classDomain = entry.getKey();
            List<ColumnDomain> foreignKeys = entry.getValue();
            for (ColumnDomain foreignKeyDomain : foreignKeys) {
                ReferenceDomain referenceDomain = foreignKeyDomain.getReference();
                // 创建外键
                createForeignKey(classDomain.getTableName(), referenceDomain);
            }
        }
    }

    public void updateTableComments(String tableName, String comment) throws SQLException {
        String sql = "ALTER TABLE `%s` COMMENT '%s'".formatted(tableName, comment);
        executeUpdate(sql, JdbcTemplateHelper.Operation.UPDATE);
    }

    public void createForeignKey(String tableName, ReferenceDomain referenceDomain) throws SQLException {
        // 字段名
        String columnName = referenceDomain.getColumn();
        // 目标表
        String targetTableName = referenceDomain.getTargetTableName();
        // 目标字段
        String targetColumnName = referenceDomain.getTargetColumn();
        this.createForeignKey(tableName, columnName, targetTableName, targetColumnName);
    }

    public void createForeignKey(String tableName, String columnName, String targetTableName, String targetColumnName) throws SQLException {
        String foreignKeyName = ColumnDomain.generateForeignKeyName(tableName, columnName, targetTableName, targetColumnName);
        StringBuilder sb = new StringBuilder();
        sb.append("ALTER TABLE").append(" ").append("`").append(tableName).append("`").append(" ");
        sb.append("ADD CONSTRAINT").append(" ").append(foreignKeyName).append(" ");
        sb.append("FOREIGN KEY").append(" ").append("(").append("`").append(columnName).append("`").append(")").append(" ");
        sb.append("REFERENCES").append(" ").append("`").append(targetTableName).append("`").append(" ").append("(").append("`").append(targetColumnName).append("`").append(")");
        executeUpdate(sb.toString(), JdbcTemplateHelper.Operation.ADD);
    }

    public void dropForeignKey(String tableName, String foreignKeyName) throws SQLException {
        this.executeUpdate("ALTER TABLE `%s` DROP FOREIGN KEY `%s`".formatted(tableName, foreignKeyName), Operation.DROP);
        this.dropIndex(tableName, foreignKeyName, false);
    }

}
