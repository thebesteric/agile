package io.github.thebesteric.framework.agile.plugins.database.core.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityColumn;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * BaseEntity
 *
 * @author wangweijun
 * @version v1.0
 * @since 2026-01-19 13:49:26
 */
@Getter
@Setter
public abstract class BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = -4420494594607379574L;

    // 基础序号
    protected static final int BASE_SEQUENCE = 10000;

    /** 主键：使用雪花算法，数据库对应类型 bigint */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @EntityColumn(sequence = Integer.MIN_VALUE, type = EntityColumn.Type.BIG_INT, primary = true, comment = "主键")
    protected Long id;

    /** 创建日期：无需手动维护，对应数据库类型 datetime */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @EntityColumn(sequence = BASE_SEQUENCE + 1, type = EntityColumn.Type.DATETIME, comment = "创建日期")
    protected Date createdAt;

    /** 更新日期：无需手动维护，对应数据库类型 datetime */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    @EntityColumn(sequence = BASE_SEQUENCE + 2, type = EntityColumn.Type.DATETIME, comment = "更新日期")
    protected Date updatedAt;

    /** 创建人：无需手动维护，对应数据库类型 varchar(32) */
    @TableField(value = "created_by", fill = FieldFill.INSERT)
    @EntityColumn(sequence = BASE_SEQUENCE + 3, type = EntityColumn.Type.VARCHAR, length = 32, comment = "创建人")
    protected String createdBy;

    /** 更新人：无需手动维护，对应数据库类型 varchar(32) */
    @TableField(value = "updated_by", fill = FieldFill.INSERT_UPDATE)
    @EntityColumn(sequence = BASE_SEQUENCE + 4, type = EntityColumn.Type.VARCHAR, length = 32, comment = "更新人")
    protected String updatedBy;

    /** 描述：对应数据库类型 varchar(255) */
    @TableField(value = "`desc`")
    @EntityColumn(sequence = BASE_SEQUENCE + 9999, type = EntityColumn.Type.VARCHAR, length = 255, comment = "描述")
    protected String desc;
}
