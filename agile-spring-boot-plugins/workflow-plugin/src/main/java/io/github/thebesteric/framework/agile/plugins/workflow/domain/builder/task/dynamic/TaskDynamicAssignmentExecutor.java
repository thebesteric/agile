package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.task.dynamic;

import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.builder.Query;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.builder.QueryBuilderWrapper;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.TaskDynamicAssignment;
import lombok.Getter;
import lombok.Setter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * TaskDynamicAssignmentExecutor
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-10-17 17:28:47
 */
@Getter
@Setter
public class TaskDynamicAssignmentExecutor extends AbstractExecutor<TaskDynamicAssignment> {

    private TaskDynamicAssignment taskDynamicAssignment;

    public TaskDynamicAssignmentExecutor(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
        this.taskDynamicAssignment = new TaskDynamicAssignment();
    }

    /**
     * 根据节点定义获取动态审批用户
     *
     * @param tenantId         租户 ID
     * @param nodeDefinitionId 节点定义 ID
     *
     * @return List<TaskDynamicAssignment>
     *
     * @author wangweijun
     * @since 2024/10/18 18:03
     */
    public List<TaskDynamicAssignment> findByNodeDefinitionId(String tenantId, Integer nodeDefinitionId) {
        Query query = QueryBuilderWrapper.createLambda(TaskDynamicAssignment.class)
                .eq(TaskDynamicAssignment::getTenantId, tenantId)
                .eq(TaskDynamicAssignment::getNodeDefinitionId, nodeDefinitionId)
                .eq(TaskDynamicAssignment::getState, 1)
                .build();
        return this.find(query).getRecords();
    }

    /**
     * 根据任务实例获取动态审批用户
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     *
     * @return List<TaskDynamicAssignment>
     *
     * @author wangweijun
     * @since 2024/10/18 18:03
     */
    public List<TaskDynamicAssignment> findByTaskInstanceId(String tenantId, Integer taskInstanceId) {
        Query query = QueryBuilderWrapper.createLambda(TaskDynamicAssignment.class)
                .eq(TaskDynamicAssignment::getTenantId, tenantId)
                .eq(TaskDynamicAssignment::getTaskInstanceId, taskInstanceId)
                .eq(TaskDynamicAssignment::getState, 1)
                .build();
        return this.find(query).getRecords();
    }

    /**
     * 根据任务实例、审批人获取动态审批用户
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     * @param approverId     审批人 ID
     *
     * @return TaskDynamicAssignment
     *
     * @author wangweijun
     * @since 2024/12/18 10:12
     */
    public TaskDynamicAssignment getByTaskInstanceIdAndApproverId(String tenantId, Integer taskInstanceId, String approverId) {
        Query query = QueryBuilderWrapper.createLambda(TaskDynamicAssignment.class)
                .eq(TaskDynamicAssignment::getTenantId, tenantId)
                .eq(TaskDynamicAssignment::getTaskInstanceId, taskInstanceId)
                .eq(TaskDynamicAssignment::getApproverId, approverId)
                .eq(TaskDynamicAssignment::getState, 1)
                .build();
        return this.get(query);
    }
}
