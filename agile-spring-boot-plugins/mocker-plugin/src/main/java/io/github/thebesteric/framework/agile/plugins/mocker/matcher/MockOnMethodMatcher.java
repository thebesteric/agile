package io.github.thebesteric.framework.agile.plugins.mocker.matcher;

import io.github.thebesteric.framework.agile.core.matcher.method.MethodMatcher;
import io.github.thebesteric.framework.agile.plugins.mocker.annotation.Mock;

import java.lang.reflect.Method;

/**
 * MockerOnMethodMatcher
 *
 * @author wangweijun
 * @version v1.0
 * @since 2025-01-23 17:38:33
 */
public class MockOnMethodMatcher implements MethodMatcher {
    @Override
    public boolean matcher(Method method) {
        return method.isAnnotationPresent(Mock.class);
    }
}
