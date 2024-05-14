package io.github.thebesteric.framework.agile.plugins.database.domain;

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
    private String tableName;
}
