package io.github.thebesteric.framework.agile.core.pointcut;

import io.github.thebesteric.framework.agile.core.domain.PackageFinder;
import io.github.thebesteric.framework.agile.core.matcher.clazz.ClassMatcher;
import lombok.EqualsAndHashCode;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.support.JdkRegexpMethodPointcut;
import org.springframework.lang.NonNull;

import java.io.Serial;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 抽象的 Spring 组件的切点
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-05-06 17:39:22
 */
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractSpringComponentAnnotationPointcut extends JdkRegexpMethodPointcut {
    @Serial
    private static final long serialVersionUID = -1341792015044651410L;

    protected final List<ClassMatcher> classMatchers;

    protected AbstractSpringComponentAnnotationPointcut(List<ClassMatcher> classMatchers) {
        this.classMatchers = classMatchers;
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
                for (ClassMatcher classMatcher : this.classMatchers) {
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
        return method.isAnnotationPresent(matchedAnnotation());
    }

    /**
     * 返回匹配的注解
     *
     * @return Class<Annotation> 匹配的注解
     *
     * @author wangweijun
     * @since 2024/5/6 17:44
     */
    protected abstract Class<? extends Annotation> matchedAnnotation();

}
