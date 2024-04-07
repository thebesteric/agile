package io.github.thebesteric.framework.agile.plugins.logger.annotation;

import java.lang.annotation.*;

/**
 * IgnoreMethods
 * 作用在类上，忽略的方法表达式
 *
 * @author Eric Joe
 * @version 1.0
 * @since 2022-08-02 13:44:04
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IgnoreMethods {
    String[] value() default {};
}
