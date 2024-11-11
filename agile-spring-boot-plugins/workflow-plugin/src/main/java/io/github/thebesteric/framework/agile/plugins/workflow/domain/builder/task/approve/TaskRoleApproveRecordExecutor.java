package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.task.approve;

import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.builder.Query;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.builder.QueryBuilderWrapper;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.ApproveStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.RoleApproveStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.TaskRoleApproveRecord;
import io.vavr.control.Try;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * TaskRoleApproveRecordExecutor
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-09-14 11:57:41
 */
@Getter
@Setter
public class TaskRoleApproveRecordExecutor extends AbstractExecutor<TaskRoleApproveRecord> {

    private TaskRoleApproveRecord taskRoleApproveRecord;

    protected TaskRoleApproveRecordExecutor(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
        this.taskRoleApproveRecord = new TaskRoleApproveRecord();
    }

    /**
     * 根据任务审批实例 ID 删除
     *
     * @param tenantId      租户 ID
     * @param taskApproveId 任务审批实例 ID
     *
     * @author wangweijun
     * @since 2024/9/14 16:05
     */
    public void deleteByTaskApproveIdAndNodeRoleAssignmentId(String tenantId, Integer taskApproveId, Integer nodeRoleAssignmentId) {
        Query query = QueryBuilderWrapper.createLambda(TaskRoleApproveRecord.class)
                .eq(TaskRoleApproveRecord::getTenantId, tenantId)
                .eq(TaskRoleApproveRecord::getTaskApproveId, taskApproveId)
                .eq(TaskRoleApproveRecord::getNodeRoleAssignmentId, nodeRoleAssignmentId)
                .build();
        super.delete(query);
    }

    /**
     * 根据任务审批实例 ID 获取
     *
     * @param tenantId      租户 ID
     * @param taskApproveId 任务审批实例 ID
     *
     * @return TaskRoleApproveRecord
     *
     * @author wangweijun
     * @since 2024/9/14 16:06
     */
    public List<TaskRoleApproveRecord> findByTaskApproveId(String tenantId, Integer taskApproveId) {
        Query query = QueryBuilderWrapper.createLambda(TaskRoleApproveRecord.class)
                .eq(TaskRoleApproveRecord::getTenantId, tenantId)
                .eq(TaskRoleApproveRecord::getTaskApproveId, taskApproveId)
                .eq(TaskRoleApproveRecord::getState, 1)
                .build();
        return super.find(query).getRecords();
    }

    /**
     * 根据任务实例 ID 获取
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     *
     * @return List<TaskRoleApproveRecord>
     *
     * @author wangweijun
     * @since 2024/9/18 10:37
     */
    public List<TaskRoleApproveRecord> findByTaskInstanceId(String tenantId, Integer taskInstanceId) {
        Query query = QueryBuilderWrapper.createLambda(TaskRoleApproveRecord.class)
                .eq(TaskRoleApproveRecord::getTenantId, tenantId)
                .eq(TaskRoleApproveRecord::getTaskInstanceId, taskInstanceId)
                .eq(TaskRoleApproveRecord::getState, 1)
                .build();
        return super.find(query).getRecords();
    }

    /**
     * 根据任务实例 ID，审批状态获取
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     * @param approveStatus  审批状态
     *
     * @return List<TaskRoleApproveRecord>
     *
     * @author wangweijun
     * @since 2024/9/18 10:37
     */
    public List<TaskRoleApproveRecord> findByTaskInstanceIdAndStatus(String tenantId, Integer taskInstanceId, RoleApproveStatus approveStatus) {
        Query query = QueryBuilderWrapper.createLambda(TaskRoleApproveRecord.class)
                .eq(TaskRoleApproveRecord::getTenantId, tenantId)
                .eq(TaskRoleApproveRecord::getTaskInstanceId, taskInstanceId)
                .eq(TaskRoleApproveRecord::getStatus, approveStatus.getCode())
                .eq(TaskRoleApproveRecord::getState, 1)
                .build();
        return super.find(query).getRecords();
    }

    /**
     * 根据任务实例 ID，审批状态获取
     *
     * @param tenantId        租户 ID
     * @param taskInstanceId  任务实例 ID
     * @param approveStatuses 审批状态集合
     *
     * @return List<TaskRoleApproveRecord>
     *
     * @author wangweijun
     * @since 2024/9/25 15:03
     */
    public List<TaskRoleApproveRecord> findByTaskInstanceIdAndStatuses(String tenantId, Integer taskInstanceId, List<ApproveStatus> approveStatuses) {
        List<Integer> approveStatusCodes = approveStatuses.stream().map(ApproveStatus::getCode).toList();
        Query query = QueryBuilderWrapper.createLambda(TaskRoleApproveRecord.class)
                .eq(TaskRoleApproveRecord::getTenantId, tenantId)
                .eq(TaskRoleApproveRecord::getTaskInstanceId, taskInstanceId)
                .in(CollectionUtils.isNotEmpty(approveStatusCodes), TaskRoleApproveRecord::getStatus, new ArrayList<>(approveStatusCodes))
                .eq(TaskRoleApproveRecord::getState, 1)
                .build();
        return super.find(query).getRecords();
    }

