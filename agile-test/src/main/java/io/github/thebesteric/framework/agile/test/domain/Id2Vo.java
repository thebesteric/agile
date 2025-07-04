package io.github.thebesteric.framework.agile.test.domain;

import io.github.thebesteric.framework.agile.plugins.idempotent.annotation.IdempotentKey;
import lombok.Data;

import java.util.List;

/**
 * Id2Vo
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-04-30 18:29:06
 */
@Data
public class Id2Vo extends Parent {
    @IdempotentKey
    private String name;
    @IdempotentKey
    private Integer age;
    @IdempotentKey
    private List<String> hobbies;
}
