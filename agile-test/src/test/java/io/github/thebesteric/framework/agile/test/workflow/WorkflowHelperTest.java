package io.github.thebesteric.framework.agile.test.workflow;

import io.github.thebesteric.framework.agile.plugins.database.core.domain.Page;
import io.github.thebesteric.framework.agile.plugins.workflow.WorkflowEngine;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.*;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.Condition;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.Conditions;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.RequestCondition;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.RequestConditions;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.definition.NodeDefinitionBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.response.TaskHistoryResponse;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.*;
import io.github.thebesteric.framework.agile.plugins.workflow.helper.WorkflowHelper;
import io.github.thebesteric.framework.agile.plugins.workflow.helper.service.*;
import io.github.thebesteric.framework.agile.plugins.workflow.service.WorkflowService;
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

        // workflowServiceHelper.createStartNode(workflowDefinition, "请假流程开始");
        // workflowServiceHelper.createTaskNode(NodeDefinitionBuilder.builderTaskNode(tenantId, workflowDefinition.getId(), 1)
        //         .name("部门主管审批").approverId("张三").approverId("李四"));
        // workflowServiceHelper.createTaskNode(NodeDefinitionBuilder.builderTaskNode(tenantId, workflowDefinition.getId(), 2)
        //         .name("部门经理审批").approverId("王五"));
        // workflowServiceHelper.createEndNode(workflowDefinition, "请假流程结束");

        createWorkflow4(tenantId, workflowDefinition);
    }

    private void createWorkflow4(String tenantId, WorkflowDefinition workflowDefinition) {
        WorkflowService workflowService = workflowEngine.getWorkflowService();
        NodeDefinition nodeDefinition = NodeDefinitionBuilder.builderStartNode(tenantId, workflowDefinition.getId())
                .name("请假流程开始").desc("开始节点").build();
        nodeDefinition = workflowService.createNode(nodeDefinition);
        System.out.println(nodeDefinition);

        Conditions conditions = Conditions.defaultConditions();
        conditions.addCondition(Condition.of("day", "3", Operator.LESS_THAN));
        nodeDefinition = NodeDefinitionBuilder.builderTaskNode(tenantId, workflowDefinition.getId(), 1)
                .name("部门主管审批").desc("任务节点").conditions(conditions).approveType(ApproveType.ANY)
                .approverId("张三").approverId("李四")
                .build();
        nodeDefinition = workflowService.createNode(nodeDefinition);
        System.out.println(nodeDefinition);

        conditions = Conditions.defaultConditions();
        conditions.addCondition(Condition.of("day", "3", Operator.GREATER_THAN_AND_EQUAL));
        nodeDefinition = NodeDefinitionBuilder.builderTaskNode(tenantId, workflowDefinition.getId(), 1)
                .name("部门经理审批").desc("任务节点").conditions(conditions)
                .approverId("王五")
                .build();
        nodeDefinition = workflowService.createNode(nodeDefinition);
        System.out.println(nodeDefinition);

        conditions = Conditions.defaultConditions();
        conditions.addCondition(Condition.of("day", "3", Operator.LESS_THAN));
        nodeDefinition = NodeDefinitionBuilder.builderTaskNode(tenantId, workflowDefinition.getId(), 2)
                .name("人事主管审批").desc("任务节点").conditions(conditions)
                .approverId("赵六")
                .build();
        nodeDefinition = workflowService.createNode(nodeDefinition);
        System.out.println(nodeDefinition);

        conditions = Conditions.defaultConditions();
        conditions.addCondition(Condition.of("day", "3", Operator.GREATER_THAN_AND_EQUAL));
        nodeDefinition = NodeDefinitionBuilder.builderTaskNode(tenantId, workflowDefinition.getId(), 2)
                .name("人事经理审批").desc("任务节点").conditions(conditions)
                .approverId("孙七")
                .build();
        nodeDefinition = workflowService.createNode(nodeDefinition);
        System.out.println(nodeDefinition);

        nodeDefinition = NodeDefinitionBuilder.builderEndNode(tenantId, workflowDefinition.getId())
                .name("请假流程结束").desc("结束节点").build();
        nodeDefinition = workflowService.createNode(nodeDefinition);
        System.out.println(nodeDefinition);
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
        // runtimeServiceHelper.start(workflowDefinition, userId, "123-123", "project", "申请请假 3 天");

        RequestConditions requestConditions = RequestConditions.newInstance();
        requestConditions.addRequestCondition(RequestCondition.of("day", "2"));
        WorkflowInstance workflowInstance = runtimeServiceHelper.start(workflowDefinition, userId, "123-123", "project", "申请请假 3 天", requestConditions);

        // 添加附件
        RepositoryServiceHelper repositoryServiceHelper = workflowHelper.getRepositoryServiceHelper();
        repositoryServiceHelper.addAttachment(workflowInstance, "123456", "test.txt", "txt", "/attachment/test.txt");
    }

    /**
     * 审批-同意
     */
    @Test
    void approve() {
        String approverId = "张三";
        // String approverId = "李四";
        // String approverId = "王五";
        // String approverId = "赵六";
        // String approverId = "孙七";
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

            // 查看附件
            RepositoryServiceHelper repositoryServiceHelper = workflowHelper.getRepositoryServiceHelper();
            List<WorkflowRepository> attachments = repositoryServiceHelper.findAttachments(taskInstance);
            attachments.forEach(System.out::println);
        });
    }

    /**
     * 审批-拒绝
     */
    @Test
    void reject() {
        String approverId = "张三";
        // String approverId = "李四";
        // String approverId = "王五";
        // String approverId = "赵六";
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
        String approverId = "张三";
        // String approverId = "李四";
        // String approverId = "王五";
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


    @Test
    void attachments() {
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        RepositoryServiceHelper repositoryServiceHelper = workflowHelper.getRepositoryServiceHelper();

        List<WorkflowRepository> attachments = repositoryServiceHelper.findAttachmentsByWorkflowDefinitionId(tenantId, 1);
        attachments.forEach(System.out::println);
        System.out.println("===");

        Page<WorkflowRepository> page = repositoryServiceHelper.findAttachmentsByWorkflowDefinitionId(tenantId, 1, 2, 2);
        System.out.println(page);
        System.out.println("===");

        attachments = repositoryServiceHelper.findAttachmentsByWorkflowInstanceId(tenantId, 8);
        attachments.forEach(System.out::println);
        System.out.println("===");

        page = repositoryServiceHelper.findAttachmentsByWorkflowInstanceId(tenantId, 8, 1, 2);
        System.out.println(page);
        System.out.println("===");

        attachments = repositoryServiceHelper.findAttachmentsByTaskInstanceId(tenantId, 23);
        attachments.forEach(System.out::println);
        System.out.println("===");

        page = repositoryServiceHelper.findAttachmentsByTaskInstanceId(tenantId, 24, 1, 2);
        System.out.println(page);
        System.out.println("===");

        Integer affect = repositoryServiceHelper.clearAttachmentsByWorkflowInstance(tenantId, 8);
        System.out.println(affect);
        System.out.println("===");

        affect = repositoryServiceHelper.clearAttachmentsByWorkflowDefinition(tenantId, 1);
        System.out.println(affect);

    }

}
