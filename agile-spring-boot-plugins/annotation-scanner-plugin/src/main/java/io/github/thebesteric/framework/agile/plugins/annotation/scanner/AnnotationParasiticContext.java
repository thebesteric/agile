package io.github.thebesteric.framework.agile.plugins.annotation.scanner;

import io.github.thebesteric.framework.agile.commons.util.AbstractUtils;
import io.github.thebesteric.framework.agile.plugins.annotation.scanner.domain.Parasitic;

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
public class AnnotationParasiticContext extends AbstractUtils {

    /** 存储注解类和对应的宿主集合 */
    private static final Map<Class<? extends Annotation>, List<Parasitic>> ANNOTATION_PARASITIC_MAP = new HashMap<>();

    /**
     * 添加注解对应的宿主
     *
     * @param annotationClass 注解类
     * @param parasitic       宿主
     *
     * @author wangweijun
     * @since 2024/8/13 13:51
     */
    public static void add(Class<? extends Annotation> annotationClass, Parasitic parasitic) {
        List<Parasitic> parasites = ANNOTATION_PARASITIC_MAP.getOrDefault(annotationClass, new ArrayList<>());
        parasites.add(parasitic);
        ANNOTATION_PARASITIC_MAP.put(annotationClass, parasites);
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
    public static void addAll(Class<? extends Annotation> annotationClass, List<Parasitic> parasites) {
        ANNOTATION_PARASITIC_MAP.put(annotationClass, parasites);
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
    public static List<Parasitic> get(Class<? extends Annotation> annotationClass) {
        return ANNOTATION_PARASITIC_MAP.getOrDefault(annotationClass, new ArrayList<>());
    }

    /**
     * 获取注解对应的宿主集合
     *
     * @return Map
     *
     * @author wangweijun
     * @since 2024/8/13 15:03
     */
    public static Map<Class<? extends Annotation>, List<Parasitic>> getAnnotationParasiticMap() {
        return ANNOTATION_PARASITIC_MAP;
    }
}
