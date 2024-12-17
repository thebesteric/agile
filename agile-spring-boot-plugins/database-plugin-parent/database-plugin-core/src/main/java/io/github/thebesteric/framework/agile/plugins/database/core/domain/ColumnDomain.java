package io.github.thebesteric.framework.agile.plugins.database.core.domain;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.github.thebesteric.framework.agile.commons.util.LoggerPrinter;
import io.github.thebesteric.framework.agile.core.domain.None;
import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityColumn;
import io.github.thebesteric.framework.agile.plugins.database.core.annotation.IgnoredEntityColumn;
import io.github.thebesteric.framework.agile.plugins.database.core.annotation.Reference;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Transient;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * ColumnDomain
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-05-13 17:14:20
 */
@Slf4j
@Data
@Accessors(chain = true)
public class ColumnDomain {
    /** 所属表 */
    private String tableName;

    /** 名称 */
    private String name;

    /** 类型 */
    private EntityColumn.Type type = EntityColumn.Type.VARCHAR;

    /** 字段名 */
    private String fieldName;

    /** 字段类型 */
    private Class<?> fieldType;

    /** 长度 */
    private int length;

    /** 精读 */
    private int precision;

    /** 主键 */
    private boolean primary = false;

    /** 唯一键 */
    private boolean unique = false;

    /** 联合唯一（组名，相同组名会组合成唯一索引） */
    private String uniqueGroup;

    /** 索引 */
    private boolean index;

    /** 索引组 */
    private String indexGroup;

    /** 索引组顺序 */
    private int indexGroupSort;

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

    /** 外键 */
    private ReferenceDomain reference;

    /** 字段排序 */
    private int sequence;

    /** 是否是数据库字段 */
    private boolean exist = true;

    public static final String UNDERLINE = "_";
    public static final String PRIMARY_KEY_PREFIX_SUFFIX = UNDERLINE + "pk" + UNDERLINE;
    public static final String FOREIGN_KEY_PREFIX_SUFFIX = UNDERLINE + "fk" + UNDERLINE;
    public static final String UNIQUE_KEY_PREFIX_SUFFIX = UNDERLINE + "uk" + UNDERLINE;
    public static final String INDEX_KEY_PREFIX_SUFFIX = UNDERLINE + "index" + UNDERLINE;


    public static ColumnDomain of(String tableName, Field field) {
        EntityColumn column = field.getAnnotation(EntityColumn.class);
        TableId tableId = field.getAnnotation(TableId.class);
        ColumnDomain domain = new ColumnDomain();
        domain.tableName = tableName;
        domain.name = EntityUtils.getColumnName(field);
        domain.type = EntityUtils.getColumnType(field);
        domain.fieldName = field.getName();
        domain.fieldType = field.getType();
        domain.length = fieldLength(field, column, domain.type);
        domain.precision = column != null ? column.precision() : 0;
        domain.primary = column != null ? column.primary() : tableId != null;
        domain.unique = column != null && column.unique();
        domain.uniqueGroup = column != null ? column.uniqueGroup() : null;
        domain.index = column != null && column.index();
        domain.indexGroup = column != null ? column.indexGroup() : null;
        domain.indexGroupSort = column != null ? column.indexGroupSort() : 0;
        domain.comment = column != null ? column.comment() : null;
        domain.unsigned = column != null && column.unsigned();
        domain.nullable = nullable(column, tableId);
        domain.defaultExpression = column != null ? column.defaultExpression() : null;
        domain.autoIncrement = autoIncrement(field, column);
        domain.forUpdate = column != null ? column.forUpdate() : null;
        domain.reference = referenceDomain(tableName, domain.name, column);
        domain.sequence = column != null ? column.sequence() : Integer.MAX_VALUE;
        domain.exist = columnExist(field, column);
        return domain;
    }

