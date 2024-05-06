package io.github.thebesteric.framework.agile.plugins.idempotent.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 接口幂等注解
 *
 * @author wangweijun
 * @since 2024/4/30 14:49
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {

    /** 幂等的超时时间 */
    int timeout() default 500;

    /** 时间单位，默认为毫秒 */
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

    /** 幂等前缀 */
    String keyPrefix() default "idempotent:";

    /** key 分隔符 */
    String delimiter() default "|";

    /** 提示信息，正在执行中的提示 */
    String message() default "Repeated request, please try again later";

}
