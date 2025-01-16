package io.github.thebesteric.framework.agile.plugins.workflow.config;

import io.github.thebesteric.framework.agile.core.config.AbstractAgileContext;
import io.github.thebesteric.framework.agile.plugins.database.core.jdbc.JdbcTemplateHelper;
import io.github.thebesteric.framework.agile.plugins.workflow.processor.AgileAutoApproveProcessor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

/**
 * AgileLoggerContext
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-14 15:28:09
 */
@Getter
@Slf4j
public class AgileWorkflowContext extends AbstractAgileContext {

    private final AgileAutoApproveProcessor agileAutoApproveProcessor;
    private final AgileWorkflowProperties properties;
    private final JdbcTemplateHelper jdbcTemplateHelper;

    private static final ThreadLocal<String> CURRENT_USER = new ThreadLocal<>();

    public AgileWorkflowContext(ApplicationContext applicationContext, AgileAutoApproveProcessor agileAutoApproveProcessor, AgileWorkflowProperties properties, JdbcTemplateHelper jdbcTemplateHelper) {
        super((GenericApplicationContext) applicationContext);
        this.agileAutoApproveProcessor = agileAutoApproveProcessor;
        this.properties = properties;
        this.jdbcTemplateHelper = jdbcTemplateHelper;
    }

    /**
     * 设置当前操作用户
     *
     * @param user 当前操作用户
     *
     * @author wangweijun
     * @since 2024/6/21 14:59
     */
    public static void setCurrentUser(String user) {
        CURRENT_USER.set(user);
    }

    /**
     * 返回当前操作用户
     *
     * @return String
     *
     * @author wangweijun
     * @since 2024/6/21 15:50
     */
    public static String getCurrentUser() {
        return CURRENT_USER.get();
    }

    /**
     * 移除当前操作用户
     *
     * @author wangweijun
     * @since 2024/6/21 14:59
     */
    public static void removeCurrentUser() {
        CURRENT_USER.remove();
    }
}
