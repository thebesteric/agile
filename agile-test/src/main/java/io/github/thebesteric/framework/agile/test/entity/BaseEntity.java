package io.github.thebesteric.framework.agile.test.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityColumn;

/**
 * BaseEntity
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-05-13 18:56:49
 */
public abstract class BaseEntity {

    // @EntityColumn(autoIncrement = true, comment = "主键", primary = true)
    @TableId(type = IdType.AUTO)
    private Integer id;

    @EntityColumn(sequence = 1)
    @TableField(value = "`desc`")
    private String desc;

    @TableField(exist = false)
    private String nickname;
}
