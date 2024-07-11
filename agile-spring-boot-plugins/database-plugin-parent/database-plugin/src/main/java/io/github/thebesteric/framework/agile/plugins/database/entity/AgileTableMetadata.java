package io.github.thebesteric.framework.agile.plugins.database.entity;

import cn.hutool.core.text.CharSequenceUtil;
import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityClass;
import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityColumn;
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
@EntityClass(value = AgileTableMetadata.TABLE_NAME, comment = "表元数据信息")
public class AgileTableMetadata implements Serializable {
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

    @EntityColumn(name = COLUMN_ID, primary = true, autoIncrement = true)
    private Integer id;

    @EntityColumn(name = COLUMN_TYPE, length = 32)
    private MetadataType type;

    @EntityColumn(name = COLUMN_TABLE_NAME, length = 128)
    private String tableName;

    @EntityColumn(name = COLUMN_COLUMN_NAME, length = 64)
    private String columnName;

    @EntityColumn(name = COLUMN_SIGNATURE, length = 32)
    private String signature;

    @EntityColumn(name = COLUMN_VERSION, defaultExpression = "0")
    private Integer version;

    @EntityColumn(name = COLUMN_CREATED_AT)
    private Date createdAt;

    @EntityColumn(name = COLUMN_UPDATED_AT)
    private Date updatedAt;

    public static String insertSql(MetadataType metadataType, String tableName, String columnName, String signature) {
        String columns = "`%s`, `%s`, `%s`, `%s`, `%s`".formatted(COLUMN_TYPE, COLUMN_TABLE_NAME, COLUMN_COLUMN_NAME, COLUMN_SIGNATURE, COLUMN_CREATED_AT);
        String values = "'%s', '%s', '%s', '%s', '%s'".formatted(metadataType.name(), tableName, columnName, signature, DATE_FORMAT.format(new Date()));
        // 表示插入的是表级的元数据信息
        if (CharSequenceUtil.isEmpty(columnName)) {
            columns = "`%s`, `%s`, `%s`, `%s`".formatted(COLUMN_TYPE, COLUMN_TABLE_NAME, COLUMN_SIGNATURE, COLUMN_CREATED_AT);
            values = "'%s', '%s', '%s', '%s'".formatted(metadataType.name(), tableName, signature, DATE_FORMAT.format(new Date()));
        }
        return "INSERT INTO %s (%s) VALUES (%s)".formatted(AgileTableMetadata.TABLE_NAME, columns, values);
    }

    public static String selectSql(MetadataType metadataType, String tableName, String columnName) {
        if (columnName != null) {
            return "SELECT * FROM %s WHERE `%s` = '%s' AND `%s` = '%s' AND `%s` = '%s' ORDER BY UPDATED_AT DESC LIMIT 1"
                    .formatted(AgileTableMetadata.TABLE_NAME, AgileTableMetadata.COLUMN_TYPE, metadataType.name(), COLUMN_TABLE_NAME, tableName, COLUMN_COLUMN_NAME, columnName);
        }
        return "SELECT * FROM %s WHERE `%s` = '%s' AND `%s` = '%s' ORDER BY UPDATED_AT DESC LIMIT 1"
                .formatted(AgileTableMetadata.TABLE_NAME, AgileTableMetadata.COLUMN_TYPE, metadataType.name(), COLUMN_TABLE_NAME, tableName);
    }

    public static String deleteSql(MetadataType metadataType, String tableName, String deleteColumn) {
        return "DELETE FROM %s WHERE `%s` = '%s' AND `%s` = '%s' AND `%s` = '%s'"
                .formatted(AgileTableMetadata.TABLE_NAME, AgileTableMetadata.COLUMN_TYPE, metadataType.name(), COLUMN_TABLE_NAME, tableName, COLUMN_COLUMN_NAME, deleteColumn);
    }

    public static String updateSql(MetadataType metadataType, String tableName, String columnName, String signature) {
        String changed = "`%s` = '%s', `%s` = '%s', `%s` = %s".formatted(COLUMN_SIGNATURE, signature, COLUMN_UPDATED_AT, DATE_FORMAT.format(new Date()), COLUMN_VERSION, "%s + 1".formatted(COLUMN_VERSION));
        String condition = "`%s` = '%s' AND `%s` = '%s' AND `%s` = '%s'".formatted(COLUMN_TABLE_NAME, tableName, COLUMN_TYPE, metadataType.name(), COLUMN_COLUMN_NAME, columnName);
        // 表示插入的是表级的元数据信息
        if (CharSequenceUtil.isEmpty(columnName)) {
            condition = "`%s` = '%s' AND `%s` = '%s'".formatted(COLUMN_TABLE_NAME, tableName, COLUMN_TYPE, metadataType.name());
        }
        return "UPDATE %s t SET %s WHERE %s".formatted(AgileTableMetadata.TABLE_NAME, changed, condition);
    }

    public enum MetadataType {
        COLUMN, TABLE
    }
}
