package io.github.thebesteric.framework.agile.core.matcher.clazz.impl;

import io.github.thebesteric.framework.agile.core.matcher.clazz.ClassMatcher;
import org.springframework.stereotype.Component;

/**
 * ComponentBeanClassMatcher
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-03-07 16:42:29
 */
public class ComponentBeanClassMatcher implements ClassMatcher {
    @Override
    public boolean matcher(Class<?> clazz) {
        return clazz.isAnnotationPresent(Component.class);
    }
}
