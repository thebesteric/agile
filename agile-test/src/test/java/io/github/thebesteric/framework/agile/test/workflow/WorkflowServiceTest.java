package io.github.thebesteric.framework.agile.test.workflow;

import io.github.thebesteric.framework.agile.commons.util.MapWrapper;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.OrderByOperator;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.builder.Query;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.query.builder.QueryBuilderWrapper;
import io.github.thebesteric.framework.agile.plugins.workflow.WorkflowEngine;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.BusinessInfo;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.Requester;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowDefinition;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowInstance;
import io.github.thebesteric.framework.agile.plugins.workflow.helper.WorkflowHelper;
import io.github.thebesteric.framework.agile.plugins.workflow.helper.service.DeploymentServiceHelper;
import io.github.thebesteric.framework.agile.plugins.workflow.helper.service.RuntimeServiceHelper;
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
                .page(1L, 10L).build();
        List<WorkflowDefinition> workflowInstances = workflowService.findWorkflowDefinitions(query).getRecords();
        workflowInstances.forEach(System.out::println);
    }

    @Test
    void findWorkflowInstances() {
        String tenantId = "8888";
        WorkflowService workflowService = workflowEngine.getWorkflowService();
        Query query = QueryBuilderWrapper.createLambda(WorkflowInstance.class).eq(WorkflowInstance::getTenantId, tenantId)
                .orderBy(WorkflowInstance::getCreatedAt, OrderByOperator.DESC)
                .page(1L, 10L)
                .build();
        List<WorkflowInstance> workflowInstances = workflowService.findWorkflowInstances(query).getRecords();
        workflowInstances.forEach(System.out::println);
    }

    @Test
    void submit() {
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);

        DeploymentServiceHelper deploymentServiceHelper = workflowHelper.getDeploymentServiceHelper();
        WorkflowDefinition workflowDefinition = deploymentServiceHelper.getById("8888", 1);

        RuntimeServiceHelper runtimeServiceHelper = workflowHelper.getRuntimeServiceHelper();
        Requester requester = Requester.of("1000", "eric");
        BusinessInfo businessInfo = BusinessInfo.of(MapWrapper.create().put("businessId", 1000L).put("businessType", 1).build());
        WorkflowInstance workflowInstance = runtimeServiceHelper.start(workflowDefinition, requester, businessInfo, "测试审批");

        System.out.println(workflowInstance);
    }
}
