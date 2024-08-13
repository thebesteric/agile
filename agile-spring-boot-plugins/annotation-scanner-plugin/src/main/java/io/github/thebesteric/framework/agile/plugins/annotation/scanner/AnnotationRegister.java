package io.github.thebesteric.framework.agile.plugins.annotation.scanner;

import lombok.Getter;
import lombok.Setter;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

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
    private List<Class<? extends Annotation>> annotationClasses = new ArrayList<>();

    /**
     * 注册注解
     *
     * @param annotationClass 注解类
     *
     * @author wangweijun
     * @since 2024/8/13 13:51
     */
    public void register(Class<? extends Annotation> annotationClass) {
        annotationClasses.add(annotationClass);
    }
}
