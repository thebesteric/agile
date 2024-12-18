package io.github.thebesteric.framework.agile.plugins.workflow.domain;

import io.github.thebesteric.framework.agile.plugins.workflow.entity.NodeRoleAssignment;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.TaskApprove;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.TaskRoleApproveRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 角色审批记录包装类
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-10-11 15:14:52
 */
@Data
public class TaskRoleApprove {

    /** 角色审批记录 */
    @Schema(description = "角色审批记录")
    private TaskRoleApproveRecord taskRoleApproveRecord;
    @Schema(description = "角色审批记录-审批记录")
    private TaskApprove taskApprove;
    @Schema(description = "角色审批记录-角色用户")
    private NodeRoleAssignment nodeRoleAssignment;

    public static TaskRoleApprove of(TaskRoleApproveRecord taskRoleApproveRecord, TaskApprove taskApprove, NodeRoleAssignment nodeRoleAssignment) {
        TaskRoleApprove taskRoleApprove = new TaskRoleApprove();
        taskRoleApprove.taskRoleApproveRecord = taskRoleApproveRecord;
        taskRoleApprove.taskApprove = taskApprove;
        taskRoleApprove.nodeRoleAssignment = nodeRoleAssignment;
        return taskRoleApprove;
    }

}
