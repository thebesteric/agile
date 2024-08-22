package io.github.thebesteric.framework.agile.plugins.idempotent.config;

import io.github.thebesteric.framework.agile.core.config.AbstractAgileContext;
import io.github.thebesteric.framework.agile.core.matcher.clazz.ClassMatcher;
import io.github.thebesteric.framework.agile.core.matcher.clazz.impl.ComponentBeanClassMatcher;
import io.github.thebesteric.framework.agile.core.matcher.clazz.impl.ControllerBeanClassMatcher;
import io.github.thebesteric.framework.agile.core.matcher.clazz.impl.RepositoryBeanClassMatcher;
import io.github.thebesteric.framework.agile.core.matcher.clazz.impl.ServiceBeanClassMatcher;
import io.github.thebesteric.framework.agile.plugins.idempotent.processor.IdempotentProcessor;
import io.github.thebesteric.framework.agile.plugins.idempotent.processor.impl.InMemoryIdempotentProcessor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import java.util.List;

/**
 * AgileIdempotentContext
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-04-30 15:31:01
 */
@Getter
@Slf4j
public class AgileIdempotentContext extends AbstractAgileContext {

    private final AgileIdempotentProperties properties;
    private final List<ClassMatcher> classMatchers;
    private final IdempotentProcessor idempotentProcessor;


    public AgileIdempotentContext(ApplicationContext applicationContext, AgileIdempotentProperties properties) {
        super((GenericApplicationContext) applicationContext);
        this.properties = properties;
        this.classMatchers = List.of(new ControllerBeanClassMatcher(), new ComponentBeanClassMatcher(), new ServiceBeanClassMatcher(), new RepositoryBeanClassMatcher());
        this.idempotentProcessor = getBeanOrDefault(IdempotentProcessor.class, new InMemoryIdempotentProcessor());
    }

}
