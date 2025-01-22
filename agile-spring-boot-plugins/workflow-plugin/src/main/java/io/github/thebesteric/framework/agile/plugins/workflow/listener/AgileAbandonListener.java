package io.github.thebesteric.framework.agile.plugins.workflow.listener;

import io.github.thebesteric.framework.agile.plugins.workflow.entity.TaskInstance;

/**
 * 审批放弃监听器
 *
 * @author wangweijun
 * @version v1.0
 * @since 2025-01-22 10:07:45
 */
public interface AgileAbandonListener {

    /**
     * 放弃之前
     *
     * @param taskInstance 任务实例
     * @param roleId       角色 ID
     * @param userId       用户 ID
     * @param comment      放弃意见
     *
     * @return 返回 null 表示使用系统默认备注
     *
     * @author wangweijun
     * @since 2025/1/22 10:01
     */
    default String preAbandon(TaskInstance taskInstance, String roleId, String userId, String comment) {
        return null;
    }

    /**
     * 放弃之后
     *
     * @param taskInstance 任务实例
     * @param roleId       角色 ID
     * @param userId       用户 ID
     * @param comment      放弃意见
     *
     * @author wangweijun
     * @since 2025/1/22 10:01
     */
    default void postAbandoned(TaskInstance taskInstance, String roleId, String userId, String comment) {
    }

}
