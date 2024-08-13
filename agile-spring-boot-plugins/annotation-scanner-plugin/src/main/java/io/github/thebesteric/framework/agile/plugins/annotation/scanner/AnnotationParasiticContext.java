package io.github.thebesteric.framework.agile.plugins.annotation.scanner;

import io.github.thebesteric.framework.agile.commons.util.ClassPathUtils;
import io.github.thebesteric.framework.agile.plugins.annotation.scanner.domain.Parasitic;
import io.github.thebesteric.framework.agile.plugins.annotation.scanner.listener.AnnotationParasiticRegisteredListener;
import io.github.thebesteric.framework.agile.plugins.annotation.scanner.scan.AnnotationScanner;
import lombok.Getter;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AnnotationParasiticContext
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-13 13:42:40
 */
@Getter
public class AnnotationParasiticContext {

    private final AnnotationRegister annotationRegister;
    private final AnnotationParasiticRegisteredListener listener;


    public AnnotationParasiticContext(AnnotationRegister annotationRegister, AnnotationParasiticRegisteredListener listener) {
        this.annotationRegister = annotationRegister;
        this.listener = listener;
        String projectPath = ClassPathUtils.getProjectPath();
        List<String> compilePaths = ClassPathUtils.compilePaths;
        // 开始扫描注解
        new AnnotationScanner(this).scan(projectPath, compilePaths);
    }


    /** 存储注解类和对应的宿主集合 */
    private final Map<Class<? extends Annotation>, List<Parasitic>> annotationParasiticMap = new HashMap<>();

    /**
     * 添加注解对应的宿主
     *
     * @param annotationClass 注解类
     * @param parasitic       宿主
     *
     * @author wangweijun
     * @since 2024/8/13 13:51
     */
    public void add(Class<? extends Annotation> annotationClass, Parasitic parasitic) {
        List<Parasitic> parasites = annotationParasiticMap.getOrDefault(annotationClass, new ArrayList<>());
        parasites.add(parasitic);
        annotationParasiticMap.put(annotationClass, parasites);
    }

    /**
     * 添加注解对应的宿主
     *
     * @param annotationClass 注解类
     * @param parasites       宿主
     *
     * @author wangweijun
     * @since 2024/8/13 13:51
     */
    public void addAll(Class<? extends Annotation> annotationClass, List<Parasitic> parasites) {
        annotationParasiticMap.put(annotationClass, parasites);
    }

    /**
     * 获取注解对应的宿主
     *
     * @param annotationClass 注解类
     *
     * @return List<Parasitic>
     *
     * @author wangweijun
     * @since 2024/8/13 13:51
     */
    public List<Parasitic> get(Class<? extends Annotation> annotationClass) {
        return annotationParasiticMap.getOrDefault(annotationClass, new ArrayList<>());
    }

}
