package io.github.thebesteric.framework.agile.core.pointcut;

import io.github.thebesteric.framework.agile.core.matcher.clazz.ClassMatcher;
import org.springframework.lang.NonNull;

import java.io.Serial;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

/**
 * AbstractSpringComponentPointcut
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-26 13:55:41
 */
public abstract class AbstractSpringComponentMethodPointcut extends AbstractSpringComponentAnnotationPointcut {

    @Serial
    private static final long serialVersionUID = -2416148027886161695L;

    public AbstractSpringComponentMethodPointcut(List<ClassMatcher> classMatchers) {
        super(classMatchers);
    }

    @Override
    public boolean matches(@NonNull Method method, @NonNull Class<?> targetClass) {
        return this.matchedMethod(method, targetClass);
    }

    @Override
    public Class<? extends Annotation> matchedAnnotation() {
        return null;
    }

    protected abstract boolean matchedMethod(@NonNull Method method, @NonNull Class<?> targetClass);


}
