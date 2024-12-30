package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.definition;

import io.github.thebesteric.framework.agile.plugins.workflow.domain.Approver;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.task.instance.TaskInstanceExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.assignment.WorkflowAssignmentBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.assignment.WorkflowAssignmentExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.instance.WorkflowInstanceExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.TaskInstance;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowAssignment;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowDefinition;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowInstance;
import io.github.thebesteric.framework.agile.plugins.workflow.exception.WorkflowException;
import io.vavr.control.Try;
import lombok.Getter;
import lombok.Setter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.util.Date;
import java.util.List;

/**
 * 流程定义创建者
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-14 15:47:20
 */
@Getter
@Setter
public class WorkflowDefinitionExecutor extends AbstractExecutor<WorkflowDefinition> {

    private WorkflowDefinition workflowDefinition;
    private final WorkflowAssignmentExecutor workflowAssignmentExecutor;
    private final TaskInstanceExecutor taskInstanceExecutor;
    private final WorkflowInstanceExecutor workflowInstanceExecutor;

    public WorkflowDefinitionExecutor(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
        this.workflowDefinition = new WorkflowDefinition();
        this.workflowAssignmentExecutor = new WorkflowAssignmentExecutor(jdbcTemplate);
        this.taskInstanceExecutor = new TaskInstanceExecutor(jdbcTemplate);
        this.workflowInstanceExecutor = new WorkflowInstanceExecutor(jdbcTemplate);
    }

    /**
     * 保存流程定义
     *
     * @author wangweijun
     * @since 2024/6/17 15:24
     */
    public WorkflowDefinition save() {
        // 检查是否有已存在的流程定义
        WorkflowDefinition existsWorkflowDefinition = this.getByTenantAndKey(workflowDefinition.getTenantId(), workflowDefinition.getKey());
        if (existsWorkflowDefinition != null) {
            throw new WorkflowException("已存在相同的流程定义");
        }
        // 保存流程定义
        workflowDefinition = super.save(workflowDefinition);
        // 获取流程定义的审批人
        Approver whenEmptyApprover = workflowDefinition.getWhenEmptyApprover();
        // 当 isAllowEmptyAutoApprove 为 false 时，且 whenEmptyApprover 不为空，表示使用 whenEmptyApprover 进行审批
        if (whenEmptyApprover != null) {
            // 保存默认审批人
            WorkflowAssignment workflowAssignment = WorkflowAssignmentBuilder
                    .builder(workflowDefinition.getTenantId(), workflowDefinition.getId())
                    .approverInfo(whenEmptyApprover.getId(), whenEmptyApprover.getName(), whenEmptyApprover.getDesc())
                    .build();
            workflowAssignmentExecutor.save(workflowAssignment);
        }
        return workflowDefinition;
    }

    /**
     * 根据任务实例 ID 获取流程定义
     *
     * @param taskInstanceId 任务实例 ID
     *
     * @return WorkflowDefinition
     *
     * @author wangweijun
     * @since 2024/9/12 10:25
     */
    public WorkflowDefinition getByTaskInstanceId(Integer taskInstanceId) {
        TaskInstance taskInstance = taskInstanceExecutor.getById(taskInstanceId);
        Integer workflowInstanceId = taskInstance.getWorkflowInstanceId();
        WorkflowInstance workflowInstance = workflowInstanceExecutor.getById(workflowInstanceId);
        Integer workflowDefinitionId = workflowInstance.getWorkflowDefinitionId();
        return getById(workflowDefinitionId);
    }

    /**
     * 根据租户 ID 和 key 获取流程定义
     *
     * @return WorkflowDefinition
     *
     * @author wangweijun
     * @since 2024/6/17 15:23
     */
    public WorkflowDefinition getByTenantAndKey(String tenantId, String key) {
        final String selectSql = """
                SELECT * FROM awf_wf_definition WHERE `tenant_id` = ? AND `key` = ? AND `state` = 1
                """;
        return Try.of(() -> this.jdbcTemplate.queryForObject(selectSql, (rs, rowNum) -> {
            WorkflowDefinition wfd = WorkflowDefinition.of(rs);
            packageDefaultApprover(wfd);
            return wfd;
        }, tenantId, key)).getOrNull();
    }

    /**
     * 根据租户 ID 和 流程 ID 获取流程定义
     *
     * @return WorkflowDefinition
     *
     * @author wangweijun
     * @since 2024/12/13 14:54
     */
    public WorkflowDefinition getByTenantAndId(String tenantId, Integer id) {
        final String selectSql = """
                SELECT * FROM awf_wf_definition WHERE `tenant_id` = ? AND `id` = ? AND `state` = 1
                """;
        return Try.of(() -> this.jdbcTemplate.queryForObject(selectSql, (rs, rowNum) -> {
            WorkflowDefinition wfd = WorkflowDefinition.of(rs);
            packageDefaultApprover(wfd);
            return wfd;
        }, tenantId, id)).getOrNull();
    }

