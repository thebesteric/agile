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

    /**
     * 获取当前代理对象
     *
     * @param proxyClass 代理类
     *
     * @return T
     *
     * @author wangweijun
     * @since 2026/1/8 14:04
     */
    @SuppressWarnings("unchecked")
    public static <T> T currentProxy(Class<T> proxyClass) {
        Assert.notNull(proxyClass, "proxy class cannot be null");
        return (T) AopContext.currentProxy();
    }

    /**
     * 获取启动类
     *
     * @param applicationContext Spring 上下文
     *
     * @return Class<?> 启动类
     *
     * @author wangweijun
     * @since 2024/5/13 10:06
     */
    public static Class<?> getStartupClass(ApplicationContext applicationContext) {
        if (startupClass != null) {
            return startupClass;
        }
        Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(SpringBootApplication.class);
        for (Object bean : beansWithAnnotation.values()) {
            Class<?> beanClass = bean.getClass();
            if (AnnotationUtils.findAnnotation(beanClass, SpringBootApplication.class) != null) {
                String beanClassName = beanClass.getName();
                int indexOf = beanClassName.indexOf("$$");
                if (indexOf > 0) {
                    beanClassName = beanClassName.substring(0, indexOf);
                }
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