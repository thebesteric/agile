package io.github.thebesteric.framework.agile.plugins.annotation.scanner.scan;

import io.github.thebesteric.framework.agile.commons.util.ClassUtils;
import io.github.thebesteric.framework.agile.core.scaner.ClassPathScanner;
import io.github.thebesteric.framework.agile.plugins.annotation.scanner.AnnotationParasiticContext;
import io.github.thebesteric.framework.agile.plugins.annotation.scanner.AnnotationRegister;
import io.github.thebesteric.framework.agile.plugins.annotation.scanner.domain.Parasitic;
import io.github.thebesteric.framework.agile.plugins.annotation.scanner.listener.AnnotationParasiticRegisteredListener;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

/**
 * AnnotationScanner
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-13 11:28:06
 */
@RequiredArgsConstructor
public class AnnotationScanner implements ClassPathScanner {

    private final AnnotationRegister annotationRegister;
    private final AnnotationParasiticRegisteredListener listener;

    @Override
    public void scan(String projectPath, List<String> compilePaths) {
        if (annotationRegister != null) {
            ClassPathScanner.super.scan(projectPath, compilePaths);
        }
    }

    @Override
    public void processClassFile(String className) {
        Class<?> clazz = ClassUtils.forName(className, false);
        if (clazz == null) {
            return;
        }
        List<Class<? extends Annotation>> annotationClasses = annotationRegister.getAnnotationClasses();
        for (Class<? extends Annotation> annotationClass : annotationClasses) {
            // 检查类上是否有对应的注解
            if (clazz.isAnnotationPresent(annotationClass)) {
                Annotation annotation = clazz.getAnnotation(annotationClass);
                Parasitic parasitic = Parasitic.of(annotation, clazz);
                AnnotationParasiticContext.add(annotationClass, parasitic);
                if (listener != null) {
                    listener.onClassParasiticRegistered(parasitic);
                }
            }

            // 检查方法上是否有对应的注解
            Class<?> currentClass = clazz;
            do {
                Method[] declaredMethods = currentClass.getDeclaredMethods();
                for (Method declaredMethod : declaredMethods) {
                    if (declaredMethod.isAnnotationPresent(annotationClass)) {
                        Annotation annotation = declaredMethod.getAnnotation(annotationClass);
                        Parasitic parasitic = Parasitic.of(annotation, clazz, declaredMethod);
                        AnnotationParasiticContext.add(annotationClass, parasitic);
                        if (listener != null) {
                            listener.onMethodParasiticRegistered(parasitic);
                        }
                    }
                }
                currentClass = currentClass.getSuperclass();
            } while (currentClass != Object.class);
        }
    }
}
