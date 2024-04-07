package io.github.thebesteric.framework.agile.core;

import org.springframework.aop.framework.AopContext;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * AgileContext
 *
 * @author Eric Joe
 * @since 1.0
 */
public final class AgileContext {

    private static Class<?> startupClass = null;

    private AgileContext() {
        super();
    }

    @SuppressWarnings("unchecked")
    public static <T> T currentProxy(Class<T> proxyClass) {
        Assert.notNull(proxyClass, "proxy class cannot be null");
        return (T) AopContext.currentProxy();
    }

    public static Class<?> getStartupClass(ApplicationContext applicationContext) {
        if (startupClass != null) {
            return startupClass;
        }
        Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(SpringBootApplication.class);
        for (Object bean : beansWithAnnotation.values()) {
            Class<?> beanClass = bean.getClass();
            if (AnnotationUtils.findAnnotation(beanClass, SpringBootApplication.class) != null) {
                String beanClassName = beanClass.getName();
                beanClassName = beanClassName.substring(0, beanClassName.indexOf("$$"));
                try {
                    startupClass = Class.forName(beanClassName);
                    return startupClass;
                } catch (ClassNotFoundException ignored) {
                    // startup class cannot be null
                    break;
                }
            }
        }
        throw new IllegalArgumentException("startup class cannot be null");
    }

}
