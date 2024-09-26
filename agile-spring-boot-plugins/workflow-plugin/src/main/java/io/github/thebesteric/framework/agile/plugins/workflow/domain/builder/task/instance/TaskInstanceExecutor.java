package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.task.instance;

import io.github.thebesteric.framework.agile.commons.exception.DataExistsException;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.Page;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.builder.Query;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.builder.QueryBuilderWrapper;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.ApproveStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.NodeStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.NodeType;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.NodeRoleAssignment;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.TaskInstance;
import io.vavr.control.Try;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.stream.Collectors;

/**
 * TaskInstanceExecutor
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-24 13:21:50
 */
@Getter
@Setter
public class TaskInstanceExecutor extends AbstractExecutor<TaskInstance> {

    private TaskInstance taskInstance;

    public TaskInstanceExecutor(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
        this.taskInstance = new TaskInstance();
    }

    public TaskInstance save() {
        // 检查是否有已存在的节点定义
        TaskInstance existsTaskInstance = this.getByWorkflowInstanceIdAndNodeDefinitionId();
        if (existsTaskInstance != null) {
            throw new DataExistsException("已存在相同的任务实例");
        }
        return super.save(taskInstance);
    }

    /**
     * 根据流程实例 ID 和节点定义 ID 查询任务实例
     *
     * @return TaskInstance
     *
     * @author wangweijun
     * @since 2024/6/24 13:44
     */
    public TaskInstance getByWorkflowInstanceIdAndNodeDefinitionId() {
        return this.getByWorkflowInstanceIdAndNodeDefinitionId(taskInstance.getTenantId(), taskInstance.getWorkflowInstanceId(), taskInstance.getNodeDefinitionId());
    }

    /**
     * 根据流程实例 ID 和节点定义 ID 查询任务实例
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     * @param nodeDefinitionId   节点定义 ID
     *
     * @return TaskInstance
     *
     * @author wangweijun
     * @since 2024/6/24 13:44
     */
    public TaskInstance getByWorkflowInstanceIdAndNodeDefinitionId(String tenantId, Integer workflowInstanceId, Integer nodeDefinitionId) {
        final String selectSql = """
                SELECT * FROM awf_task_instance WHERE  `tenant_id` = ? AND `wf_inst_id` = ? AND `node_def_id` = ? AND `state` = 1
                """;
        return Try.of(() -> this.jdbcTemplate.queryForObject(selectSql, (rs, rowNum) -> TaskInstance.of(rs), tenantId, workflowInstanceId, nodeDefinitionId)).getOrNull();
    }