    /**
     * 根据租户 ID 获取流程定义列表
     *
     * @return WorkflowDefinition
     *
     * @author wangweijun
     * @since 2024/6/18 11:16
     */
    public List<WorkflowDefinition> findByTenantId() {
        return findByTenantId(workflowDefinition.getTenantId());
    }

    /**
     * 根据租户 ID 获取流程定义列表
     *
     * @return WorkflowDefinition
     *
     * @author wangweijun
     * @since 2024/6/18 11:16
     */
    public List<WorkflowDefinition> findByTenantId(String tenantId) {
        final String selectSql = """
                SELECT * FROM awf_wf_definition WHERE `tenant_id` = ? AND `state` = 1
                """;
        RowMapper<WorkflowDefinition> rowMapper = (ResultSet rs, int rowNum) -> WorkflowDefinition.of(rs);
        List<WorkflowDefinition> workflowDefinitions = jdbcTemplate.query(selectSql, rowMapper, tenantId).stream().toList();
        workflowDefinitions.forEach(this::packageDefaultApprover);
        return workflowDefinitions;
    }

    /**
     * 删除流程定义
     *
     * @author wangweijun
     * @since 2024/6/17 15:47
     */
    public void deleteByTenantAndKey() {
        deleteByTenantAndKey(workflowDefinition.getTenantId(), workflowDefinition.getKey());
    }

    /**
     * 删除流程定义
     *
     * @author wangweijun
     * @since 2024/6/17 15:47
     */
    public void deleteByTenantAndKey(String tenantId, String key) {
        String deleteSql = """
                DELETE FROM awf_wf_definition WHERE `tenant_id` = ? AND `key` = ?
                """;
        this.jdbcTemplate.update(deleteSql, tenantId, key);
    }

    /**
     * 禁用流程定义
     *
     * @author wangweijun
     * @since 2024/6/17 15:47
     */
    public void disable() {
        disable(workflowDefinition.getTenantId(), workflowDefinition.getKey(), workflowDefinition.getUpdatedBy());
    }

    /**
     * 禁用流程定义
     *
     * @author wangweijun
     * @since 2024/6/17 15:47
     */
    public void disable(String tenantId, String key, String updatedBy) {
        String updateSql = """
                UPDATE awf_wf_definition SET `state` = 0, `updated_at` = ?, `updated_by` = ?, `version` = `version` + 1 
                WHERE `tenant_id` = ? AND `key` = ?
                """;
        this.jdbcTemplate.update(updateSql, new Date(), updatedBy, tenantId, key);
    }

    /**
     * 启用流程定义
     *
     * @author wangweijun
     * @since 2024/6/17 15:47
     */
    public void enable() {
        enable(workflowDefinition.getTenantId(), workflowDefinition.getKey(), workflowDefinition.getUpdatedBy());
    }

    /**
     * 启用流程定义
     *
     * @author wangweijun
     * @since 2024/6/17 15:47
     */
    public void enable(String tenantId, String key, String updatedBy) {
        String updateSql = """
                UPDATE awf_wf_definition SET `state` = 1, `updated_at` = ?, `updated_by` = ?, `version` = `version` + 1 
                WHERE `tenant_id` = ? AND `key` = ?
                """;
        this.jdbcTemplate.update(updateSql, new Date(), updatedBy, tenantId, key);
    }

    /**
     * 根据流程定义 ID 查找流程定义
     *
     * @param workflowDefinitionId 流程定义 ID
     *
     * @return WorkflowDefinition
     *
     * @author wangweijun
     * @since 2024/9/10 14:49
     */
    public WorkflowDefinition getById(Integer workflowDefinitionId) {
        WorkflowDefinition wfd = super.getById(workflowDefinitionId);
        if (wfd == null) {
            return null;
        }
        packageDefaultApprover(wfd);
        return wfd;
    }

    /**
     * 根据流程定义 ID 查找默认审批人
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     *
     * @return Approver
     *
     * @author wangweijun
     * @since 2024/9/6 14:45
     */
    public Approver getDefaultApprover(String tenantId, Integer workflowDefinitionId) {
        WorkflowAssignment workflowAssignment = workflowAssignmentExecutor.getByWorkflowDefinitionId(tenantId, workflowDefinitionId);
        if (workflowAssignment != null) {
            return Approver.of(workflowAssignment.getApproverId(), workflowAssignment.getApproverName(), workflowAssignment.getApproverDesc());
        }
        return null;
    }

    /**
     * 设置默认审批人
     *
     * @param workflowDefinition 流程定义
     *
     * @author wangweijun
     * @since 2024/9/10 14:54
     */
    private void packageDefaultApprover(WorkflowDefinition workflowDefinition) {
        Approver defaultApprover = this.getDefaultApprover(workflowDefinition.getTenantId(), workflowDefinition.getId());
        workflowDefinition.setWhenEmptyApprover(defaultApprover);
    }
}
