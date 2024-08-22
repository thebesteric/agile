package io.github.thebesteric.framework.agile.plugins.limiter.config;

import io.github.thebesteric.framework.agile.commons.util.LoggerPrinter;
import io.github.thebesteric.framework.agile.core.AgileConstants;
import io.github.thebesteric.framework.agile.core.config.AbstractAgileInitialization;
import io.github.thebesteric.framework.agile.core.matcher.clazz.ClassMatcher;
import io.github.thebesteric.framework.agile.plugins.limiter.advisor.AgileRateLimiterAdvice;
import io.github.thebesteric.framework.agile.plugins.limiter.advisor.AgileRateLimiterPointcut;
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
 * AgileRateLimiterAutoConfiguration
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-05-06 17:21:50
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(AgileRateLimiterProperties.class)
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = AgileConstants.PROPERTIES_PREFIX + ".limiter", name = "enable", havingValue = "true", matchIfMissing = true)
public class AgileRateLimiterAutoConfiguration extends AbstractAgileInitialization {

    private final AgileRateLimiterProperties properties;

    @Override
    public void start() {
        if (!properties.isEnable()) {
            LoggerPrinter.info(log, "Limiter-plugin has been Disabled");
            return;
        }
        LoggerPrinter.info(log, "Limiter-plugin is running");
    }

    @Bean
    public AgileRateLimiterContext agileRateLimiterContext(ApplicationContext applicationContext, AgileRateLimiterProperties properties) {
        return new AgileRateLimiterContext(applicationContext, properties);
    }

    @Bean
    public AgileRateLimiterAdvice agileRateLimiterAdvice(AgileRateLimiterContext context) {
        return new AgileRateLimiterAdvice(context);
    }

    @Bean
    public AgileRateLimiterPointcut agileRateLimiterPointcut(AgileRateLimiterContext context) {
        return new AgileRateLimiterPointcut(context);
    }

    @Bean
    public DefaultPointcutAdvisor agileRateLimiterAdvisor(AgileRateLimiterPointcut agileRateLimiterPointcut, AgileRateLimiterAdvice agileRateLimiterAdvice) {
        // 创建通知器，将切点和拦截器组合
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor();
        advisor.setPointcut(agileRateLimiterPointcut);
        advisor.setAdvice(agileRateLimiterAdvice);
        return advisor;
    }
}
