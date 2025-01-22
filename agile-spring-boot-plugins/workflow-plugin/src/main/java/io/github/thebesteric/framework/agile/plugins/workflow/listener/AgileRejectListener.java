package io.github.thebesteric.framework.agile.plugins.workflow.listener;

import io.github.thebesteric.framework.agile.plugins.workflow.entity.TaskApprove;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.TaskInstance;

/**
 * 审批拒绝监听器
 *
 * @author wangweijun
 * @version v1.0
 * @since 2025-01-22 09:44:57
 */
public interface AgileRejectListener {

    /**
     * 拒绝之前
     *
     * @param taskInstance 任务实例
     * @param taskApprove  审批任务
     * @param roleId       角色 ID
     * @param userId       用户 ID
     * @param comment      审批意见
     *
     * @return 返回 null 表示使用系统默认备注
     *
     * @author wangweijun
     * @since 2025/1/22 09:57
     */
    default String preReject(TaskInstance taskInstance, TaskApprove taskApprove, String roleId, String userId, String comment) {
        return null;
    }

    /**
     * 拒绝之后
     *
     * @param taskInstance 任务实例
     * @param taskApprove  审批任务
     * @param roleId       角色 ID
     * @param userId       用户 ID
     * @param comment      审批意见
     *
     * @author wangweijun
     * @since 2025/1/22 09:58
     */
    default void postRejected(TaskInstance taskInstance, TaskApprove taskApprove, String roleId, String userId, String comment) {
    }

}
