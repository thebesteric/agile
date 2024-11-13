package io.github.thebesteric.framework.agile.plugins.logger.config;

import io.github.thebesteric.framework.agile.commons.util.LoggerPrinter;
import io.github.thebesteric.framework.agile.core.AgileConstants;
import io.github.thebesteric.framework.agile.core.config.AbstractAgileInitialization;
import io.github.thebesteric.framework.agile.core.scaner.ClassPathScanner;
import io.github.thebesteric.framework.agile.plugins.logger.advisor.AgileLoggerAdvice;
import io.github.thebesteric.framework.agile.plugins.logger.advisor.AgileLoggerPointcut;
import io.github.thebesteric.framework.agile.plugins.logger.filter.AgileLoggerFilter;
import io.github.thebesteric.framework.agile.plugins.logger.processor.ignore.RequestIgnoreProcessor;
import io.github.thebesteric.framework.agile.plugins.logger.processor.mapping.MappingProcessor;
import io.github.thebesteric.framework.agile.plugins.logger.processor.mapping.impl.*;
import io.github.thebesteric.framework.agile.plugins.logger.processor.recorder.Recorder;
import io.github.thebesteric.framework.agile.plugins.logger.processor.recorder.impl.LogRecorder;
import io.github.thebesteric.framework.agile.plugins.logger.processor.recorder.impl.StdoutRecorder;
import io.github.thebesteric.framework.agile.plugins.logger.processor.scaner.AgileLoggerControllerScanner;
import io.github.thebesteric.framework.agile.plugins.logger.recorder.LocalLogRecordController;
import io.github.thebesteric.framework.agile.plugins.logger.recorder.processor.LocalLogRecordPostProcessor;
import io.github.thebesteric.framework.agile.plugins.logger.recorder.processor.impl.DefaultLocalLogRecordPostProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * AgileLoggerInitialization
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-03-11 14:50:58
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(AgileLoggerProperties.class)
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = AgileConstants.PROPERTIES_PREFIX + ".logger", name = "enable", havingValue = "true", matchIfMissing = true)
public class AgileLoggerAutoConfiguration extends AbstractAgileInitialization {

    private final AgileLoggerProperties properties;

    @Override
    public void start() {
        if (!properties.isEnable()) {
            LoggerPrinter.info(log, "Logger-plugin has been Disabled");
            return;
        }
        LoggerPrinter.info(log, "Logger-plugin is running");
    }

    @Bean
    public FilterRegistrationBean<AgileLoggerFilter> agileLoggerFilterRegister(AgileLoggerContext agileLoggerContext) {
        FilterRegistrationBean<AgileLoggerFilter> frBean = new FilterRegistrationBean<>();
        frBean.setName(AgileLoggerFilter.class.getSimpleName());
        frBean.setFilter(new AgileLoggerFilter(agileLoggerContext));
        frBean.addUrlPatterns("/*");
        frBean.setOrder(1);
        return frBean;
    }

    @Bean
    public AgileLoggerContext agileLoggerContext(ApplicationContext applicationContext, AgileLoggerProperties properties, List<Recorder> recorders,
                                                 List<RequestIgnoreProcessor> requestIgnoreProcessors, LocalLogRecordPostProcessor localLogRecordPostProcessor) {
        return new AgileLoggerContext(applicationContext, properties, recorders, requestIgnoreProcessors, localLogRecordPostProcessor);
    }

    @Bean
    public AgileLoggerAdvice agileLoggerAdvice(AgileLoggerContext agileLoggerContext) {
        return new AgileLoggerAdvice(agileLoggerContext);
    }

    @Bean
    public AgileLoggerPointcut agileLoggerPointcut(AgileLoggerContext agileLoggerContext) {
        return new AgileLoggerPointcut(agileLoggerContext);
    }

    @Bean
    public DefaultPointcutAdvisor agileLoggerPointcutAdvisor(AgileLoggerPointcut agileLoggerPointcut, AgileLoggerAdvice agileLoggerAdvice) {
        // 创建通知器，将切点和拦截器组合
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor();
        advisor.setPointcut(agileLoggerPointcut);
        advisor.setAdvice(agileLoggerAdvice);
        return advisor;
    }

    @Bean
    public AgileLoggerControllerScanner agileLoggerControllerScanner(List<MappingProcessor> mappingProcessors) {
        return new AgileLoggerControllerScanner(mappingProcessors);
    }

    @Bean
    public AgileLoggerContextInitializer agileLoggerContextInitializer(List<ClassPathScanner> scanners) {
        return new AgileLoggerContextInitializer(scanners);
    }

    @Bean
    public LocalLogRecordController localLogRecordController() {
        return new LocalLogRecordController();
    }

    @Bean
    @ConditionalOnMissingBean
    public LocalLogRecordPostProcessor localLogRecordPostProcessor() {
        return new DefaultLocalLogRecordPostProcessor();
    }

    @Configuration
    @EnableConfigurationProperties(AgileLoggerProperties.class)
    public static class RecorderConfig {
        @Bean
        public Recorder logRecorder(AgileLoggerProperties properties) {
            return new LogRecorder(properties);
        }

        @Bean
        public Recorder stdoutRecorder(AgileLoggerProperties properties) {
            return new StdoutRecorder(properties);
        }
    }

    @Configuration
    public static class MappingProcessorConfig {
        @Bean
        public MappingProcessor deleteMappingProcessor() {
            return new DeleteMappingProcessor();
        }

        @Bean
        public MappingProcessor getMappingProcessor() {
            return new GetMappingProcessor();
        }

        @Bean
        public MappingProcessor patchMappingProcessor() {
            return new PatchMappingProcessor();
        }

        @Bean
        public MappingProcessor postMappingProcessor() {
            return new PostMappingProcessor();
        }

        @Bean
        public MappingProcessor putMappingProcessor() {
            return new PutMappingProcessor();
        }

        @Bean
        public MappingProcessor requestMappingProcessor() {
            return new RequestMappingProcessor();
        }
    }
}
