package io.github.thebesteric.framework.agile.distributed.locks.config;

import io.github.thebesteric.framework.agile.core.config.AbstractAgileContext;
import io.github.thebesteric.framework.agile.core.matcher.clazz.ClassMatcher;
import io.github.thebesteric.framework.agile.core.matcher.clazz.impl.ComponentBeanClassMatcher;
import io.github.thebesteric.framework.agile.core.matcher.clazz.impl.ControllerBeanClassMatcher;
import io.github.thebesteric.framework.agile.core.matcher.clazz.impl.RepositoryBeanClassMatcher;
import io.github.thebesteric.framework.agile.core.matcher.clazz.impl.ServiceBeanClassMatcher;
import io.github.thebesteric.framework.agile.distributed.locks.processor.DistributedLocksProcessor;
import io.github.thebesteric.framework.agile.distributed.locks.processor.impl.SingleInstanceLocksProcessor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import java.util.List;

/**
 * AgileDistributedLocksContext
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-22 16:11:30
 */
@Getter
@Slf4j
public class AgileDistributedLocksContext extends AbstractAgileContext {

    private final AgileDistributedLocksProperties properties;
    private final List<ClassMatcher> classMatchers;
    private final DistributedLocksProcessor distributedLocksProcessor;


    public AgileDistributedLocksContext(ApplicationContext applicationContext, AgileDistributedLocksProperties properties) {
        super((GenericApplicationContext) applicationContext);
        this.properties = properties;
        this.classMatchers = List.of(new ControllerBeanClassMatcher(), new ComponentBeanClassMatcher(), new ServiceBeanClassMatcher(), new RepositoryBeanClassMatcher());
        this.distributedLocksProcessor = getBeanOrDefault(DistributedLocksProcessor.class, new SingleInstanceLocksProcessor());
    }
}
