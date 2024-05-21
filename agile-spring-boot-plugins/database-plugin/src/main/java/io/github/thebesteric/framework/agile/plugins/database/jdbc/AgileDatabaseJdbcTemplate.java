package io.github.thebesteric.framework.agile.plugins.database.jdbc;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.db.sql.SqlFormatter;
import com.baomidou.mybatisplus.annotation.TableField;
import io.github.thebesteric.framework.agile.commons.util.LoggerPrinter;
import io.github.thebesteric.framework.agile.commons.util.ReflectUtils;
import io.github.thebesteric.framework.agile.core.domain.Pair;
import io.github.thebesteric.framework.agile.plugins.database.annotation.EntityColumn;
import io.github.thebesteric.framework.agile.plugins.database.config.AgileDatabaseContext;
import io.github.thebesteric.framework.agile.plugins.database.config.AgileDatabaseProperties;
import io.github.thebesteric.framework.agile.plugins.database.domain.ColumnDomain;
import io.github.thebesteric.framework.agile.plugins.database.domain.EntityClassDomain;
import io.github.thebesteric.framework.agile.plugins.database.entity.TableMetadata;
import io.vavr.control.Try;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;

import javax.sql.DataSource;
import java.beans.Transient;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * AgileDatabaseJdbcTemplate
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-05-13 11:11:54
 */
@Slf4j
public class AgileDatabaseJdbcTemplate {

    private final AgileDatabaseContext context;
    private final JdbcTemplate jdbcTemplate;
    private final AgileDatabaseProperties properties;

    enum Operation {
        CREATE, ADD, DROP, UPDATE, DELETE, INSERT
    }


