package io.github.thebesteric.framework.agile.core.matcher.clazz.impl;

import io.github.thebesteric.framework.agile.core.matcher.clazz.ClassMatcher;
import org.springframework.stereotype.Repository;

/**
 * RepositoryBeanClassMatcher
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-03-07 16:40:48
 */
public class RepositoryBeanClassMatcher implements ClassMatcher {
    @Override
    public boolean matcher(Class<?> clazz) {
        return clazz.isAnnotationPresent(Repository.class);
    }
}
