package io.github.thebesteric.framework.agile.plugins.database.core.listener;

import io.github.thebesteric.framework.agile.plugins.database.core.domain.ChangeFields;

/**
 * 表更新处理器
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-12-10 18:30:31
 */
public interface EntityClassUpdateListener {
    /**
     * 表更新之前
     *
     * @param changeFields changeFields
     *
     * @return 返回 null，则不进行任何更新
     *
     * @author wangweijun
     * @since 2024/12/10 18:33
     */
    ChangeFields preUpdateTable(ChangeFields changeFields);

    /**
     * 表更新之后
     *
     * @author wangweijun
     * @since 2024/12/10 18:33
     */
    void postUpdateTable();

}
