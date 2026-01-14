package io.github.thebesteric.framework.agile.plugins.mocker.config;

import io.github.thebesteric.framework.agile.commons.constant.AgilePlugins;
import io.github.thebesteric.framework.agile.commons.util.LoggerPrinter;
import io.github.thebesteric.framework.agile.core.AgileConstants;
import io.github.thebesteric.framework.agile.core.config.AbstractAgileInitialization;
import io.github.thebesteric.framework.agile.plugins.mocker.advisor.AgileMockerAdvice;
import io.github.thebesteric.framework.agile.plugins.mocker.advisor.AgileMockerPointcut;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AgileMockerAutoConfiguration
 *
 * @author wangweijun
 * @version v1.0
 * @since 2025-01-23 17:22:28
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(AgileMockerProperties.class)
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = AgileConstants.PROPERTIES_PREFIX + ".mocker", name = "enable", havingValue = "true", matchIfMissing = true)
public class AgileMockerAutoConfiguration extends AbstractAgileInitialization {

    private static final LoggerPrinter loggerPrinter = LoggerPrinter.newInstance();

    private final AgileMockerProperties properties;

    @Override
    public void start() {
        if (!properties.isEnable()) {
            loggerPrinter.info("{} has been Disabled", AgilePlugins.MOCKER_PLUGIN.getName());
            return;
        }
        loggerPrinter.info("{} is running", AgilePlugins.MOCKER_PLUGIN.getName());
    }

    @Bean
    public AgileMockerContext agileMockerContext(ApplicationContext applicationContext, AgileMockerProperties properties) {
        return new AgileMockerContext(applicationContext, properties);
    }

    @Bean
    public AgileMockerAdvice agileMockerAdvice(AgileMockerContext agileLoggerContext) {
        return new AgileMockerAdvice(agileLoggerContext);
    }

    @Bean
    public AgileMockerPointcut a(AgileMockerContext agileLoggerContext) {
        return new AgileMockerPointcut(agileLoggerContext);
    }

    @Bean
    public DefaultPointcutAdvisor agileMockerPointcutAdvisor(AgileMockerPointcut agileMockerPointcut, AgileMockerAdvice agileMockerAdvice) {
        // 创建通知器，将切点和拦截器组合
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor();
        advisor.setPointcut(agileMockerPointcut);
        advisor.setAdvice(agileMockerAdvice);
        return advisor;
    }

}
