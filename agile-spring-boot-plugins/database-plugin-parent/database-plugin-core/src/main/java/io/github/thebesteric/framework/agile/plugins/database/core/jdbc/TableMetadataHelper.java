package io.github.thebesteric.framework.agile.plugins.database.core.jdbc;

import io.github.thebesteric.framework.agile.commons.util.AbstractUtils;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

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

}
