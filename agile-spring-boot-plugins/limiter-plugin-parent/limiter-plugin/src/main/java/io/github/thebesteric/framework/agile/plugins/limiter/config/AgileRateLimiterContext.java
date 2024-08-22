package io.github.thebesteric.framework.agile.plugins.limiter.config;

import io.github.thebesteric.framework.agile.core.config.AbstractAgileContext;
import io.github.thebesteric.framework.agile.core.matcher.clazz.ClassMatcher;
import io.github.thebesteric.framework.agile.core.matcher.clazz.impl.ComponentBeanClassMatcher;
import io.github.thebesteric.framework.agile.core.matcher.clazz.impl.ControllerBeanClassMatcher;
import io.github.thebesteric.framework.agile.core.matcher.clazz.impl.RepositoryBeanClassMatcher;
import io.github.thebesteric.framework.agile.core.matcher.clazz.impl.ServiceBeanClassMatcher;
import io.github.thebesteric.framework.agile.plugins.limiter.processor.RateLimiterProcessor;
import io.github.thebesteric.framework.agile.plugins.limiter.processor.impl.InMemoryRateLimiterProcessor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import java.util.List;

/**
 * AgileRateLimiterContext
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-05-06 17:16:01
 */
@Getter
@Slf4j
public class AgileRateLimiterContext extends AbstractAgileContext {

    private final AgileRateLimiterProperties properties;
    private final List<ClassMatcher> classMatchers;
    private final RateLimiterProcessor rateLimiterProcessor;


    public AgileRateLimiterContext(ApplicationContext applicationContext, AgileRateLimiterProperties properties) {
        super((GenericApplicationContext) applicationContext);
        this.properties = properties;
        this.classMatchers = List.of(new ControllerBeanClassMatcher(), new ComponentBeanClassMatcher(), new ServiceBeanClassMatcher(), new RepositoryBeanClassMatcher());
        this.rateLimiterProcessor = getBeanOrDefault(RateLimiterProcessor.class, new InMemoryRateLimiterProcessor());
    }

}
