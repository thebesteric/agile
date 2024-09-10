package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.assignment;

import io.github.thebesteric.framework.agile.commons.exception.DataExistsException;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowAssignment;
import io.vavr.control.Try;
import lombok.Getter;
import lombok.Setter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.util.List;

/**
 * WorkflowAssignmentExecutor
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-09-10 12:06:00
 */
@Getter
@Setter
public class WorkflowAssignmentExecutor extends AbstractExecutor<WorkflowAssignment> {

    private WorkflowAssignment workflowAssignment;

    public WorkflowAssignmentExecutor(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
        this.workflowAssignment = new WorkflowAssignment();
    }

    /**
     * 保存流程定义
     *
     * @author wangweijun
     * @since 2024/9/10 12:06
     */
    public WorkflowAssignment save() {
        // 检查是否有已存在的流程定义
        WorkflowAssignment existsWorkflowAssignment = this.getByWorkflowDefinitionIdAndUserId();
        if (existsWorkflowAssignment != null) {
            throw new DataExistsException("已存在相同的流程审批人");
        }
        return super.save(workflowAssignment);
    }

    /**
     * 根据流程定义 ID 和用户 ID 获取审批人
     *
     * @return WorkflowAssignment
     *
     * @author wangweijun
     * @since 2024/9/10 12:06
     */
    public WorkflowAssignment getByWorkflowDefinitionIdAndUserId() {
        return this.getByWorkflowDefinitionIdAndUserId(workflowAssignment.getTenantId(), workflowAssignment.getWorkflowDefinitionId(), workflowAssignment.getUserId());
    }

    /**
     * 根据流程定义 ID 和用户 ID 获取审批人
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     * @param userId               用户 ID
     *
     * @return WorkflowAssignment
     *
     * @author wangweijun
     * @since 2024/9/10 12:06
     */
    public WorkflowAssignment getByWorkflowDefinitionIdAndUserId(String tenantId, Integer workflowDefinitionId, String userId) {
        final String selectSql = """
                SELECT * FROM awf_wf_assignment WHERE `tenant_id` = ? AND `wf_def_id` = ? AND `user_id` = ? AND `state` = 1
                """;
        return Try.of(() -> this.jdbcTemplate.queryForObject(selectSql, (rs, rowNum) -> WorkflowAssignment.of(rs), tenantId, workflowDefinitionId, userId)).getOrNull();
    }

    /**
     * 根据流程定义 ID 查找所有审批人
     *
     * @return List<WorkflowAssignment>
     *
     * @author wangweijun
     * @since 2024/9/10 12:06
     */
    public List<WorkflowAssignment> findByWorkflowDefinitionId() {
        return this.findByWorkflowDefinitionId(workflowAssignment.getTenantId(), workflowAssignment.getWorkflowDefinitionId());
    }

    /**
     * 根据流程定义 ID 查找所有审批人
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     *
     * @return List<WorkflowAssignment>
     *
     * @author wangweijun
     * @since 2024/9/10 12:06
     */
    public List<WorkflowAssignment> findByWorkflowDefinitionId(String tenantId, Integer workflowDefinitionId) {
        final String selectSql = """
                SELECT * FROM awf_wf_assignment WHERE `tenant_id` = ? AND `wf_def_id` = ? AND `state` = 1 ORDER BY `user_seq` ASC
                """;
        RowMapper<WorkflowAssignment> rowMapper = (ResultSet rs, int rowNum) -> WorkflowAssignment.of(rs);
        return jdbcTemplate.query(selectSql, rowMapper, tenantId, workflowDefinitionId).stream().toList();
    }

    /**
     * 根据流程定义 ID 删除所有审批人
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     *
     * @author wangweijun
     * @since 2024/9/10 12:06
     */
    public void deleteByWorkflowDefinitionId(String tenantId, Integer workflowDefinitionId) {
        final String deleteSql = """
                DELETE FROM awf_wf_assignment WHERE `tenant_id` = ? AND `wf_def_id` = ?
                """;
        jdbcTemplate.update(deleteSql, tenantId, workflowDefinitionId);
    }
}
