package io.github.thebesteric.framework.agile.plugins.database.core.domain;

import lombok.Data;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * UpdateFieds
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-12-10 19:02:23
 */
@Data
public class ChangeFields {
    /** 表名 */
    private EntityClassDomain entityClassDomain;
    /** 需要新增的字段 */
    private List<Field> newFields = new ArrayList<>();
    /** 需要更新的字段 */
    private List<Field> updateFields = new ArrayList<>();
    /** 需要删除的字段 */
    private List<String> deleteColumns = new ArrayList<>();
    /** 目前所有字段 */
    private List<ColumnDomain> columnDomains = new ArrayList<>();

    public static ChangeFields of(EntityClassDomain entityClassDomain, List<Field> newFields, List<Field> updateFields, List<String> deleteColumns, List<ColumnDomain> columnDomains) {
        ChangeFields instance = new ChangeFields();
        instance.entityClassDomain = entityClassDomain;
        instance.newFields = newFields;
        instance.updateFields = updateFields;
        instance.deleteColumns = deleteColumns;
        instance.columnDomains = columnDomains;
        return instance;
    }
}