    public AgileDatabaseJdbcTemplate(AgileDatabaseContext context, DataSource dataSource, AgileDatabaseProperties properties) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.properties = properties;
    }

    @SneakyThrows
    public void createOrUpdateTable() {
        AgileDatabaseProperties.DDLAuto ddlAuto = properties.getDdlAuto();
        // DDLAuto.NONE 直接退出，适用于自己维护表的情况
        if (AgileDatabaseProperties.DDLAuto.NONE == ddlAuto) {
            return;
        }

        // 是否需要更新表结构
        boolean update = AgileDatabaseProperties.DDLAuto.UPDATE == ddlAuto;

        DataSource dataSource = jdbcTemplate.getDataSource();
        assert dataSource != null;
        Set<Class<?>> entityClasses = context.getEntityClasses();
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            // 初始化元数据表
            initTableMeta(metaData);
            for (Class<?> clazz : entityClasses) {
                EntityClassDomain entityClassDomain = EntityClassDomain.of(properties.getTableNamePrefix(), clazz);
                String tableName = entityClassDomain.getName();
                ResultSet resultSet = metaData.getTables(null, null, tableName, new String[]{"TABLE"});
                if (!resultSet.next()) {
                    createTable(entityClassDomain);
                } else {
                    if (update) {
                        updateTable(entityClassDomain, metaData);
                    }
                }
            }
        }
    }

    private void initTableMeta(DatabaseMetaData metaData) throws SQLException {
        String jdbcUrl = metaData.getURL();
        String databaseName = jdbcUrl.split("//")[1].split("/")[1].split("\\?")[0];
        String existsSql = TableMetadata.tableExists(databaseName);
        Map<String, Object> result = executeSelect(existsSql);
        if (result != null && !result.isEmpty() && (Long) result.get("exists") == 0) {
            EntityClassDomain entityClassDomain = EntityClassDomain.of(null, TableMetadata.class);
            createTable(entityClassDomain);
        }
    }

    public void createTable(EntityClassDomain entityClassDomain) throws SQLException {
        String tableName = entityClassDomain.getName();
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE `").append(tableName).append("` (");
        List<Field> fields = getEntityFields(entityClassDomain.getEntityClass());

        List<ColumnDomain> columnDomains = new ArrayList<>();

        ColumnDomain primaryKey = null;
        List<ColumnDomain> uniqueFieldNames = new ArrayList<>();
        Map<String, List<String>> uniqueGroups = new HashMap<>();
        List<String> indexFieldNames = new ArrayList<>();
        Map<String, List<Pair<String, Integer>>> indexGroups = new HashMap<>();

        for (Field field : fields) {
            // 获取字段信息
            ColumnDomain columnDomain = ColumnDomain.of(tableName, field);
            columnDomains.add(columnDomain);

            if (columnDomain.isPrimary()) {
                if (primaryKey != null) {
                    throw new SQLException("Primary key is duplicate: %s".formatted(primaryKey));
                }
                primaryKey = columnDomain;
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
                uniqueFieldNames.add(columnDomain);
            }

            // 判断联合唯一键
            if (CharSequenceUtil.isNotEmpty(columnDomain.getUniqueGroup())) {
                List<String> columns = uniqueGroups.getOrDefault(columnDomain.getUniqueGroup(), new ArrayList<>());
                columns.add(columnDomain.getName());
                uniqueGroups.put(columnDomain.getUniqueGroup(), columns);
            }

            // 判断索引键
            if (columnDomain.isIndex()) {
                String indexFieldName = columnDomain.getName();
                indexFieldNames.add(indexFieldName);
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
        for (int i = 0; i < uniqueFieldNames.size(); i++) {
            if (i == 0) {
                sb.append(" ");
            }
            ColumnDomain uniqueColumnDomain = uniqueFieldNames.get(i);
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
        this.executeUpdate(sb.toString(), Operation.CREATE);

        // 主键设置为第一列
        if (primaryKey != null) {
            String firstColumnSql = "ALTER TABLE `%s` MODIFY COLUMN `%s` %s %s NOT NULL FIRST"
                    .formatted(tableName, primaryKey.getName(), primaryKey.typeWithLength(), primaryKey.isAutoIncrement() ? "AUTO_INCREMENT" : "");
            this.executeUpdate(firstColumnSql, Operation.UPDATE);
        }

        // 创建索引键
        for (String indexName : indexFieldNames) {
            createIndex(tableName, indexName);
        }

        // 创建联合索引键
        for (Map.Entry<String, List<Pair<String, Integer>>> entry : indexGroups.entrySet()) {
            String indexGroupName = entry.getKey();
            List<Pair<String, Integer>> pairs = entry.getValue();
            // 排序
            pairs.sort(Comparator.comparingInt(Pair::getValue));

            List<String> indexNames = pairs.stream().map(Pair::getKey).toList();
            createIndex(tableName, indexGroupName, indexNames);
        }

        // 插入元数据
        for (ColumnDomain columnDomain : columnDomains) {
            String insertSql = TableMetadata.insertSql(columnDomain.getTableName(), columnDomain.getName(), columnDomain.signature());
            executeUpdate(insertSql, Operation.INSERT);
        }
    }

    public void updateTable(EntityClassDomain entityClassDomain, DatabaseMetaData metaData) throws SQLException {
        String tableName = entityClassDomain.getName();
        List<Field> fields = getEntityFields(entityClassDomain.getEntityClass());
        ResultSet dataColumns = metaData.getColumns(null, "%", tableName, "%");

        // 表的所有字段名称
        Set<String> columnNames = new LinkedHashSet<>();
        while (dataColumns.next()) {
            String columnName = dataColumns.getString("COLUMN_NAME");
            // 这里是防止 INNODB_COLUMN 有冗余字段
            List<String> list = columnNames.stream().map(String::toLowerCase).toList();
            if (list.contains(columnName.toLowerCase())) {
                continue;
            }
            columnNames.add(columnName);
        }

        List<Field> newFields = new ArrayList<>();
        List<Field> updateFields = new ArrayList<>();

        for (Field field : fields) {
            ColumnDomain columnDomain = ColumnDomain.of(tableName, field);
            String columnName = columnDomain.getName();

            // 查元数据表，对比签名
            String signature = null;
            String columnSignature = columnDomain.signature();
            final String selectSql = TableMetadata.selectSql(tableName, columnName);
            Map<String, Object> result = executeSelect(selectSql);
            // 元数据中存在对应的字段记录
            if (!result.isEmpty()) {
                signature = (String) result.get(TableMetadata.COLUMN_SIGNATURE);
            }

            boolean shouldUpdate = (CharSequenceUtil.isEmpty(signature) || !Objects.equals(columnSignature, signature)) && columnNames.contains(columnName);

            String forUpdateColumn = columnDomain.getForUpdate();
            // 需要更新的字段
            if (shouldUpdate || (columnNames.contains(forUpdateColumn) && CharSequenceUtil.isNotEmpty(forUpdateColumn))) {
                updateFields.add(field);
            }
            // 需要新增的字段
            else if (!columnNames.contains(columnName)) {
                newFields.add(field);
            }
        }

        List<String> currentColumnNames = fields.stream().map(field -> ColumnDomain.of(tableName, field).getName()).toList();
        List<String> deleteColumns = columnNames.stream().filter(columnName -> !currentColumnNames.contains(columnName)).toList();

        if (!deleteColumns.isEmpty() && properties.isDeleteColumn()) {
            deleteColumns(tableName, deleteColumns);
        }

        if (!updateFields.isEmpty()) {
            updateColumns(tableName, updateFields, metaData);
        }

        if (!newFields.isEmpty()) {
            addColumns(tableName, newFields);
        }
    }

    private void deleteColumns(String tableName, List<String> deleteColumns) throws SQLException {
        for (String deleteColumn : deleteColumns) {
            StringBuilder sb = new StringBuilder();
            sb.append("ALTER TABLE").append(" ").append("`").append(tableName).append("`").append(" ");
            sb.append("DROP COLUMN").append(" ").append("`").append(deleteColumn).append("`");
            executeUpdate(sb.toString(), Operation.DELETE);

            // 删除元数据信息
            executeUpdate(TableMetadata.deleteSql(tableName, deleteColumn), Operation.DELETE);
        }
    }

    private void addColumns(String tableName, List<Field> newFields) throws SQLException {
        Map<String, List<String>> uniqueGroups = new HashMap<>();
        Map<String, List<Pair<String, Integer>>> indexGroups = new HashMap<>();
        for (Field field : newFields) {
            // 新增字段
            ColumnDomain columnDomain = ColumnDomain.of(tableName, field);
            StringBuilder sb = new StringBuilder();
            sb.append("ALTER TABLE").append(" ").append("`").append(tableName).append("`").append(" ");
            String columnName = columnDomain.getName();
            sb.append("ADD").append(" ").append("`").append(columnName).append("`").append(" ").append(columnDomain.typeWithLength()).append(" ");

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
            executeUpdate(sb.toString(), Operation.ADD);

            // 新建唯一约束
            if (columnDomain.isUnique()) {
                String uniqueIndexName = columnDomain.uniqueKeyName(tableName);
                sb = new StringBuilder();
                sb.append("ALTER TABLE").append(" ").append("`").append(tableName).append("`").append(" ");
                sb.append("ADD CONSTRAINT").append(" ").append(uniqueIndexName).append(" ");
                sb.append("UNIQUE").append(" ").append("(").append("`").append(columnDomain.getName()).append("`").append(")");
                executeUpdate(sb.toString(), Operation.ADD);
            }

            // 判断联合唯一键
            if (CharSequenceUtil.isNotEmpty(columnDomain.getUniqueGroup())) {
                List<String> columns = uniqueGroups.getOrDefault(columnDomain.getUniqueGroup(), new ArrayList<>());
                columns.add(columnDomain.getName());
                uniqueGroups.put(columnDomain.getUniqueGroup(), columns);
            }

            // 判断索引
            if (columnDomain.isIndex()) {
                createIndex(tableName, columnDomain.getName());
            }

            // 判断联合索引
            if (CharSequenceUtil.isNotEmpty(columnDomain.getIndexGroup())) {
                List<Pair<String, Integer>> columns = indexGroups.getOrDefault(columnDomain.getIndexGroup(), new ArrayList<>());
                columns.add(Pair.of(columnDomain.getName(), columnDomain.getIndexGroupSort()));
                indexGroups.put(columnDomain.getIndexGroup(), columns);
            }

            // 新增元数据
            DataSource dataSource = this.jdbcTemplate.getDataSource();
            assert dataSource != null;
            try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
                String columnSignature = columnDomain.signature();
                final String insertSql = TableMetadata.insertSql(tableName, columnName, columnSignature);
                executeUpdate(insertSql, Operation.INSERT);
            }
        }

        // 创建联合唯一键
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
            StringBuilder sb = new StringBuilder();
            String uniqueKeyGroupName = ColumnDomain.generateUniqueKeyName(tableName, uniqueGroupName);
            sb.append("ALTER TABLE").append(" ").append("`").append(tableName).append("`").append(" ");
            sb.append("ADD CONSTRAINT").append(" ").append(uniqueKeyGroupName).append(" ");
            sb.append("UNIQUE").append(" ").append("(").append(uniqueKeys).append(")");
            executeUpdate(sb.toString(), Operation.ADD);
        }

        // 创建联合索引键
        for (Map.Entry<String, List<Pair<String, Integer>>> entry : indexGroups.entrySet()) {
            String indexGroupName = entry.getKey();
            List<Pair<String, Integer>> pairs = entry.getValue();
            // 排序
            pairs.sort(Comparator.comparingInt(Pair::getValue));

            List<String> indexNames = pairs.stream().map(Pair::getKey).toList();
            createIndex(tableName, indexGroupName, indexNames);
        }
    }

    private void updateColumns(String tableName, List<Field> updateFields, DatabaseMetaData metaData) throws SQLException {

        // 唯一索引
        Set<String> uniqueIndexNames = new LinkedHashSet<>();
        ResultSet uniqueIndexInfo = metaData.getIndexInfo(null, null, tableName, true, false);
        while (uniqueIndexInfo.next()) {
            String indexName = uniqueIndexInfo.getString("INDEX_NAME");
            // 主键和非唯一索引，跳过
            if (uniqueIndexInfo.getBoolean("NON_UNIQUE") || "PRIMARY".equalsIgnoreCase(indexName)) {
                continue;
            }
            uniqueIndexNames.add(indexName);
        }

        // 普通索引
        Set<String> indexNames = new LinkedHashSet<>();
        ResultSet indexInfo = metaData.getIndexInfo(null, null, tableName, false, false);
        while (indexInfo.next()) {
            String indexName = indexInfo.getString("INDEX_NAME");
            // 非唯一索引
            if (indexInfo.getBoolean("NON_UNIQUE")) {
                indexNames.add(indexName);
            }
        }

        for (Field field : updateFields) {
            // 字段信息变更
            ColumnDomain columnDomain = ColumnDomain.of(tableName, field);
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
            executeUpdate(sb.toString(), Operation.UPDATE);

            // 唯一约束变更
            String uniqueKeyName = ColumnDomain.generateUniqueKeyName(tableName, columnName);
            if (columnDomain.isUnique() && !uniqueIndexNames.contains(uniqueKeyName)) {
                createUniqueIndex(tableName, columnName);
            } else if (!columnDomain.isUnique() && uniqueIndexNames.contains(uniqueKeyName)) {
                dropUniqueIndex(tableName, columnName);
            }

            // 普通索引变更
            String indexNameKey = ColumnDomain.generateIndexName(tableName, columnDomain.getName());
            if (columnDomain.isIndex() && !indexNames.contains(indexNameKey)) {
                createIndex(tableName, columnName);
            } else if (!columnDomain.isIndex() && indexNames.contains(indexNameKey)) {
                dropIndex(tableName, columnName);
            }

            // 更新元信息
            Map<String, Object> result = executeSelect(TableMetadata.selectSql(tableName, columnName));
            // 元数据中不存在对应的字段记录，则表示需要新增
            if (result.isEmpty()) {
                // 新增元数据
                executeUpdate(TableMetadata.insertSql(tableName, columnName, columnDomain.signature()), Operation.INSERT);
            }
            // 元数据中存在对应的字段记录，则表示需要更新
            else {
                // 更新元数据
                executeUpdate(TableMetadata.updateSql(tableName, columnName, columnDomain.signature()), Operation.UPDATE);
            }

        }
    }

    private void createUniqueIndex(String tableName, String columnName) throws SQLException {
        String uniqueKeyName = ColumnDomain.generateUniqueKeyName(tableName, columnName);
        String uniqueIndexSql = String.format("ALTER TABLE `%s` ADD CONSTRAINT `%s` UNIQUE (`%s`)", tableName, uniqueKeyName, columnName);
        this.executeUpdate(uniqueIndexSql, Operation.CREATE);
    }

    private void dropUniqueIndex(String tableName, String columnName) throws SQLException {
        String uniqueKeyName = ColumnDomain.generateUniqueKeyName(tableName, columnName);
        this.executeUpdate("ALTER TABLE `%s` DROP KEY `%s`".formatted(tableName, uniqueKeyName), Operation.DROP);
    }

    private void createIndex(String tableName, String columnName) throws SQLException {
        String indexNameKey = ColumnDomain.generateIndexName(tableName, columnName);
        String indexSql = String.format("CREATE INDEX %s ON `%s` (`%s`)", indexNameKey, tableName, columnName);
        this.executeUpdate(indexSql, Operation.CREATE);
    }

    private void createIndex(String tableName, String indexGroupName, List<String> indexNames) throws SQLException {
        StringBuilder indexKeys = new StringBuilder();
        for (int i = 0; i < indexNames.size(); i++) {
            indexKeys.append("`").append(indexNames.get(i)).append("`");
            if (i != indexNames.size() - 1) {
                indexKeys.append(",").append(" ");
            }
        }
        String indexGroupNameKey = ColumnDomain.generateIndexName(tableName, indexGroupName);
        String indexSql = String.format("CREATE INDEX %s ON `%s` (%s)", indexGroupNameKey, tableName, indexKeys);
        this.executeUpdate(indexSql, Operation.CREATE);
    }

    private void dropIndex(String tableName, String columnName) throws SQLException {
        String indexNameKey = ColumnDomain.generateIndexName(tableName, columnName);
        this.executeUpdate("DROP INDEX `%s` on `%s`".formatted(indexNameKey, tableName), Operation.DROP);
    }

    public Map<String, Object> executeSelect(final String sql) throws SQLException {
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
        }).onFailure(e -> LoggerPrinter.error(log, e.getMessage(), e)).andThen(result -> {
            if (properties.isShowSql() && log.isDebugEnabled()) {
                if (properties.isFormatSql()) {
                    String formattedSql = SqlFormatter.format(sql);
                    LoggerPrinter.info(log, formattedSql);
                } else {
                    LoggerPrinter.info(log, sql);
                }
            }
        }).andFinallyTry(() -> connection.get().close()).get();
    }


    public void executeUpdate(final String sql, final Operation operation) throws SQLException {
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
            if (Operation.DELETE != operation) {
                LoggerPrinter.error(log, e.getMessage(), e);
            }
        }).andThen(result -> {
            if (properties.isShowSql() && result == 0) {
                if (properties.isFormatSql()) {
                    String formattedSql = SqlFormatter.format(sql);
                    LoggerPrinter.info(log, formattedSql);
                } else {
                    LoggerPrinter.info(log, sql);
                }
            }
        }).andFinallyTry(() -> connection.get().close());
    }

    private List<Field> getEntityFields(Class<?> clazz) {
        return ReflectUtils.getFields(clazz, field -> {
            EntityColumn entityColumn = field.getAnnotation(EntityColumn.class);
            if (entityColumn != null && !entityColumn.exist()) {
                return false;
            }
            TableField tableField = field.getAnnotation(TableField.class);
            if (tableField != null && !tableField.exist()) {
                return false;
            }
            Transient aTransient = field.getAnnotation(Transient.class);
            if (aTransient != null) {
                return false;
            }
            return !ReflectUtils.isStatic(field) && !ReflectUtils.isFinal(field);
        });
    }
}