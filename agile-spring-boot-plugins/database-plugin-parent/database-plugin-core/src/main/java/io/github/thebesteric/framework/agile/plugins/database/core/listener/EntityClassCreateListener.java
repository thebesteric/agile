package io.github.thebesteric.framework.agile.plugins.database.core.listener;

import io.github.thebesteric.framework.agile.plugins.database.core.domain.EntityClassDomain;
import io.github.thebesteric.framework.agile.plugins.database.core.jdbc.JdbcTemplateHelper;

/**
 * 表创建处理器
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-12-10 18:30:31
 */
public interface EntityClassCreateListener {

    /**
     * 表创建之前
     *
     * @param entityClassDomain  entityClassDomain
     * @param jdbcTemplateHelper jdbcTemplateHelper
     *
     * @return 返回 null，则不创建表
     *
     * @author wangweijun
     * @since 2024/12/10 18:33
     */
    EntityClassDomain preCreateTable(EntityClassDomain entityClassDomain, JdbcTemplateHelper jdbcTemplateHelper);

    /**
     * 表创建之后
     *
     * @param jdbcTemplateHelper jdbcTemplateHelper
     *
     * @author wangweijun
     * @since 2024/12/10 18:33
     */
    void postCreateTable(JdbcTemplateHelper jdbcTemplateHelper);

}
