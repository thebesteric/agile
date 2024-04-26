package io.github.thebesteric.framework.agile.plugins.logger.config;

import io.github.thebesteric.framework.agile.commons.util.LoggerPrinter;
import io.github.thebesteric.framework.agile.core.config.AbstractAgileInitialization;
import io.github.thebesteric.framework.agile.core.matcher.clazz.ClassMatcher;
import io.github.thebesteric.framework.agile.core.matcher.clazz.impl.ComponentBeanClassMatcher;
import io.github.thebesteric.framework.agile.core.matcher.clazz.impl.ControllerBeanClassMatcher;
import io.github.thebesteric.framework.agile.core.matcher.clazz.impl.RepositoryBeanClassMatcher;
import io.github.thebesteric.framework.agile.core.matcher.clazz.impl.ServiceBeanClassMatcher;
import io.github.thebesteric.framework.agile.core.matcher.method.MethodMatcher;
import io.github.thebesteric.framework.agile.core.scaner.ClassPathScanner;
import io.github.thebesteric.framework.agile.plugins.logger.advisor.AgileLoggerAdvice;
import io.github.thebesteric.framework.agile.plugins.logger.advisor.AgileLoggerPointcut;
import io.github.thebesteric.framework.agile.plugins.logger.filter.AgileLoggerFilter;
import io.github.thebesteric.framework.agile.plugins.logger.processor.ignore.RequestIgnoreProcessor;
import io.github.thebesteric.framework.agile.plugins.logger.processor.mapping.MappingProcessor;
import io.github.thebesteric.framework.agile.plugins.logger.processor.mapping.impl.*;
import io.github.thebesteric.framework.agile.plugins.logger.processor.matcher.AgileLoggerOnClassMatcher;
import io.github.thebesteric.framework.agile.plugins.logger.processor.matcher.AgileLoggerOnMethodMatcher;
import io.github.thebesteric.framework.agile.plugins.logger.processor.recorder.Recorder;
import io.github.thebesteric.framework.agile.plugins.logger.processor.recorder.impl.LogRecorder;
import io.github.thebesteric.framework.agile.plugins.logger.processor.recorder.impl.StdoutRecorder;
import io.github.thebesteric.framework.agile.plugins.logger.processor.scaner.AgileLoggerControllerScanner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.DefaultPointcutAdvisor;
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
    @SuppressWarnings({"unchecked", "rawtypes"})
    public FilterRegistrationBean filterRegister(AgileLoggerContext agileLoggerContext) {
        FilterRegistrationBean frBean = new FilterRegistrationBean();
        frBean.setName(AgileLoggerFilter.class.getSimpleName());
        frBean.setFilter(new AgileLoggerFilter(agileLoggerContext));
        frBean.addUrlPatterns("/*");
        frBean.setOrder(1);
        return frBean;
    }

    @Bean
    public AgileLoggerContext agileLoggerContext(ApplicationContext applicationContext, AgileLoggerProperties properties, List<Recorder> recorders,
                                                 List<ClassMatcher> classMatchers, List<MethodMatcher> methodMatchers, List<RequestIgnoreProcessor> requestIgnoreProcessors) {
        return new AgileLoggerContext(applicationContext, properties, recorders, classMatchers, methodMatchers, requestIgnoreProcessors);
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
    public DefaultPointcutAdvisor defaultPointcutAdvisor(AgileLoggerPointcut agileLoggerPointcut, AgileLoggerAdvice agileLoggerAdvice) {
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
    public static class MatcherConfig {
        @Bean
        public ClassMatcher controllerBeanClassMatcher() {
            return new ControllerBeanClassMatcher();
        }

        @Bean
        public ClassMatcher serviceBeanClassMatcher() {
            return new ServiceBeanClassMatcher();
        }

        @Bean
        public ClassMatcher repositoryBeanClassMatcher() {
            return new RepositoryBeanClassMatcher();
        }

        @Bean
        public ClassMatcher componentBeanClassMatcher() {
            return new ComponentBeanClassMatcher();
        }

        @Bean
        public MethodMatcher agileLoggerOnMethodMatcher() {
            return new AgileLoggerOnMethodMatcher();
        }

        @Bean
        public MethodMatcher agileLoggerOnClassMatcher() {
            return new AgileLoggerOnClassMatcher();
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
