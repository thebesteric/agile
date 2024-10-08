package io.github.thebesteric.framework.agile.test.workflow;

import io.github.thebesteric.framework.agile.plugins.workflow.WorkflowEngine;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.definition.WorkflowDefinitionBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowDefinition;
import io.github.thebesteric.framework.agile.plugins.workflow.service.DeploymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DeploymentServiceTest {

    @Autowired
    WorkflowEngine workflowEngine;

    private final String tenantId = "8888";
    private final String key = "QJ-M-1";

    /**
     * 创建一个流程定义
     */
    @Test
    void create() {
        workflowEngine.setCurrentUser("admin");
        DeploymentService deploymentService = workflowEngine.getDeploymentService();
        WorkflowDefinition workflowDefinition = WorkflowDefinitionBuilder.builder().tenantId(tenantId).key(key)
                .name("测试流程").type("测试").desc("这是一个测试流程").build();
        WorkflowDefinition workflow = deploymentService.create(workflowDefinition);
        System.out.println(workflow);
    }

    @Test
    void delete() {
        DeploymentService deploymentService = workflowEngine.getDeploymentService();
        deploymentService.delete(tenantId, key);
    }

    @Test
    void list() {
        DeploymentService deploymentService = workflowEngine.getDeploymentService();
        for (WorkflowDefinition workflow : deploymentService.find(tenantId)) {
            System.out.println(workflow);
        }
    }

    @Test
    void disable() {
        DeploymentService deploymentService = workflowEngine.getDeploymentService();
        deploymentService.disable(tenantId, key);
    }

    @Test
    void enable() {
        DeploymentService deploymentService = workflowEngine.getDeploymentService();
        deploymentService.enable(tenantId, key);
    }

    @Test
    void update() {
        DeploymentService deploymentService = workflowEngine.getDeploymentService();
        WorkflowDefinition workflowDefinition = deploymentService.getByKey(tenantId, key);
        workflowDefinition.setTenantId("8888");
        workflowDefinition.setKey("test-key");
        workflowDefinition.setName("测试流程");
        workflowDefinition.setDesc("这是一个测试流程");
        deploymentService.update(workflowDefinition);
    }

}
