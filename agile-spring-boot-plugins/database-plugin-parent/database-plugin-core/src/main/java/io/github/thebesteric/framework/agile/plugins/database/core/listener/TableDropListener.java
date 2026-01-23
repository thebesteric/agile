package io.github.thebesteric.framework.agile.plugins.database.core.listener;

import io.github.thebesteric.framework.agile.plugins.database.core.jdbc.JdbcTemplateHelper;

/**
 * 表删除监听器
 *
 * @author wangweijun
 * @since 2026/1/17 17:00
 */
public interface TableDropListener {
    /**
     * 表删除之前
     *
     * @param tableName          表名
     * @param jdbcTemplateHelper jdbcTemplateHelper
     *
     * @return boolean 返回 false，则不删除表
     *
     * @author wangweijun
     * @since 2026/01/21 16:18
     */
    default boolean preDropTable(String tableName, JdbcTemplateHelper jdbcTemplateHelper) {
        return true;
    }

    /**
     * 表删除之后
     *
     * @param tableName          表名
     * @param jdbcTemplateHelper jdbcTemplateHelper
     *
     * @author wangweijun
     * @since 2026/01/21 16:18
     */
    default void postDropTable(String tableName, JdbcTemplateHelper jdbcTemplateHelper) {
    }
}
