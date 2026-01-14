package io.github.thebesteric.framework.agile.plugins.database.config;

import io.github.thebesteric.framework.agile.commons.util.LoggerPrinter;
import io.github.thebesteric.framework.agile.core.AgileConstants;
import io.github.thebesteric.framework.agile.core.config.AbstractAgileInitialization;
import io.github.thebesteric.framework.agile.plugins.database.jdbc.AgileDatabaseJdbcTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * AgileDatabaseAutoConfiguration
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-05-13 11:01:55
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(AgileDatabaseProperties.class)
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = AgileConstants.PROPERTIES_PREFIX + ".database", name = "enable", havingValue = "true", matchIfMissing = true)
public class AgileDatabaseAutoConfiguration extends AbstractAgileInitialization {

    private static final LoggerPrinter loggerPrinter = LoggerPrinter.newInstance();

    private final AgileDatabaseProperties properties;

    @Override
    public void start() {
        if (!properties.isEnable()) {
            loggerPrinter.info("Database-plugin has been Disabled");
            return;
        }
        loggerPrinter.info("Database-plugin is running");

        AgileDatabaseJdbcTemplate jdbcTemplate = getBean(AgileDatabaseJdbcTemplate.class);
        jdbcTemplate.createOrUpdateTable();
    }

    @Bean
    public AgileDatabaseContext agileDatabaseContext(ApplicationContext applicationContext) {
        return new AgileDatabaseContext(applicationContext, properties);
    }

    @Bean
    public AgileDatabaseJdbcTemplate agileDatabaseJdbcTemplate(AgileDatabaseContext context, @Nullable DataSource dataSource, @Nullable PlatformTransactionManager transactionManager) throws SQLException {
        return new AgileDatabaseJdbcTemplate(context, dataSource, transactionManager, properties);
    }

}
