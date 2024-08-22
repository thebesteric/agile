package io.github.thebesteric.framework.agile.plugins.limiter.annotation;

import io.github.thebesteric.framework.agile.plugins.limiter.RateLimitType;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 限流注解
 *
 * @author wangweijun
 * @since 2024/5/06 16:48
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimiter {

    /** 限流时间窗 */
    int timeout() default 60;

    /** 时间窗内的限流次数 */
    int count() default 100;

    /** 时间单位，默认为毫秒 */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /** 限流前缀 */
    String keyPrefix() default "rate_limiter";

    /** 限流类型 */
    RateLimitType type() default RateLimitType.DEFAULT;

    /** KEY 分隔符 */
    String delimiter() default "|";

    /** 提示信息，正在执行中的提示 */
    String message() default "";

}
