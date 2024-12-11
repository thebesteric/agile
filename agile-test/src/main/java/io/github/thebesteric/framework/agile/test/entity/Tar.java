package io.github.thebesteric.framework.agile.test.entity;

import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityClass;
import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityColumn;

/**
 * Tar
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-12-11 09:58:55
 */
@EntityClass(comment = "The tar")
public class Tar extends BaseEntity {

    private String name;

    @EntityColumn(sequence = 2, comment = "租户 ID")
    private String tenantId;

    @EntityColumn(sequence = 1, comment = "租户 ID")
    private String a;

}
