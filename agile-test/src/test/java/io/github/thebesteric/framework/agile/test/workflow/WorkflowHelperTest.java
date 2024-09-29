package io.github.thebesteric.framework.agile.test.workflow;

import io.github.thebesteric.framework.agile.commons.util.JsonUtils;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.Page;
import io.github.thebesteric.framework.agile.plugins.workflow.WorkflowEngine;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.*;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.*;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.definition.NodeDefinitionBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.definition.WorkflowDefinitionBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.response.TaskHistoryResponse;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.response.WorkflowInstanceApproveRecords;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.*;
import io.github.thebesteric.framework.agile.plugins.workflow.helper.WorkflowHelper;
import io.github.thebesteric.framework.agile.plugins.workflow.helper.service.*;
import io.github.thebesteric.framework.agile.plugins.workflow.service.WorkflowService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

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
    String workflowKey = "QJ-M-1";

    /**
     * 创建流程定义
     */
    @Test
    void deploy() {
        DeploymentServiceHelper deploymentServiceHelper = new WorkflowHelper(workflowEngine).getDeploymentServiceHelper();
        deploymentServiceHelper.setCurrentUser("admin");
        // 创建流程定义
        WorkflowDefinitionBuilder builder = WorkflowDefinitionBuilder.builder()
                .tenantId(tenantId)
                .key(workflowKey)
                .name("请假流程-1")
                // 连续审批模式
                .continuousApproveMode(ContinuousApproveMode.APPROVE_ALL)
                // 是否允许节点审批人为空的时候，自动通过
                .allowEmptyAutoApprove(false)
                // 当节点审批人为空的时候，使用的默认审批人
                .whenEmptyApprovers(Set.of(Approver.of("admin", "系统管理员"), Approver.of("admin-1", "系统管理员-1")))
                .allowRedo(true)
                .requiredComment(true)
                .type("日常办公流程");
        WorkflowDefinition workflowDefinition = deploymentServiceHelper.deploy(builder);
        System.out.println(workflowDefinition);
    }

    /**
     * 创建节点关系
     */
    @Test
    void createNode() {
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        DeploymentServiceHelper deploymentServiceHelper = workflowHelper.getDeploymentServiceHelper();
        WorkflowDefinition workflowDefinition = deploymentServiceHelper.getByKey(tenantId, workflowKey);

        WorkflowServiceHelper workflowServiceHelper = workflowHelper.getWorkflowServiceHelper();
        workflowServiceHelper.setCurrentUser("admin");

        // workflowServiceHelper.createStartNode(workflowDefinition, "请假流程开始");
        // workflowServiceHelper.createTaskNode(NodeDefinitionBuilder.builderTaskNode(tenantId, workflowDefinition.getId(), 1)
        //         .name("部门主管审批").approverId("张三").approverId("李四"));
        // workflowServiceHelper.createTaskNode(NodeDefinitionBuilder.builderTaskNode(tenantId, workflowDefinition.getId(), 2)
        //         .name("部门经理审批").approverId("王五"));
        // workflowServiceHelper.createEndNode(workflowDefinition, "请假流程结束");

        createWorkflow6(tenantId, workflowDefinition);
    }

    /** 多节点案例。*/
    private void createWorkflow1(String tenantId, WorkflowDefinition workflowDefinition) {
        WorkflowService workflowService = workflowEngine.getWorkflowService();
        NodeDefinition nodeDefinition = NodeDefinitionBuilder.builderStartNode(tenantId, workflowDefinition.getId())
                .name("请假流程开始").desc("开始节点").build();
        nodeDefinition = workflowService.createNode(nodeDefinition);
        System.out.println(nodeDefinition);

        nodeDefinition = NodeDefinitionBuilder.builderTaskNode(tenantId, workflowDefinition.getId(), 1)
                .name("部门主管审批").desc("任务节点").approveType(ApproveType.ALL)
                .approver(Approver.of("张三", "张三姓名")).approver(Approver.of("李四", "李四姓名"))
                .build();
        nodeDefinition = workflowService.createNode(nodeDefinition);
        System.out.println(nodeDefinition);

        nodeDefinition = NodeDefinitionBuilder.builderTaskNode(tenantId, workflowDefinition.getId(), 2)
                .name("部门经理审批").desc("任务节点").approveType(ApproveType.ANY)
                .approver(Approver.of("王五", "王五姓名"))
                .build();
        nodeDefinition = workflowService.createNode(nodeDefinition);
        System.out.println(nodeDefinition);

        nodeDefinition = NodeDefinitionBuilder.builderTaskNode(tenantId, workflowDefinition.getId(), 3)
                .name("总经理审批").desc("任务节点").approveType(ApproveType.ANY)
                .approver(Approver.of("赵六", "赵六姓名"))
                .build();
        nodeDefinition = workflowService.createNode(nodeDefinition);
        System.out.println(nodeDefinition);

        nodeDefinition = NodeDefinitionBuilder.builderEndNode(tenantId, workflowDefinition.getId())
                .name("请假流程结束").desc("结束节点").build();
        nodeDefinition = workflowService.createNode(nodeDefinition);
        System.out.println(nodeDefinition);
    }

    /** 条件审批案例 */
    private void createWorkflow2(String tenantId, WorkflowDefinition workflowDefinition) {
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

    /** 动态审批案例 */
    private void createWorkflow3(String tenantId, WorkflowDefinition workflowDefinition) {
        WorkflowService workflowService = workflowEngine.getWorkflowService();
        NodeDefinition nodeDefinition = NodeDefinitionBuilder.builderStartNode(tenantId, workflowDefinition.getId())
                .name("请假流程开始").desc("开始节点").build();
        nodeDefinition = workflowService.createNode(nodeDefinition);
        System.out.println(nodeDefinition);

        nodeDefinition = NodeDefinitionBuilder.builderTaskNode(tenantId, workflowDefinition.getId(), 1)
                .name("部门主管审批").desc("任务节点").approveType(ApproveType.ALL)
                .dynamicAssignmentApprovers(2) // 预设 2 个审批人
                .build();
        nodeDefinition = workflowService.createNode(nodeDefinition);
        System.out.println(nodeDefinition);

        nodeDefinition = NodeDefinitionBuilder.builderTaskNode(tenantId, workflowDefinition.getId(), 2)
                .name("部门经理审批").desc("任务节点").approveType(ApproveType.ANY)
                .dynamicAssignmentApprovers(1) // 预设 1 个审批人
                .build();
        nodeDefinition = workflowService.createNode(nodeDefinition);
        System.out.println(nodeDefinition);

        nodeDefinition = NodeDefinitionBuilder.builderEndNode(tenantId, workflowDefinition.getId())
                .name("请假流程结束").desc("结束节点").build();
        nodeDefinition = workflowService.createNode(nodeDefinition);
        System.out.println(nodeDefinition);
    }

    /** 动态审批（空节点自动审核，默认审批人）案例 */
    private void createWorkflow4(String tenantId, WorkflowDefinition workflowDefinition) {
        WorkflowService workflowService = workflowEngine.getWorkflowService();
        NodeDefinition nodeDefinition = NodeDefinitionBuilder.builderStartNode(tenantId, workflowDefinition.getId())
                .name("请假流程开始").desc("开始节点").build();
        nodeDefinition = workflowService.createNode(nodeDefinition);
        System.out.println(nodeDefinition);

        nodeDefinition = NodeDefinitionBuilder.builderTaskNode(tenantId, workflowDefinition.getId(), 1)
                .name("部门主管审批").desc("任务节点").approveType(ApproveType.ANY)
                .build();
        nodeDefinition = workflowService.createNode(nodeDefinition);
        System.out.println(nodeDefinition);

        nodeDefinition = NodeDefinitionBuilder.builderTaskNode(tenantId, workflowDefinition.getId(), 2)
                .name("部门经理审批").desc("任务节点").approveType(ApproveType.ANY)
                .approver(Approver.of("王五", "王五姓名"))
                .build();
        nodeDefinition = workflowService.createNode(nodeDefinition);
        System.out.println(nodeDefinition);

        nodeDefinition = NodeDefinitionBuilder.builderEndNode(tenantId, workflowDefinition.getId())
                .name("请假流程结束").desc("结束节点").build();
        nodeDefinition = workflowService.createNode(nodeDefinition);
        System.out.println(nodeDefinition);
    }

    /** 自动审批案例 */
    private void createWorkflow5(String tenantId, WorkflowDefinition workflowDefinition) {
        WorkflowService workflowService = workflowEngine.getWorkflowService();
        NodeDefinition nodeDefinition = NodeDefinitionBuilder.builderStartNode(tenantId, workflowDefinition.getId())
                .name("请假流程开始").desc("开始节点").build();
        nodeDefinition = workflowService.createNode(nodeDefinition);
        System.out.println(nodeDefinition);

        nodeDefinition = NodeDefinitionBuilder.builderTaskNode(tenantId, workflowDefinition.getId(), 1)
                .name("部门主管审批").desc("任务节点").approveType(ApproveType.ANY)
                .approverId("张三")
                .build();
        nodeDefinition = workflowService.createNode(nodeDefinition);
        System.out.println(nodeDefinition);

        nodeDefinition = NodeDefinitionBuilder.builderTaskNode(tenantId, workflowDefinition.getId(), 2)
                .name("部门经理审批").desc("任务节点").approveType(ApproveType.ANY)
                .approverId("李四")
                .build();
        nodeDefinition = workflowService.createNode(nodeDefinition);
        System.out.println(nodeDefinition);

        nodeDefinition = NodeDefinitionBuilder.builderTaskNode(tenantId, workflowDefinition.getId(), 3)
                .name("总经理审批").desc("任务节点").approveType(ApproveType.ANY)
                .approverId("张三")
                .build();
        nodeDefinition = workflowService.createNode(nodeDefinition);
        System.out.println(nodeDefinition);

        nodeDefinition = NodeDefinitionBuilder.builderTaskNode(tenantId, workflowDefinition.getId(), 4)
                .name("董事长审批").desc("任务节点").approveType(ApproveType.ANY)
                .approverId("张三")
                .build();
        nodeDefinition = workflowService.createNode(nodeDefinition);
        System.out.println(nodeDefinition);

        nodeDefinition = NodeDefinitionBuilder.builderEndNode(tenantId, workflowDefinition.getId())
                .name("请假流程结束").desc("结束节点").build();
        nodeDefinition = workflowService.createNode(nodeDefinition);
        System.out.println(nodeDefinition);
    }

    /** 角色审批案例 */
    private void createWorkflow6(String tenantId, WorkflowDefinition workflowDefinition) {
        WorkflowService workflowService = workflowEngine.getWorkflowService();
        NodeDefinition nodeDefinition = NodeDefinitionBuilder.builderStartNode(tenantId, workflowDefinition.getId())
                .name("请假流程开始").desc("开始节点").build();
        nodeDefinition = workflowService.createNode(nodeDefinition);
        System.out.println(nodeDefinition);


        Set<Approver> groupSet = new LinkedHashSet<>();
        groupSet.add(Approver.of("grouper-1", "组长1"));
        groupSet.add(Approver.of("grouper-2", "组长2"));
        groupSet.add(Approver.of("grouper-3", "组长3"));

        Set<Approver> manageSet = new LinkedHashSet<>();
        manageSet.add(Approver.of("manager-1", "经理1"));
        manageSet.add(Approver.of("manager-2", "经理2"));

        Set<Approver> majorSet = new LinkedHashSet<>();
        majorSet.add(Approver.of("major-1", "总监1"));
        majorSet.add(Approver.of("major-2", "总监2"));


        nodeDefinition = NodeDefinitionBuilder.builderTaskNode(tenantId, workflowDefinition.getId(), 1)
                .name("部门主管审批").desc("任务节点")
                .roleApprove(true)
                .roleApproveType(RoleApproveType.SEQ)
                .roleUserApproveType(RoleUserApproveType.ALL)
                .roleApprovers(List.of(RoleApprover.of("组长", groupSet), RoleApprover.of("经理", manageSet)))
                .build();
        nodeDefinition = workflowService.createNode(nodeDefinition);
        System.out.println(nodeDefinition);

        // nodeDefinition = NodeDefinitionBuilder.builderTaskNode(tenantId, workflowDefinition.getId(), 2)
        //         .name("总监审批").desc("任务节点")
        //         .roleApprove(true)
        //         .roleApproveType(RoleApproveType.ANY)
        //         .roleUserApproveType(RoleUserApproveType.SEQ)
        //         .roleApprovers(List.of(RoleApprover.of("总监", majorSet)))
        //         .build();
        // nodeDefinition = workflowService.createNode(nodeDefinition);
        // System.out.println(nodeDefinition);

        nodeDefinition = NodeDefinitionBuilder.builderTaskNode(tenantId, workflowDefinition.getId(), 3)
                .name("部门经理审批").desc("任务节点").approveType(ApproveType.ANY)
                .approverId("张三")
                .build();
        nodeDefinition = workflowService.createNode(nodeDefinition);
        System.out.println(nodeDefinition);

        nodeDefinition = NodeDefinitionBuilder.builderEndNode(tenantId, workflowDefinition.getId())
                .name("请假流程结束").desc("结束节点").build();
        nodeDefinition = workflowService.createNode(nodeDefinition);
        System.out.println(nodeDefinition);
    }

    /** 多角色条件审批案例 */
    private void createWorkflow7(String tenantId, WorkflowDefinition workflowDefinition) {
        Set<Approver> groupSet = new LinkedHashSet<>();
        groupSet.add(Approver.of("grouper-1", "组长1"));
        groupSet.add(Approver.of("grouper-2", "组长2"));
        groupSet.add(Approver.of("grouper-3", "组长3"));

        Set<Approver> manageSet = new LinkedHashSet<>();
        manageSet.add(Approver.of("manager-1", "经理1"));
        manageSet.add(Approver.of("manager-2", "经理2"));

        WorkflowService workflowService = workflowEngine.getWorkflowService();
        NodeDefinition nodeDefinition = NodeDefinitionBuilder.builderStartNode(tenantId, workflowDefinition.getId())
                .name("请假流程开始").desc("开始节点").build();
        nodeDefinition = workflowService.createNode(nodeDefinition);
        System.out.println(nodeDefinition);

        Conditions conditions = Conditions.defaultConditions();
        conditions.addCondition(Condition.of("day", "3", Operator.LESS_THAN));
        nodeDefinition = NodeDefinitionBuilder.builderTaskNode(tenantId, workflowDefinition.getId(), 1)
                .name("部门主管审批").desc("任务节点")
                .roleApprove(true)
                .roleApproveType(RoleApproveType.SEQ)
                .roleUserApproveType(RoleUserApproveType.ANY)
                .roleApprovers(RoleApprover.of("组长", groupSet))
                .conditions(conditions)
                .build();
        nodeDefinition = workflowService.createNode(nodeDefinition);
        System.out.println(nodeDefinition);

        conditions = Conditions.defaultConditions();
        conditions.addCondition(Condition.of("day", "3", Operator.GREATER_THAN_AND_EQUAL));
        nodeDefinition = NodeDefinitionBuilder.builderTaskNode(tenantId, workflowDefinition.getId(), 1)
                .name("部门经理审批").desc("任务节点")
                .roleApprove(true)
                .roleApproveType(RoleApproveType.SEQ)
                .roleUserApproveType(RoleUserApproveType.ALL)
                .roleApprovers(RoleApprover.of("经理", manageSet))
                .conditions(conditions)
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
        WorkflowDefinition workflowDefinition = deploymentServiceHelper.getByKey(tenantId, workflowKey);

        WorkflowServiceHelper workflowServiceHelper = workflowHelper.getWorkflowServiceHelper();
        workflowServiceHelper.setCurrentUser("admin");

        workflowServiceHelper.publish(workflowDefinition);
    }

    /**
     * 启动流程
     */
    @Test
    void start() {
        String userId = "eric";
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        DeploymentServiceHelper deploymentServiceHelper = workflowHelper.getDeploymentServiceHelper();
        WorkflowDefinition workflowDefinition = deploymentServiceHelper.getByKey(tenantId, workflowKey);

        RuntimeServiceHelper runtimeServiceHelper = workflowHelper.getRuntimeServiceHelper();
        runtimeServiceHelper.setCurrentUser(userId);
        // runtimeServiceHelper.start(workflowDefinition, userId, "123-123", "project", "申请请假 3 天");

        // // 预设：动态审批人
        // WorkflowServiceHelper workflowServiceHelper = workflowHelper.getWorkflowServiceHelper();
        // NodeDefinition firstTaskNode = workflowServiceHelper.getFirstTaskNode(tenantId, workflowDefinition.getId());
        // if (firstTaskNode.isUnSettingAssignmentApprovers()) {
        //     System.out.println("需要指定审批人数量：" + firstTaskNode.getApprovers());
        //     List<Approver> approvers = List.of(Approver.of("张三"), Approver.of("李四"));
        //     runtimeServiceHelper.dynamicAssignmentApprovers(tenantId, firstTaskNode.getId(), approvers);
        //     System.out.println("审批人设置完毕：" + approvers);
        // }


        RequestConditions requestConditions = RequestConditions.newInstance();
        requestConditions.addRequestCondition(RequestCondition.of("day", "4"));
        WorkflowInstance workflowInstance = runtimeServiceHelper.start(workflowDefinition, userId, "123-123", "project", "申请请假 3 天", requestConditions);

        // 添加附件
        RepositoryServiceHelper repositoryServiceHelper = workflowHelper.getRepositoryServiceHelper();
        repositoryServiceHelper.addAttachment(workflowInstance, "123456", "test.txt", "txt", "/attachment/test.txt");
    }

    @Test
    void combo() {
        deploy();
        createNode();
        publish();
        start();
    }

    /**
     * 审批-同意
     */
    @Test
    void approve() {
        String roleId = null;
        String approverId = "张三";
        // String approverId = "李四";
        // String approverId = "小明";
        // String approverId = "王五";
        // String approverId = "王五-1";
        // String approverId = "赵六";
        // String approverId = "孙七";
        // String approverId = "admin";
        // String approverId = "admin-1";

        // String roleId = "经理";
        // String approverId = "manager-1";
        // String approverId = "manager-2";

        // String roleId = "组长";
        // String approverId = "grouper-1";
        // String approverId = "grouper-2";
        // String approverId = "grouper-3";

        // String roleId = "总监";
        // String approverId = "major-1";
        // String approverId = "major-2";

        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        RuntimeServiceHelper runtimeServiceHelper = workflowHelper.getRuntimeServiceHelper();
        runtimeServiceHelper.setCurrentUser(approverId);

        // 查找待审批待实例
        Page<TaskInstance> page = runtimeServiceHelper.findTaskInstances(tenantId, null, roleId, approverId, NodeStatus.IN_PROGRESS, ApproveStatus.IN_PROGRESS, 1, 10);
        List<TaskInstance> taskInstances = page.getRecords();
        taskInstances.forEach(System.out::println);

        // int i = 1/0;

        taskInstances.forEach(taskInstance -> {
            String comment = "同意";
            runtimeServiceHelper.approve(taskInstance, roleId, approverId, comment);
            System.out.println(approverId + ": " + comment);

            // 查看附件
            RepositoryServiceHelper repositoryServiceHelper = workflowHelper.getRepositoryServiceHelper();
            List<WorkflowRepository> attachments = repositoryServiceHelper.findAttachments(taskInstance);
            attachments.forEach(System.out::println);


            // 当前生效的任务实例
            Integer workflowInstanceId = taskInstance.getWorkflowInstanceId();
            NodeDefinition inCurrentlyEffectNodeDefinition = runtimeServiceHelper.getInCurrentlyEffectNodeDefinition(tenantId, workflowInstanceId);
            if (inCurrentlyEffectNodeDefinition != null && inCurrentlyEffectNodeDefinition.isUnSettingAssignmentApprovers()) {
                Set<Approver> waitingToSetApprovers = inCurrentlyEffectNodeDefinition.getApprovers();
                List<Approver> actualApprovers = new ArrayList<>();
                int index = 1;
                for (Approver waitingToSetApprover : waitingToSetApprovers) {
                    actualApprovers.add(Approver.of("王五-" + index));
                    index++;
                }
                runtimeServiceHelper.dynamicAssignmentApprovers(tenantId, inCurrentlyEffectNodeDefinition.getId(), actualApprovers);
            }
        });
    }


    /**
     * 审批-撤回
     */
    @Test
    void redo() {
        // String roleId = null;
        // String approverId = "张三";
        // String approverId = "李四";
        // String approverId = "王五";
        // String approverId = "赵六";

        String roleId = "经理";
        String approverId = "manager-1";
        // String approverId = "manager-2";
        // String roleId = "组长";
        // String approverId = "grouper-1";
        // String approverId = "grouper-2";
        // String approverId = "grouper-3";
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        RuntimeServiceHelper runtimeServiceHelper = workflowHelper.getRuntimeServiceHelper();
        runtimeServiceHelper.setCurrentUser(approverId);

        // 查找待审批待实例
        Page<TaskInstance> page = runtimeServiceHelper.findTaskInstances(tenantId, 1, roleId == null ? null : List.of(roleId), approverId, new ArrayList<>(), List.of(ApproveStatus.APPROVED, ApproveStatus.ABANDONED), 1, 10);
        List<TaskInstance> taskInstances = page.getRecords();

        // int i = 1/0;

        taskInstances.forEach(taskInstance -> {
            runtimeServiceHelper.redo(taskInstance, roleId, approverId, "审批撤回");
        });
    }

    /**
     * 审批-拒绝
     */
    @Test
    void reject() {
        // String roleId = null;
        // String approverId = "张三";
        // String approverId = "李四";
        // String approverId = "王五";
        // String approverId = "赵六";

        String roleId = "经理";
        String approverId = "manager-1";
        // String approverId = "manager-2";

        // String roleId = "组长";
        // String approverId = "grouper-1";
        // String approverId = "grouper-2";
        // String approverId = "grouper-3";
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        RuntimeServiceHelper runtimeServiceHelper = workflowHelper.getRuntimeServiceHelper();
        runtimeServiceHelper.setCurrentUser(approverId);

        // 查找待审批待实例
        Page<TaskInstance> page = runtimeServiceHelper.findTaskInstances(tenantId, null, roleId, approverId, NodeStatus.IN_PROGRESS, ApproveStatus.IN_PROGRESS, 1, 10);
        List<TaskInstance> taskInstances = page.getRecords();
        taskInstances.forEach(System.out::println);

        // int i = 1/0;

        taskInstances.forEach(taskInstance -> {
            runtimeServiceHelper.reject(taskInstance, roleId, approverId, "不同意");
        });
    }

    /**
     * 审批-放弃
     */
    @Test
    void abandon() {


        // String approverId = "张三";
        // String approverId = "李四";
        // String approverId = "王五";
        // String approverId = "赵六";

        // String roleId = "经理";
        // String approverId = "manager-1";
        // String approverId = "manager-2";
        String roleId = "组长";
        // String approverId = "grouper-1";
        String approverId = "grouper-2";
        // String approverId = "grouper-3";

        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        RuntimeServiceHelper runtimeServiceHelper = workflowHelper.getRuntimeServiceHelper();
        runtimeServiceHelper.setCurrentUser(approverId);

        // 查找待审批待实例
        Page<TaskInstance> page = runtimeServiceHelper.findTaskInstances(tenantId, null, roleId, approverId, NodeStatus.IN_PROGRESS, ApproveStatus.IN_PROGRESS, 1, 10);
        List<TaskInstance> taskInstances = page.getRecords();
        taskInstances.forEach(System.out::println);

        taskInstances.forEach(taskInstance -> {
            runtimeServiceHelper.abandon(taskInstance, roleId, approverId, "放弃审批");
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

        Page<WorkflowInstance> page = runtimeServiceHelper.findWorkflowInstancesByRequestId(tenantId, requesterId, WorkflowStatus.IN_PROGRESS, 1, 10);
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

        nodeDefinition.setSequence(2.0);
        workflowServiceHelper.updateNode(nodeDefinition);

        List<NodeDefinition> nodeDefinitions = workflowServiceHelper.getNodes(tenantId, 2);
        for (NodeDefinition definition : nodeDefinitions) {
            definition.setSequence(1.0);
            workflowServiceHelper.updateNode(definition);
        }
    }

    /**
     * 插入节点
     */
    @Test
    void insertNode() {
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        WorkflowServiceHelper workflowServiceHelper = workflowHelper.getWorkflowServiceHelper();

        NodeDefinition nodeDefinition = NodeDefinitionBuilder.builderTaskNode(tenantId, 1)
                .name("新插入的节点").approver(Approver.of("小明", "李小明")).build();

        workflowServiceHelper.insertNode(nodeDefinition, 2, 3);
    }

    /**
     * 删除节点
     */
    @Test
    void deleteNode() {
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        WorkflowServiceHelper workflowServiceHelper = workflowHelper.getWorkflowServiceHelper();

        workflowServiceHelper.deleteNode(tenantId, 4);
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

    @Test
    void findWorkflowInstancesByApprover() {
        String approverId = "admin";
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        RuntimeServiceHelper runtimeServiceHelper = workflowHelper.getRuntimeServiceHelper();
        runtimeServiceHelper.setCurrentUser(approverId);

        DeploymentServiceHelper deploymentServiceHelper = workflowHelper.getDeploymentServiceHelper();

        Page<WorkflowInstance> page = runtimeServiceHelper.findWorkflowInstancesByApproverId(tenantId, approverId, WorkflowStatus.IN_PROGRESS, ApproveStatus.IN_PROGRESS, 1, 10);
        List<WorkflowInstance> workflowInstances = page.getRecords();
        workflowInstances.forEach(workflowInstance -> {
            WorkflowDefinition workflowDefinition = deploymentServiceHelper.getById(tenantId, workflowInstance.getWorkflowDefinitionId());
            System.out.printf("当前流程: %s, 当前实例 ID：%s\n", workflowDefinition.getName(), workflowInstance.getId());
        });
    }

    @Test
    void updateApprover() {
        String approverId = "admin";
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        RuntimeServiceHelper runtimeServiceHelper = workflowHelper.getRuntimeServiceHelper();
        runtimeServiceHelper.setCurrentUser(approverId);

        runtimeServiceHelper.updateApprover(tenantId, "admin", "test");
    }

    @Test
    void workflowInstanceApproveRecords() {
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        RuntimeServiceHelper runtimeServiceHelper = workflowHelper.getRuntimeServiceHelper();

        WorkflowInstanceApproveRecords workflowInstanceApproveRecords = runtimeServiceHelper.getWorkflowInstanceApproveRecords(tenantId, 1);
        System.out.println("===============================");
        System.out.println(JsonUtils.toJson(workflowInstanceApproveRecords));
    }

}
