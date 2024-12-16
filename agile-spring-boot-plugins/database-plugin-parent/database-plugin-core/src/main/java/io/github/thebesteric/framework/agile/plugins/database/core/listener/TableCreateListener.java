package io.github.thebesteric.framework.agile.plugins.database.core.listener;

import io.github.thebesteric.framework.agile.plugins.database.core.jdbc.JdbcTemplateHelper;

public interface TableCreateListener {

    /**
     * 表创建之前
     *
     * @param tableName          表名
     * @param jdbcTemplateHelper jdbcTemplateHelper
     *
     * @return boolean 返回 false，则不创建表
     *
     * @author wangweijun
     * @since 2024/12/16 09:41
     */
    default boolean preCreateTable(String tableName, JdbcTemplateHelper jdbcTemplateHelper) {
        return true;
    }

    /**
     * 表创建之后
     *
     * @param tableName          表名
     * @param jdbcTemplateHelper jdbcTemplateHelper
     *
     * @author wangweijun
     * @since 2024/12/16 09:25
     */
    default void postCreateTable(String tableName, JdbcTemplateHelper jdbcTemplateHelper) {
    }

}
