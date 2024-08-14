package io.github.thebesteric.framework.agile.plugins.annotation.scanner;

import io.github.thebesteric.framework.agile.plugins.annotation.scanner.domain.Parasitic;
import lombok.Getter;
import lombok.Setter;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 注解注册器
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-13 11:38:48
 */
@Getter
@Setter
public class AnnotationRegister {

    /** 存放注解与过滤条件 */
    private final Map<Class<? extends Annotation>, Function<Parasitic, Boolean>> registry = new HashMap<>();

    /**
     * 注册注解
     *
     * @param annotationClass 注解类
     * @param filter          过滤函数
     *
     * @author wangweijun
     * @since 2024/8/13 13:51
     */
    public void register(Class<? extends Annotation> annotationClass, Function<Parasitic, Boolean> filter) {
        registry.put(annotationClass, filter);
    }

    /**
     * 注册注解
     *
     * @param annotationClass 注解类
     *
     * @author wangweijun
     * @since 2024/8/13 13:51
     */
    public void register(Class<? extends Annotation> annotationClass) {
        registry.put(annotationClass, parasitic -> true);
    }
}
