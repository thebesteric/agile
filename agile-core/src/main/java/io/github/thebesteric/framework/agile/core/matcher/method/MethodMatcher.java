package io.github.thebesteric.framework.agile.core.matcher.method;

import java.lang.reflect.Method;

@FunctionalInterface
public interface MethodMatcher {
    boolean matcher(Method method);
}
