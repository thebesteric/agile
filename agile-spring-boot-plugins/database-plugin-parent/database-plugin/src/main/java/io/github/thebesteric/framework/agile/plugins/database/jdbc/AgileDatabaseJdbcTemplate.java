package io.github.thebesteric.framework.agile.plugins.database.jdbc;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.db.sql.SqlFormatter;
import io.github.thebesteric.framework.agile.commons.util.CollectionUtils;
import io.github.thebesteric.framework.agile.commons.util.LoggerPrinter;
import io.github.thebesteric.framework.agile.core.domain.Pair;
import io.github.thebesteric.framework.agile.plugins.database.config.AgileDatabaseContext;
import io.github.thebesteric.framework.agile.plugins.database.config.AgileDatabaseProperties;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.ColumnDomain;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.EntityClassDomain;
import io.github.thebesteric.framework.agile.plugins.database.entity.TableMetadata;
import io.vavr.control.Try;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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
                String tableName = entityClassDomain.getTableName();
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
        String existsSql = TableMetadata.tableExistsSql(databaseName);
        Map<String, Object> result = executeSelect(existsSql);
        if (result != null && !result.isEmpty() && (Long) result.get("exists") == 0) {
            EntityClassDomain entityClassDomain = EntityClassDomain.of(null, TableMetadata.class);
            // 创建表
            createTable(entityClassDomain);
        }
    }

    public void createTable(EntityClassDomain entityClassDomain) throws SQLException {
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
        this.executeUpdate(sb.toString(), Operation.CREATE);

        // 主键设置为第一列
        if (primaryKey != null) {
            String firstColumnSql = "ALTER TABLE `%s` MODIFY COLUMN `%s` %s %s NOT NULL FIRST"
                    .formatted(tableName, primaryKey.getName(), primaryKey.typeWithLength(), primaryKey.isAutoIncrement() ? "AUTO_INCREMENT" : "");
            this.executeUpdate(firstColumnSql, Operation.UPDATE);
        }

        // 创建唯一索引（包含使用 @Unique 注解的类上的列）
        List<String> uniqueColumns = entityClassDomain.getOnClassUniqueColumns();
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

        // 插入元数据
        for (ColumnDomain columnDomain : columnDomains) {
            String selectSql = TableMetadata.selectSql(TableMetadata.MetadataType.COLUMN, columnDomain.getTableName(), columnDomain.getName());
            Map<String, Object> result = executeSelect(selectSql);
            if (!result.isEmpty()) {
                String signature = (String) result.get(TableMetadata.COLUMN_SIGNATURE);
                String currSignature = columnDomain.signature();
                if (!signature.equals(currSignature)) {
                    // 更新元数据
                    String insertSql = TableMetadata.updateSql(TableMetadata.MetadataType.COLUMN, columnDomain.getTableName(), columnDomain.getName(), currSignature);
                    executeUpdate(insertSql, Operation.UPDATE);
                }
            } else {
                // 插入元数据
                String insertSql = TableMetadata.insertSql(TableMetadata.MetadataType.COLUMN, columnDomain.getTableName(), columnDomain.getName(), columnDomain.signature());
                executeUpdate(insertSql, Operation.INSERT);
            }
        }

        // 更新表元数据信息
        updateTableMetaData(entityClassDomain, tableName);
    }

    private void updateTableMetaData(EntityClassDomain entityClassDomain, String tableName) throws SQLException {
        String selectSql = TableMetadata.selectSql(TableMetadata.MetadataType.TABLE, tableName, null);
        Map<String, Object> result = executeSelect(selectSql);
        if (!result.isEmpty()) {
            String signature = (String) result.get(TableMetadata.COLUMN_SIGNATURE);
            String currSignature = entityClassDomain.signature();
            if (!signature.equals(currSignature)) {
                // 更新元数据
                String insertSql = TableMetadata.updateSql(TableMetadata.MetadataType.TABLE, tableName, null, currSignature);
                executeUpdate(insertSql, Operation.UPDATE);
            }
        } else {
            String insertSql = TableMetadata.insertSql(TableMetadata.MetadataType.TABLE, tableName, null, entityClassDomain.signature());
            executeUpdate(insertSql, Operation.INSERT);
        }
    }

    public void updateTable(EntityClassDomain entityClassDomain, DatabaseMetaData metaData) throws SQLException {
        String tableName = entityClassDomain.getTableName();
        List<Field> fields = entityClassDomain.getEntityFields();
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

        List<ColumnDomain> columnDomains = new ArrayList<>();
        for (Field field : fields) {
            ColumnDomain columnDomain = ColumnDomain.of(tableName, field);
            columnDomains.add(columnDomain);
            String columnName = columnDomain.getName();

            // 查元数据表，对比签名
            String signature = null;
            String columnSignature = columnDomain.signature();
            final String selectSql = TableMetadata.selectSql(TableMetadata.MetadataType.COLUMN, tableName, columnName);
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
            deleteColumns(entityClassDomain, deleteColumns);
        }

        if (!updateFields.isEmpty()) {
            updateColumns(entityClassDomain, updateFields, metaData);
        }

        if (!newFields.isEmpty()) {
            addColumns(entityClassDomain, newFields);
        }

        // 更新表上的相关注解
        String currTableSignature = entityClassDomain.signature();
        final String selectSql = TableMetadata.selectSql(TableMetadata.MetadataType.TABLE, tableName, null);
        Map<String, Object> result = executeSelect(selectSql);
        // 元数据中存在对应的字段记录
        if (!result.isEmpty()) {
            String tableSignature = (String) result.get(TableMetadata.COLUMN_SIGNATURE);
            if (!currTableSignature.equals(tableSignature)) {
                // 执行更新表上的相关注解
                updateTableHeaderAnnotation(metaData, entityClassDomain, columnDomains);
            }
        }

        // 更新表元数据信息
        updateTableMetaData(entityClassDomain, tableName);
    }

    private void updateTableHeaderAnnotation(DatabaseMetaData metaData, EntityClassDomain entityClassDomain, List<ColumnDomain> columnDomains) throws SQLException {
        String tableName = entityClassDomain.getTableName();

        // 更新表注释
        String remarks = TableMetadata.tableRemarks(metaData, tableName);
        String comment = entityClassDomain.getComment();
        if (!Objects.equals(remarks, comment)) {
            executeUpdate("ALTER TABLE `%s` COMMENT '%s'".formatted(tableName, comment), Operation.UPDATE);
        }

        // 更新普通索引
        updateTableHeaderAnnotationByIndex(metaData, entityClassDomain, columnDomains);

        // 更新唯一索引
        updateTableHeaderAnnotationByUniqueIndex(metaData, entityClassDomain, columnDomains);

        // 更新表元数据
        String updateSql = TableMetadata.updateSql(TableMetadata.MetadataType.TABLE, tableName, null, entityClassDomain.signature());
        executeUpdate(updateSql, Operation.UPDATE);
    }

    private void updateTableHeaderAnnotationByUniqueIndex(DatabaseMetaData metaData, EntityClassDomain entityClassDomain, List<ColumnDomain> columnDomains) throws SQLException {
        String tableName = entityClassDomain.getTableName();
        // 唯一索引（现存的）
        Set<String> inDatabaseUniqueIndexNames = TableMetadata.uniqueIndexNames(metaData, tableName);

        // 列上的所有的唯一索引
        Set<String> columnUniqueIndexNames = columnDomains.stream().filter(ColumnDomain::isUnique)
                .map(c -> ColumnDomain.generateUniqueKeyName(tableName, c.getName())).collect(Collectors.toSet());
        // 列上的所有唯一索引组
        Set<String> columnUniqueGroupIndexNames = columnDomains.stream().filter(c -> CharSequenceUtil.isNotBlank(c.getUniqueGroup()))
                .collect(Collectors.groupingBy(ColumnDomain::getUniqueGroup))
                .keySet().stream().map(s -> ColumnDomain.generateUniqueKeyName(tableName, s)).collect(Collectors.toSet());
        // 合并唯一索引
        columnUniqueIndexNames.addAll(columnUniqueGroupIndexNames);

        // 类上当前索引（最新的）
        List<String> onClassUniqueIndexColumns = entityClassDomain.getOnClassUniqueColumns();
        List<List<String>> onClassUniqueIndexGroupColumns = entityClassDomain.getOnClassUniqueGroupColumns();

        // 更新索引逻辑
        // 创建新的的索引
        for (String onClassUniqueIndexColumn : onClassUniqueIndexColumns) {
            String onClassUniqueIndexName = ColumnDomain.generateUniqueKeyName(tableName, onClassUniqueIndexColumn);
            if (!inDatabaseUniqueIndexNames.contains(onClassUniqueIndexName)) {
                if (columnUniqueIndexNames.stream().anyMatch(onClassUniqueIndexName::equals)) {
                    continue;
                }
                createUniqueIndex(tableName, onClassUniqueIndexColumn);
            }
        }
        // 创建新的的索引组
        for (List<String> onClassUniqueIndexGroupColumn : onClassUniqueIndexGroupColumns) {
            String onClassUniqueIndexGroupName = ColumnDomain.generateUniqueKeyName(tableName, String.join("_", onClassUniqueIndexGroupColumn));
            if (!inDatabaseUniqueIndexNames.contains(onClassUniqueIndexGroupName)) {
                if (columnUniqueIndexNames.stream().anyMatch(onClassUniqueIndexGroupName::equals)) {
                    continue;
                }
                createUniqueGroupIndex(tableName, onClassUniqueIndexGroupName, onClassUniqueIndexGroupColumn, false);
            }
        }

        // 删除不存在的索引
        for (String uniqueIndexName : inDatabaseUniqueIndexNames) {
            boolean uniqueIndexNameOnClass = false;
            boolean uniqueIndexGroupNameOnClass = false;
            // 判断当前的索引是否在类上
            for (String onCLassUniqueIndexColumn : onClassUniqueIndexColumns) {
                String onClassUniqueIndexName = ColumnDomain.generateUniqueKeyName(tableName, onCLassUniqueIndexColumn);
                if (uniqueIndexName.equals(onClassUniqueIndexName)) {
                    uniqueIndexNameOnClass = true;
                    break;
                }
            }
            // 判断当前的索引组是否在类上
            for (List<String> uniqueIndexGroupColumns : onClassUniqueIndexGroupColumns) {
                String onClassUniqueIndexGroupName = ColumnDomain.generateUniqueKeyName(tableName, String.join("_", uniqueIndexGroupColumns));
                if (uniqueIndexName.equals(onClassUniqueIndexGroupName)) {
                    uniqueIndexGroupNameOnClass = true;
                    break;
                }
            }

            // 当前索引不在字段上，同时也不在类上，则删除
            if (columnUniqueIndexNames.stream().noneMatch(uniqueIndexName::equals) && !uniqueIndexNameOnClass && !uniqueIndexGroupNameOnClass) {
                dropUniqueIndex(tableName, uniqueIndexName, false);
            }
        }

    }

    private void updateTableHeaderAnnotationByIndex(DatabaseMetaData metaData, EntityClassDomain entityClassDomain, List<ColumnDomain> columnDomains) throws SQLException {
        String tableName = entityClassDomain.getTableName();
        // 普通索引（现存的）
        Set<String> indexNames = TableMetadata.indexNames(metaData, tableName);

        // 列上的所有的索引
        Set<String> columnIndexNames = columnDomains.stream().filter(ColumnDomain::isIndex)
                .map(c -> ColumnDomain.generateIndexKeyName(tableName, c.getName())).collect(Collectors.toSet());
        // 列上的所有索引组
        Set<String> columnGroupIndexNames = columnDomains.stream().filter(c -> CharSequenceUtil.isNotBlank(c.getIndexGroup()))
                .collect(Collectors.groupingBy(ColumnDomain::getIndexGroup))
                .keySet().stream().map(s -> ColumnDomain.generateIndexKeyName(tableName, s)).collect(Collectors.toSet());
        // 合并索引
        columnIndexNames.addAll(columnGroupIndexNames);

        // 类上当前索引（最新的）
        List<String> onClassIndexColumns = entityClassDomain.getOnClassIndexColumns();
        List<List<String>> onClassIndexGroupColumns = entityClassDomain.getOnClassIndexGroupColumns();

        // 更新索引逻辑
        // 创建新的的索引
        for (String onClassIndexColumn : onClassIndexColumns) {
            String onClassIndexName = ColumnDomain.generateIndexKeyName(tableName, onClassIndexColumn);
            if (!indexNames.contains(onClassIndexName)) {
                if (columnIndexNames.stream().anyMatch(onClassIndexName::equals)) {
                    continue;
                }
                createIndex(tableName, onClassIndexColumn);
            }
        }
        // 创建新的的索引组
        for (List<String> onClassIndexGroupColumn : onClassIndexGroupColumns) {
            String onClassIndexGroupName = ColumnDomain.generateIndexKeyName(tableName, String.join("_", onClassIndexGroupColumn));
            if (!indexNames.contains(onClassIndexGroupName)) {
                if (columnIndexNames.stream().anyMatch(onClassIndexGroupName::equals)) {
                    continue;
                }
                createGroupIndex(tableName, onClassIndexGroupName, onClassIndexGroupColumn, false);
            }
        }

        // 删除不存在的索引
        for (String indexName : indexNames) {
            boolean indexNameOnClass = false;
            boolean indexGroupNameOnClass = false;
            // 判断当前的索引是否在类上
            for (String onCLassIndexColumn : onClassIndexColumns) {
                String onClassIndexName = ColumnDomain.generateIndexKeyName(tableName, onCLassIndexColumn);
                if (indexName.equals(onClassIndexName)) {
                    indexNameOnClass = true;
                    break;
                }
            }
            // 判断当前的索引组是否在类上
            for (List<String> indexGroupColumns : onClassIndexGroupColumns) {
                String onClassIndexGroupName = ColumnDomain.generateIndexKeyName(tableName, String.join("_", indexGroupColumns));
                if (indexName.equals(onClassIndexGroupName)) {
                    indexGroupNameOnClass = true;
                    break;
                }
            }


            // 当前索引不在字段上，同时也不在类上，则删除
            if (columnIndexNames.stream().noneMatch(indexName::equals) && !indexNameOnClass && !indexGroupNameOnClass) {
                dropIndex(tableName, indexName, false);
            }
        }
    }

    private void deleteColumns(EntityClassDomain entityClassDomain, List<String> deleteColumns) throws SQLException {
        String tableName = entityClassDomain.getTableName();
        for (String deleteColumn : deleteColumns) {
            StringBuilder sb = new StringBuilder();
            sb.append("ALTER TABLE").append(" ").append("`").append(tableName).append("`").append(" ");
            sb.append("DROP COLUMN").append(" ").append("`").append(deleteColumn).append("`");
            executeUpdate(sb.toString(), Operation.DELETE);

            // 删除元数据信息
            executeUpdate(TableMetadata.deleteSql(TableMetadata.MetadataType.COLUMN, tableName, deleteColumn), Operation.DELETE);
        }
    }

    private void addColumns(EntityClassDomain entityClassDomain, List<Field> newFields) throws SQLException {
        String tableName = entityClassDomain.getTableName();
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
                final String insertSql = TableMetadata.insertSql(TableMetadata.MetadataType.COLUMN, tableName, columnName, columnSignature);
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
            createIndex(tableName, indexGroupName, indexNames, true);
        }
    }

    private void updateColumns(EntityClassDomain entityClassDomain, List<Field> updateFields, DatabaseMetaData metaData) throws SQLException {
        String tableName = entityClassDomain.getTableName();

        // 唯一索引（数据库）
        Set<String> inDatabaseUniqueIndexNames = TableMetadata.uniqueIndexNames(metaData, tableName);
        // 普通索引（数据库）
        Set<String> inDatabaseIndexNames = TableMetadata.indexNames(metaData, tableName);

        Map<String, List<String>> uniqueGroups = new HashMap<>();
        Map<String, List<Pair<String, Integer>>> indexGroups = new HashMap<>();

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
            List<String> onClassUniqueIndexColumns = entityClassDomain.getOnClassUniqueColumns();
            String uniqueKeyName = ColumnDomain.generateUniqueKeyName(tableName, columnName);
            if (columnDomain.isUnique() && !inDatabaseUniqueIndexNames.contains(uniqueKeyName) && !onClassUniqueIndexColumns.contains(columnName)) {
                createUniqueIndex(tableName, columnName);
                // 从唯一索引（数据库）添加
                inDatabaseUniqueIndexNames.add(uniqueKeyName);

            } else if (!columnDomain.isUnique() && inDatabaseUniqueIndexNames.contains(uniqueKeyName) && !onClassUniqueIndexColumns.contains(columnName)) {
                dropUniqueIndex(tableName, columnName, true);
                // 从唯一索引（数据库）删除
                inDatabaseUniqueIndexNames.remove(uniqueKeyName);
            }

            // 普通索引变更
            List<String> onClassIndexColumns = entityClassDomain.getOnClassIndexColumns();
            String indexNameKey = ColumnDomain.generateIndexKeyName(tableName, columnDomain.getName());
            if (columnDomain.isIndex() && !inDatabaseIndexNames.contains(indexNameKey) && !onClassIndexColumns.contains(columnName)) {
                createIndex(tableName, columnName);
                // 从普通索引（数据库）添加
                inDatabaseIndexNames.add(indexNameKey);
            } else if (!columnDomain.isIndex() && inDatabaseIndexNames.contains(indexNameKey) && !onClassIndexColumns.contains(columnName)) {
                dropIndex(tableName, columnName, true);
                // 从普通索引（数据库）删除
                inDatabaseIndexNames.remove(indexNameKey);
            }

            // 判断联合唯一键
            if (CharSequenceUtil.isNotEmpty(columnDomain.getUniqueGroup())) {
                List<String> columns = uniqueGroups.getOrDefault(columnDomain.getUniqueGroup(), new ArrayList<>());
                columns.add(columnDomain.getName());
                uniqueGroups.put(columnDomain.getUniqueGroup(), columns);
            }

            // 判断联合索引
            if (CharSequenceUtil.isNotEmpty(columnDomain.getIndexGroup())) {
                List<Pair<String, Integer>> columns = indexGroups.getOrDefault(columnDomain.getIndexGroup(), new ArrayList<>());
                columns.add(Pair.of(columnDomain.getName(), columnDomain.getIndexGroupSort()));
                indexGroups.put(columnDomain.getIndexGroup(), columns);
            }

            // 更新元信息
            Map<String, Object> result = executeSelect(TableMetadata.selectSql(TableMetadata.MetadataType.COLUMN, tableName, columnName));
            // 元数据中不存在对应的字段记录，则表示需要新增
            if (result.isEmpty()) {
                // 新增元数据
                executeUpdate(TableMetadata.insertSql(TableMetadata.MetadataType.COLUMN, tableName, columnName, columnDomain.signature()), Operation.INSERT);
            }
            // 元数据中存在对应的字段记录，则表示需要更新
            else {
                // 更新元数据
                executeUpdate(TableMetadata.updateSql(TableMetadata.MetadataType.COLUMN, tableName, columnName, columnDomain.signature()), Operation.UPDATE);
            }
        }

        // 更新普通索引
        updateIndex(inDatabaseIndexNames, entityClassDomain);

        // 更新唯一索引
        updateUniqueIndex(inDatabaseUniqueIndexNames, entityClassDomain);
    }

    private void updateUniqueIndex(Set<String> inDatabaseUniqueIndexNames, EntityClassDomain entityClassDomain) throws SQLException {
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
                createUniqueGroupIndex(tableName, uniqueGroupKey, entry.getValue(), false);
            }
        }

        // 删除不需要的唯一索引组
        for (String uniqueIndexName : inDatabaseUniqueIndexNames) {
            Set<String> uniqueGroupKeys = uniqueGroupMap.keySet();
            if (!uniqueGroupKeys.contains(uniqueIndexName) && !allUniqueNames.contains(uniqueIndexName)) {
                dropUniqueIndex(tableName, uniqueIndexName, false);
            }
        }
    }

    private void updateIndex(Set<String> indexNames, EntityClassDomain entityClassDomain) throws SQLException {
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
                createGroupIndex(tableName, indexGroupKey, entry.getValue(), false);
            }
        }

        // 删除不需要的普通索引组
        for (String indexName : indexNames) {
            Set<String> indexGroupKeys = indexGroupMap.keySet();
            if (!indexGroupKeys.contains(indexName) && !allIndexNames.contains(indexName)) {
                dropIndex(tableName, indexName, false);
            }
        }
    }

    private void createUniqueIndex(String tableName, String columnName) throws SQLException {
        String uniqueKeyName = ColumnDomain.generateUniqueKeyName(tableName, columnName);
        String uniqueIndexSql = String.format("ALTER TABLE `%s` ADD CONSTRAINT `%s` UNIQUE (`%s`)", tableName, uniqueKeyName, columnName);
        this.executeUpdate(uniqueIndexSql, Operation.CREATE);
    }

    private void createUniqueGroupIndex(String tableName, String uniqueGroupName, List<String> columnNames, boolean generateUniqueIndexName) throws SQLException {
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

    private void createUniqueGroupIndex(String tableName, List<String> columnNames) throws SQLException {
        String uniqueGroupName = tableName + ColumnDomain.UNIQUE_KEY_PREFIX_SUFFIX + String.join("_", columnNames);
        createUniqueGroupIndex(tableName, uniqueGroupName, columnNames, true);
    }

    private void dropUniqueIndex(String tableName, String name, boolean generateIndexName) throws SQLException {
        String uniqueKeyName = generateIndexName ? ColumnDomain.generateUniqueKeyName(tableName, name) : name;
        this.executeUpdate("ALTER TABLE `%s` DROP KEY `%s`".formatted(tableName, uniqueKeyName), Operation.DROP);
    }

    private void createIndex(String tableName, String columnName) throws SQLException {
        String indexNameKey = ColumnDomain.generateIndexKeyName(tableName, columnName);
        String indexSql = String.format("CREATE INDEX %s ON `%s` (`%s`)", indexNameKey, tableName, columnName);
        this.executeUpdate(indexSql, Operation.CREATE);
    }

    private void createIndex(String tableName, String indexGroupName, List<String> columnNames, boolean generateIndexName) throws SQLException {
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

    private void createGroupIndex(String tableName, List<String> columnNames) throws SQLException {
        String indexGroupName = String.join("_", columnNames);
        createGroupIndex(tableName, indexGroupName, columnNames, true);
    }

    private void createGroupIndex(String tableName, String indexGroupName, List<String> columnNames, boolean generateIndexName) throws SQLException {
        createIndex(tableName, indexGroupName, columnNames, generateIndexName);
    }

    private void dropIndex(String tableName, String name, boolean generateIndexName) throws SQLException {
        String indexNameKey = generateIndexName ? ColumnDomain.generateIndexKeyName(tableName, name) : name;
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
        }).onFailure(e -> LoggerPrinter.error(log, e.getMessage() + ": {}", sql, e)).andThen(result -> {
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
            // 更新或新建重复数据，忽略
            if (e instanceof SQLSyntaxErrorException && e.getMessage().startsWith("Duplicate key")) {
                LoggerPrinter.warn(log, e.getMessage() + ": {}", sql);
                return;
            }
            if (Operation.DELETE != operation) {
                LoggerPrinter.error(log, e.getMessage() + ": {}", sql, e);
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
}