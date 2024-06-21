package io.github.thebesteric.framework.agile.plugins.database.core.domain;

import lombok.Data;

/**
 * ReferenceDomain
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-20 13:15:19
 */
@Data
public class ReferenceDomain {
    /** 表名 */
    private String tableName;
    /** 列名 */
    private String column;
    /** 目标表名 */
    private String targetTableName;
    /** 目标列名 */
    private String targetColumn;

    private ReferenceDomain(String tableName, String column, String targetTableName, String targetColumn) {
        this.tableName = tableName;
        this.column = column;
        this.targetTableName = targetTableName;
        this.targetColumn = targetColumn;
    }

    /**
     * 获取完整的外键名称
     *
     * @return String
     *
     * @author wangweijun
     * @since 2024/6/20 19:24
     */
    public String getForeignKeyName() {
        return ColumnDomain.generateForeignKeyName(tableName, column, targetTableName, targetColumn);
    }

    public static ReferenceDomain of(String tableName, String column, String targetTableName, String targetColumn) {
        return new ReferenceDomain(tableName, column, targetTableName, targetColumn);
    }
}
