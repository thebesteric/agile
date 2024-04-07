package io.github.thebesteric.framework.agile.plugins.logger.annotation;

import java.lang.annotation.*;

/**
 * IgnoreMethod
 * 作用在方法上，表示忽略该方法
 *
 * @author Eric Joe
 * @version 1.0
 * @since 2022-08-02 13:43:06
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IgnoreMethod {
}
