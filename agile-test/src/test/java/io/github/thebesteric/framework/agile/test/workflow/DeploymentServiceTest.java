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

    /**
     * 创建一个流程定义
     */
    @Test
    void create() {
        workflowEngine.setCurrentUser("admin");
        DeploymentService deploymentService = workflowEngine.getDeploymentService();
        WorkflowDefinition workflowDefinition = WorkflowDefinitionBuilder.builder().tenantId("1").key("test-key").name("测试流程").type("测试").desc("这是一个测试流程").build();
        WorkflowDefinition workflow = deploymentService.create(workflowDefinition);
        System.out.println(workflow);
    }

    @Test
    void delete() {
        DeploymentService deploymentService = workflowEngine.getDeploymentService();
        deploymentService.delete("1", "test-key");
    }

    @Test
    void list() {
        DeploymentService deploymentService = workflowEngine.getDeploymentService();
        for (WorkflowDefinition workflow : deploymentService.find("1")) {
            System.out.println(workflow);
        }
    }

    @Test
    void disable() {
        DeploymentService deploymentService = workflowEngine.getDeploymentService();
        deploymentService.disable("1", "test-key");
    }

    @Test
    void enable() {
        DeploymentService deploymentService = workflowEngine.getDeploymentService();
        deploymentService.enable("1", "test-key");
    }

    @Test
    void update() {
        String tenantId = "8888-1";
        DeploymentService deploymentService = workflowEngine.getDeploymentService();
        WorkflowDefinition workflowDefinition = deploymentService.getByKey(tenantId, "test-key");
        workflowDefinition.setTenantId("8888");
        workflowDefinition.setKey("test-key");
        workflowDefinition.setName("测试流程");
        workflowDefinition.setDesc("这是一个测试流程");
        deploymentService.update(workflowDefinition);
    }

}
