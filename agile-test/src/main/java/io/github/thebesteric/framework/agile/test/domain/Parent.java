package io.github.thebesteric.framework.agile.test.domain;

import io.github.thebesteric.framework.agile.plugins.idempotent.annotation.IdempotentKey;
import lombok.Data;

/**
 * Parent
 *
 * @author wangweijun
 * @version v1.0
 * @since 2025-01-07 17:16:58
 */
@Data
public abstract class Parent {

    @IdempotentKey
    private String parentId;

}
