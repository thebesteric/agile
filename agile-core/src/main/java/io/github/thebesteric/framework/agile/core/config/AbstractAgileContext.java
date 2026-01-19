package io.github.thebesteric.framework.agile.core.config;

import io.github.thebesteric.framework.agile.commons.util.LoggerPrinter;
import lombok.Getter;
import org.springframework.context.support.GenericApplicationContext;

import java.util.List;

/**
 * AbstractAgileContext
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-05-04 17:33:32
 */
@Getter
public abstract class AbstractAgileContext {


    private static final LoggerPrinter loggerPrinter = LoggerPrinter.newInstance();

    protected final GenericApplicationContext applicationContext;

    protected AbstractAgileContext(GenericApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Get bean, using the default value if null
     *
     * @param beanType     Class<T>
     * @param defaultValue defaultValue
     *
     * @return T
     */
    public <T> T getBeanOrDefault(Class<T> beanType, T defaultValue) {
        T obj = null;
        try {
            obj = this.applicationContext.getBean(beanType);
        } catch (Exception e) {
            if (defaultValue != null) {
                obj = defaultValue;
            } else {
                loggerPrinter.warn(e.getMessage());
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
                loggerPrinter.debug(e.getMessage());
            }
        }
        return obj;
    }

    /**
     * Get beans for type
     *
     * @param beanType beanType
     *
     * @return List<T>
     *
     * @author wangweijun
     * @since 2024/12/16 09:37
     */
    public <T> List<T> getBeans(Class<T> beanType) {
        return this.applicationContext.getBeansOfType(beanType).values().stream().toList();
    }

}
