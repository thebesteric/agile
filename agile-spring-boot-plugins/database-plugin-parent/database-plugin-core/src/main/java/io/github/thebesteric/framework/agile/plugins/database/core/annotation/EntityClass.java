package io.github.thebesteric.framework.agile.plugins.database.core.annotation;

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

    /** 注释 */
    String comment() default "";

    /** 忽略创建表 */
    boolean ignore() default false;

    /** 指定数据库（默认表示所有库都会创建） */
    String[] schemas() default {};
}
