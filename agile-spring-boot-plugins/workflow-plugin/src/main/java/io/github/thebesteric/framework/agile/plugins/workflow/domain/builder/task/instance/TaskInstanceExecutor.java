package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.task.instance;

import io.github.thebesteric.framework.agile.commons.exception.DataExistsException;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.ApproveStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.NodeStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.NodeType;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.TaskInstance;
import io.vavr.control.Try;
import lombok.Getter;
import lombok.Setter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

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
            throw new DataExistsException("TaskInstance already exists");
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
     * @param tenantId      租户 ID
     * @param approverId    审批人 ID
     * @param nodeStatus    节点状态
     * @param approveStatus 审批人审批状态
     * @param page          当前页
     * @param pageSize      每页数量
     *
     * @return List<TaskInstance>
     *
     * @author wangweijun
     * @since 2024/6/25 10:03
     */
    public List<TaskInstance> findByApproverId(String tenantId, String approverId, NodeStatus nodeStatus, ApproveStatus approveStatus, Integer page, Integer pageSize) {
        String selectSql = """
                SELECT i.* FROM awf_task_instance i LEFT JOIN awf_task_approve t ON i.id = t.task_inst_id 
                WHERE i.tenant_id = ? AND t.approver_id = ? AND i.`state` = 1
                """;
        if (nodeStatus != null) {
            selectSql += " AND i.`status` = " + nodeStatus.getCode();
        }
        if (approveStatus != null) {
            selectSql += " AND t.`status` = " + approveStatus.getCode();
        }
        selectSql += " ORDER BY i.`id` DESC LIMIT ? OFFSET ?";
        Integer offset = (page - 1) * pageSize;
        return this.jdbcTemplate.query(selectSql, (rs, rowNum) -> TaskInstance.of(rs), tenantId, approverId, pageSize, offset);
    }

    /**
     * 根据审批人 ID 和 节点状态 查询任务实例
     *
     * @param tenantId      租户 ID
     * @param approverId    审批人 ID
     * @param nodeStatus    节点状态
     * @param approveStatus 审批人审批状态
     *
     * @return List<TaskInstance>
     *
     * @author wangweijun
     * @since 2024/6/25 10:03
     */
    public List<TaskInstance> findByApproverId(String tenantId, String approverId, NodeStatus nodeStatus, ApproveStatus approveStatus) {
        return this.findByApproverId(tenantId, approverId, nodeStatus, approveStatus, 1, Integer.MAX_VALUE);
    }

    /**
     * 根据审批人 ID 查询任务实例
     *
     * @param tenantId   租户 ID
     * @param approverId 审批人 ID
     *
     * @return List<TaskInstance>
     *
     * @author wangweijun
     * @since 2024/6/25 10:03
     */
    public List<TaskInstance> findByApproverId(String tenantId, String approverId) {
        return this.findByApproverId(tenantId, approverId, null, null);
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
}
