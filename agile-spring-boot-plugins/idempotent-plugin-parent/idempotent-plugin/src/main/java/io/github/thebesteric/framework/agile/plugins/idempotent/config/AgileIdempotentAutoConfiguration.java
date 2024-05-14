package io.github.thebesteric.framework.agile.plugins.idempotent.config;

import io.github.thebesteric.framework.agile.commons.util.LoggerPrinter;
import io.github.thebesteric.framework.agile.core.AgileConstants;
import io.github.thebesteric.framework.agile.core.config.AbstractAgileInitialization;
import io.github.thebesteric.framework.agile.core.matcher.clazz.ClassMatcher;
import io.github.thebesteric.framework.agile.plugins.idempotent.advisor.AgileIdempotentAdvice;
import io.github.thebesteric.framework.agile.plugins.idempotent.advisor.AgileIdempotentPointcut;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * AgileIdempotentAutoConfiguration
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-04-30 15:24:05
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(AgileIdempotentProperties.class)
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = AgileConstants.PROPERTIES_PREFIX + ".idempotent", name = "enable", havingValue = "true", matchIfMissing = true)
public class AgileIdempotentAutoConfiguration extends AbstractAgileInitialization {

    private final AgileIdempotentProperties properties;

    @Override
    public void start() {
        if (!properties.isEnable()) {
            LoggerPrinter.info(log, "Idempotent-plugin has been Disabled");
            return;
        }
        LoggerPrinter.info(log, "Idempotent-plugin is running");
    }

    @Bean
    public AgileIdempotentContext agileIdempotentContext(ApplicationContext applicationContext, AgileIdempotentProperties properties,
                                                         List<ClassMatcher> classMatchers) {
        return new AgileIdempotentContext(applicationContext, properties, classMatchers);
    }

    @Bean
    public AgileIdempotentAdvice agileIdempotentAdvice(AgileIdempotentContext context) {
        return new AgileIdempotentAdvice(context);
    }

    @Bean
    public AgileIdempotentPointcut agileIdempotentPointcut(AgileIdempotentContext context) {
        return new AgileIdempotentPointcut(context);
    }

    @Bean
    public DefaultPointcutAdvisor agileIdempotentAdvisor(AgileIdempotentPointcut agileIdempotentPointcut, AgileIdempotentAdvice agileIdempotentAdvice) {
        // 创建通知器，将切点和拦截器组合
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor();
        advisor.setPointcut(agileIdempotentPointcut);
        advisor.setAdvice(agileIdempotentAdvice);
        return advisor;
    }


}
