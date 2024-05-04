package io.github.thebesteric.framework.agile.starter;

import io.github.thebesteric.framework.agile.commons.util.LoggerPrinter;
import io.github.thebesteric.framework.agile.core.AgileContext;
import io.github.thebesteric.framework.agile.core.domain.PackageFinder;
import io.github.thebesteric.framework.agile.starter.annotaion.EnableAgile;
import lombok.SneakyThrows;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;

/**
 * AgileApplicationContextInitializer
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-03-13 23:04:55
 */
public class AgileApplicationContextInitializer implements ApplicationContextAware {

    private void initialize(Class<?> startupClass) {

        if (!startupClass.isAnnotationPresent(EnableAgile.class)) {
            return;
        }

        // 设置包扫描路径
        LoggerPrinter.info("Base package path is {}", PackageFinder.getPackageNames());

        AgilePlugins[] plugins = AgilePlugins.values();
        for (AgilePlugins plugin : plugins) {
            try {
                Class.forName(plugin.className);
            } catch (ClassNotFoundException ignored) {
                LoggerPrinter.warn("{} plug-in not found.", plugin.name());
                continue;
            }
            LoggerPrinter.info("The {} has been installed.", plugin.name);
        }
    }

    @SneakyThrows
    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        Class<?> startupClass = AgileContext.getStartupClass(applicationContext);
        initialize(startupClass);
    }
}
