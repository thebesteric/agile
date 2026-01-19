package io.github.thebesteric.framework.agile.distributed.locks.config;

import io.github.thebesteric.framework.agile.commons.util.LoggerPrinter;
import io.github.thebesteric.framework.agile.core.AgileConstants;
import io.github.thebesteric.framework.agile.core.config.AbstractAgileInitialization;
import io.github.thebesteric.framework.agile.distributed.locks.advisor.AgileDistributedLocksAdvice;
import io.github.thebesteric.framework.agile.distributed.locks.advisor.AgileDistributedLocksPointcut;
import lombok.RequiredArgsConstructor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AgileDistributedLocksAutoConfiguration
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-22 16:13:47
 */
@Configuration
@EnableConfigurationProperties(AgileDistributedLocksProperties.class)
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = AgileConstants.PROPERTIES_PREFIX + ".distribute-locks", name = "enable", havingValue = "true", matchIfMissing = true)
public class AgileDistributedLocksAutoConfiguration extends AbstractAgileInitialization {

    private static final LoggerPrinter loggerPrinter = LoggerPrinter.newInstance();

    private final AgileDistributedLocksProperties properties;

    @Override
    public void start() {
        if (!properties.isEnable()) {
            loggerPrinter.info("Distribute-locks-plugin has been Disabled");
            return;
        }
        loggerPrinter.info("Distribute-locks-plugin is running");
    }

    @Bean
    public AgileDistributedLocksContext agileDistributedLocksContext(ApplicationContext applicationContext, AgileDistributedLocksProperties properties) {
        return new AgileDistributedLocksContext(applicationContext, properties);
    }

    @Bean
    public AgileDistributedLocksAdvice agileDistributedLocksAdvice(AgileDistributedLocksContext context) {
        return new AgileDistributedLocksAdvice(context);
    }

    @Bean
    public AgileDistributedLocksPointcut agileDistributedLocksPointcut(AgileDistributedLocksContext context) {
        return new AgileDistributedLocksPointcut(context);
    }

    @Bean
    public DefaultPointcutAdvisor agileDistributedLocksAdvisor(AgileDistributedLocksPointcut agileDistributedLocksPointcut, AgileDistributedLocksAdvice agileDistributedLocksAdvice) {
        // 创建通知器，将切点和拦截器组合
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor();
        advisor.setPointcut(agileDistributedLocksPointcut);
        advisor.setAdvice(agileDistributedLocksAdvice);
        return advisor;
    }
}
