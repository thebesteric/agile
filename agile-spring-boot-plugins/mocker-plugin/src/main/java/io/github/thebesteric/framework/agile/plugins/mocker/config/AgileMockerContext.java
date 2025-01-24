package io.github.thebesteric.framework.agile.plugins.mocker.config;

import io.github.thebesteric.framework.agile.core.config.AbstractAgileContext;
import io.github.thebesteric.framework.agile.core.matcher.clazz.ClassMatcher;
import io.github.thebesteric.framework.agile.core.matcher.method.MethodMatcher;
import io.github.thebesteric.framework.agile.plugins.mocker.matcher.MockOnMethodMatcher;
import io.github.thebesteric.framework.agile.plugins.mocker.matcher.MockerOnClassMatcher;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

/**
 * AgileMockerContext
 *
 * @author wangweijun
 * @version v1.0
 * @since 2025-01-23 17:30:47
 */
@Getter
@Slf4j
public class AgileMockerContext extends AbstractAgileContext {

    private final String[] activeProfiles;
    private final AgileMockerProperties properties;
    private final ClassMatcher classMatcher;
    private final MethodMatcher methodMatcher;


    public AgileMockerContext(ApplicationContext applicationContext, AgileMockerProperties properties) {
        super((GenericApplicationContext) applicationContext);
        this.activeProfiles = applicationContext.getEnvironment().getActiveProfiles();
        this.properties = properties;
        this.classMatcher = new MockerOnClassMatcher();
        this.methodMatcher = new MockOnMethodMatcher();
    }

}
