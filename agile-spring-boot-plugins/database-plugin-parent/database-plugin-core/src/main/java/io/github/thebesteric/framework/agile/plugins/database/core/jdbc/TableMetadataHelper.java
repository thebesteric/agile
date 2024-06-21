package io.github.thebesteric.framework.agile.plugins.database.core.jdbc;

import io.github.thebesteric.framework.agile.commons.util.AbstractUtils;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.ColumnDomain;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.ReferenceDomain;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.TableColumn;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 表元信息工具类
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-14 14:40:46
 */
public final class TableMetadataHelper extends AbstractUtils {

    /**
     * 表是否存在
     *
     * @param schema    库
     * @param tableName 表名
     *
     * @return String
     *
     * @author wangweijun
     * @since 2024/6/14 14:44
     */
    public static String tableExistsSql(String schema, String tableName) {
        return "SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = '%s' AND table_name = '%s') AS 'exists'"
                .formatted(schema, tableName);
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
        Set<ReferenceDomain> referenceDomains = foreignKeyDomains(metaData, tableName);
        Set<String> foreignKeyNames = referenceDomains.stream().map(ReferenceDomain::getForeignKeyName).collect(Collectors.toSet());
        try (ResultSet rs = metaData.getIndexInfo(null, null, tableName, false, false)) {
            while (rs.next()) {
                String indexName = rs.getString("INDEX_NAME");
                // 非唯一索引
                if (rs.getBoolean("NON_UNIQUE") && !foreignKeyNames.contains(indexName)) {
                    indexNames.add(indexName);
                }
            }
        }
        return indexNames;
    }

    /**
     * 获取表的外键信息
     *
     * @param metaData  元数据
     * @param tableName 表名
     *
     * @return Set<ReferenceDomain>
     *
     * @author wangweijun
     * @since 2024/6/21 13:15
     */
    public static Set<ReferenceDomain> foreignKeyDomains(DatabaseMetaData metaData, String tableName) throws SQLException {
        Set<ReferenceDomain> foreignKeyReferenceDomains = new LinkedHashSet<>();
        try (ResultSet rs = metaData.getImportedKeys(null, null, tableName)) {
            while (rs.next()) {
                String foreignKeyColumnName = rs.getString("FKCOLUMN_NAME");
                String targetTableName = rs.getString("PKTABLE_NAME");
                String targetColumnName = rs.getString("PKCOLUMN_NAME");
                String foreignKeyName = ColumnDomain.generateForeignKeyName(tableName, foreignKeyColumnName, targetTableName, targetColumnName);
                ReferenceDomain referenceDomain = ReferenceDomain.of(tableName, foreignKeyColumnName, targetTableName, targetColumnName);
                foreignKeyReferenceDomains.add(referenceDomain);
            }
        }
        return foreignKeyReferenceDomains;
    }

    /**
     * 获取表的所有字段信息
     *
     * @param metaData  元数据
     * @param tableName 表名
     *
     * @return Set<TableColumn>
     *
     * @author wangweijun
     * @see DatabaseMetaData#getColumns(String, String, String, String)
     * @since 2024/6/21 13:25
     */
    public static Set<TableColumn> tableColumns(DatabaseMetaData metaData, String tableName) throws SQLException {
        // 表的所有字段名称
        Set<TableColumn> tableColumns = new LinkedHashSet<>();
        try (ResultSet rs = metaData.getColumns(null, "%", tableName, "%")) {
            while (rs.next()) {
                String tableCat = rs.getString("TABLE_CAT");
                String tableSchema = rs.getString("TABLE_SCHEM");
                String columnName = rs.getString("COLUMN_NAME");
                Integer dataType = rs.getInt("DATA_TYPE");
                String typeName = rs.getString("TYPE_NAME");
                int columnSize = rs.getInt("COLUMN_SIZE");
                int decimalDigits = rs.getInt("DECIMAL_DIGITS");
                int numPrecisionRadix = rs.getInt("NUM_PREC_RADIX");
                String remarks = rs.getString("REMARKS");
                boolean isNullable = !"NO".equals(rs.getString("IS_NULLABLE"));
                // 这里是防止 INNODB_COLUMN 有冗余字段
                List<String> columnNames = tableColumns.stream().map(tc -> tc.getColumnName().toLowerCase()).toList();
                if (columnNames.contains(columnName.toLowerCase())) {
                    continue;
                }
                TableColumn tableColumn = new TableColumn()
                        .setTableCat(tableCat).setTableSchema(tableSchema).setTableName(tableName).setColumnName(columnName)
                        .setDataType(dataType).setTypeName(typeName).setColumnSize(columnSize).setDecimalDigits(decimalDigits)
                        .setNumPrecisionRadix(numPrecisionRadix).setNullable(isNullable).setRemarks(remarks);
                tableColumns.add(tableColumn);
            }
        }
        return tableColumns;
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

}
