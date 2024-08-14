package io.github.thebesteric.framework.agile.plugins.annotation.scanner.config;

import io.github.thebesteric.framework.agile.commons.util.LoggerPrinter;
import io.github.thebesteric.framework.agile.core.AgileConstants;
import io.github.thebesteric.framework.agile.core.config.AbstractAgileInitialization;
import io.github.thebesteric.framework.agile.plugins.annotation.scanner.AnnotationParasiticContext;
import io.github.thebesteric.framework.agile.plugins.annotation.scanner.AnnotationRegister;
import io.github.thebesteric.framework.agile.plugins.annotation.scanner.listener.AnnotationParasiticRegisteredListener;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * AnnotationScannerAutoConfiguration
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-13 13:59:31
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(AnnotationScannerProperties.class)
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = AgileConstants.PROPERTIES_PREFIX + ".annotation-scanner", name = "enable", havingValue = "true", matchIfMissing = true)
public class AnnotationScannerAutoConfiguration extends AbstractAgileInitialization {

    private final AnnotationScannerProperties properties;

    @Override
    public void start() {
        if (!properties.isEnable()) {
            LoggerPrinter.info(log, "Annotation-scanner-plugin has been Disabled");
            return;
        }
        LoggerPrinter.info(log, "Annotation-scanner-plugin is running");
    }

    @Bean
    public AnnotationParasiticContext annotationParasiticContext(AnnotationRegister annotationRegister, @Nullable AnnotationParasiticRegisteredListener listener) {
        return new AnnotationParasiticContext(annotationRegister, listener);
    }


    @Bean
    @ConditionalOnMissingBean
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public AnnotationRegister annotationRegister() {
        AnnotationRegister annotationRegister = new AnnotationRegister();
        List<String> annotationClassNames = properties.getAnnotationClassNames();
        for (String annotationClassName : annotationClassNames) {
            Class<?> annotationClass = Class.forName(annotationClassName);
            annotationRegister.register((Class<? extends Annotation>) annotationClass, parasitic -> true);
        }
        return annotationRegister;
    }
}