    private static ReferenceDomain referenceDomain(String tableName, String columnName, EntityColumn column) {
        if (column != null) {
            Reference reference = column.reference();
            if (reference.targetEntityClass() != None.class) {
                Class<?> targetEntityClass = reference.targetEntityClass();
                EntityClassDomain entityClassDomain = EntityClassDomain.of(targetEntityClass);
                String targetTableName = entityClassDomain.getTableName();
                return ReferenceDomain.of(tableName, columnName, targetTableName, reference.targetColumn());
            }
        }
        return null;
    }

    private static boolean autoIncrement(Field field, EntityColumn column) {
        if (column != null && column.autoIncrement()) {
            return true;
        }
        TableId tableId = field.getAnnotation(TableId.class);
        return tableId != null && IdType.AUTO == tableId.type();
    }

    private static boolean nullable(EntityColumn column, TableId tableId) {
        if (column != null) {
            return !column.primary() && column.nullable();
        }
        return tableId == null;
    }

    private static boolean columnExist(Field field, EntityColumn column) {
        if (column != null) {
            return column.exist();
        }
        if (field.isAnnotationPresent(IgnoredEntityColumn.class)) {
            return false;
        }
        return !field.isAnnotationPresent(Transient.class);
    }

    private static Integer fieldLength(Field field, EntityColumn column, EntityColumn.Type columnType) {
        if (column == null && field.getType() == String.class) {
            return 255;
        }
        if (column == null && field.getType() == Boolean.class || field.getType() == boolean.class) {
            return 1;
        }
        if (column != null && EntityColumn.Type.DETERMINE == column.type()) {
            if (columnType == EntityColumn.Type.VARCHAR && column.length() == -1) {
                return 255;
            }
            return column.length();
        }
        if (column != null) {
            return column.length();
        }
        return -1;
    }

    public String typeWithLength() {
        if (this.type.isSupportLength() && this.type.isSupportPrecision() && this.length > 0 && this.precision > 0) {
            return type.getJdbcType() + "(" + length + "," + precision + ")";
        } else if (this.type.isSupportLength() && this.length > 0) {
            return type.getJdbcType() + "(" + length + ")";
        } else if (this.type == EntityColumn.Type.VARCHAR && this.length < 0) {
            return type.getJdbcType() + "(255)";
        }
        return type.getJdbcType();
    }

    public String signature() {
        String str = this.toString();
        return DigestUtil.md5Hex(str);
    }

    public static String generatePrimaryKeyName(String tableName, String columnName) {
        String key = tableName + PRIMARY_KEY_PREFIX_SUFFIX + columnName;
        return validateKeyName(tableName, key, columnName);
    }

    public static String generateForeignKeyName(String tableName, String columnName, String targetTableName, String targetColumnName) {
        String key = tableName + UNDERLINE + columnName + FOREIGN_KEY_PREFIX_SUFFIX + targetTableName + UNDERLINE + targetColumnName;
        return validateKeyName(tableName, key, columnName);
    }

    public static String generateUniqueKeyName(String tableName, String columnName) {
        String key = tableName + UNIQUE_KEY_PREFIX_SUFFIX + columnName;
        return validateKeyName(tableName, key, columnName);
    }

    public static String generateIndexKeyName(String tableName, String columnName) {
        String key = tableName + INDEX_KEY_PREFIX_SUFFIX + columnName;
        return validateKeyName(tableName, key, columnName);
    }

    /**
     * 超过 64 位，则取每个单词的首字母加上下划线作为 key
     *
     * @param tableName  表名
     * @param key        key
     * @param columnName columnName
     *
     * @return String
     *
     * @author wangweijun
     * @since 2024/12/17 16:29
     */
    private static String validateKeyName(String tableName, String key, String columnName) {
        if (key.length() > 64) {
            String[] words = columnName.split(UNDERLINE);
            List<String> columns = new ArrayList<>();
            for (String word : words) {
                columns.add(String.valueOf(word.charAt(0)));
            }
            key = String.join(UNDERLINE, columns);
            if (key.length() > 64) {
                key = key.substring(0, 64);
            }
            LoggerPrinter.warn(log, "The table {}'s key name is too long, so it is truncated to {}", tableName, key);
        }
        return key;
    }
}
