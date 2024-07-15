package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.repository;

import io.github.thebesteric.framework.agile.plugins.database.core.domain.Page;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.builder.Query;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.builder.QueryBuilderWrapper;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * WorkflowInstanceExecutor
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-24 10:14:01
 */
@Getter
@Setter
public class WorkflowRepositoryExecutor extends AbstractExecutor<WorkflowRepository> {

    private WorkflowRepository workflowRepository;

    public WorkflowRepositoryExecutor(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
        this.workflowRepository = new WorkflowRepository();
    }

    /**
     * 根据流程定义 ID 清空附件
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     *
     * @return 影响的行数
     *
     * @author wangweijun
     * @since 2024/7/15 14:43
     */
    public Integer clearAttachmentByWorkflowDefinitionId(String tenantId, Integer workflowDefinitionId) {
        String deleteSql = """
                DELETE FROM awf_wf_repository
                WHERE `id` IN (
                    SELECT t.id FROM (
                        SELECT r.id FROM awf_wf_repository r
                            LEFT JOIN awf_wf_instance i ON i.id = r.wf_inst_id
                            LEFT JOIN awf_wf_definition d ON d.id = i.wf_def_id
                        WHERE r.tenant_id = ? AND d.id = ?        
                    ) t
                )
                """;
        return this.jdbcTemplate.update(deleteSql, tenantId, workflowDefinitionId);
    }

    /**
     * 根据流程实例 ID 查询附件列表
     *
     * @param tenantId           租户 ID
     * @param workflowInstanceId 流程实例 ID
     *
     * @return Page<WorkflowRepository>
     *
     * @author wangweijun
     * @since 2024/7/15 12:03
     */
    public Page<WorkflowRepository> findByWorkflowInstanceId(String tenantId, Integer workflowInstanceId, Integer page, Integer pageSize) {
        String selectSql = """
                SELECT * FROM awf_wf_repository WHERE `tenant_id` = ? AND `wf_inst_id` = ? AND `state` = 1
                """;
        String countSql = "SELECT COUNT(*) FROM (" + selectSql + ") AS t";
        Integer count = this.jdbcTemplate.queryForObject(countSql, Integer.class, tenantId, workflowInstanceId);

        selectSql += " ORDER BY `id` DESC LIMIT ? OFFSET ?";
        Integer offset = (page - 1) * pageSize;
        List<WorkflowRepository> records = this.jdbcTemplate.query(selectSql, (rs, rowNum) -> WorkflowRepository.of(rs), tenantId, workflowInstanceId, pageSize, offset);

        return Page.of(page, pageSize, count == null ? 0 : count, records);
    }

    /**
     * 根据流程定义 ID 查询附件列表
     *
     * @param tenantId             租户 ID
     * @param workflowDefinitionId 流程定义 ID
     *
     * @return Page<WorkflowRepository>
     *
     * @author wangweijun
     * @since 2024/7/15 12:03
     */
    public Page<WorkflowRepository> findByWorkflowDefinitionId(String tenantId, Integer workflowDefinitionId, Integer page, Integer pageSize) {
        String selectSql = """
                SELECT r.* FROM awf_wf_repository r 
                LEFT JOIN awf_wf_instance i ON i.id = r.wf_inst_id 
                LEFT JOIN awf_wf_definition d ON d.id = i.wf_def_id 
                WHERE r.`tenant_id` = ? AND i.`wf_def_id` = ? AND r.`state` = 1
                """;
        String countSql = "SELECT COUNT(*) FROM (" + selectSql + ") AS t";
        Integer count = this.jdbcTemplate.queryForObject(countSql, Integer.class, tenantId, workflowDefinitionId);

        selectSql += " ORDER BY `id` DESC LIMIT ? OFFSET ?";
        Integer offset = (page - 1) * pageSize;
        List<WorkflowRepository> records = this.jdbcTemplate.query(selectSql, (rs, rowNum) -> WorkflowRepository.of(rs), tenantId, workflowDefinitionId, pageSize, offset);

        return Page.of(page, pageSize, count == null ? 0 : count, records);
    }

    /**
     * 根据流程定义 ID 查询附件列表
     *
     * @param tenantId       租户 ID
     * @param taskInstanceId 任务实例 ID
     *
     * @return Page<WorkflowRepository>
     *
     * @author wangweijun
     * @since 2024/7/15 12:03
     */
    public Page<WorkflowRepository> findAttachmentsByTaskInstanceId(String tenantId, Integer taskInstanceId, Integer page, Integer pageSize) {
        String selectSql = """
                SELECT r.* FROM awf_wf_repository r 
                LEFT JOIN awf_task_instance i ON i.wf_inst_id = r.wf_inst_id 
                WHERE r.`tenant_id` = ? AND i.`id` = ? AND r.`state` = 1
                """;
        String countSql = "SELECT COUNT(*) FROM (" + selectSql + ") AS t";
        Integer count = this.jdbcTemplate.queryForObject(countSql, Integer.class, tenantId, taskInstanceId);

        selectSql += " ORDER BY `id` DESC LIMIT ? OFFSET ?";
        Integer offset = (page - 1) * pageSize;
        List<WorkflowRepository> records = this.jdbcTemplate.query(selectSql, (rs, rowNum) -> WorkflowRepository.of(rs), tenantId, taskInstanceId, pageSize, offset);

        return Page.of(page, pageSize, count == null ? 0 : count, records);
    }

    /**
     * 查询所有附件列表
     *
     * @param tenantId 租户 ID
     *
     * @return Page<WorkflowRepository>
     *
     * @author wangweijun
     * @since 2024/7/15 12:03
     */
    public Page<WorkflowRepository> findAll(String tenantId, Integer page, Integer pageSize) {
        Query query = QueryBuilderWrapper.createLambda(WorkflowRepository.class)
                .eq(WorkflowRepository::getTenantId, tenantId)
                .eq(WorkflowRepository::getState, 1)
                .page(page, pageSize).build();
        return this.find(query);
    }
}
