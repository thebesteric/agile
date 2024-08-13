package io.github.thebesteric.framework.agile.plugins.annotation.scanner.domain;

import lombok.Getter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * 注解所在的宿主
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-13 13:37:11
 */
@Getter
public class Parasitic {
    /** 注解实例 */
    private Annotation annotation;

    /** 注解所在的类 */
    private Class<?> clazz;

    /** 注解所在的方法 */
    private Method method;

    public static Parasitic of(Annotation annotation, Class<?> clazz, Method method) {
        Parasitic parasitic = new Parasitic();
        parasitic.annotation = annotation;
        parasitic.clazz = clazz;
        parasitic.method = method;
        return parasitic;
    }

    public static Parasitic of(Annotation annotation, Class<?> clazz) {
        return Parasitic.of(annotation, clazz, null);
    }

    public boolean annotationOnClass() {
        return clazz.isAnnotationPresent(annotation.annotationType());
    }

    public boolean annotationOnMethod() {
        return method != null && method.isAnnotationPresent(annotation.annotationType());
    }
}
