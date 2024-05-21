package io.github.thebesteric.framework.agile.plugins.database.entity;

import io.github.thebesteric.framework.agile.plugins.database.annotation.EntityClass;
import io.github.thebesteric.framework.agile.plugins.database.annotation.EntityColumn;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 表的元数据信息
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-05-17 09:34:01
 */
@Getter
@Setter
@EntityClass(TableMetadata.TABLE_NAME)
public class TableMetadata implements Serializable {
    @Serial
    private static final long serialVersionUID = -234369174588816818L;

    public static final String TABLE_NAME = "AGILE_TABLE_METADATA";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_TABLE_NAME = "TABLE_NAME";
    public static final String COLUMN_COLUMN_NAME = "COLUMN_NAME";
    public static final String COLUMN_SIGNATURE = "SIGNATURE";
    public static final String COLUMN_VERSION = "VERSION";
    public static final String COLUMN_CREATED_AT = "CREATED_AT";
    public static final String COLUMN_UPDATED_AT = "UPDATED_AT";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @EntityColumn(name = COLUMN_ID, primary = true, autoIncrement = true)
    private Integer id;

    @EntityColumn(name = COLUMN_TABLE_NAME, length = 64, nullable = false)
    private String tableName;

    @EntityColumn(name = COLUMN_COLUMN_NAME, length = 64, nullable = false)
    private String columnName;

    @EntityColumn(name = COLUMN_SIGNATURE, length = 128, nullable = false)
    private String signature;

    @EntityColumn(name = COLUMN_VERSION, unsigned = true, nullable = false, defaultExpression = "1")
    private Integer version;

    @EntityColumn(name = COLUMN_CREATED_AT, type = EntityColumn.Type.DATETIME, nullable = false, defaultExpression = "CURRENT_TIMESTAMP")
    private Date createdAt;

    @EntityColumn(name = COLUMN_UPDATED_AT, type = EntityColumn.Type.DATETIME)
    private Date updatedAt;

    public static String tableExists(String schema) {
        return "SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = '%s' AND table_name = '%s') AS 'exists'"
                .formatted(schema, TABLE_NAME);
    }

    public static String insertSql(String tableName, String columnName, String signature) {
        String columns = "`%s`, `%s`, `%s`, `%s`".formatted(COLUMN_TABLE_NAME, COLUMN_COLUMN_NAME, COLUMN_SIGNATURE, COLUMN_CREATED_AT);
        String values = "'%s', '%s', '%s', '%s'".formatted(tableName, columnName, signature, DATE_FORMAT.format(new Date()));
        return "INSERT INTO %s (%s) VALUES (%s)".formatted(TableMetadata.TABLE_NAME, columns, values);
    }

    public static String selectSql(String tableName, String columnName) {
        return "SELECT * FROM %s WHERE `%s` = '%s' AND `%s` = '%s' ORDER BY UPDATED_AT DESC LIMIT 1"
                .formatted(TableMetadata.TABLE_NAME, COLUMN_TABLE_NAME, tableName, COLUMN_COLUMN_NAME, columnName);
    }

    public static String deleteSql(String tableName, String deleteColumn) {
        return "DELETE FROM %s WHERE `%s` = '%s' AND `%s` = '%s'"
                .formatted(TableMetadata.TABLE_NAME, COLUMN_TABLE_NAME, tableName, COLUMN_COLUMN_NAME, deleteColumn);
    }

    public static String updateSql(String tableName, String columnName, String signature) {
        return "UPDATE %s t SET `%s` = '%s', `%s` = '%s', `%s` = %s WHERE `%s` = '%s' AND `%s` = '%s'"
                .formatted(TableMetadata.TABLE_NAME,
                        COLUMN_SIGNATURE, signature, COLUMN_UPDATED_AT, DATE_FORMAT.format(new Date()), COLUMN_VERSION, "%s + 1".formatted(COLUMN_VERSION),
                        COLUMN_TABLE_NAME, tableName, COLUMN_COLUMN_NAME, columnName);
    }
}
