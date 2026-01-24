package io.github.thebesteric.framework.agile.plugins.database.core.domain;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.thebesteric.framework.agile.commons.util.AbstractUtils;
import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityClass;
import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityColumn;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * EntityUtils
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-29 15:26:56
 */
public final class EntityUtils extends AbstractUtils {

    /**
     * 获取表名
     *
     * @param tableNamePrefix 表前缀
     * @param entityClass     实体类
     *
     * @return String
     *
     * @author wangweijun
     * @since 2024/8/29 15:42
     */
    public static String getTableName(String tableNamePrefix, Class<?> entityClass) {
        EntityClass entityClassAnno = entityClass.getDeclaredAnnotation(EntityClass.class);
        TableName tableNameAnno = entityClass.getDeclaredAnnotation(TableName.class);

        String tableName = tableNamePrefix == null ? "" : tableNamePrefix.trim();
        if (entityClassAnno != null) {
            tableName = entityClassAnno.value();
        }
        if (CharSequenceUtil.isEmpty(tableName) && tableNameAnno != null) {
            tableName = tableNameAnno.value();
        }
        if (CharSequenceUtil.isEmpty(tableName)) {
            tableName = CharSequenceUtil.toUnderlineCase(entityClass.getSimpleName());
        }
        return tableName;
    }

    /**
     * 获取表名
     *
     * @param entityClass 实体类
     *
     * @return String
     *
     * @author wangweijun
     * @since 2024/8/29 15:43
     */
    public static String getTableName(Class<?> entityClass) {
        return getTableName(null, entityClass);
    }

    /**
     * 获取列名
     *
     * @param field 字段
     *
     * @return String
     *
     * @author wangweijun
     * @since 2024/8/29 15:52
     */
    public static String getColumnName(Field field) {
        EntityColumn entityColumn = field.getAnnotation(EntityColumn.class);
        if (entityColumn != null && CharSequenceUtil.isNotEmpty(entityColumn.name())) {
            return entityColumn.name().replace("`", "");
        }
        TableField tableField = field.getAnnotation(TableField.class);
        if (tableField != null && CharSequenceUtil.isNotEmpty(tableField.value())) {
            return tableField.value().replace("`", "");
        }
        return CharSequenceUtil.toUnderlineCase(field.getName());
    }

    /**
     * 获取列名
     *
     * @param entityClass 实体类
     * @param fieldName   字段名
     *
     * @return String
     *
     * @author wangweijun
     * @since 2024/8/29 15:53
     */
    public static String getColumnName(Class<?> entityClass, String fieldName) throws NoSuchFieldException {
        Field field = entityClass.getDeclaredField(fieldName);
        return getColumnName(field);
    }

    /**
     * 获取字段类型
     *
     * @param field 字段
     *
     * @return Type
     */
    public static EntityColumn.Type getColumnType(Field field) {
        EntityColumn entityColumn = field.getAnnotation(EntityColumn.class);
        if (entityColumn != null && entityColumn.type() != EntityColumn.Type.DETERMINE) {
            return entityColumn.type();
        }
        Class<?> fieldType = field.getType();
        if (fieldType == Boolean.class || fieldType == boolean.class) {
            return EntityColumn.Type.BOOLEAN;
        } else if (fieldType == Byte.class || fieldType == byte.class) {
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
        } else if (fieldType == Date.class || fieldType == LocalDateTime.class) {
            return EntityColumn.Type.DATETIME;
        } else if (fieldType == LocalDate.class) {
            return EntityColumn.Type.DATE;
        } else if (fieldType == Byte[].class || fieldType == byte[].class) {
            return EntityColumn.Type.BLOB;
        }
        return EntityColumn.Type.VARCHAR;
    }

}
