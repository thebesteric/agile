package io.github.thebesteric.framework.agile.test.entity;

import io.github.thebesteric.framework.agile.plugins.database.annotation.EntityColumn;

/**
 * BaseEntity
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-05-13 18:56:49
 */
public abstract class BaseEntity {
    @EntityColumn(autoIncrement = true, comment = "主键", primary = true)
    // @TableId(type = IdType.AUTO)
    private Integer id;
}
