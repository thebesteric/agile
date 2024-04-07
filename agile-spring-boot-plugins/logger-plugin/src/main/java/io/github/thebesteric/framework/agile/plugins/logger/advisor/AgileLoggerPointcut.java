package io.github.thebesteric.framework.agile.plugins.logger.advisor;

import io.github.thebesteric.framework.agile.core.domain.PackageFinder;
import io.github.thebesteric.framework.agile.core.matcher.clazz.ClassMatcher;
import io.github.thebesteric.framework.agile.plugins.logger.config.AgileLoggerContext;
import io.github.thebesteric.framework.agile.plugins.logger.domain.IgnoredMethod;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.support.JdkRegexpMethodPointcut;
import org.springframework.lang.NonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * AgileLoggerPointcut
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-04-06 16:42:05
 */
public class AgileLoggerPointcut extends JdkRegexpMethodPointcut {

    private final AgileLoggerContext context;

    private static final Map<Class<?>, Set<IgnoredMethod>> ignoredMethods = new HashMap<>(8);

    public AgileLoggerPointcut(AgileLoggerContext context) {
        this.context = context;

        // 创建正则表达式切点，匹配方法
        String[] packageNames = PackageFinder.getPackageNames().toArray(new String[0]);
        for (int i = 0; i < packageNames.length; i++) {
            packageNames[i] = packageNames[i] + ".*";
        }
        this.setPatterns(packageNames);
    }

    @NonNull
    @Override
    public ClassFilter getClassFilter() {
        return clazz -> {
            for (Annotation annotation : clazz.getAnnotations()) {
                for (ClassMatcher classMatcher : context.getClassMatchers()) {
                    if (classMatcher.matcher(annotation.annotationType())) {
                        return true;
                    }
                }
            }
            return false;
        };
    }

    @Override
    public boolean matches(@NonNull Method method, @NonNull Class<?> targetClass) {
        Set<IgnoredMethod> currentIgnoredMethods = ignoredMethods.get(targetClass);
        if (currentIgnoredMethods != null) {
            return doMatches(method, currentIgnoredMethods);
        }
        currentIgnoredMethods = IgnoredMethod.findIgnoreMethods(targetClass);
        ignoredMethods.put(targetClass, currentIgnoredMethods);
        return doMatches(method, currentIgnoredMethods);
    }

    private boolean doMatches(Method method, Set<IgnoredMethod> currentIgnoredMethods) {
        return !IgnoredMethod.matches(method, currentIgnoredMethods, context.getMethodMatchers());
    }
}
