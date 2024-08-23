package io.github.thebesteric.framework.agile.distributed.locks.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁注解
 *
 * @author wangweijun
 * @since 2024/8/22 15:58
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {

    /** 锁前缀：默认为 distributed-lock: */
    String keyPrefix() default "distributed-lock:";

    /** 锁名称：默认使用方法名 */
    String key() default "";

    /** 等待时间：默认 10s */
    long waitTime() default 10;

    /** 租约时间：默认 60s */
    long leaseTime() default 60;

    /** 时间单位：默认秒 */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /** 提示信息 */
    String message() default "";
}
