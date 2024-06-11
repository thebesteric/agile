package io.github.thebesteric.framework.agile.plugins.database.annotation;

import java.lang.annotation.*;

/**
 * 唯一索引组
 *
 * @author wangweijun
 * @since 2024/5/28 11:10
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IndexGroups {
    IndexGroup[] value();
}
