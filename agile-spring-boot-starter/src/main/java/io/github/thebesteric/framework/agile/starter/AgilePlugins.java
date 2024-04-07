package io.github.thebesteric.framework.agile.starter;

/**
 * AgilePlugins
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-03-11 16:32:01
 */
public enum AgilePlugins {

    LOGGER_PLUGIN("logger-plugin", "io.github.thebesteric.framework.agile.plugins.logger.config.AgileLoggerAutoConfiguration");

    final String name;
    final String className;

    AgilePlugins(String name, String className) {
        this.name = name;
        this.className = className;
    }
}
