package io.github.thebesteric.framework.agile.plugins.mocker.matcher;

import io.github.thebesteric.framework.agile.core.matcher.clazz.ClassMatcher;
import io.github.thebesteric.framework.agile.core.matcher.clazz.impl.ComponentBeanClassMatcher;
import io.github.thebesteric.framework.agile.core.matcher.clazz.impl.ControllerBeanClassMatcher;
import io.github.thebesteric.framework.agile.core.matcher.clazz.impl.RepositoryBeanClassMatcher;
import io.github.thebesteric.framework.agile.core.matcher.clazz.impl.ServiceBeanClassMatcher;
import io.github.thebesteric.framework.agile.plugins.mocker.annotation.Mocker;

import java.util.List;

/**
 * MockerOnClassMatcher
 *
 * @author wangweijun
 * @version v1.0
 * @since 2025-01-23 17:41:10
 */
public class MockerOnClassMatcher implements ClassMatcher {

    private final List<ClassMatcher> beanClassMatchers;

    public MockerOnClassMatcher() {
        this.beanClassMatchers = List.of(new ControllerBeanClassMatcher(), new ServiceBeanClassMatcher(), new ComponentBeanClassMatcher(), new RepositoryBeanClassMatcher());
    }

    @Override
    public boolean matcher(Class<?> clazz) {
        for (ClassMatcher beanClassMatcher : beanClassMatchers) {
            if (beanClassMatcher.matcher(clazz) || clazz.isAnnotationPresent(Mocker.class)) {
                return true;
            }
        }
        return false;
    }
}
