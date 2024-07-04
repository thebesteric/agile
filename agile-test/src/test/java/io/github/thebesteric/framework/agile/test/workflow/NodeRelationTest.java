package io.github.thebesteric.framework.agile.test.workflow;

import io.github.thebesteric.framework.agile.plugins.workflow.WorkflowEngine;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowDefinition;
import io.github.thebesteric.framework.agile.plugins.workflow.service.DeploymentService;
import io.github.thebesteric.framework.agile.plugins.workflow.service.WorkflowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class NodeRelationTest {

    @Autowired
    WorkflowEngine workflowEngine;

    @Test
    void create() {
        String tenantId = "8888";
        DeploymentService deploymentService = workflowEngine.getDeploymentService();
        WorkflowDefinition workflow = deploymentService.get(tenantId, "test-key");

        WorkflowService workflowService = workflowEngine.getWorkflowService();
        workflowService.createRelations(tenantId, workflow.getId());
    }
}
