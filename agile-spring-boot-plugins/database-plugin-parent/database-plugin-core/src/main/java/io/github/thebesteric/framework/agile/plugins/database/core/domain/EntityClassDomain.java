package io.github.thebesteric.framework.agile.plugins.database.core.domain;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.thebesteric.framework.agile.commons.util.ReflectUtils;
import io.github.thebesteric.framework.agile.plugins.database.core.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Transient;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * EntityClassDomain
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-05-13 14:56:34
 */
@Data
@Accessors(chain = true)
public class EntityClassDomain {
    private String tableName;
    private String comment;
    private List<String> schemas = new ArrayList<>();
    private Class<?> entityClass;
    private List<Field> entityFields = new ArrayList<>();
    private List<ColumnDomain> columnDomains = new ArrayList<>();
    private List<String> onClassUniqueColumns = new ArrayList<>();
    private List<List<String>> onClassUniqueGroupColumns = new ArrayList<>();
    private List<String> onClassIndexColumns = new ArrayList<>();
    private List<List<String>> onClassIndexGroupColumns = new ArrayList<>();

    public static EntityClassDomain of(Class<?> entityClass) {
        return of(null, entityClass);
    }

    public static EntityClassDomain of(String tableNamePrefix, Class<?> entityClass) {
        EntityClassDomain entityClassDomain = new EntityClassDomain();
        EntityClass entityClassAnno = entityClass.getDeclaredAnnotation(EntityClass.class);
        TableName tableNameAnno = entityClass.getDeclaredAnnotation(TableName.class);
        Unique[] uniqueAnnotations = entityClass.getAnnotationsByType(Unique.class);
        UniqueGroup[] uniqueGroupAnnotations = entityClass.getAnnotationsByType(UniqueGroup.class);
        Index[] indexAnnotations = entityClass.getAnnotationsByType(Index.class);
        IndexGroup[] indexGroupAnnotations = entityClass.getAnnotationsByType(IndexGroup.class);

        String name = null;
        String comment = null;
        if (entityClassAnno != null) {
            name = entityClassAnno.value();
            comment = CharSequenceUtil.isNotEmpty(entityClassAnno.comment()) ? entityClassAnno.comment() : name;
        }
        if (CharSequenceUtil.isEmpty(name) && tableNameAnno != null) {
            name = tableNameAnno.value();
        }
        if (CharSequenceUtil.isEmpty(comment)) {
            comment = name;
        }

        // 生成完整表名
        String tableName;
        tableNamePrefix = tableNamePrefix == null ? "" : tableNamePrefix.trim();
        if (CharSequenceUtil.isNotEmpty(name)) {
            tableName = tableNamePrefix + name;
        } else {
            tableName = tableNamePrefix + CharSequenceUtil.toUnderlineCase(entityClass.getSimpleName());
        }

        // 获取 schemas
        List<String> schemas = new ArrayList<>();
        if (entityClassAnno != null) {
            schemas = Arrays.stream(entityClassAnno.schemas()).toList();
        }

        entityClassDomain.tableName = tableName;
        entityClassDomain.comment = comment;
        entityClassDomain.schemas = schemas;
        entityClassDomain.entityClass = entityClass;
        entityClassDomain.entityFields = entityFields(entityClass);

        // 封装为 ColumnDomain
        for (Field entityField : entityClassDomain.entityFields) {
            entityClassDomain.columnDomains.add(ColumnDomain.of(tableName, entityField));
        }

        // 字段排序
        List<Field> sortedEntityFields = new ArrayList<>(entityClassDomain.entityFields.size());
        entityClassDomain.columnDomains.sort(Comparator.comparingInt(ColumnDomain::getSequence));
        for (ColumnDomain columnDomain : entityClassDomain.columnDomains) {
            Class<?> fieldType = columnDomain.getFieldType();
            String fieldName = columnDomain.getFieldName();
            Field field = entityClassDomain.entityFields.stream().filter(f -> fieldName.equals(f.getName()) && fieldType == f.getType())
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("字段类型解析错误，请核实"));
            sortedEntityFields.add(field);
        }
        entityClassDomain.entityFields = sortedEntityFields;