    /**
     * 根据审批人 ID 和 节点状态 查询任务实例
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     * @param roleIds            审批人角色 IDs
     * @param approverId         审批人 ID
     * @param nodeStatuses       节点状态
     * @param approveStatuses    审批人审批状态
     * @param page               当前页
     * @param pageSize           每页数量
     *
     * @return List<TaskInstance>
     *
     * @author wangweijun
     * @since 2024/6/25 10:03
     */
    public Page<TaskInstance> findByApproverId(String tenantId, Integer workflowInstanceId, List<String> roleIds, String approverId, List<NodeStatus> nodeStatuses, List<ApproveStatus> approveStatuses, Integer page, Integer pageSize) {
        String roleIdStrs = null;
        if (roleIds == null || roleIds.isEmpty()) {
            String selectRoleSql = """
                    SELECT distinct nro.* FROM awf_node_role_assignment nro
                    LEFT JOIN awf_node_definition nd ON nd.id = nro.node_def_id
                    LEFT JOIN awf_wf_definition wd ON wd.id = nd.wf_def_id
                    LEFT JOIN awf_wf_instance wi ON wi.wf_def_id = nd.wf_def_id
                    LEFT JOIN awf_task_instance ti ON ti.wf_inst_id = wi.id
                    WHERE nro.tenant_id = ? AND nro.user_id = ?
                    """;
            if (workflowInstanceId != null) {
                selectRoleSql += " AND wi.id = " + workflowInstanceId;
            }
            if (nodeStatuses != null && !nodeStatuses.isEmpty()) {
                String codes = nodeStatuses.stream().map(NodeStatus::getCode).map(String::valueOf).collect(Collectors.joining(","));
                selectRoleSql += " AND ti.status in  (" + codes + ")";
            }
            List<NodeRoleAssignment> nodeRoleAssignment = this.jdbcTemplate.query(selectRoleSql, (rs, rowNum) -> NodeRoleAssignment.of(rs), tenantId, approverId);
            if (!nodeRoleAssignment.isEmpty()) {
                roleIdStrs = nodeRoleAssignment.stream().map(i -> "'" + i.getRoleId() + "'").collect(Collectors.joining(","));
            }
        } else {
            roleIdStrs = roleIds.stream().map(i -> "'" + i + "'").collect(Collectors.joining(","));
        }

        String selectSql = """
                SELECT distinct i.* FROM awf_task_instance i 
                LEFT JOIN awf_task_approve t ON i.id = t.task_inst_id 
                LEFT JOIN awf_task_role_approve_record tr ON tr.task_approve_id = t.id
                LEFT JOIN awf_node_role_assignment nra ON nra.id = tr.node_role_assignment_id
                WHERE i.`state` = 1 AND i.tenant_id = ?
                """;
        if (workflowInstanceId != null) {
            selectSql += " AND i.`wf_inst_id` = " + workflowInstanceId;
        }
        if (nodeStatuses != null && !nodeStatuses.isEmpty()) {
            String codes = nodeStatuses.stream().map(NodeStatus::getCode).map(String::valueOf).collect(Collectors.joining(","));
            selectSql += " AND i.`status` in (" + codes + ")";
        }
        if (approveStatuses != null && !approveStatuses.isEmpty()) {
            String codes = approveStatuses.stream().map(ApproveStatus::getCode).map(String::valueOf).collect(Collectors.joining(","));
            if (roleIdStrs == null) {
                selectSql += " AND t.`status` in (" + codes + ")";
            } else {
                selectSql += " AND (t.`status` in (" + codes + ") OR tr.status in (" + codes + "))";
            }
        }
        if (roleIdStrs != null) {
            selectSql += " AND (t.`approver_id` = '%s' OR t.`approver_id` in (%s))".formatted(approverId, roleIdStrs);
            selectSql += " AND nra.`user_id` = '%s'".formatted(approverId);
            selectSql += " AND NOT EXISTS(SELECT * FROM awf_task_role_approve_record r WHERE r.task_approve_id = t.id ";
            if (approveStatuses != null && !approveStatuses.isEmpty()) {
                String codes = approveStatuses.stream().map(ApproveStatus::getCode).map(String::valueOf).collect(Collectors.joining(","));
                selectSql += " AND r.`status` not in (" + codes + ")";
            }
            selectSql += " AND r.node_role_assignment_id = (SELECT id FROM awf_node_role_assignment where tenant_id = %s and user_id = '%s' and role_id in (%s)))".formatted(tenantId, approverId, roleIdStrs);
        } else {
            selectSql += " AND t.`approver_id` = '%s'".formatted(approverId);
        }

        String countSql = "SELECT COUNT(*) FROM (" + selectSql + ") AS t";
        Integer count = this.jdbcTemplate.queryForObject(countSql, Integer.class, tenantId);

        selectSql += " ORDER BY i.`id` DESC LIMIT ? OFFSET ?";
        Integer offset = (page - 1) * pageSize;
        List<TaskInstance> records = this.jdbcTemplate.query(selectSql, (rs, rowNum) -> TaskInstance.of(rs), tenantId, pageSize, offset);

        return Page.of(page, pageSize, count, records);
    }

    /**
     * 根据审批人 ID 和 节点状态 查询任务实例
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     * @param roleId             审批人角色 ID
     * @param approverId         审批人 ID
     * @param nodeStatus         节点状态
     * @param approveStatus      审批人审批状态
     *
     * @return List<TaskInstance>
     *
     * @author wangweijun
     * @since 2024/6/25 10:03
     */
    public List<TaskInstance> findByApproverId(String tenantId, Integer workflowInstanceId, String roleId, String approverId, NodeStatus nodeStatus, ApproveStatus approveStatus) {
        List<NodeStatus> nodeStatuses = nodeStatus == null ? null : List.of(nodeStatus);
        List<ApproveStatus> approveStatuses = approveStatus == null ? null : List.of(approveStatus);
        List<String> roleIds = StringUtils.isEmpty(roleId) ? null : List.of(roleId);
        return this.findByApproverId(tenantId, workflowInstanceId, roleIds, approverId, nodeStatuses, approveStatuses, 1, Integer.MAX_VALUE).getRecords();
    }

