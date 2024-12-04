package io.github.thebesteric.framework.agile.plugins.workflow.entity.base;

import io.github.thebesteric.framework.agile.commons.util.DateUtils;
import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityColumn;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * BaseEntity
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-11 20:44:25
 */
@Data
public abstract class BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1746619105652547202L;

    public static final String[] IGNORE_COPY_FIELD_NAMES = {"id", "createdAt", "updatedAt", "createdBy", "updatedBy", "desc", "version"};

    @EntityColumn(primary = true, autoIncrement = true, comment = "主键")
    protected Integer id;

    @EntityColumn(comment = "创建时间")
    protected Date createdAt = new Date();

    @EntityColumn(comment = "更新日期")
    protected Date updatedAt;

    @EntityColumn(length = 32, comment = "创建人")
    protected String createdBy;

    @EntityColumn(length = 32, comment = "更新人")
    protected String updatedBy;

    @EntityColumn(type = EntityColumn.Type.SMALL_INT, defaultExpression = "1", comment = "状态：0-禁用，1-启用")
    protected Integer state = 1;

    @EntityColumn(length = 255, comment = "描述")
    protected String desc;

    @EntityColumn(comment = "版本号")
    protected Integer version = 0;

    public static <T extends BaseEntity> T of(T entity, ResultSet rs) throws SQLException {
        entity.setId(rs.getInt("id"));
        entity.setCreatedAt(DateUtils.parseToDateTime(rs.getString("created_at")));
        entity.setCreatedBy(rs.getString("created_by"));
        entity.setUpdatedAt(DateUtils.parseToDateTime(rs.getString("updated_at")));
        entity.setUpdatedBy(rs.getString("updated_by"));
        entity.setState(rs.getInt("state"));
        entity.setDesc(rs.getString("desc"));
        entity.setVersion(rs.getInt("version"));
        return entity;
    }
}