        // 唯一索引
        if (uniqueAnnotations.length > 0) {
            entityClassDomain.onClassUniqueColumns = new ArrayList<>(Arrays.stream(uniqueAnnotations).map(Unique::column).toList());
        }

        // 唯一索引组
        if (uniqueGroupAnnotations.length > 0) {
            entityClassDomain.onClassUniqueGroupColumns = new ArrayList<>(Arrays.stream(uniqueGroupAnnotations).map(uniqueGroup -> Arrays.stream(uniqueGroup.columns()).toList()).toList());
        }

        // 普通索引
        if (indexAnnotations.length > 0) {
            entityClassDomain.onClassIndexColumns = new ArrayList<>(Arrays.stream(indexAnnotations).map(Index::column).toList());
        }

        // 普通索引组
        if (indexGroupAnnotations.length > 0) {
            entityClassDomain.onClassIndexGroupColumns = new ArrayList<>(Arrays.stream(indexGroupAnnotations).map(indexGroup -> Arrays.stream(indexGroup.columns()).toList()).toList());
        }

        return entityClassDomain;
    }

    /**
     * 类签名
     *
     * @return String
     *
     * @author wangweijun
     * @since 2024/6/5 22:50
     */
    public String signature() {
        String str = this.toString();
        return DigestUtil.md5Hex(str);
    }

    /**
     * 获取类上的所有字段信息
     *
     * @return List<ColumnDomain>
     *
     * @author wangweijun
     * @since 2024/6/5 22:47
     */
    public List<ColumnDomain> tableColumnDomains() {
        List<ColumnDomain> columnDomains = new ArrayList<>();
        Field[] tableFields = this.entityClass.getDeclaredFields();
        for (Field field : tableFields) {
            ColumnDomain columnDomain = ColumnDomain.of(this.tableName, field);
            columnDomains.add(columnDomain);
        }
        return columnDomains;
    }

    /**
     * 获取普通索引（非普通索引组）
     * key 是索引的名称，value 是索引的列名
     *
     * @author wangweijun
     * @since 2024/6/5 23:18
     */
    public Map<String, String> indices() {
        List<ColumnDomain> columnDomains = tableColumnDomains();
        Map<String, String> indices = columnDomains.stream().filter(ColumnDomain::isIndex)
                .collect(Collectors.toMap(c -> ColumnDomain.generateIndexKeyName(tableName, c.getName()), ColumnDomain::getName));
        this.onClassIndexColumns.forEach(name -> {
            String indexKeyName = ColumnDomain.generateIndexKeyName(tableName, name);
            indices.put(indexKeyName, name);
        });
        return indices;
    }

    /**
     * 获取普通索引组
     * key 是索引的名称，value 是索引的列名
     *
     * @author wangweijun
     * @since 2024/6/5 23:18
     */
    public Map<String, List<String>> indexGroups() {
        Map<String, List<String>> indexGroups = new HashMap<>();
        // 获取所有字段上的索引组
        Map<String, List<ColumnDomain>> group = tableColumnDomains().stream().filter(columnDomain -> CharSequenceUtil.isNotEmpty(columnDomain.getIndexGroup()))
                .collect(Collectors.groupingBy(ColumnDomain::getIndexGroup));
        for (Map.Entry<String, List<ColumnDomain>> entry : group.entrySet()) {
            String groupIndexName = ColumnDomain.generateIndexKeyName(tableName, entry.getKey());
            List<ColumnDomain> groupColumns = entry.getValue();
            groupColumns.sort(Comparator.comparingInt(ColumnDomain::getIndexGroupSort));
            List<String> groupColumnNames = groupColumns.stream().map(ColumnDomain::getName).toList();
            indexGroups.put(groupIndexName, groupColumnNames);
        }
        // 获取所有类上的索引组
        List<List<String>> indexGroupColumns = this.getOnClassIndexGroupColumns();
        for (List<String> groupColumns : indexGroupColumns) {
            String indexGroupName = String.join("_", groupColumns);
            String indexGroupKey = ColumnDomain.generateIndexKeyName(tableName, indexGroupName);
            indexGroups.put(indexGroupKey, groupColumns);
        }
        return indexGroups;
    }

    /**
     * 获取普通索引（非普通索引组）
     * key 是索引的名称，value 是索引的列名
     *
     * @author wangweijun
     * @since 2024/6/5 23:18
     */
    public Map<String, String> uniques() {
        List<ColumnDomain> columnDomains = tableColumnDomains();
        Map<String, String> uniques = columnDomains.stream().filter(ColumnDomain::isUnique)
                .collect(Collectors.toMap(c -> ColumnDomain.generateUniqueKeyName(tableName, c.getName()), ColumnDomain::getName));
        this.onClassUniqueColumns.forEach(name -> {
            String uniqueKeyName = ColumnDomain.generateUniqueKeyName(tableName, name);
            uniques.put(uniqueKeyName, name);
        });
        return uniques;
    }

    /**
     * 获取唯一索引组
     * key 是索引的名称，value 是索引的列名
     *
     * @author wangweijun
     * @since 2024/6/5 23:18
     */
    public Map<String, List<String>> uniqueGroups() {
        Map<String, List<String>> uniqueGroups = new HashMap<>();
        // 获取所有字段上的唯一索引组
        Map<String, List<ColumnDomain>> group = tableColumnDomains().stream().filter(columnDomain -> CharSequenceUtil.isNotEmpty(columnDomain.getUniqueGroup()))
                .collect(Collectors.groupingBy(ColumnDomain::getUniqueGroup));
        for (Map.Entry<String, List<ColumnDomain>> entry : group.entrySet()) {
            String groupUniqueName = ColumnDomain.generateUniqueKeyName(tableName, entry.getKey());
            List<ColumnDomain> groupColumns = entry.getValue();
            List<String> groupColumnNames = groupColumns.stream().map(ColumnDomain::getName).toList();
            uniqueGroups.put(groupUniqueName, groupColumnNames);
        }
        // 获取所有类上的唯一索引组
        List<List<String>> uniqueGroupColumns = this.getOnClassUniqueGroupColumns();
        for (List<String> groupColumns : uniqueGroupColumns) {
            String uniqueGroupName = String.join("_", groupColumns);
            String uniqueGroupKey = ColumnDomain.generateUniqueKeyName(tableName, uniqueGroupName);
            uniqueGroups.put(uniqueGroupKey, groupColumns);
        }
        return uniqueGroups;
    }

    /**
     * 获取实体类所有字段
     *
     * @return List<Field>
     *
     * @author wangweijun
     * @since 2024/6/13 16:43
     */
    private static List<Field> entityFields(Class<?> clazz) {
        return ReflectUtils.getFields(clazz, field -> {
            EntityColumn entityColumn = field.getAnnotation(EntityColumn.class);
            if (entityColumn != null && !entityColumn.exist()) {
                return false;
            }
            TableField tableField = field.getAnnotation(TableField.class);
            if (tableField != null && !tableField.exist()) {
                return false;
            }
            Transient aTransient = field.getAnnotation(Transient.class);
            if (aTransient != null) {
                return false;
            }
            IgnoredEntityColumn ignoredEntityColumn = field.getAnnotation(IgnoredEntityColumn.class);
            if (ignoredEntityColumn != null) {
                return false;
            }
            return !ReflectUtils.isStatic(field) && !ReflectUtils.isFinal(field);
        });
    }
}
