package io.github.thebesteric.framework.agile.plugins.logger.processor.matcher;

import io.github.thebesteric.framework.agile.core.matcher.method.MethodMatcher;
import io.github.thebesteric.framework.agile.plugins.logger.annotation.AgileLogger;

import java.lang.reflect.Method;

/**
 * AgileLoggerOnClassMethodMatcher
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-03-14 10:04:50
 */
public class AgileLoggerOnClassMatcher implements MethodMatcher {
    @Override
    public boolean matcher(Method method) {
        Class<?> clazz = method.getDeclaringClass();
        return clazz.isAnnotationPresent(AgileLogger.class);
    }
}
