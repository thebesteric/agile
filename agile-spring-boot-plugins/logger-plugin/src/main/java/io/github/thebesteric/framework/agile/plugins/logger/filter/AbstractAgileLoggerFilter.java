package io.github.thebesteric.framework.agile.plugins.logger.filter;

import io.github.thebesteric.framework.agile.core.matcher.method.MethodMatcher;
import io.github.thebesteric.framework.agile.plugins.logger.config.AgileLoggerContext;
import io.github.thebesteric.framework.agile.plugins.logger.domain.IgnoredMethod;
import jakarta.servlet.Filter;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AbstractAgileLoggerFilter
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-04-07 22:00:15
 */
public abstract class AbstractAgileLoggerFilter implements Filter {
    protected final AgileLoggerContext agileLoggerContext;

    public static final Map<String, Method> URL_MAPPING = new ConcurrentHashMap<>(128);

    protected static Map<Class<?>, Set<IgnoredMethod>> classIgnoredMethods = new HashMap<>();

    protected AbstractAgileLoggerFilter(AgileLoggerContext agileLoggerContext) {
        this.agileLoggerContext = agileLoggerContext;
    }

    protected String getRelativeRequestURI(ServletRequest servletRequest) {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String uri = request.getRequestURI();
        // Add uri prefix if present
        String uriPrefix = this.agileLoggerContext.getProperties().getLogger().getUriPrefix();
        if (uriPrefix == null) {
            uriPrefix = this.agileLoggerContext.getContextPath();
        }
        if (uriPrefix != null) {
            uri = uri.substring(uriPrefix.length());
        }
        return uri;
    }

    protected Set<IgnoredMethod> findIgnoredMethods(Class<?> clazz) {
        Set<IgnoredMethod> ignoredMethods = classIgnoredMethods.getOrDefault(clazz, Collections.emptySet());
        if (ignoredMethods.isEmpty()) {
            ignoredMethods = IgnoredMethod.findIgnoreMethods(clazz);
            classIgnoredMethods.put(clazz, ignoredMethods);
        }
        return ignoredMethods;
    }

    protected boolean ignoreMethodMatchers(Method method, Set<IgnoredMethod> ignoredMethods, List<MethodMatcher> methodMatchers) {
        return IgnoredMethod.matches(method, ignoredMethods, methodMatchers);
    }
}