    /**
     * 根据审批人 ID 查询任务实例
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     * @param roleId             审批人角色 ID
     * @param approverId         审批人 ID
     *
     * @return List<TaskInstance>
     *
     * @author wangweijun
     * @since 2024/6/25 10:03
     */
    public List<TaskInstance> findByApproverId(String tenantId, Integer workflowInstanceId, String roleId, String approverId) {
        return this.findByApproverId(tenantId, workflowInstanceId, roleId, approverId, null, null);
    }

    /**
     * 根据流程实例 ID 查询任务实例
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     *
     * @return List<TaskInstance>
     *
     * @author wangweijun
     * @since 2024/6/25 10:03
     */
    public List<TaskInstance> findByWorkflowInstanceId(String tenantId, Integer workflowInstanceId) {
        final String selectSql = """
                SELECT * FROM awf_task_instance WHERE `tenant_id` = ? AND `wf_inst_id` = ? AND `state` = 1
                """;
        return this.jdbcTemplate.query(selectSql, (rs, rowNum) -> TaskInstance.of(rs), tenantId, workflowInstanceId);
    }

    /**
     * 根据流程实例 ID 和节点类型 查询任务实例
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     * @param nodeType           节点类型
     *
     * @return List<TaskInstance>
     *
     * @author wangweijun
     * @since 2024/6/27 14:13
     */
    public List<TaskInstance> findByWorkflowInstanceIdAndNodeType(String tenantId, Integer workflowInstanceId, NodeType nodeType) {
        final String selectSql = """
                SELECT i.* FROM awf_task_instance i LEFT JOIN awf_node_definition d
                ON i.`node_def_id` = d.`id`
                WHERE i.`tenant_id` = ? AND i.`wf_inst_id` = ? AND i.`state` = 1 AND d.node_type = %s
                """.formatted(nodeType.getCode());
        return this.jdbcTemplate.query(selectSql, (rs, rowNum) -> TaskInstance.of(rs), tenantId, workflowInstanceId);
    }

    /**
     * 根据流程实例 ID 查询开始任务实例
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     *
     * @return List<TaskInstance>
     *
     * @author wangweijun
     * @since 2024/6/25 10:03
     */
    public TaskInstance getStartTaskInstanceByWorkflowInstanceId(String tenantId, Integer workflowInstanceId) {
        List<TaskInstance> taskInstances = this.findByWorkflowInstanceIdAndNodeType(tenantId, workflowInstanceId, NodeType.START);
        return taskInstances.isEmpty() ? null : taskInstances.get(0);
    }

    /**
     * 根据流程实例 ID 查询结束任务实例
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     *
     * @return List<TaskInstance>
     *
     * @author wangweijun
     * @since 2024/6/25 10:03
     */
    public TaskInstance getEndTaskInstanceByWorkflowInstanceId(String tenantId, Integer workflowInstanceId) {
        List<TaskInstance> taskInstances = this.findByWorkflowInstanceIdAndNodeType(tenantId, workflowInstanceId, NodeType.END);
        return taskInstances.isEmpty() ? null : taskInstances.get(0);
    }

    /**
     * 获取当前流程实例下正在进行的审批节点
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     *
     * @return TaskInstance
     *
     * @author wangweijun
     * @since 2024/9/9 15:06
     */
    public TaskInstance getInCurrentlyEffectTaskInstance(String tenantId, Integer workflowInstanceId) {
        Query query = QueryBuilderWrapper.createLambda(TaskInstance.class)
                .eq(TaskInstance::getTenantId, tenantId)
                .eq(TaskInstance::getWorkflowInstanceId, workflowInstanceId)
                .eq(TaskInstance::getStatus, NodeStatus.IN_PROGRESS.getCode())
                .eq(TaskInstance::getState, 1).build();
        return super.get(query);
    }
}
