package io.github.thebesteric.framework.agile.plugins.database.core.annotation;

import java.lang.annotation.*;

/**
 * 唯一索引
 *
 * @author wangweijun
 * @since 2024/5/13 09:59
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Uniques {
    Unique[] value();
}
