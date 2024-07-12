package io.github.thebesteric.framework.agile.test.workflow;

import io.github.thebesteric.framework.agile.plugins.database.core.domain.Page;
import io.github.thebesteric.framework.agile.plugins.workflow.WorkflowEngine;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.ApproveStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.NodeStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.WorkflowStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.definition.NodeDefinitionBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.response.TaskHistoryResponse;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.NodeDefinition;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.TaskInstance;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowDefinition;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowInstance;
import io.github.thebesteric.framework.agile.plugins.workflow.helper.WorkflowHelper;
import io.github.thebesteric.framework.agile.plugins.workflow.helper.service.DeploymentServiceHelper;
import io.github.thebesteric.framework.agile.plugins.workflow.helper.service.RuntimeServiceHelper;
import io.github.thebesteric.framework.agile.plugins.workflow.helper.service.TaskHistoryServiceHelper;
import io.github.thebesteric.framework.agile.plugins.workflow.helper.service.WorkflowServiceHelper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * WorkflowHelperTest
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-09 09:45:29
 */
@SpringBootTest
class WorkflowHelperTest {
    @Resource
    private WorkflowEngine workflowEngine;

    String tenantId = "8888";
    String workflowKey = "QJ-M";

    /**
     * 创建流程定义
     */
    @Test
    void deploy() {
        DeploymentServiceHelper deploymentServiceHelper = new WorkflowHelper(workflowEngine).getDeploymentServiceHelper();
        deploymentServiceHelper.setCurrentUser("admin");
        // 创建流程定义
        deploymentServiceHelper.deploy(tenantId, "请假流程", workflowKey, "日常办公流程");
        WorkflowDefinition workflowDefinition = deploymentServiceHelper.get(tenantId, workflowKey);
        System.out.println(workflowDefinition);
    }

    /**
     * 创建节点关系
     */
    @Test
    void createNode() {
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        DeploymentServiceHelper deploymentServiceHelper = workflowHelper.getDeploymentServiceHelper();
        WorkflowDefinition workflowDefinition = deploymentServiceHelper.get(tenantId, workflowKey);

        WorkflowServiceHelper workflowServiceHelper = workflowHelper.getWorkflowServiceHelper();
        workflowServiceHelper.setCurrentUser("admin");

        workflowServiceHelper.createStartNode(workflowDefinition, "请假流程开始");
        workflowServiceHelper.createTaskNode(NodeDefinitionBuilder.builderTaskNode(tenantId, workflowDefinition.getId(), 1)
                .name("部门主管审批").approverId("张三").approverId("李四"));
        workflowServiceHelper.createTaskNode(NodeDefinitionBuilder.builderTaskNode(tenantId, workflowDefinition.getId(), 2)
                .name("部门经理审批").approverId("王五"));
        workflowServiceHelper.createEndNode(workflowDefinition, "请假流程结束");
    }

    /**
     * 发布流程
     */
    @Test
    void publish() {
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        DeploymentServiceHelper deploymentServiceHelper = workflowHelper.getDeploymentServiceHelper();
        WorkflowDefinition workflowDefinition = deploymentServiceHelper.get(tenantId, workflowKey);

        WorkflowServiceHelper workflowServiceHelper = workflowHelper.getWorkflowServiceHelper();
        workflowServiceHelper.setCurrentUser("admin");

        workflowServiceHelper.publish(workflowDefinition);
    }

    /**
     * 发布流程
     */
    @Test
    void start() {
        String userId = "eric";
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        DeploymentServiceHelper deploymentServiceHelper = workflowHelper.getDeploymentServiceHelper();
        WorkflowDefinition workflowDefinition = deploymentServiceHelper.get(tenantId, workflowKey);

        RuntimeServiceHelper runtimeServiceHelper = workflowHelper.getRuntimeServiceHelper();
        runtimeServiceHelper.setCurrentUser(userId);
        runtimeServiceHelper.start(workflowDefinition, userId, "123-123", "project", "申请请假 3 天");
    }

