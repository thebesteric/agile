package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.instance;

import io.github.thebesteric.framework.agile.plugins.database.core.domain.Page;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.WorkflowStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractExecutor;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowInstance;
import io.vavr.control.Try;
import lombok.Getter;
import lombok.Setter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.stream.Collectors;

/**
 * WorkflowInstanceExecutor
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-24 10:14:01
 */
@Getter
@Setter
public class WorkflowInstanceExecutor extends AbstractExecutor<WorkflowInstance> {

    private WorkflowInstance workflowInstance;

    public WorkflowInstanceExecutor(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
        this.workflowInstance = new WorkflowInstance();
    }

    /**
     * 保存流程实例
     *
     * @author wangweijun
     * @since 2024/6/24 10:24
     */
    public WorkflowInstance save() {
        return super.save(workflowInstance);
    }

    /**
     * 根据流程定义 ID、发起人 ID、业务 ID 查询流程实例
     *
     * @param workflowDefinitionId 流程定义 ID
     * @param requesterId          发起人 ID
     * @param businessId           业务 ID
     *
     * @return WorkflowInstance
     *
     * @author wangweijun
     * @since 2024/6/24 10:40
     */
    public WorkflowInstance getByWorkflowDefinitionIdAndRequesterIdAndBusinessId(String tenantId, Integer workflowDefinitionId, String requesterId, String businessId) {
        final String selectSql = """
                SELECT * FROM awf_wf_instance WHERE `tenant_id` = ? AND `wf_def_id` = ? AND `requester_id` = ? AND `business_id` = ? AND `state` = 1
                """;
        return Try.of(() -> this.jdbcTemplate.queryForObject(selectSql, (rs, rowNum) -> WorkflowInstance.of(rs), tenantId, workflowDefinitionId, requesterId, businessId)).getOrNull();
    }


    /**
     * 根据发起人 ID 查询流程实例
     *
     * @param tenantId         租户 ID
     * @param requesterId      发起人 ID
     * @param workflowStatuses 流程状态
     * @param page             页码
     * @param pageSize         每页数量
     *
     * @return List<WorkflowInstance>
     *
     * @author wangweijun
     * @since 2024/6/27 12:40
     */
    public Page<WorkflowInstance> findByRequesterId(String tenantId, String requesterId, List<WorkflowStatus> workflowStatuses, Integer page, Integer pageSize) {
        String selectSql = """
                SELECT i.* FROM awf_wf_instance i left join awf_wf_definition d
                ON i.wf_def_id = d.id AND d.tenant_id = ?
                WHERE i.`requester_id` = ? AND i.`state` = 1
                """;
        if (workflowStatuses != null && !workflowStatuses.isEmpty()) {
            String codes = workflowStatuses.stream().map(WorkflowStatus::getCode).map(String::valueOf).collect(Collectors.joining(","));
            selectSql += " AND i.`status` in (" + codes + ")";
        }

        String countSql = "SELECT COUNT(*) FROM (" + selectSql + ") AS t";
        Integer count = this.jdbcTemplate.queryForObject(countSql, Integer.class, tenantId, requesterId);

        selectSql += " ORDER BY i.`id` DESC LIMIT ? OFFSET ?";
        Integer offset = (page - 1) * pageSize;
        List<WorkflowInstance> records = this.jdbcTemplate.query(selectSql, (rs, rowNum) -> WorkflowInstance.of(rs), tenantId, requesterId, pageSize, offset);

        return Page.of(page, pageSize, count == null ? 0 : count, records);
    }

    /**
     * 根据发起人 ID 查询流程实例
     *
     * @param tenantId       租户 ID
     * @param requesterId    发起人 ID
     * @param workflowStatus 流程状态
     *
     * @return List<WorkflowInstance>
     *
     * @author wangweijun
     * @since 2024/6/27 12:40
     */
    public Page<WorkflowInstance> findByRequesterId(String tenantId, String requesterId, WorkflowStatus workflowStatus) {
        return findByRequesterId(tenantId, requesterId, workflowStatus == null ? null : List.of(workflowStatus), 1, Integer.MAX_VALUE);
    }

    /**
     * 根据发起人 ID 查询流程实例
     *
     * @param tenantId    租户 ID
     * @param requesterId 发起人 ID
     *
     * @return List<WorkflowInstance>
     *
     * @author wangweijun
     * @since 2024/6/27 12:40
     */
    public List<WorkflowInstance> findByRequesterId(String tenantId, String requesterId) {
        Page<WorkflowInstance> page = findByRequesterId(tenantId, requesterId, null);
        return page.getRecords();
    }
}
