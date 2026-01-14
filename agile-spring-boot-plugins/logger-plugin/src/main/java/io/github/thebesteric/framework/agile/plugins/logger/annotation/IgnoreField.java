package io.github.thebesteric.framework.agile.plugins.logger.annotation;

import io.github.thebesteric.framework.agile.plugins.logger.constant.IgnoreFieldHandleType;

import java.lang.annotation.*;

/**
 * 日志忽略字段
 *
 * @author wangweijun
 * @version v1.0
 * @since 2026-01-14 15:43:00
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IgnoreField {

    /** 忽略的字段处理方式 */
    IgnoreFieldHandleType handleType() default IgnoreFieldHandleType.FILL_NULL;

}
