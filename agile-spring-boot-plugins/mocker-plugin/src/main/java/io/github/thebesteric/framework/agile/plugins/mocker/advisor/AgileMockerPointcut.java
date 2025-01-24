package io.github.thebesteric.framework.agile.plugins.mocker.advisor;

import io.github.thebesteric.framework.agile.core.domain.PackageFinder;
import io.github.thebesteric.framework.agile.core.matcher.clazz.ClassMatcher;
import io.github.thebesteric.framework.agile.core.matcher.method.MethodMatcher;
import io.github.thebesteric.framework.agile.plugins.mocker.config.AgileMockerContext;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.support.JdkRegexpMethodPointcut;
import org.springframework.lang.NonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * AgileMockerPointcut
 *
 * @author wangweijun
 * @version v1.0
 * @since 2025-01-23 17:28:58
 */
public class AgileMockerPointcut extends JdkRegexpMethodPointcut {

    private final AgileMockerContext context;


    public AgileMockerPointcut(AgileMockerContext context) {
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
                ClassMatcher classMatcher = context.getClassMatcher();
                if (classMatcher.matcher(annotation.annotationType())) {
                    return true;
                }
            }
            return false;
        };
    }

    @Override
    public boolean matches(@NonNull Method method, @NonNull Class<?> targetClass) {
        MethodMatcher methodMatcher = context.getMethodMatcher();
        return methodMatcher.matcher(method);
    }

}
