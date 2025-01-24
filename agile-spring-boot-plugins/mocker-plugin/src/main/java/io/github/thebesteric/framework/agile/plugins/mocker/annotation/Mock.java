package io.github.thebesteric.framework.agile.plugins.mocker.annotation;

import io.github.thebesteric.framework.agile.plugins.mocker.mocker.EmptyMocker;
import io.github.thebesteric.framework.agile.plugins.mocker.mocker.MockType;
import io.github.thebesteric.framework.agile.plugins.mocker.mocker.Mocker;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Mock {

    /** 生效环境 */
    String[] envs() default {};

    /** 条件 */
    String condition() default "true";

    /** 类型 */
    MockType type();

    /** 目标类 */
    Class<? extends Mocker<?>> targetClass() default EmptyMocker.class;

    /** 路径 */
    String path() default "";
}
