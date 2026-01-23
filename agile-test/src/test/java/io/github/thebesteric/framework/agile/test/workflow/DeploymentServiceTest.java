package io.github.thebesteric.framework.agile.test.workflow;

import io.github.thebesteric.framework.agile.commons.util.JsonUtils;
import io.github.thebesteric.framework.agile.core.domain.page.PagingResponse;
import io.github.thebesteric.framework.agile.plugins.workflow.WorkflowEngine;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.definition.WorkflowDefinitionBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.response.WorkflowDefinitionFlowSchema;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowDefinition;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowDefinitionHistory;
import io.github.thebesteric.framework.agile.plugins.workflow.helper.WorkflowHelper;
import io.github.thebesteric.framework.agile.plugins.workflow.helper.service.DeploymentServiceHelper;
import io.github.thebesteric.framework.agile.plugins.workflow.service.DeploymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

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

    /**
     * 删除流程定义
     */
    @Test
    void delete() {
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        DeploymentServiceHelper deploymentServiceHelper = workflowHelper.getDeploymentServiceHelper();
        deploymentServiceHelper.delete(tenantId, key);
    }

    @Test
    void getById() {
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        DeploymentServiceHelper deploymentServiceHelper = workflowHelper.getDeploymentServiceHelper();
        WorkflowDefinition workflowDefinition = deploymentServiceHelper.getById(tenantId, 1);
        System.out.println(workflowDefinition);
    }

    @Test
    void getByKey() {
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        DeploymentServiceHelper deploymentServiceHelper = workflowHelper.getDeploymentServiceHelper();
        WorkflowDefinition workflowDefinition = deploymentServiceHelper.getByKey(tenantId, key);
        System.out.println(workflowDefinition);
    }

    /**
     * 获取所有流程定义
     */
    @Test
    void list() {
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        DeploymentServiceHelper deploymentServiceHelper = workflowHelper.getDeploymentServiceHelper();
        List<WorkflowDefinition> workflowDefinitions = deploymentServiceHelper.list(tenantId);
        for (WorkflowDefinition workflow : workflowDefinitions) {
            System.out.println(workflow);
        }
    }

    @Test
    void disable() {
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        DeploymentServiceHelper deploymentServiceHelper = workflowHelper.getDeploymentServiceHelper();
        deploymentServiceHelper.disable(tenantId, key);
    }

    @Test
    void enable() {
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        DeploymentServiceHelper deploymentServiceHelper = workflowHelper.getDeploymentServiceHelper();
        deploymentServiceHelper.enable(tenantId, key);
    }

    @Test
    void update() {
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        DeploymentServiceHelper deploymentServiceHelper = workflowHelper.getDeploymentServiceHelper();
        WorkflowDefinition workflowDefinition = deploymentServiceHelper.getByKey(tenantId, key);
        workflowDefinition.setTenantId("8888");
        workflowDefinition.setKey("test-key");
        workflowDefinition.setName("测试流程");
        workflowDefinition.setDesc("这是一个测试流程");
        deploymentServiceHelper.update(workflowDefinition);
    }

    @Test
    void schema() {
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        DeploymentServiceHelper deploymentServiceHelper = workflowHelper.getDeploymentServiceHelper();
        WorkflowDefinitionFlowSchema schema = deploymentServiceHelper.schema(tenantId, key);
        System.out.println(JsonUtils.toJson(schema));
    }

    @Test
    void history() {
        DeploymentService deploymentService = workflowEngine.getDeploymentService();
        PagingResponse<WorkflowDefinitionHistory> page = deploymentService.findHistories(tenantId, 1, 2);
        for (WorkflowDefinitionHistory history : page.getRecords()) {
            System.out.println(history);
        }
    }

}
