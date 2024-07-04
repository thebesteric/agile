package io.github.thebesteric.framework.agile.plugins.workflow.config;

import io.github.thebesteric.framework.agile.commons.util.LoggerPrinter;
import io.github.thebesteric.framework.agile.core.AgileConstants;
import io.github.thebesteric.framework.agile.core.config.AbstractAgileInitialization;
import io.github.thebesteric.framework.agile.plugins.database.core.jdbc.JdbcTemplateHelper;
import io.github.thebesteric.framework.agile.plugins.workflow.WorkflowEngine;
import io.github.thebesteric.framework.agile.plugins.workflow.filter.AgileWorkflowFilter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * AgileWorkflowAutoConfiguration
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-11 20:59:43
 */
@Slf4j
@Configuration
@EnableTransactionManagement
@EnableConfigurationProperties(AgileWorkflowProperties.class)
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = AgileConstants.PROPERTIES_PREFIX + ".workflow", name = "enable", havingValue = "true", matchIfMissing = true)
public class AgileWorkflowAutoConfiguration extends AbstractAgileInitialization {

    private final AgileWorkflowProperties properties;

    @Override
    public void start() {
        if (!properties.isEnable()) {
            LoggerPrinter.info(log, "Workflow-plugin has been Disabled");
            return;
        }
        LoggerPrinter.info(log, "Workflow-plugin is running");
        // 初始化
        init();
    }

    @SneakyThrows
    private void init() {
        WorkflowEngine workflowEngine = getBean(WorkflowEngine.class);
        workflowEngine.createOrUpdateTable();
    }

    @Bean
    public FilterRegistrationBean<AgileWorkflowFilter> agileWorkflowFilterRegister() {
        FilterRegistrationBean<AgileWorkflowFilter> frBean = new FilterRegistrationBean<>();
        frBean.setName(AgileWorkflowFilter.class.getSimpleName());
        frBean.setFilter(new AgileWorkflowFilter());
        frBean.addUrlPatterns("/*");
        frBean.setOrder(1);
        return frBean;
    }

    @Bean
    public AgileWorkflowContext agileWorkflowContext(ApplicationContext applicationContext, AgileWorkflowProperties properties, JdbcTemplateHelper jdbcTemplateHelper) {
        return new AgileWorkflowContext(applicationContext, properties, jdbcTemplateHelper);
    }

    @Bean
    @ConditionalOnMissingBean
    public JdbcTemplateHelper jdbcTemplateHelper(@Nullable DataSource dataSource, @Nullable PlatformTransactionManager transactionManager) throws SQLException {
        if (dataSource == null) {
            throw new IllegalArgumentException("DataSource must not be null");
        }
        return new JdbcTemplateHelper(dataSource, transactionManager);
    }

    @Bean
    public WorkflowEngine agileWorkflowEngine(AgileWorkflowContext context) {
        return new WorkflowEngine(context);
    }

}
