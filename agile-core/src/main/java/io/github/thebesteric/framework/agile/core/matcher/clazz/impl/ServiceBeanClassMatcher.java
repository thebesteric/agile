package io.github.thebesteric.framework.agile.core.matcher.clazz.impl;

import io.github.thebesteric.framework.agile.core.matcher.clazz.ClassMatcher;
import org.springframework.stereotype.Service;

/**
 * ServiceBeanClassMatcher
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-03-07 16:37:28
 */
public class ServiceBeanClassMatcher implements ClassMatcher {
    @Override
    public boolean matcher(Class<?> clazz) {
        return clazz.isAnnotationPresent(Service.class);
    }
}
