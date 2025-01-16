package io.github.thebesteric.framework.agile.plugins.workflow.processor;

import io.github.thebesteric.framework.agile.plugins.workflow.constant.ContinuousApproveMode;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.TaskInstance;

/**
 * 自动审批监听器
 *
 * @author wangweijun
 * @version v1.0
 * @since 2025-01-16 17:41:21
 */
public interface AgileAutoApproveProcessor {

    /**
     * 自动审核之前
     * 返回 null 表示使用系统默认备注
     *
     * @param approveMode  自动审批模式
     * @param taskInstance 任务实例
     * @param roleId       角色 ID
     * @param userId       用户 ID
     *
     * @return String 备注
     *
     * @author wangweijun
     * @since 2025/1/16 17:46
     */
    default String preAutoApprove(ContinuousApproveMode approveMode, TaskInstance taskInstance, String roleId, String userId) {
        return null;
    }

    /**
     * 自动审核之后
     *
     * @param approveMode  自动审批模式
     * @param taskInstance 任务实例
     * @param roleId       角色 ID
     * @param userId       用户 ID
     * @param comment      备注
     *
     * @author wangweijun
     * @since 2025/1/16 17:49
     */
    default void postAutoApprove(ContinuousApproveMode approveMode, TaskInstance taskInstance, String roleId, String userId, String comment) {
    }
}
