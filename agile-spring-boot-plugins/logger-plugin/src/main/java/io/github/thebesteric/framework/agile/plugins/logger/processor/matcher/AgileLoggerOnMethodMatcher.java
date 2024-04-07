package io.github.thebesteric.framework.agile.plugins.logger.processor.matcher;

import io.github.thebesteric.framework.agile.core.matcher.method.MethodMatcher;
import io.github.thebesteric.framework.agile.plugins.logger.annotation.AgileLogger;
import io.github.thebesteric.framework.agile.plugins.logger.annotation.IgnoreMethod;

import java.lang.reflect.Method;

/**
 * AgileLoggerMatcher
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-03-08 11:44:16
 */
public class AgileLoggerOnMethodMatcher implements MethodMatcher {
    @Override
    public boolean matcher(Method method) {
        Class<?> clazz = method.getDeclaringClass();
        if (clazz.isAnnotationPresent(AgileLogger.class)) {
            return !method.isAnnotationPresent(IgnoreMethod.class);
        }
        return method.isAnnotationPresent(AgileLogger.class) && !method.isAnnotationPresent(IgnoreMethod.class);
    }
}
