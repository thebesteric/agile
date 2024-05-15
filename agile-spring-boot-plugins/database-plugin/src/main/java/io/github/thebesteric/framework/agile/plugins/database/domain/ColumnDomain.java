package io.github.thebesteric.framework.agile.plugins.database.domain;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.github.thebesteric.framework.agile.plugins.database.annotation.EntityColumn;
import lombok.Data;
import lombok.experimental.Accessors;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

/**
 * ColumnDomain
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-05-13 17:14:20
 */
@Data
@Accessors(chain = true)
public class ColumnDomain {

    /** 名称 */
    private String name;

    /** 类型 */
    private EntityColumn.Type type = EntityColumn.Type.VARCHAR;

    /** 长度 */
    private int length;

    /** 精读 */
    private int precision;

    /** 主键 */
    private boolean primary = false;

    /** 唯一键 */
    private boolean unique = false;

    /** 注释 */
    private String comment;

    /** 是否无符号 */
    private boolean unsigned = false;

    /** 是否为空 */
    private boolean nullable = true;

    /** 默认值 */
    private String defaultExpression;

    /** 是否自增 */
    private boolean autoIncrement = false;

    /** 需要更新的表字段 */
    private String forUpdate;

    public static ColumnDomain of(Field field) {
        EntityColumn column = field.getAnnotation(EntityColumn.class);
        TableId tableId = field.getAnnotation(TableId.class);
        ColumnDomain domain = new ColumnDomain();
        domain.name = fieldName(field, column);
        domain.type = fieldType(field, column);
        domain.length = fieldLength(field, column);
        domain.precision = column != null ? column.precision() : 0;
        domain.primary = column != null ? column.primary() : tableId != null;
        domain.unique = column != null && column.unique();
        domain.comment = column != null ? column.comment() : null;
        domain.unsigned = column != null && column.unsigned();
        domain.nullable = nullable(column, tableId);
        domain.defaultExpression = column != null ? column.defaultExpression() : null;
        domain.autoIncrement = autoIncrement(field, column);
        domain.forUpdate = column != null ? column.forUpdate() : null;
        return domain;
    }

    public static boolean autoIncrement(Field field, EntityColumn column) {
        if (column != null && column.autoIncrement()) {
            return true;
        }
        TableId tableId = field.getAnnotation(TableId.class);
        return tableId != null && IdType.AUTO == tableId.type();
    }

    public static boolean nullable(EntityColumn column, TableId tableId) {
        if (column != null) {
            return !column.primary() && column.nullable();
        }
        return tableId == null;
    }

    public static String fieldName(Field field, EntityColumn column) {
        if (column != null && CharSequenceUtil.isNotEmpty(column.name())) {
            return column.name().replace("`", "");
        }
        TableField tableField = field.getAnnotation(TableField.class);
        if (tableField != null && CharSequenceUtil.isNotEmpty(tableField.value())) {
            return tableField.value().replace("`", "");
        }
        return CharSequenceUtil.toUnderlineCase(field.getName());
    }

    public static Integer fieldLength(Field field, EntityColumn column) {
        if (column == null && field.getType() == String.class) {
            return 255;
        }
        if (column == null && field.getType() == Boolean.class || field.getType() == boolean.class) {
            return 1;
        }
        if (column != null && EntityColumn.Type.DETERMINE == column.type()) {
            EntityColumn.Type type = fieldType(field, column);
            if (type == EntityColumn.Type.VARCHAR) {
                if (column.length() == -1) {
                    return 255;
                }
            }
            return column.length();
        }
        if (column != null) {
            return column.length();
        }
        return -1;
    }

    public static EntityColumn.Type fieldType(Field field, EntityColumn column) {
        if (column != null && column.type() != EntityColumn.Type.DETERMINE) {
            return column.type();
        }

        Class<?> fieldType = field.getType();
        if (fieldType == Boolean.class || fieldType == boolean.class) {
            return EntityColumn.Type.TINY_INT;
        }else if (fieldType == Byte.class || fieldType == byte.class) {
            return EntityColumn.Type.TINY_INT;
        } else if (fieldType == Short.class || fieldType == short.class) {
            return EntityColumn.Type.SMALL_INT;
        } else if (fieldType == Integer.class || fieldType == int.class) {
            return EntityColumn.Type.INT;
        } else if (fieldType == Long.class || fieldType == long.class) {
            return EntityColumn.Type.BIG_INT;
        } else if (fieldType == Float.class || fieldType == float.class) {
            return EntityColumn.Type.FLOAT;
        } else if (fieldType == Double.class || fieldType == double.class) {
            return EntityColumn.Type.DOUBLE;
        } else if (fieldType == BigDecimal.class) {
            return EntityColumn.Type.DECIMAL;
        } else if (fieldType == Date.class) {
            return EntityColumn.Type.DATETIME;
        } else if (fieldType == LocalDate.class) {
            return EntityColumn.Type.DATE;
        }
        return EntityColumn.Type.VARCHAR;
    }

    public String typeWithLength() {
        if (this.type.isSupportLength() && this.type.isSupportPrecision()) {
            return type.getJdbcType() + "(" + length + "," + precision + ")";
        } else if (this.type.isSupportLength()) {
            return type.getJdbcType() + "(" + length + ")";
        }
        return type.getJdbcType();
    }

    public String uniqueKeyName(String tableName) {
        return generateUniqueKeyName(tableName, this.name);
    }

    public static String generateUniqueKeyName(String tableName, String columnName) {
        return tableName + "_uk_" + columnName;
    }
}
