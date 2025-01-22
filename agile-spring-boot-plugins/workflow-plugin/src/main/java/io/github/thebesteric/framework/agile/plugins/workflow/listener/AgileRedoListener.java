package io.github.thebesteric.framework.agile.plugins.workflow.listener;

import io.github.thebesteric.framework.agile.plugins.workflow.entity.TaskInstance;

/**
 * 审批撤回监听器
 *
 * @author wangweijun
 * @version v1.0
 * @since 2025-01-22 09:44:57
 */
public interface AgileRedoListener {

    /**
     * 撤回之前
     *
     * @param taskInstance 任务实例
     * @param roleId       角色 ID
     * @param userId       用户 ID
     * @param comment      撤回意见
     *
     * @return 返回 null 表示使用系统默认备注
     *
     * @author wangweijun
     * @since 2025/1/22 10:01
     */
    default String preRedo(TaskInstance taskInstance, String roleId, String userId, String comment) {
        return null;
    }

    /**
     * 撤回之后
     *
     * @param taskInstance 任务实例
     * @param roleId       角色 ID
     * @param userId       用户 ID
     * @param comment      撤回意见
     *
     * @author wangweijun
     * @since 2025/1/22 10:02
     */
    default void postRedid(TaskInstance taskInstance, String roleId, String userId, String comment) {
    }

}
