package io.github.thebesteric.framework.agile.plugins.workflow.service;

import io.github.thebesteric.framework.agile.plugins.workflow.config.AgileWorkflowContext;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * AbstractDeploymentService
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-17 13:55:43
 */
public abstract class AbstractRepositoryService implements RepositoryService {

    protected AgileWorkflowContext context;

    protected AbstractRepositoryService(AgileWorkflowContext context) {
        this.context = context;
    }

    /**
     * 获取 JdbcTemplate
     *
     * @return JdbcTemplate
     *
     * @author wangweijun
     * @since 2024/6/18 10:06
     */
    public JdbcTemplate getJdbcTemplate() {
        return context.getJdbcTemplateHelper().getJdbcTemplate();
    }

}
