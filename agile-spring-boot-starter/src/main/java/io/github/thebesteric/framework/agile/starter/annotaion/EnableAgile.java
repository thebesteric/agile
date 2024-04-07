package io.github.thebesteric.framework.agile.starter.annotaion;

import io.github.thebesteric.framework.agile.starter.AgileMarker;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * EnableAgile
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-03-11 16:53:38
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(AgileMarker.class)
@Documented
public @interface EnableAgile {
    boolean logger() default true;
    boolean versioner() default false;
    boolean mocker() default false;
}
