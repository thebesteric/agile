package io.github.thebesteric.framework.agile.plugins.database.entity;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 表的元数据信息
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-05-17 09:34:01
 */
@Getter
@Setter
public class TableMetadata implements Serializable {
    @Serial
    private static final long serialVersionUID = -234369174588816818L;

    public static final String TABLE_NAME = "AGILE_TABLE_METADATA";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_TYPE = "TYPE";
    public static final String COLUMN_TABLE_NAME = "TABLE_NAME";
    public static final String COLUMN_COLUMN_NAME = "COLUMN_NAME";
    public static final String COLUMN_SIGNATURE = "SIGNATURE";
    public static final String COLUMN_VERSION = "VERSION";
    public static final String COLUMN_CREATED_AT = "CREATED_AT";
    public static final String COLUMN_UPDATED_AT = "UPDATED_AT";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private Integer id;

    private MetadataType type;

    private String tableName;

    private String columnName;

    private String signature;

    private Integer version;

    private Date createdAt;

    private Date updatedAt;

    public static String tableExistsSql(String schema) {
        return "SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = '%s' AND table_name = '%s') AS 'exists'"
                .formatted(schema, TABLE_NAME);
    }

    public static String insertSql(MetadataType metadataType, String tableName, String columnName, String signature) {
        String columns = "`%s`, `%s`, `%s`, `%s`, `%s`".formatted(COLUMN_TYPE, COLUMN_TABLE_NAME, COLUMN_COLUMN_NAME, COLUMN_SIGNATURE, COLUMN_CREATED_AT);
        String values = "'%s', '%s', '%s', '%s', '%s'".formatted(metadataType.name(), tableName, columnName, signature, DATE_FORMAT.format(new Date()));
        // 表示插入的是表级的元数据信息
        if (CharSequenceUtil.isEmpty(columnName)) {
            columns = "`%s`, `%s`, `%s`, `%s`".formatted(COLUMN_TYPE, COLUMN_TABLE_NAME, COLUMN_SIGNATURE, COLUMN_CREATED_AT);
            values = "'%s', '%s', '%s', '%s'".formatted(metadataType.name(), tableName, signature, DATE_FORMAT.format(new Date()));
        }
        return "INSERT INTO %s (%s) VALUES (%s)".formatted(TableMetadata.TABLE_NAME, columns, values);
    }

    public static String selectSql(MetadataType metadataType, String tableName, String columnName) {
        if (columnName != null) {
            return "SELECT * FROM %s WHERE `%s` = '%s' AND `%s` = '%s' AND `%s` = '%s' ORDER BY UPDATED_AT DESC LIMIT 1"
                    .formatted(TableMetadata.TABLE_NAME, TableMetadata.COLUMN_TYPE, metadataType.name(), COLUMN_TABLE_NAME, tableName, COLUMN_COLUMN_NAME, columnName);
        }
        return "SELECT * FROM %s WHERE `%s` = '%s' AND `%s` = '%s' ORDER BY UPDATED_AT DESC LIMIT 1"
                .formatted(TableMetadata.TABLE_NAME, TableMetadata.COLUMN_TYPE, metadataType.name(), COLUMN_TABLE_NAME, tableName);
    }

    public static String deleteSql(MetadataType metadataType, String tableName, String deleteColumn) {
        return "DELETE FROM %s WHERE `%s` = '%s' AND `%s` = '%s' AND `%s` = '%s'"
                .formatted(TableMetadata.COLUMN_TYPE, metadataType.name(), TableMetadata.TABLE_NAME, COLUMN_TABLE_NAME, tableName, COLUMN_COLUMN_NAME, deleteColumn);
    }

    public static String updateSql(MetadataType metadataType, String tableName, String columnName, String signature) {
        String changed = "`%s` = '%s', `%s` = '%s', `%s` = %s".formatted(COLUMN_SIGNATURE, signature, COLUMN_UPDATED_AT, DATE_FORMAT.format(new Date()), COLUMN_VERSION, "%s + 1".formatted(COLUMN_VERSION));
        String condition = "`%s` = '%s' AND `%s` = '%s' AND `%s` = '%s'".formatted(COLUMN_TABLE_NAME, tableName, COLUMN_TYPE, metadataType.name(), COLUMN_COLUMN_NAME, columnName);
        // 表示插入的是表级的元数据信息
        if (CharSequenceUtil.isEmpty(columnName)) {
            condition = "`%s` = '%s' AND `%s` = '%s'".formatted(COLUMN_TABLE_NAME, tableName, COLUMN_TYPE, metadataType.name());
        }
        return "UPDATE %s t SET %s WHERE %s".formatted(TableMetadata.TABLE_NAME, changed, condition);
    }

    /**
     * 获取表的所有唯一索引
     *
     * @param metaData  元数据
     * @param tableName 表名
     *
     * @return Set<String>
     *
     * @author wangweijun
     * @since 2024/6/5 11:51
     */
    public static Set<String> uniqueIndexNames(DatabaseMetaData metaData, String tableName) throws SQLException {
        Set<String> uniqueIndexNames = new LinkedHashSet<>();
        try (ResultSet uniqueIndexInfo = metaData.getIndexInfo(null, null, tableName, true, false)) {
            while (uniqueIndexInfo.next()) {
                String indexName = uniqueIndexInfo.getString("INDEX_NAME");
                // 主键和非唯一索引，跳过
                if (uniqueIndexInfo.getBoolean("NON_UNIQUE") || "PRIMARY".equalsIgnoreCase(indexName)) {
                    continue;
                }
                uniqueIndexNames.add(indexName);
            }
        }
        return uniqueIndexNames;
    }

    /**
     * 获取表的所有普通索引（不包含唯一索引）
     *
     * @param metaData  元数据
     * @param tableName 表名
     *
     * @return Set<String>
     *
     * @author wangweijun
     * @since 2024/6/5 11:51
     */
    public static Set<String> indexNames(DatabaseMetaData metaData, String tableName) throws SQLException {
        Set<String> indexNames = new LinkedHashSet<>();
        try (ResultSet rs = metaData.getIndexInfo(null, null, tableName, false, false)) {
            while (rs.next()) {
                String indexName = rs.getString("INDEX_NAME");
                // 非唯一索引
                if (rs.getBoolean("NON_UNIQUE")) {
                    indexNames.add(indexName);
                }
            }
        }
        return indexNames;
    }

    /**
     * 获取表注释
     *
     * @param metaData  元数据
     * @param tableName 表名
     *
     * @return String
     *
     * @author wangweijun
     * @since 2024/6/5 15:09
     */
    public static String tableRemarks(DatabaseMetaData metaData, String tableName) throws SQLException {
        String remarks = "";
        try (ResultSet rs = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
            while (rs.next()) {
                remarks = rs.getString("REMARKS");
                if (remarks != null && !remarks.isEmpty()) {
                    return remarks;
                }
            }
        }
        return remarks;
    }

    public enum MetadataType {
        COLUMN, TABLE
    }
}
