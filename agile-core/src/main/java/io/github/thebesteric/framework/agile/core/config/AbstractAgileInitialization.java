package io.github.thebesteric.framework.agile.core.config;

import io.github.thebesteric.framework.agile.core.AgileContext;
import io.github.thebesteric.framework.agile.core.domain.PackageFinder;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.lang.NonNull;

/**
 * AbstractAgileInitialization
 *
 * @author Eric Joe
 * @version 1.0
 */
public abstract class AbstractAgileInitialization implements SmartLifecycle, ApplicationContextAware {

    protected boolean isRunning = false;
    protected GenericApplicationContext applicationContext;

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop() {
        isRunning = false;
    }

    @Override
    public boolean isRunning() {
        return this.isRunning;
    }

    @Override
    public int getPhase() {
        return 0;
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (GenericApplicationContext) applicationContext;
        Class<?> startupClass = AgileContext.getStartupClass(this.applicationContext);
        // 初始化扫描包
        PackageFinder.init(startupClass);
    }

    protected <T> T getBean(Class<T> clazz) {
        try {
            return applicationContext.getBean(clazz);
        } catch (Exception ex) {
            return null;
        }
    }

    protected <T> T getBean(String name, Class<T> clazz) {
        return getBeanOrDefault(name, clazz, null);
    }

    protected <T> T getBeanOrDefault(String name, Class<T> clazz, T defaultValue) {
        try {
            return applicationContext.getBean(name, clazz);
        } catch (Exception ex) {
            return defaultValue;
        }
    }
}
