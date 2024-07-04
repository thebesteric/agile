package io.github.thebesteric.framework.agile.plugins.database.core.annotation;

import java.lang.annotation.*;

/**
 * 忽略实体类对应字段
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-05-13 10:18:26
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IgnoredEntityColumn {

}
