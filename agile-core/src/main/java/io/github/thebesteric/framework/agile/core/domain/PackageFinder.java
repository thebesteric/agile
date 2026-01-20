package io.github.thebesteric.framework.agile.core.domain;

import lombok.Getter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * PackageFinder
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-04-06 11:46:37
 */
public class PackageFinder {

    // 包路径
    @Getter
    private static final Set<String> packageNames = new LinkedHashSet<>();

    private PackageFinder() {
        super();
    }

    public static void init(Class<?> startupClass) {
        ComponentScan componentScan = startupClass.getAnnotation(ComponentScan.class);
        if (componentScan != null && (componentScan.basePackages().length > 0 || componentScan.value().length > 0)) {
            packageNames.addAll(Arrays.asList(parseComponentScanAnnotation(componentScan)));
        }

        ComponentScans componentScans = startupClass.getAnnotation(ComponentScans.class);
        if (componentScans != null) {
            for (ComponentScan cs : componentScans.value()) {
                packageNames.addAll(Arrays.asList(parseComponentScanAnnotation(cs)));
            }
        }

        if (packageNames.isEmpty()) {
            packageNames.add(startupClass.getPackageName());
        }
    }

    private static String[] parseComponentScanAnnotation(ComponentScan componentScan) {
        if (componentScan != null) {
            if (componentScan.value().length > 0) {
                return componentScan.value();
            }
            if (componentScan.basePackages().length > 0) {
                return componentScan.basePackages();
            }
        }
        return new String[0];
    }
}
