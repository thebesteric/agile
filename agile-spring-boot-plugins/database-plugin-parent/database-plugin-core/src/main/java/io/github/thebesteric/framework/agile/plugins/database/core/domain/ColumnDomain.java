package io.github.thebesteric.framework.agile.plugins.database.core.domain;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.github.thebesteric.framework.agile.core.domain.None;
import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityColumn;
import io.github.thebesteric.framework.agile.plugins.database.core.annotation.IgnoredEntityColumn;
import io.github.thebesteric.framework.agile.plugins.database.core.annotation.Reference;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Transient;

import java.lang.reflect.Field;

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

    /** 是否是数据库字段 */
    private boolean exist = true;

    public static final String PRIMARY_KEY_PREFIX_SUFFIX = "_pk_";
    public static final String FOREIGN_KEY_PREFIX_SUFFIX = "_fk_";
    public static final String UNIQUE_KEY_PREFIX_SUFFIX = "_uk_";
    public static final String INDEX_KEY_PREFIX_SUFFIX = "_index_";

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
        return tableName + PRIMARY_KEY_PREFIX_SUFFIX + columnName;
    }

    public static String generateForeignKeyName(String tableName, String columnName, String targetTableName, String targetColumnName) {
        return tableName + "_" + columnName + FOREIGN_KEY_PREFIX_SUFFIX + targetTableName + "_" + targetColumnName;
    }

    public static String generateUniqueKeyName(String tableName, String columnName) {
        return tableName + UNIQUE_KEY_PREFIX_SUFFIX + columnName;
    }

    public static String generateIndexKeyName(String tableName, String columnName) {
        return tableName + INDEX_KEY_PREFIX_SUFFIX + columnName;
    }
}
