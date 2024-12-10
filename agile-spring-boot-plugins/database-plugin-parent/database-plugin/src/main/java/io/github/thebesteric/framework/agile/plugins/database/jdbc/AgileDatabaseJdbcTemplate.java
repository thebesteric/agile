package io.github.thebesteric.framework.agile.plugins.database.jdbc;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import io.github.thebesteric.framework.agile.core.domain.Pair;
import io.github.thebesteric.framework.agile.plugins.database.config.AgileDatabaseContext;
import io.github.thebesteric.framework.agile.plugins.database.config.AgileDatabaseProperties;
import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityClass;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.*;
import io.github.thebesteric.framework.agile.plugins.database.core.jdbc.JdbcTemplateHelper;
import io.github.thebesteric.framework.agile.plugins.database.core.jdbc.TableMetadataHelper;
import io.github.thebesteric.framework.agile.plugins.database.core.listener.EntityClassCreateListener;
import io.github.thebesteric.framework.agile.plugins.database.core.listener.EntityClassUpdateListener;
import io.github.thebesteric.framework.agile.plugins.database.entity.AgileTableMetadata;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
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
    private final JdbcTemplateHelper jdbcTemplateHelper;
    private final AgileDatabaseProperties properties;

    public AgileDatabaseJdbcTemplate(AgileDatabaseContext context, DataSource dataSource, PlatformTransactionManager transactionManager, AgileDatabaseProperties properties) throws SQLException {
        this.context = context;
        this.jdbcTemplateHelper = new JdbcTemplateHelper(dataSource, transactionManager);
        this.jdbcTemplate = this.jdbcTemplateHelper.getJdbcTemplate();
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
        String databaseName = jdbcTemplateHelper.getDatabaseName();
        assert dataSource != null;
        Set<Class<?>> entityClasses = context.getEntityClasses();

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            // 初始化元数据表
            initTableMeta();
            for (Class<?> clazz : entityClasses) {
                EntityClass entityClass = clazz.getAnnotation(EntityClass.class);
                if (entityClass != null) {
                    String[] schemas = entityClass.schemas();
                    if (schemas.length > 0 && Arrays.stream(schemas).noneMatch(schema -> Objects.equals(schema, databaseName))) {
                        continue;
                    }
                }
                EntityClassDomain entityClassDomain = EntityClassDomain.of(properties.getTableNamePrefix(), clazz);
                String tableName = entityClassDomain.getTableName();
                String catalog = connection.getCatalog();
                ResultSet resultSet = metaData.getTables(catalog, null, tableName, new String[]{"TABLE"});
                if (!resultSet.next()) {
                    EntityClassCreateListener createListener = null;
                    // 创建表：执行前置处理
                    if (EntityClassCreateListener.class.isAssignableFrom(clazz)) {
                        createListener = (EntityClassCreateListener) clazz.getDeclaredConstructor().newInstance();
                        entityClassDomain = createListener.preCreateTable(entityClassDomain);
                    }
                    // 创建表
                    if (entityClassDomain != null) {
                        createTable(entityClassDomain);
                    }
                    // 创建表：执行后置处理
                    if (createListener != null) {
                        createListener.postCreateTable();
                    }
                } else {
                    if (update) {
                        ChangeFields changeFields = getChangeFields(entityClassDomain, metaData);
                        EntityClassUpdateListener updateListener = null;
                        // 更新表：执行前置处理
                        if (EntityClassUpdateListener.class.isAssignableFrom(clazz)) {
                            updateListener = (EntityClassUpdateListener) clazz.getDeclaredConstructor().newInstance();
                            changeFields = updateListener.preUpdateTable(changeFields);
                        }
                        // 更新表
                        if (changeFields != null) {
                            updateTable(changeFields, metaData);
                        }
                        // 更新表：执行后置处理
                        if (updateListener != null) {
                            updateListener.postUpdateTable();
                        }

                    }
                }
            }
            // 创建外键（只会在新增表的时候生效）
            jdbcTemplateHelper.createForeignKeyOnCreated();
        }
    }

    private void initTableMeta() throws SQLException {
        boolean tableExists = this.jdbcTemplateHelper.tableExists(AgileTableMetadata.TABLE_NAME);
        if (!tableExists) {
            EntityClassDomain entityClassDomain = EntityClassDomain.of(AgileTableMetadata.class);
            createTable(entityClassDomain);
        }
    }

    public void createTable(EntityClassDomain entityClassDomain) throws SQLException {
        // 创建表
        List<ColumnDomain> columnDomains = this.jdbcTemplateHelper.createTable(entityClassDomain, properties.isShowSql(), properties.isFormatSql());

        // 插入元数据
        for (ColumnDomain columnDomain : columnDomains) {
            String selectSql = AgileTableMetadata.selectSql(AgileTableMetadata.MetadataType.COLUMN, columnDomain.getTableName(), columnDomain.getName());
            Map<String, Object> result = executeSelect(selectSql);
            if (!result.isEmpty()) {
                String signature = (String) result.get(AgileTableMetadata.COLUMN_SIGNATURE);
                String currSignature = columnDomain.signature();
                if (!signature.equals(currSignature)) {
                    // 更新元数据
                    String insertSql = AgileTableMetadata.updateSql(AgileTableMetadata.MetadataType.COLUMN, columnDomain.getTableName(), columnDomain.getName(), currSignature);
                    executeUpdate(insertSql, JdbcTemplateHelper.Operation.UPDATE);
                }
            } else {
                // 插入元数据
                String insertSql = AgileTableMetadata.insertSql(AgileTableMetadata.MetadataType.COLUMN, columnDomain.getTableName(), columnDomain.getName(), columnDomain.signature());
                executeUpdate(insertSql, JdbcTemplateHelper.Operation.INSERT);
            }
        }

        // 更新表元数据信息
        updateTableMetaData(entityClassDomain);
    }

    private void updateTableMetaData(EntityClassDomain entityClassDomain) throws SQLException {
        String tableName = entityClassDomain.getTableName();
        String selectSql = AgileTableMetadata.selectSql(AgileTableMetadata.MetadataType.TABLE, tableName, null);
        Map<String, Object> result = executeSelect(selectSql);
        if (!result.isEmpty()) {
            String signature = (String) result.get(AgileTableMetadata.COLUMN_SIGNATURE);
            String currSignature = entityClassDomain.signature();
            if (!signature.equals(currSignature)) {
                // 更新元数据
                String insertSql = AgileTableMetadata.updateSql(AgileTableMetadata.MetadataType.TABLE, tableName, null, currSignature);
                executeUpdate(insertSql, JdbcTemplateHelper.Operation.UPDATE);
            }
        } else {
            String insertSql = AgileTableMetadata.insertSql(AgileTableMetadata.MetadataType.TABLE, tableName, null, entityClassDomain.signature());
            executeUpdate(insertSql, JdbcTemplateHelper.Operation.INSERT);
        }
    }

    public void updateTable(ChangeFields changeFields, DatabaseMetaData metaData) throws SQLException {
        EntityClassDomain entityClassDomain = changeFields.getEntityClassDomain();
        String tableName = entityClassDomain.getTableName();

        // 当前所有字段
        List<ColumnDomain> columnDomains = changeFields.getColumnDomains();

        // 待删除的字段
        List<String> deleteColumns = changeFields.getDeleteColumns();
        if (!deleteColumns.isEmpty() && properties.isDeleteColumn()) {
            deleteColumns(metaData, entityClassDomain, deleteColumns);
        }

        // 待更新的字段
        List<Field> updateFields = changeFields.getUpdateFields();
        if (!updateFields.isEmpty()) {
            updateColumns(entityClassDomain, updateFields, metaData);
        }

        // 待新增的字段
        List<Field> newFields = changeFields.getNewFields();
        if (!newFields.isEmpty()) {
            createColumns(entityClassDomain, newFields);
        }

        // 更新表上的相关注解
        String currTableSignature = entityClassDomain.signature();
        final String selectSql = AgileTableMetadata.selectSql(AgileTableMetadata.MetadataType.TABLE, tableName, null);
        Map<String, Object> result = executeSelect(selectSql);
        // 元数据中存在对应的字段记录
        if (!result.isEmpty()) {
            String tableSignature = (String) result.get(AgileTableMetadata.COLUMN_SIGNATURE);
            if (!currTableSignature.equals(tableSignature)) {
                // 执行更新表上的相关注解
                updateTableHeaderAnnotation(metaData, entityClassDomain, columnDomains);
            }
        }

        // 更新表元数据信息
        updateTableMetaData(entityClassDomain);
    }

    public ChangeFields getChangeFields(EntityClassDomain entityClassDomain, DatabaseMetaData metaData) throws SQLException {
        String tableName = entityClassDomain.getTableName();
        List<Field> fields = entityClassDomain.getEntityFields();

        // 表的所有字段名称
        Set<TableColumn> tableColumns = TableMetadataHelper.tableColumns(metaData, tableName);
        Set<String> columnNames = tableColumns.stream().map(TableColumn::getColumnName).collect(Collectors.toSet());

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
            final String selectSql = AgileTableMetadata.selectSql(AgileTableMetadata.MetadataType.COLUMN, tableName, columnName);
            Map<String, Object> result = executeSelect(selectSql);
            // 元数据中存在对应的字段记录
            if (!result.isEmpty()) {
                signature = (String) result.get(AgileTableMetadata.COLUMN_SIGNATURE);
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

        return ChangeFields.of(entityClassDomain, newFields, updateFields, deleteColumns, columnDomains);
    }

    private void updateTableHeaderAnnotation(DatabaseMetaData metaData, EntityClassDomain entityClassDomain, List<ColumnDomain> columnDomains) throws SQLException {
        String tableName = entityClassDomain.getTableName();

        // 更新表注释
        String remarks = TableMetadataHelper.tableRemarks(metaData, tableName);
        String comment = entityClassDomain.getComment();
        if (!Objects.equals(remarks, comment)) {
            this.jdbcTemplateHelper.updateTableComments(tableName, comment);
        }

        // 更新普通索引
        updateTableHeaderAnnotationByIndex(metaData, entityClassDomain, columnDomains);

        // 更新唯一索引
        updateTableHeaderAnnotationByUniqueIndex(metaData, entityClassDomain, columnDomains);

        // 更新表元数据
        String updateSql = AgileTableMetadata.updateSql(AgileTableMetadata.MetadataType.TABLE, tableName, null, entityClassDomain.signature());
        executeUpdate(updateSql, JdbcTemplateHelper.Operation.UPDATE);
    }

    private void updateTableHeaderAnnotationByUniqueIndex(DatabaseMetaData metaData, EntityClassDomain entityClassDomain, List<ColumnDomain> columnDomains) throws SQLException {
        String tableName = entityClassDomain.getTableName();
        // 唯一索引（现存的）
        Set<String> inDatabaseUniqueIndexNames = TableMetadataHelper.uniqueIndexNames(metaData, tableName);

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
                this.jdbcTemplateHelper.createUniqueIndex(tableName, onClassUniqueIndexColumn);
            }
        }
        // 创建新的的索引组
        for (List<String> onClassUniqueIndexGroupColumn : onClassUniqueIndexGroupColumns) {
            String onClassUniqueIndexGroupName = ColumnDomain.generateUniqueKeyName(tableName, String.join("_", onClassUniqueIndexGroupColumn));
            if (!inDatabaseUniqueIndexNames.contains(onClassUniqueIndexGroupName)) {
                if (columnUniqueIndexNames.stream().anyMatch(onClassUniqueIndexGroupName::equals)) {
                    continue;
                }
                this.jdbcTemplateHelper.createUniqueGroupIndex(tableName, onClassUniqueIndexGroupName, onClassUniqueIndexGroupColumn, false);
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
                this.jdbcTemplateHelper.dropUniqueIndex(tableName, uniqueIndexName, false);
            }
        }

    }

    private void updateTableHeaderAnnotationByIndex(DatabaseMetaData metaData, EntityClassDomain entityClassDomain, List<ColumnDomain> columnDomains) throws SQLException {
        String tableName = entityClassDomain.getTableName();
        // 普通索引（现存的）
        Set<String> indexNames = TableMetadataHelper.indexNames(metaData, tableName);

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
                this.jdbcTemplateHelper.createIndex(tableName, onClassIndexColumn);
            }
        }
        // 创建新的的索引组
        for (List<String> onClassIndexGroupColumn : onClassIndexGroupColumns) {
            String onClassIndexGroupName = ColumnDomain.generateIndexKeyName(tableName, String.join("_", onClassIndexGroupColumn));
            if (!indexNames.contains(onClassIndexGroupName)) {
                if (columnIndexNames.stream().anyMatch(onClassIndexGroupName::equals)) {
                    continue;
                }
                this.jdbcTemplateHelper.createGroupIndex(tableName, onClassIndexGroupName, onClassIndexGroupColumn, false);
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
                this.jdbcTemplateHelper.dropIndex(tableName, indexName, false);
            }
        }
    }

    private void deleteColumns(DatabaseMetaData metaData, EntityClassDomain entityClassDomain, List<String> deleteColumns) throws SQLException {
        String tableName = entityClassDomain.getTableName();
        for (String deleteColumn : deleteColumns) {
            jdbcTemplateHelper.deleteColumn(metaData, tableName, deleteColumn);
            // 删除元数据信息
            executeUpdate(AgileTableMetadata.deleteSql(AgileTableMetadata.MetadataType.COLUMN, tableName, deleteColumn), JdbcTemplateHelper.Operation.DELETE);
        }
    }

    private void createColumns(EntityClassDomain entityClassDomain, List<Field> newFields) throws SQLException {
        String tableName = entityClassDomain.getTableName();
        Map<String, List<String>> uniqueGroups = new HashMap<>();
        Map<String, List<Pair<String, Integer>>> indexGroups = new HashMap<>();
        for (Field field : newFields) {
            // 新增字段
            ColumnDomain columnDomain = this.jdbcTemplateHelper.addColumn(tableName, field);

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

            // 新增元数据
            DataSource dataSource = this.jdbcTemplate.getDataSource();
            assert dataSource != null;
            try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
                String columnSignature = columnDomain.signature();
                final String insertSql = AgileTableMetadata.insertSql(AgileTableMetadata.MetadataType.COLUMN, tableName, columnDomain.getName(), columnSignature);
                executeUpdate(insertSql, JdbcTemplateHelper.Operation.INSERT);
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
            executeUpdate(sb.toString(), JdbcTemplateHelper.Operation.ADD);
        }

        // 创建联合索引键
        for (Map.Entry<String, List<Pair<String, Integer>>> entry : indexGroups.entrySet()) {
            String indexGroupName = entry.getKey();
            List<Pair<String, Integer>> pairs = entry.getValue();
            // 排序
            pairs.sort(Comparator.comparingInt(Pair::getValue));

            List<String> indexNames = pairs.stream().map(Pair::getKey).toList();
            this.jdbcTemplateHelper.createIndex(tableName, indexGroupName, indexNames, true);
        }
    }

    private void updateColumns(EntityClassDomain entityClassDomain, List<Field> updateFields, DatabaseMetaData metaData) throws SQLException {
        String tableName = entityClassDomain.getTableName();

        // 唯一索引（数据库）
        Set<String> inDatabaseUniqueIndexNames = TableMetadataHelper.uniqueIndexNames(metaData, tableName);
        // 普通索引（数据库）
        Set<String> inDatabaseIndexNames = TableMetadataHelper.indexNames(metaData, tableName);
        // 外键（数据库）
        Set<String> inDatabaseForeignKeyNames = TableMetadataHelper.foreignKeyDomains(metaData, tableName).stream().map(ReferenceDomain::getForeignKeyName).collect(Collectors.toSet());
        // 外键（当前）
        Set<String> currentForeignKeyNames = new HashSet<>();

        Map<String, List<String>> uniqueGroups = new HashMap<>();
        Map<String, List<Pair<String, Integer>>> indexGroups = new HashMap<>();

        for (Field field : updateFields) {
            // 字段信息变更
            ColumnDomain columnDomain = this.jdbcTemplateHelper.updateColumn(tableName, field);
            String columnName = columnDomain.getName();

            // 外键变更
            ReferenceDomain reference = columnDomain.getReference();
            if (reference != null) {
                String foreignKeyName = reference.getForeignKeyName();
                if (!inDatabaseForeignKeyNames.contains(foreignKeyName)) {
                    // 创建外键
                    this.jdbcTemplateHelper.createForeignKey(tableName, reference);
                    currentForeignKeyNames.add(foreignKeyName);
                    inDatabaseForeignKeyNames.add(foreignKeyName);
                }
            }

            // 唯一约束变更
            List<String> onClassUniqueIndexColumns = entityClassDomain.getOnClassUniqueColumns();
            String uniqueKeyName = ColumnDomain.generateUniqueKeyName(tableName, columnName);
            if (columnDomain.isUnique() && !inDatabaseUniqueIndexNames.contains(uniqueKeyName) && !onClassUniqueIndexColumns.contains(columnName)) {
                this.jdbcTemplateHelper.createUniqueIndex(tableName, columnName);
                // 从唯一索引（数据库）添加
                inDatabaseUniqueIndexNames.add(uniqueKeyName);

            } else if (!columnDomain.isUnique() && inDatabaseUniqueIndexNames.contains(uniqueKeyName) && !onClassUniqueIndexColumns.contains(columnName)) {
                this.jdbcTemplateHelper.dropUniqueIndex(tableName, columnName, true);
                // 从唯一索引（数据库）删除
                inDatabaseUniqueIndexNames.remove(uniqueKeyName);
            }

            // 普通索引变更
            List<String> onClassIndexColumns = entityClassDomain.getOnClassIndexColumns();
            String indexNameKey = ColumnDomain.generateIndexKeyName(tableName, columnDomain.getName());
            if (columnDomain.isIndex() && !inDatabaseIndexNames.contains(indexNameKey) && !onClassIndexColumns.contains(columnName)) {
                this.jdbcTemplateHelper.createIndex(tableName, columnName);
                // 从普通索引（数据库）添加
                inDatabaseIndexNames.add(indexNameKey);
            } else if (!columnDomain.isIndex() && inDatabaseIndexNames.contains(indexNameKey) && !onClassIndexColumns.contains(columnName)) {
                this.jdbcTemplateHelper.dropIndex(tableName, columnName, true);
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
            Map<String, Object> result = executeSelect(AgileTableMetadata.selectSql(AgileTableMetadata.MetadataType.COLUMN, tableName, columnName));
            // 元数据中不存在对应的字段记录，则表示需要新增
            if (result.isEmpty()) {
                // 新增元数据
                executeUpdate(AgileTableMetadata.insertSql(AgileTableMetadata.MetadataType.COLUMN, tableName, columnName, columnDomain.signature()), JdbcTemplateHelper.Operation.INSERT);
            }
            // 元数据中存在对应的字段记录，则表示需要更新
            else {
                // 更新元数据
                executeUpdate(AgileTableMetadata.updateSql(AgileTableMetadata.MetadataType.COLUMN, tableName, columnName, columnDomain.signature()), JdbcTemplateHelper.Operation.UPDATE);
            }
        }

        // 删除多余的外键
        List<String> toDeleteForeignKeyNames = inDatabaseForeignKeyNames.stream().filter(fk -> !currentForeignKeyNames.contains(fk)).toList();
        if (CollUtil.isNotEmpty(toDeleteForeignKeyNames)) {
            for (String toDeleteForeignKeyName : toDeleteForeignKeyNames) {
                this.jdbcTemplateHelper.dropForeignKey(tableName, toDeleteForeignKeyName);
            }
        }

        // 更新普通索引
        this.jdbcTemplateHelper.updateIndex(inDatabaseIndexNames, entityClassDomain);

        // 更新唯一索引
        this.jdbcTemplateHelper.updateUniqueIndex(inDatabaseUniqueIndexNames, entityClassDomain);
    }

    public Map<String, Object> executeSelect(final String sql) throws SQLException {
        return this.jdbcTemplateHelper.executeSelect(sql, properties.isShowSql() && log.isDebugEnabled(), properties.isFormatSql());
    }


    public void executeUpdate(final String sql, final JdbcTemplateHelper.Operation operation) throws SQLException {
        this.jdbcTemplateHelper.executeUpdate(sql, operation, properties.isShowSql(), properties.isFormatSql());
    }
}