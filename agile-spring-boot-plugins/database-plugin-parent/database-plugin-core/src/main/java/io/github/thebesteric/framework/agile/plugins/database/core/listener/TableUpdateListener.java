package io.github.thebesteric.framework.agile.plugins.database.core.listener;

import io.github.thebesteric.framework.agile.plugins.database.core.jdbc.JdbcTemplateHelper;

public interface TableUpdateListener {

    /**
     * 表更新之前
     *
     * @param tableName          表名
     * @param jdbcTemplateHelper jdbcTemplateHelper
     *
     * @return boolean 返回 false，则不更新表
     *
     * @author wangweijun
     * @since 2024/12/16 09:24
     */
    default boolean preUpdateTable(String tableName, JdbcTemplateHelper jdbcTemplateHelper) {
        return true;
    }

    /**
     * 表更新之后
     *
     * @param tableName          表名
     * @param jdbcTemplateHelper jdbcTemplateHelper
     *
     * @author wangweijun
     * @since 2024/12/16 09:24
     */
    default void postUpdateTable(String tableName, JdbcTemplateHelper jdbcTemplateHelper) {
    }

}
