package io.github.thebesteric.framework.agile.test.controller;

import io.github.thebesteric.framework.agile.core.domain.R;
import io.github.thebesteric.framework.agile.plugins.logger.annotation.AgileLogger;
import io.github.thebesteric.framework.agile.plugins.workflow.WorkflowEngine;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.definition.WorkflowDefinitionBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowDefinition;
import io.github.thebesteric.framework.agile.plugins.workflow.service.DeploymentService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * WorkflowController
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-04 18:39:13
 */
@RestController
@RequestMapping("/workflow")
@AgileLogger
public class WorkflowController {

    @Resource
    private WorkflowEngine workflowEngine;

    @GetMapping("/createWorkflowDefinition")
    public R<WorkflowDefinition> createWorkflowDefinition() {
        workflowEngine.setCurrentUser("admin");
        DeploymentService deploymentService = workflowEngine.getDeploymentService();
        WorkflowDefinition workflowDefinition = WorkflowDefinitionBuilder.builder().tenantId("1").key("test-key").name("测试流程").type("测试").desc("这是一个测试流程").build();
        deploymentService.create(workflowDefinition);
        return R.success(workflowDefinition);
    }

}
