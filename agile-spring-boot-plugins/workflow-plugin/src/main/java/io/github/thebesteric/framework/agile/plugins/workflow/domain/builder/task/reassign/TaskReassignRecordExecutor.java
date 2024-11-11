package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.task.reassign;

import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.OrderByOperator;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.builder.Query;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.builder.QueryBuilderWrapper;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.TaskReassignRecord;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * TaskReassignRecordExecutor
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-11-07 16:22:34
 */
@Getter
@Setter
public class TaskReassignRecordExecutor extends AbstractExecutor<TaskReassignRecord> {

    private TaskReassignRecord taskReassignRecord;

    public TaskReassignRecordExecutor(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
        this.taskReassignRecord = new TaskReassignRecord();
    }

    /**
     * 根据任务审批任务 ID、角色 ID、用户 ID 查询转派记录
     *
     * @param tenantId      租户 ID
     * @param taskApproveId 审批任务 ID
     * @param toRoleId      角色 ID
     * @param toUserId      用户 ID
     *
     * @return TaskReassignRecord
     *
     * @author wangweijun
     * @since 2024/11/8 11:29
     */
    public TaskReassignRecord getToUser(String tenantId, Integer taskApproveId, String toRoleId, String toUserId) {
        Query query = QueryBuilderWrapper.createLambda(TaskReassignRecord.class)
                .eq(TaskReassignRecord::getTenantId, tenantId)
                .eq(TaskReassignRecord::getTaskApproveId, taskApproveId)
                .eq(StringUtils.isNotEmpty(toRoleId), TaskReassignRecord::getToRoleId, toRoleId)
                .eq(TaskReassignRecord::getToUserId, toUserId)
                .eq(TaskReassignRecord::getState, 1)
                .build();
        return this.get(query);
    }

    /**
     * 根据任务审批任务 ID 查询转派记录
     *
     * @param tenantId      租户 ID
     * @param taskApproveId 审批任务 ID
     *
     * @return List<TaskReassignRecord>
     *
     * @author wangweijun
     * @since 2024/11/8 11:29
     */
    public List<TaskReassignRecord> findByTaskApproveId(String tenantId, Integer taskApproveId) {
        Query query = QueryBuilderWrapper.createLambda(TaskReassignRecord.class)
                .eq(TaskReassignRecord::getTenantId, tenantId)
                .eq(TaskReassignRecord::getTaskApproveId, taskApproveId)
                .eq(TaskReassignRecord::getState, 1)
                .orderBy(TaskReassignRecord::getCreatedAt, OrderByOperator.DESC)
                .build();
        return this.find(query).getRecords();
    }

    /**
     * 根据任务审批任务 ID 查询最新的转派记录
     *
     * @param tenantId      租户 ID
     * @param taskApproveId 审批任务 ID
     *
     * @return TaskReassignRecord
     *
     * @author wangweijun
     * @since 2024/11/8 11:39
     */
    public TaskReassignRecord getByLatestTaskApproveId(String tenantId, Integer taskApproveId) {
        List<TaskReassignRecord> records = this.findByTaskApproveId(tenantId, taskApproveId);
        return records.isEmpty() ? null : records.get(0);
    }

    /**
     * 获取转派记录
     *
     * @param tenantId      租户 ID
     * @param taskApproveId 审批任务 ID
     * @param fromRoleId    from 角色 ID
     * @param fromUserId    from 用户 ID
     * @param toRoleId      to 角色 ID
     * @param toUserId      to 用户 ID
     *
     * @return TaskReassignRecord
     *
     * @author wangweijun
     * @since 2024/11/11 14:53
     */
    public TaskReassignRecord getRecord(String tenantId, Integer taskApproveId, String fromRoleId, String fromUserId, String toRoleId, String toUserId) {
        Query query = QueryBuilderWrapper.createLambda(TaskReassignRecord.class)
                .eq(TaskReassignRecord::getTenantId, tenantId)
                .eq(TaskReassignRecord::getTaskApproveId, taskApproveId)
                .eq(TaskReassignRecord::getFromRoleId, fromRoleId)
                .eq(TaskReassignRecord::getFromUserId, fromUserId)
                .eq(TaskReassignRecord::getToRoleId, toRoleId)
                .eq(TaskReassignRecord::getToUserId, toUserId)
                .eq(TaskReassignRecord::getState, 1)
                .build();
        return this.get(query);
    }
}
