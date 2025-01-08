package io.github.thebesteric.framework.agile.plugins.sensitive.filter.config;

import io.github.thebesteric.framework.agile.commons.util.LoggerPrinter;
import io.github.thebesteric.framework.agile.core.AgileConstants;
import io.github.thebesteric.framework.agile.core.config.AbstractAgileInitialization;
import io.github.thebesteric.framework.agile.plugins.sensitive.filter.AgileSensitiveFilter;
import io.github.thebesteric.framework.agile.plugins.sensitive.filter.processor.AgileSensitiveResultProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

/**
 * AgileSensitiveFilterAutoConfiguration
 *
 * @author wangweijun
 * @version v1.0
 * @since 2025-01-08 13:49:05
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(AgileSensitiveFilterProperties.class)
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = AgileConstants.PROPERTIES_PREFIX + ".sensitive", name = "enable", havingValue = "true", matchIfMissing = true)
public class AgileSensitiveFilterAutoConfiguration  extends AbstractAgileInitialization  {

    private final AgileSensitiveFilterProperties properties;

    @Override
    public void start() {
        if (!properties.isEnable()) {
            LoggerPrinter.info(log, "Sensitive-filter-plugin has been Disabled");
            return;
        }
        LoggerPrinter.info(log, "Sensitive-filter-plugin is running");
    }

    @Bean
    public AgileSensitiveFilter agileSensitiveFilter(@Nullable AgileSensitiveResultProcessor sensitiveResultProcessor) {
        return new AgileSensitiveFilter(properties, sensitiveResultProcessor);
    }

}