    /**
     * 根据任务实例 ID，角色 ID 获取
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     * @param roleId         角色 ID
     *
     * @return List<TaskRoleApproveRecord>
     *
     * @author wangweijun
     * @since 2024/9/19 14:46
     */
    public List<TaskRoleApproveRecord> findByTaskInstanceIdAndRoleId(String tenantId, Integer taskInstanceId, String roleId) {
        String selectSql = """
                SELECT t.* FROM awf_task_role_approve_record t
                LEFT JOIN awf_node_role_assignment r ON r.id = t.node_role_assignment_id
                WHERE t.tenant_id = ? AND t.task_inst_id = ? AND r.role_id = ?
                """;
        return jdbcTemplate.query(selectSql, (rs, rowNum) -> TaskRoleApproveRecord.of(rs), tenantId, taskInstanceId, roleId);
    }

    /**
     * 根据任务实例 ID，角色 ID 获取
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     * @param roleId         角色 ID
     * @param approveStatus  审批状态
     *
     * @return List<TaskRoleApproveRecord>
     *
     * @author wangweijun
     * @since 2024/9/19 14:46
     */
    public List<TaskRoleApproveRecord> findByTaskInstanceIdAndRoleIdAndStatus(String tenantId, Integer taskInstanceId, String roleId, ApproveStatus approveStatus) {
        String selectSql = """
                SELECT t.* FROM awf_task_role_approve_record t
                LEFT JOIN awf_node_role_assignment r ON r.id = t.node_role_assignment_id
                WHERE t.tenant_id = ? AND t.task_inst_id = ? AND r.role_id = ? AND t.status = ?
                """;
        return jdbcTemplate.query(selectSql, (rs, rowNum) -> TaskRoleApproveRecord.of(rs), tenantId, taskInstanceId, roleId, approveStatus.getCode());
    }

    /**
     * 根据任务实例 ID，角色 ID，用户 ID 获取
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     * @param roleId         角色 ID
     * @param userId         用户 ID
     *
     * @return TaskRoleApproveRecord
     *
     * @author wangweijun
     * @since 2024/9/19 19:17
     */
    public TaskRoleApproveRecord getByTaskInstanceIdAndRoleIdAndUserId(String tenantId, Integer taskInstanceId, String roleId, String userId) {
        String selectSql = """
                SELECT t.* FROM awf_task_role_approve_record t
                LEFT JOIN awf_node_role_assignment r ON r.id = t.node_role_assignment_id
                WHERE t.tenant_id = ? AND t.task_inst_id = ? AND r.role_id = ? AND r.user_id = ?
                """;
        RowMapper<TaskRoleApproveRecord> rowMapper = (ResultSet rs, int rowNum) -> TaskRoleApproveRecord.of(rs);
        return Try.of(() -> this.jdbcTemplate.queryForObject(selectSql, rowMapper, tenantId, taskInstanceId, roleId, userId)).getOrNull();
    }

    /**
     * 根据流程实例 ID 获取角色审批记录
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     *
     * @return List<TaskRoleApproveRecord>
     *
     * @author wangweijun
     * @since 2024/10/11 14:26
     */
    public List<TaskRoleApproveRecord> findByTWorkflowInstanceId(String tenantId, Integer workflowInstanceId) {
        return this.findByTWorkflowInstanceId(tenantId, workflowInstanceId, null);
    }

    /**
     * 根据流程实例 ID 获取角色审批记录
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     * @param approveStatus      审批状态
     *
     * @return List<TaskRoleApproveRecord>
     *
     * @author wangweijun
     * @since 2024/10/11 14:26
     */
    public List<TaskRoleApproveRecord> findByTWorkflowInstanceId(String tenantId, Integer workflowInstanceId, ApproveStatus approveStatus) {
        QueryBuilderWrapper.Builder<TaskRoleApproveRecord> builder = QueryBuilderWrapper.createLambda(TaskRoleApproveRecord.class)
                .eq(TaskRoleApproveRecord::getTenantId, tenantId)
                .eq(TaskRoleApproveRecord::getWorkflowInstanceId, workflowInstanceId)
                .eq(TaskRoleApproveRecord::getState, 1);
        if (approveStatus != null) {
            builder.eq(TaskRoleApproveRecord::getStatus, approveStatus.getCode());
        }
        Query query = builder.build();
        return super.find(query).getRecords();
    }
}