    /**
     * 审批-同意
     */
    @Test
    void approve() {
        // String approverId = "张三";
        // String approverId = "李四";
        String approverId = "王五";
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        RuntimeServiceHelper runtimeServiceHelper = workflowHelper.getRuntimeServiceHelper();
        runtimeServiceHelper.setCurrentUser(approverId);

        // 查找待审批待实例
        Page<TaskInstance> page = runtimeServiceHelper.findTaskInstances(tenantId, approverId, NodeStatus.IN_PROGRESS, ApproveStatus.IN_PROGRESS, 1, 10);
        List<TaskInstance> taskInstances = page.getRecords();
        taskInstances.forEach(System.out::println);

        taskInstances.forEach(taskInstance -> {
            String comment = "同意";
            runtimeServiceHelper.approve(taskInstance, approverId, comment);
            System.out.println(approverId + ": " + comment);
        });
    }

    /**
     * 审批-拒绝
     */
    @Test
    void reject() {
        // String approverId = "张三";
        // String approverId = "李四";
        String approverId = "王五";
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        RuntimeServiceHelper runtimeServiceHelper = workflowHelper.getRuntimeServiceHelper();
        runtimeServiceHelper.setCurrentUser(approverId);

        // 查找待审批待实例
        Page<TaskInstance> page = runtimeServiceHelper.findTaskInstances(tenantId, approverId, NodeStatus.IN_PROGRESS, ApproveStatus.IN_PROGRESS, 1, 10);
        List<TaskInstance> taskInstances = page.getRecords();
        taskInstances.forEach(System.out::println);

        taskInstances.forEach(taskInstance -> {
            runtimeServiceHelper.reject(taskInstance, approverId, "不同意");
        });
    }

    /**
     * 审批-拒绝
     */
    @Test
    void abandon() {
        // String approverId = "张三";
        // String approverId = "李四";
        String approverId = "王五";
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        RuntimeServiceHelper runtimeServiceHelper = workflowHelper.getRuntimeServiceHelper();
        runtimeServiceHelper.setCurrentUser(approverId);

        // 查找待审批待实例
        Page<TaskInstance> page = runtimeServiceHelper.findTaskInstances(tenantId, approverId, NodeStatus.IN_PROGRESS, ApproveStatus.IN_PROGRESS, 1, 10);
        List<TaskInstance> taskInstances = page.getRecords();
        taskInstances.forEach(System.out::println);

        taskInstances.forEach(taskInstance -> {
            runtimeServiceHelper.abandon(taskInstance, approverId, "放弃审批");
        });
    }

    /**
     * 审批-拒绝
     */
    @Test
    void cancel() {
        String requesterId = "eric";
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        RuntimeServiceHelper runtimeServiceHelper = workflowHelper.getRuntimeServiceHelper();
        runtimeServiceHelper.setCurrentUser(requesterId);

        Page<WorkflowInstance> page = runtimeServiceHelper.findWorkflowInstances(tenantId, requesterId, WorkflowStatus.IN_PROGRESS, 1, 10);
        List<WorkflowInstance> workflowInstances = page.getRecords();
        workflowInstances.forEach(workflowInstance -> {
            runtimeServiceHelper.cancel(workflowInstance);
            System.out.println("取消: " + workflowInstance.getCreatedBy() + " - " + workflowInstance.getDesc());
        });
    }

    /**
     * 审批-日志
     */
    @Test
    void history() {
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        TaskHistoryServiceHelper taskHistoryServiceHelper = workflowHelper.getTaskHistoryServiceHelper();

        Page<TaskHistoryResponse> taskHistories = taskHistoryServiceHelper.findTaskHistories(tenantId, 5, null, 1, 10);
        taskHistories.getRecords().forEach(System.out::println);
    }

    @Test
    void updateNode() {
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        WorkflowServiceHelper workflowServiceHelper = workflowHelper.getWorkflowServiceHelper();

        NodeDefinition nodeDefinition = workflowServiceHelper.getNode(tenantId, 3);
        // nodeDefinition.setName("部门经理审批");
        // nodeDefinition.setDesc("任务节点");
        // nodeDefinition.removeApprovers("测试审批人");

        nodeDefinition.setSequence(2);
        workflowServiceHelper.update(nodeDefinition);

        List<NodeDefinition> nodeDefinitions = workflowServiceHelper.getNodes(tenantId, 2);
        for (NodeDefinition definition : nodeDefinitions) {
            definition.setSequence(1);
            workflowServiceHelper.update(definition);
        }
    }



}
