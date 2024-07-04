package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.definition;

import io.github.thebesteric.framework.agile.commons.exception.DataExistsException;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowDefinition;
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

    public WorkflowDefinitionExecutor(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
        this.workflowDefinition = new WorkflowDefinition();
    }

    /**
     * 保存流程定义
     *
     * @author wangweijun
     * @since 2024/6/17 15:24
     */
    public WorkflowDefinition save() {
        // 检查是否有已存在的流程定义
        WorkflowDefinition existsWorkflowDefinition = this.getByTenantAndKey();
        if (existsWorkflowDefinition != null) {
            throw new DataExistsException("WorkflowDefinition already exists");
        }
        return super.save(workflowDefinition);
    }

    /**
     * 根据租户 ID 和 key 获取流程定义
     *
     * @return WorkflowDefinition
     *
     * @author wangweijun
     * @since 2024/6/17 15:23
     */
    public WorkflowDefinition getByTenantAndKey() {
        return getByTenantAndKey(workflowDefinition.getTenantId(), workflowDefinition.getKey());
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
        return Try.of(() -> this.jdbcTemplate.queryForObject(selectSql, (rs, rowNum) -> WorkflowDefinition.of(rs), tenantId, key)).getOrNull();
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
        return jdbcTemplate.query(selectSql, rowMapper, tenantId).stream().toList();
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
}
