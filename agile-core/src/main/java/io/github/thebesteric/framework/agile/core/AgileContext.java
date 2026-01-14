package io.github.thebesteric.framework.agile.core;

import io.github.thebesteric.framework.agile.commons.util.LoggerPrinter;
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
     * 获取代理类的最终目标类名
     *
     * @param proxyClass 代理类
     *
     * @return String 目标类名
     *
     * @author wangweijun
     * @since 2024/5/13 10:05
     */
    public static Class<?> ultimateTargetClass(Class<?> proxyClass) {
        String proxyClassName = proxyClass.getName();
        int indexOf = proxyClassName.indexOf("$$SpringCGLIB$$");
        if (indexOf > 0) {
            proxyClassName = proxyClassName.substring(0, indexOf);
        }
        try {
            return Class.forName(proxyClassName);
        } catch (ClassNotFoundException ignored) {
            LoggerPrinter.warn("Cannot find ultimate target class for proxy class: {}", proxyClass.getName());
        }
        return proxyClass;
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
                return ultimateTargetClass(beanClass);
            }
        }
        throw new IllegalArgumentException("startup class cannot be null");
    }

}