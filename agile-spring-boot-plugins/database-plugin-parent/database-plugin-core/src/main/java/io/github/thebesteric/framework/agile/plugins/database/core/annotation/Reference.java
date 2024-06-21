package io.github.thebesteric.framework.agile.plugins.database.core.annotation;

import io.github.thebesteric.framework.agile.core.domain.None;

import java.lang.annotation.*;

/**
 * 外键定义
 *
 * @author wangweijun
 * @since 2024/6/20 13:15
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Reference {
    /** 对应的目标实体类 */
    Class<?> targetEntityClass() default None.class;

    /** 对应的目标实体类中的字段名称 */
    String targetColumn() default "";
}
