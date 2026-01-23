package io.github.thebesteric.framework.agile.plugins.database.core.listener;

import io.github.thebesteric.framework.agile.plugins.database.core.domain.ChangeFields;
import io.github.thebesteric.framework.agile.plugins.database.core.jdbc.JdbcTemplateHelper;

/**
 * 表更新处理器
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-12-10 20:08:31
 */
public interface EntityClassUpdateListener {
    /**
     * 表更新之前
     *
     * @param changeFields       changeFields
     * @param jdbcTemplateHelper jdbcTemplateHelper
     *
     * @return 返回 null，则不进行任何更新
     *
     * @author wangweijun
     * @since 2024/12/10 18:33
     */
    default ChangeFields preUpdateTable(ChangeFields changeFields, JdbcTemplateHelper jdbcTemplateHelper) {
        return changeFields;
    }

    /**
     * 表更新之后
     *
     * @param jdbcTemplateHelper jdbcTemplateHelper
     *
     * @author wangweijun
     * @since 2024/12/10 18:33
     */
    default void postUpdateTable(JdbcTemplateHelper jdbcTemplateHelper) {
    }

}
