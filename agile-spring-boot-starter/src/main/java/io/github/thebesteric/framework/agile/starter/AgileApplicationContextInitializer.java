package io.github.thebesteric.framework.agile.starter;

import io.github.thebesteric.framework.agile.commons.constant.AgilePlugins;
import io.github.thebesteric.framework.agile.commons.util.LoggerPrinter;
import io.github.thebesteric.framework.agile.core.domain.PackageFinder;
import io.github.thebesteric.framework.agile.starter.annotaion.EnableAgile;
import lombok.SneakyThrows;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.util.List;

/**
 * AgileApplicationContextInitializer
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-03-13 23:04:55
 */
public class AgileApplicationContextInitializer implements ApplicationContextAware {

    private static final List<String> REQUIRED_CLASSES = List.of(
            "org.aspectj.lang.annotation.Aspect",
            "org.aspectj.weaver.Advice"
    );

    private void initialize(ApplicationContext applicationContext) {

        // 打印包扫描路径
        LoggerPrinter.info("Base package path is {}", PackageFinder.getPackageNames());

        // 检测是否包含 AspectJ 相关类
        REQUIRED_CLASSES.forEach(className -> {
            try {
                Class.forName(className);
            } catch (ClassNotFoundException e) {
                LoggerPrinter.error(" The required class '{}' is not found. Please make sure the AspectJ library is included. ", className);
            }
        });

        // 获取所有 @EnableAspectJAutoProxy 注解的配置类
        applicationContext.getBeansWithAnnotation(EnableAspectJAutoProxy.class).values().stream()
                .map(Object::getClass)
                .filter(clazz -> clazz.isAnnotationPresent(Configuration.class))
                .findAny()
                .ifPresentOrElse(clazz -> {
                }, () -> {
                    LoggerPrinter.error("Make sure @EnableAspectJAutoProxy is enabled in the project configuration.");
                });

        // 获取 @EnableAgile 启动类
        applicationContext.getBeansWithAnnotation(EnableAgile.class).values().stream()
                .map(Object::getClass)
                .filter(clazz -> clazz.isAnnotationPresent(Configuration.class))
                .findAny()
                .ifPresentOrElse(clazz -> {
                }, () -> {
                    LoggerPrinter.error("Make sure @EnableAgile is added to the configuration class.");
                });


        // 检测各个插件是否安装
        AgilePlugins[] plugins = AgilePlugins.values();
        for (AgilePlugins plugin : plugins) {
            try {
                Class.forName(plugin.getClassName());
            } catch (ClassNotFoundException ignored) {
                LoggerPrinter.warn("{} plug-in not found.", plugin.getName());
                continue;
            }
            LoggerPrinter.info("The {} has been installed.", plugin.getName());
        }
    }

    @SneakyThrows
    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        // 获取启动类
        initialize(applicationContext);
    }
}
