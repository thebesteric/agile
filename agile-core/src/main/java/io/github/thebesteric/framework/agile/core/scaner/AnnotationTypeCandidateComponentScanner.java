package io.github.thebesteric.framework.agile.core.scaner;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * 组件扫描器
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-05-13 10:23:18
 */
public class AnnotationTypeCandidateComponentScanner {

    private final List<String> basePackages;

    private List<AnnotationTypeFilter> includeFilters = new ArrayList<>();

    public AnnotationTypeCandidateComponentScanner(List<String> basePackages, List<Class<? extends Annotation>> classes) {
        this.basePackages = basePackages;
        for (Class<? extends Annotation> clazz : classes) {
            this.includeFilters.add(new AnnotationTypeFilter(clazz));
        }
    }

    public AnnotationTypeCandidateComponentScanner(List<String> basePackages, Class<? extends Annotation> clazz) {
        this(basePackages, List.of(clazz));
    }

    public <T extends Annotation> Set<String> scan() {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        for (AnnotationTypeFilter filter : includeFilters) {
            scanner.addIncludeFilter(filter);
        }
        Set<String> classNames = new HashSet<>();
        for (String basePackage : Optional.ofNullable(basePackages).orElse(new ArrayList<>())) {
            Set<BeanDefinition> components = scanner.findCandidateComponents(basePackage);
            classNames.addAll(components.stream().map(BeanDefinition::getBeanClassName).toList());
        }
        return classNames;
    }

}
