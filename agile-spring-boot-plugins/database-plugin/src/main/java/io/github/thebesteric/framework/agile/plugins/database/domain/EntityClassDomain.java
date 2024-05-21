package io.github.thebesteric.framework.agile.plugins.database.domain;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.thebesteric.framework.agile.plugins.database.annotation.EntityClass;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * EntityClass
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-05-13 14:56:34
 */
@Data
@Accessors(chain = true)
public class EntityClassDomain {
    private String name;
    private String comment;
    private Class<?> entityClass;

    public static EntityClassDomain of(String tableNamePrefix, Class<?> entityClass) {
        EntityClassDomain entityClassDomain = new EntityClassDomain();
        EntityClass entityClassAnno = entityClass.getDeclaredAnnotation(EntityClass.class);
        TableName tableNameAnno = entityClass.getDeclaredAnnotation(TableName.class);

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

        // 生产完整表名
        String tableName;
        if (CharSequenceUtil.isNotEmpty(name)) {
            tableName = (tableNamePrefix == null ? "" : tableNamePrefix.trim()) + name;
        } else {
            tableName = CharSequenceUtil.toUnderlineCase(entityClass.getSimpleName());
        }

        entityClassDomain.name = tableName;
        entityClassDomain.comment = comment;
        entityClassDomain.entityClass = entityClass;
        return entityClassDomain;
    }
}
