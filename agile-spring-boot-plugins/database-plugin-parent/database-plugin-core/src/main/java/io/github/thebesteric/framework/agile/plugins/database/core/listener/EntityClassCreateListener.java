package io.github.thebesteric.framework.agile.plugins.database.core.listener;

import io.github.thebesteric.framework.agile.plugins.database.core.domain.EntityClassDomain;

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
     * @param entityClassDomain entityClassDomain
     *
     * @return 返回 null，则不创建表
     *
     * @author wangweijun
     * @since 2024/12/10 18:33
     */
    EntityClassDomain preCreateTable(EntityClassDomain entityClassDomain);

    /**
     * 表创建之后
     *
     * @author wangweijun
     * @since 2024/12/10 18:33
     */
    void postCreateTable();

}
