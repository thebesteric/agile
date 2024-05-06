package io.github.thebesteric.framework.agile.plugins.idempotent.annotation;

import java.lang.annotation.*;

/**
 * 需要加入幂等校验的参数
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-04-30 14:56:10
 */
@Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IdempotentKey {
}
