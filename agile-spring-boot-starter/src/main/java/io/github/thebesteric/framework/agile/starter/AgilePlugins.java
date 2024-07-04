package io.github.thebesteric.framework.agile.starter;

/**
 * AgilePlugins
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-03-11 16:32:01
 */
public enum AgilePlugins {

    LOGGER_PLUGIN("logger-plugin", "io.github.thebesteric.framework.agile.plugins.logger.config.AgileLoggerAutoConfiguration"),
    IDEMPOTENT_PLUGIN("idempotent-plugin", "io.github.thebesteric.framework.agile.plugins.idempotent.config.AgileIdempotentAutoConfiguration"),
    LIMITER_PLUGIN("limiter-plugin", "io.github.thebesteric.framework.agile.plugins.limiter.config.AgileRateLimiterAutoConfiguration"),
    DATABASE_PLUGIN("database-plugin", "io.github.thebesteric.framework.agile.plugins.database.config.AgileDatabaseAutoConfiguration"),
    WORKFLOW_PLUGIN("workflow-plugin", "io.github.thebesteric.framework.agile.plugins.workflow.config.AgileWorkflowAutoConfiguration");

    final String name;
    final String className;

    AgilePlugins(String name, String className) {
        this.name = name;
        this.className = className;
    }
}
