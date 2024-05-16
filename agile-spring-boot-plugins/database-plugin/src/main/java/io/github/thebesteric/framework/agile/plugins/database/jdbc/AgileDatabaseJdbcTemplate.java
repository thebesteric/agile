package io.github.thebesteric.framework.agile.plugins.database.jdbc;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.db.sql.SqlFormatter;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.thebesteric.framework.agile.commons.util.LoggerPrinter;
import io.github.thebesteric.framework.agile.commons.util.ReflectUtils;
import io.github.thebesteric.framework.agile.plugins.database.annotation.EntityClass;
import io.github.thebesteric.framework.agile.plugins.database.annotation.EntityColumn;
import io.github.thebesteric.framework.agile.plugins.database.config.AgileDatabaseContext;
import io.github.thebesteric.framework.agile.plugins.database.config.AgileDatabaseProperties;
import io.github.thebesteric.framework.agile.plugins.database.domain.ColumnDomain;
import io.github.thebesteric.framework.agile.plugins.database.domain.EntityClassDomain;
import io.vavr.control.Try;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;

import javax.sql.DataSource;
import java.beans.Transient;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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


    public AgileDatabaseJdbcTemplate(AgileDatabaseContext context, DataSource dataSource, AgileDatabaseProperties propertie) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.properties = propertie;
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
            for (Class<?> clazz : entityClasses) {
                String tableName = getTableName(properties.getTableNamePrefix(), clazz);
                ResultSet tables = metaData.getTables(null, null, tableName, new String[]{"TABLE"});
                if (!tables.next()) {
                    createTable(tableName, clazz);
                } else {
                    if (update) {
                        updateTable(tableName, clazz, metaData);
                    }
                }
            }
        }
    }

    public void createTable(String tableName, Class<?> clazz) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE `").append(tableName).append("` (");
        List<Field> fields = getEntityFields(clazz);
        String primaryKey = null;
        List<String> uniqueFieldNames = new ArrayList<>();
        for (Field field : fields) {
            // 获取字段信息
            ColumnDomain columnDomain = ColumnDomain.of(field);
            if (columnDomain.isPrimary()) {
                if (primaryKey != null) {
                    throw new SQLException("Primary key is duplicate: %s".formatted(primaryKey));
                }
                primaryKey = columnDomain.getName();
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

            if (columnDomain.isUnique()) {
                String uniqueFieldName = columnDomain.getName();
                uniqueFieldNames.add(uniqueFieldName);
            }
        }

        // 主键
        if (CharSequenceUtil.isNotEmpty(primaryKey)) {
            sb.append(String.format("CONSTRAINT %s_pk PRIMARY KEY (`%s`)", tableName, primaryKey)).append(",");
        }

        // 唯一键
        for (int i = 0; i < uniqueFieldNames.size(); i++) {
            if (i == 0) {
                sb.append(" ");
            }
            String uniqueName = uniqueFieldNames.get(i);
            sb.append(String.format("CONSTRAINT %s_uk_%s UNIQUE (`%s`)", tableName, uniqueName, uniqueName));
            if (i != uniqueFieldNames.size() - 1) {
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

        sb.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci");

        this.executeUpdate(sb.toString());
    }

    public void updateTable(String tableName, Class<?> clazz, DatabaseMetaData metaData) throws SQLException {
        List<Field> fields = getEntityFields(clazz);
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
            ColumnDomain columnDomain = ColumnDomain.of(field);
            String forUpdateColumn = columnDomain.getForUpdate();
            // 需要更新的字段
            if (columnNames.contains(forUpdateColumn) && CharSequenceUtil.isNotEmpty(forUpdateColumn)) {
                updateFields.add(field);
            }
            // 需要新增的字段
            else if (!columnNames.contains(columnDomain.getName())) {
                newFields.add(field);
            }
        }

        List<String> currentColumnNames = fields.stream().map(field -> ColumnDomain.of(field).getName()).toList();
        List<String> deleteColumns = columnNames.stream().filter(columnName->!currentColumnNames.contains(columnName)).toList();

        if (!deleteColumns.isEmpty()) {
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
            executeUpdate(sb.toString());
        }
    }

    private void addColumns(String tableName, List<Field> newFields) throws SQLException {
        for (Field field : newFields) {
            // 新增字段
            ColumnDomain columnDomain = ColumnDomain.of(field);
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
            executeUpdate(sb.toString());

            // 新建唯一约束
            if (columnDomain.isUnique()) {
                String uniqueIndexName = columnDomain.uniqueKeyName(tableName);
                sb = new StringBuilder();
                sb.append("ALTER TABLE").append(" ").append("`").append(tableName).append("`").append(" ");
                sb.append("ADD CONSTRAINT").append(" ").append(uniqueIndexName).append(" ");
                sb.append("UNIQUE").append(" ").append("(").append("`").append(columnDomain.getName()).append("`").append(")");
                executeUpdate(sb.toString());
            }
        }
    }

    private void updateColumns(String tableName, List<Field> updateFields, DatabaseMetaData metaData) throws SQLException {

        // 唯一索引
        List<String> uniqueIndexNames = new ArrayList<>();
        ResultSet indexInfo = metaData.getIndexInfo(null, null, tableName, true, false);
        while (indexInfo.next()) {
            String indexName = indexInfo.getString("INDEX_NAME");
            // 主键和非唯一索引，跳过
            if (indexInfo.getBoolean("NON_UNIQUE") || "PRIMARY".equalsIgnoreCase(indexName)) {
                continue;
            }
            uniqueIndexNames.add(indexName);
        }

        for (Field field : updateFields) {
            // 字段信息变更
            ColumnDomain columnDomain = ColumnDomain.of(field);
            StringBuilder sb = new StringBuilder();
            sb.append("ALTER TABLE").append(" ").append("`").append(tableName).append("`").append(" ");
            String forUpdateColumn = columnDomain.getForUpdate();
            if (CharSequenceUtil.isNotEmpty(forUpdateColumn)) {
                sb.append("CHANGE").append(" ").append("`").append(forUpdateColumn).append("`").append(" ").append("`").append(columnDomain.getName()).append("`").append(" ");
            }
            sb.append(columnDomain.typeWithLength()).append(" ");

            if (columnDomain.getType().isSupportSign() && columnDomain.isUnsigned()) {
                sb.append("UNSIGNED").append(" ");
            }

            sb.append("DEFAULT").append(" ");
            String defaultExpression = columnDomain.getDefaultExpression();
            if (CharSequenceUtil.isNotEmpty(defaultExpression)) {
                sb.append(defaultExpression).append(" ");
            }
            sb.append(columnDomain.isNullable() ? "NULL" : "NOT NULL").append(" ");

            String comment = columnDomain.getComment();
            if (CharSequenceUtil.isNotEmpty(comment)) {
                sb.append("COMMENT").append(" ").append("'").append(comment).append("'");
            }
            executeUpdate(sb.toString());

            // 唯一约束变更
            if (columnDomain.isUnique()) {
                String uniqueIndexName = columnDomain.uniqueKeyName(tableName);
                if (!uniqueIndexNames.contains(uniqueIndexName)) {
                    // 新建唯一约束
                    sb = new StringBuilder();
                    sb.append("ALTER TABLE").append(" ").append("`").append(tableName).append("`").append(" ");
                    sb.append("ADD CONSTRAINT").append(" ").append(uniqueIndexName).append(" ");
                    sb.append("UNIQUE").append(" ").append("(").append("`").append(columnDomain.getName()).append("`").append(")");
                    executeUpdate(sb.toString());

                    // 删除原来的唯一约束
                    sb = new StringBuilder();
                    sb.append("ALTER TABLE").append(" ").append("`").append(tableName).append("`").append(" ");
                    String oldUniqueKeyName = ColumnDomain.generateUniqueKeyName(tableName, forUpdateColumn);
                    sb.append("DROP KEY").append(" ").append(oldUniqueKeyName).append(" ");
                    executeUpdate(sb.toString());
                }
            }
        }
    }

    public String getTableName(String tableNamePrefix, Class<?> clazz) {
        String tableName = tableNamePrefix == null ? "" : tableNamePrefix.trim();
        synchronized (this) {
            EntityClassDomain entityClassDomain = new EntityClassDomain();
            EntityClass entityClassAnno = clazz.getDeclaredAnnotation(EntityClass.class);
            if (entityClassAnno != null) {
                entityClassDomain.setTableName(entityClassAnno.value());
            } else {
                // Mybatis plus @TableName annotation supports
                TableName tableNameAnno = clazz.getDeclaredAnnotation(TableName.class);
                entityClassDomain.setTableName(tableNameAnno.value());
            }

            String name = entityClassDomain.getTableName().trim();
            if (CharSequenceUtil.isNotEmpty(name)) {
                tableName += name;
            } else {
                tableName += CharSequenceUtil.toUnderlineCase(clazz.getSimpleName());
            }
        }
        return tableName;
    }


    public void executeUpdate(final String sql) throws SQLException {
        DataSource dataSource = this.jdbcTemplate.getDataSource();
        if (dataSource == null) {
            throw new SQLException("DataSource is null");
        }
        AtomicReference<Connection> connection = new AtomicReference<>();
        Try.of(() -> {
            connection.set(dataSource.getConnection());
            PreparedStatementCreator creator = conn -> conn.prepareStatement(sql);
            return creator.createPreparedStatement(connection.get()).executeUpdate();
        }).onFailure(e -> LoggerPrinter.error(log, e.getMessage(), e)).andThen(result -> {
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