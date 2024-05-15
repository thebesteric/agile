package io.github.thebesteric.framework.agile.plugins.database.annotation;

import java.lang.annotation.*;

/**
 * 实体类对于表名
 *
 * @author wangweijun
 * @since 2024/5/13 09:59
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EntityClass {

    /** 表名 */
    String value() default "";

    /** 忽略创建表 */
    boolean ignore() default false;
}
