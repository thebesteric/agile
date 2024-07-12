package io.github.thebesteric.framework.agile.test.workflow;

import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.OrderByOperator;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.builder.Query;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.builder.QueryBuilderWrapper;
import io.github.thebesteric.framework.agile.plugins.workflow.WorkflowEngine;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowDefinition;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowInstance;
import io.github.thebesteric.framework.agile.plugins.workflow.service.WorkflowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * WorkflowServiceTest
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-28 18:00:34
 */
@SpringBootTest
class WorkflowServiceTest {

    @Autowired
    WorkflowEngine workflowEngine;

    @Test
    void findWorkflowDefinitions() {
        String tenantId = "8888";
        WorkflowService workflowService = workflowEngine.getWorkflowService();
        Query query = QueryBuilderWrapper.createLambda(WorkflowDefinition.class).eq(WorkflowDefinition::getTenantId, tenantId)
                .page(1, 10).build();
        List<WorkflowDefinition> workflowInstances = workflowService.findWorkflowDefinitions(query).getRecords();
        workflowInstances.forEach(System.out::println);
    }

    @Test
    void findWorkflowInstances() {
        String tenantId = "8888";
        WorkflowService workflowService = workflowEngine.getWorkflowService();
        Query query = QueryBuilderWrapper.createLambda(WorkflowInstance.class).eq(WorkflowInstance::getTenantId, tenantId)
                .like(WorkflowInstance::getBusinessId, "123%").orderBy(WorkflowInstance::getCreatedAt, OrderByOperator.DESC)
                .page(1, 10)
                .build();
        List<WorkflowInstance> workflowInstances = workflowService.findWorkflowInstances(query).getRecords();
        workflowInstances.forEach(System.out::println);
    }
}
