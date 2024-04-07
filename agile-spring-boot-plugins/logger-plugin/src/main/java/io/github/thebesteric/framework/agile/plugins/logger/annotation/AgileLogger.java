package io.github.thebesteric.framework.agile.plugins.logger.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AgileLogger {
    /**
     * Tag information
     * <p>For example: API, UPSTREAM, Controller, Service, Adapter etc.
     */
    String tag() default "default";

    /**
     * Extra information
     */
    String extra() default "";

    /**
     * The level of logging
     * <p>When an exception occurs, the log level is automatically changed to ERROR
     */
    String level() default "info";

    /**
     * Methods to ignore
     * <p>Takes effect only on class
     * <p>Method overloading is not considered if you need to consider method overloading, please use: {@link IgnoreMethod}
     *
     * @see IgnoreMethod
     */
    String[] ignoreMethods() default {};
}
