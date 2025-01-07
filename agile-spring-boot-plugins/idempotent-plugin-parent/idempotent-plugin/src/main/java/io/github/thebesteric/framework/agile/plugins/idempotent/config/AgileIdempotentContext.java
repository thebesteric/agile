package io.github.thebesteric.framework.agile.plugins.idempotent.config;

import io.github.thebesteric.framework.agile.core.config.AbstractAgileContext;
import io.github.thebesteric.framework.agile.core.matcher.clazz.ClassMatcher;
import io.github.thebesteric.framework.agile.core.matcher.clazz.impl.ControllerBeanClassMatcher;
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

    private final ThreadLocal<String> idempotentKeyThreadLocal = new InheritableThreadLocal<>();


    public AgileIdempotentContext(ApplicationContext applicationContext, AgileIdempotentProperties properties, List<ClassMatcher> classMatchers) {
        super((GenericApplicationContext) applicationContext);
        this.properties = properties;
        // 没有定义 ClassMatcher，则使用 ControllerBeanClassMatcher
        if (classMatchers == null || classMatchers.isEmpty()) {
            this.classMatchers = List.of(new ControllerBeanClassMatcher());
        }
        // 用户自定义 ClassMatcher，则使用用户定义的配置
        else {
            this.classMatchers = classMatchers;
        }
        this.idempotentProcessor = getBeanOrDefault(IdempotentProcessor.class, new InMemoryIdempotentProcessor());
    }

    public void setIdempotentKey(String idempotentKey) {
        idempotentKeyThreadLocal.set(idempotentKey);
    }

    public String getIdempotentKey() {
        return idempotentKeyThreadLocal.get();
    }

    public void removeIdempotentKey() {
        idempotentKeyThreadLocal.remove();
    }

}
