package io.github.thebesteric.framework.agile.plugins.idempotent.advisor;

import io.github.thebesteric.framework.agile.core.domain.PackageFinder;
import io.github.thebesteric.framework.agile.core.matcher.clazz.ClassMatcher;
import io.github.thebesteric.framework.agile.plugins.idempotent.annotation.Idempotent;
import io.github.thebesteric.framework.agile.plugins.idempotent.config.AgileIdempotentContext;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.support.JdkRegexpMethodPointcut;
import org.springframework.lang.NonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * AgileIdempotentPointcut
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-04-30 15:29:43
 */
public class AgileIdempotentPointcut extends JdkRegexpMethodPointcut {

    private final AgileIdempotentContext context;

    public AgileIdempotentPointcut(AgileIdempotentContext context) {
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
        return method.isAnnotationPresent(Idempotent.class);
    }

}
