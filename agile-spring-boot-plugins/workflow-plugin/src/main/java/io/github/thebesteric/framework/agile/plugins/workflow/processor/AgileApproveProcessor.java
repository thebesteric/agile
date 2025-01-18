package io.github.thebesteric.framework.agile.plugins.workflow.processor;

import io.github.thebesteric.framework.agile.plugins.workflow.constant.ContinuousApproveMode;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.TaskInstance;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowInstance;

/**
 * 自动审批监听器
 *
 * @author wangweijun
 * @version v1.0
 * @since 2025-01-16 17:41:21
 */
public interface AgileApproveProcessor {

    /**
     * 审核之前
     * 审核之前，表示当前审核开始，一次审批，只有一次调用，
     *
     * @param taskInstance 任务实例
     * @param roleId       角色 ID
     * @param userId       用户 ID
     *
     * @return 返回 null 表示使用系统默认备注
     *
     * @author wangweijun
     * @since 2025/1/18 17:24
     */
    default String preApprove(TaskInstance taskInstance, String roleId, String userId) {
        return null;
    }

    /**
     * 自动审核之前
     * 自动审核之前，表示自动审核开始，一次审批，可能会有多次调用
     *
     * @param approveMode  自动审批模式
     * @param taskInstance 任务实例
     * @param roleId       角色 ID
     * @param userId       用户 ID
     *
     * @return 返回 null 表示使用系统默认备注
     *
     * @author wangweijun
     * @since 2025/1/16 17:46
     */
    default String preAutoApprove(ContinuousApproveMode approveMode, TaskInstance taskInstance, String roleId, String userId) {
        return null;
    }

    /**
     * 自动审核之后
     * 自动审核之后，表示自动审核完成，一次审批，可能会有多次调用
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
    default void postAutoApproved(ContinuousApproveMode approveMode, TaskInstance taskInstance, String roleId, String userId, String comment) {
    }

    /**
     * 审核之后
     * 审核之后。表示当前审批完成，其中包括自动审批流程也完成，一次审批，只有一次调用
     *
     * @param taskInstance 任务实例
     * @param roleId       角色 ID
     * @param userId       用户 ID
     * @param comment      备注
     *
     * @author wangweijun
     * @since 2025/1/16 17:49
     */
    default void postApproved(TaskInstance taskInstance, String roleId, String userId, String comment) {
    }

    /**
     * 流程审核完成
     * 整个流程生命周期中，只会在完成的时候调用一次
     *
     * @param workflowInstance 流程实例
     * @param taskInstance     任务实例
     * @param roleId           角色 ID
     * @param userId           用户 ID
     * @param comment          备注
     *
     * @author wangweijun
     * @since 2025/1/18 18:03
     */
    default void approveCompleted(WorkflowInstance workflowInstance, TaskInstance taskInstance, String roleId, String userId, String comment) {
    }
}
