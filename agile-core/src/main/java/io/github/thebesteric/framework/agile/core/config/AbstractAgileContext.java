package io.github.thebesteric.framework.agile.core.config;

import io.github.thebesteric.framework.agile.commons.util.LoggerPrinter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.GenericApplicationContext;

/**
 * AbstractAgileContext
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-05-04 17:33:32
 */
@Slf4j
@Getter
public abstract class AbstractAgileContext {

    protected final GenericApplicationContext applicationContext;

    protected AbstractAgileContext(GenericApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public <T> T getBeanOrDefault(Class<T> beanType, T defaultValue) {
        T obj = null;
        try {
            obj = this.applicationContext.getBean(beanType);
        } catch (Exception e) {
            if (defaultValue != null) {
                obj = defaultValue;
            } else {
                LoggerPrinter.warn(log, e.getMessage());
            }
        }
        return obj;
    }

    /**
     * Get bean, using the default value if null
     *
     * @param beanName     beanName
     * @param beanType     Class<T>
     * @param defaultValue defaultValue
     *
     * @return T
     */
    public <T> T getBeanOrDefault(String beanName, Class<T> beanType, T defaultValue) {
        T obj = null;
        try {
            obj = this.applicationContext.getBean(beanName, beanType);
        } catch (Exception e) {
            if (defaultValue != null) {
                obj = defaultValue;
            } else {
                LoggerPrinter.debug(log, e.getMessage());
            }
        }
        return obj;
    }

}
