package io.github.thebesteric.framework.agile.test.workflow;

import io.github.thebesteric.framework.agile.commons.util.JsonUtils;
import io.github.thebesteric.framework.agile.commons.util.MapWrapper;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.Page;
import io.github.thebesteric.framework.agile.plugins.workflow.WorkflowEngine;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.*;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.*;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.definition.NodeDefinitionBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.workflow.definition.WorkflowDefinitionBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.response.TaskHistoryResponse;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.response.WorkflowDefinitionFlowSchema;
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
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        DeploymentServiceHelper deploymentServiceHelper = workflowHelper.getDeploymentServiceHelper();
        deploymentServiceHelper.setCurrentUser("admin");
        // 创建流程定义
        WorkflowDefinitionBuilder builder = WorkflowDefinitionBuilder.builder()
                .tenantId(tenantId)
                .key(workflowKey)
                .name("请假流程-1")
                // 连续审批模式
                .continuousApproveMode(ContinuousApproveMode.APPROVE_CONTINUOUS)
                // 没有条件节点符合时的处理策略
                .conditionNotMatchedAnyStrategy(ConditionNotMatchedAnyStrategy.PROCESS_CONTINUE_TO_NEXT)
                // 是否允许节点审批人为空的时候，自动通过
                .allowEmptyAutoApprove(false)
                // 当节点审批人为空的时候，使用的默认审批人
                .whenEmptyApprover(Approver.of("admin", "系统管理员", "系统管理员描述"))
                .allowRedo(true)
                .requiredComment(true)
                .type("日常办公流程");
        WorkflowDefinition workflowDefinition = deploymentServiceHelper.deploy(builder);
        System.out.println(workflowDefinition);
    }

    /**
     * 禁用流程定义
     *
     * @author wangweijun
     * @since 2024/10/22 13:29
     */
    @Test
    void disable() {
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        DeploymentServiceHelper deploymentServiceHelper = workflowHelper.getDeploymentServiceHelper();

        deploymentServiceHelper.disable(tenantId, workflowKey);
        // deploymentServiceHelper.enable(tenantId, workflowKey);
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

    /** 多节点案例。 */
    private void createWorkflow1(String tenantId, WorkflowDefinition workflowDefinition) {
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        WorkflowServiceHelper workflowServiceHelper = workflowHelper.getWorkflowServiceHelper();

        NodeDefinitionBuilder nodeDefinitionBuilder = NodeDefinitionBuilder.builderStartNode(tenantId, workflowDefinition.getId())
                .name("请假流程开始").desc("开始节点");
        NodeDefinition nodeDefinition = workflowServiceHelper.createStartNode(nodeDefinitionBuilder);
        System.out.println(nodeDefinition);

        nodeDefinitionBuilder = NodeDefinitionBuilder.builderTaskNode(tenantId, workflowDefinition.getId(), 1)
                .name("部门主管审批").desc("任务节点").approveType(ApproveType.ALL)
                .approver(Approver.of("张三", "张三姓名", "张三备注")).approver(Approver.of("李四", "李四姓名", "李四备注"));
        nodeDefinition = workflowServiceHelper.createTaskNode(nodeDefinitionBuilder);
        System.out.println(nodeDefinition);

        nodeDefinitionBuilder = NodeDefinitionBuilder.builderTaskNode(tenantId, workflowDefinition.getId(), 2)
                .name("部门经理审批").desc("任务节点").approveType(ApproveType.ANY)
                .approver(Approver.of("王五", "王五姓名"));
        nodeDefinition = workflowServiceHelper.createTaskNode(nodeDefinitionBuilder);
        System.out.println(nodeDefinition);

        nodeDefinitionBuilder = NodeDefinitionBuilder.builderTaskNode(tenantId, workflowDefinition.getId(), 3)
                .name("总经理审批").desc("任务节点").approveType(ApproveType.ANY)
                .approver(Approver.of("赵六", "赵六姓名"));
        nodeDefinition = workflowServiceHelper.createTaskNode(nodeDefinitionBuilder);
        System.out.println(nodeDefinition);

        nodeDefinitionBuilder = NodeDefinitionBuilder.builderEndNode(tenantId, workflowDefinition.getId())
                .name("请假流程结束").desc("结束节点");
        nodeDefinition = workflowServiceHelper.createEndNode(nodeDefinitionBuilder);
        System.out.println(nodeDefinition);
    }

    /** 条件审批案例 */
    private void createWorkflow2(String tenantId, WorkflowDefinition workflowDefinition) {
        WorkflowService workflowService = workflowEngine.getWorkflowService();
        NodeDefinition nodeDefinition = NodeDefinitionBuilder.builderStartNode(tenantId, workflowDefinition.getId())
                .name("请假流程开始").desc("开始节点").build();
        nodeDefinition = workflowService.createNode(nodeDefinition);
        System.out.println(nodeDefinition);


        Conditions conditions = Conditions.newInstance(LogicOperator.AND, 1);
        conditions.addCondition(Condition.of("day", "1", Operator.GREATER_THAN_AND_EQUAL, "请假日期大于 1 天"));
        conditions.addCondition(Condition.of("day", "3", Operator.LESS_THAN, "请假日期小于 3 天"));
        nodeDefinition = NodeDefinitionBuilder.builderTaskNode(tenantId, workflowDefinition.getId(), 1)
                .name("部门组长审批").desc("任务节点").conditions(conditions).approveType(ApproveType.ANY)
                .approverId("张三")
                .build();
        nodeDefinition = workflowService.createNode(nodeDefinition);
        System.out.println(nodeDefinition);

        conditions = Conditions.newInstance(LogicOperator.AND, 2);
        conditions.addCondition(Condition.of("day", "3", Operator.GREATER_THAN_AND_EQUAL, "请假日期大于等于 3 天"));
        conditions.addCondition(Condition.of("day", "5", Operator.LESS_THAN, "请假日期小于 5 天"));
        nodeDefinition = NodeDefinitionBuilder.builderTaskNode(tenantId, workflowDefinition.getId(), 1)
                .name("部门主管审批").desc("任务节点").conditions(conditions)
                .approverId("李四")
                .build();
        nodeDefinition = workflowService.createNode(nodeDefinition);
        System.out.println(nodeDefinition);

        conditions = Conditions.newInstance(LogicOperator.AND, 3);
        conditions.addCondition(Condition.of("day", "5", Operator.GREATER_THAN_AND_EQUAL, "请假日期大于等于 5 天"));
        nodeDefinition = NodeDefinitionBuilder.builderTaskNode(tenantId, workflowDefinition.getId(), 1)
                .name("部门经理审批").desc("任务节点").conditions(conditions)
                .approverId("王五")
                .build();
        nodeDefinition = workflowService.createNode(nodeDefinition);
        System.out.println(nodeDefinition);

        nodeDefinition = NodeDefinitionBuilder.builderTaskNode(tenantId, workflowDefinition.getId(), 2)
                .name("部门总监审批").desc("任务节点")
                .approverId("哈哈")
                .build();
        nodeDefinition = workflowService.createNode(nodeDefinition);
        System.out.println(nodeDefinition);

        conditions = Conditions.defaultConditions();
        conditions.addCondition(Condition.of("day", "1", Operator.GREATER_THAN_AND_EQUAL, "请假日期大于 1 天"));
        conditions.addCondition(Condition.of("day", "3", Operator.LESS_THAN, "请假日期小于 3 天"));
        nodeDefinition = NodeDefinitionBuilder.builderTaskNode(tenantId, workflowDefinition.getId(), 3)
                .name("人事主管审批").desc("任务节点").conditions(conditions)
                .approverId("赵六")
                .build();
        nodeDefinition = workflowService.createNode(nodeDefinition);
        System.out.println(nodeDefinition);

        conditions = Conditions.defaultConditions();
        conditions.addCondition(Condition.of("day", "3", Operator.GREATER_THAN_AND_EQUAL, "请假日期大于等于 3 天"));
        conditions.addCondition(Condition.of("day", "5", Operator.LESS_THAN, "请假日期小于 5 天"));
        nodeDefinition = NodeDefinitionBuilder.builderTaskNode(tenantId, workflowDefinition.getId(), 3)
                .name("人事经理审批").desc("任务节点").conditions(conditions)
                .approverId("孙七")
                .build();
        nodeDefinition = workflowService.createNode(nodeDefinition);
        System.out.println(nodeDefinition);

        nodeDefinition = NodeDefinitionBuilder.builderTaskNode(tenantId, workflowDefinition.getId(), 4)
                .name("总经理审批").desc("任务节点")
                .approverId("嘿嘿")
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
                .name("部门主管审批").desc("任务节点").approveType(ApproveType.SEQ)
                .dynamicAssignmentApproversNum(-1) // 预设 2 个审批人
                .build();
        nodeDefinition = workflowService.createNode(nodeDefinition);
        System.out.println(nodeDefinition);

        nodeDefinition = NodeDefinitionBuilder.builderTaskNode(tenantId, workflowDefinition.getId(), 2)
                .name("部门经理审批").desc("任务节点").approveType(ApproveType.ANY)
                .dynamicAssignmentApproversNum(1) // 预设 1 个审批人
                .build();
        nodeDefinition = workflowService.createNode(nodeDefinition);
        System.out.println(nodeDefinition);

        nodeDefinition = NodeDefinitionBuilder.builderEndNode(tenantId, workflowDefinition.getId())
                .name("请假流程结束").desc("结束节点").build();
        nodeDefinition = workflowService.createNode(nodeDefinition);
        System.out.println(nodeDefinition);
    }

    /** 默认审批人（空节点自动审核，默认审批人）案例 */
    private void createWorkflow4(String tenantId, WorkflowDefinition workflowDefinition) {
        WorkflowService workflowService = workflowEngine.getWorkflowService();
        NodeDefinition nodeDefinition = NodeDefinitionBuilder.builderStartNode(tenantId, workflowDefinition.getId())
                .name("请假流程开始").desc("开始节点").build();
        nodeDefinition = workflowService.createNode(nodeDefinition);
        System.out.println(nodeDefinition);

        // 空节点：allowEmptyAutoApprove 必须为 true，表示默认通过，即：空审批岗
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
        Set<Approver> majorSet = new LinkedHashSet<>();
        majorSet.add(Approver.of("major-1", "负责人1", "负责人1-备注"));
        majorSet.add(Approver.of("major-2", "负责人2", "负责人2-备注"));

        Set<Approver> groupSet = new LinkedHashSet<>();
        groupSet.add(Approver.of("grouper-1", "组长1", "组长1-备注"));
        groupSet.add(Approver.of("grouper-2", "组长2", "组长2-备注"));
        groupSet.add(Approver.of("grouper-3", "组长3", "组长3-备注"));

        Set<Approver> manageSet = new LinkedHashSet<>();
        manageSet.add(Approver.of("manager-1", "经理1", "经理1-备注"));
        manageSet.add(Approver.of("manager-2", "经理2", "经理2-备注"));

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
                .roleApprovers(RoleApprover.of(
                        Map.of(
                                RoleApprover.ApproverRole.of("组长", "组长名称", "组长备注"), groupSet,
                                RoleApprover.ApproverRole.of("负责人", "负责人名称", "负责人备注"), majorSet)))
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
                .roleApprovers(RoleApprover.of("经理", "经理角色名称", "经理角色备注", manageSet))
                .conditions(conditions)
                .build();
        nodeDefinition = workflowService.createNode(nodeDefinition);
        System.out.println(nodeDefinition);

        conditions = Conditions.defaultConditions();
        conditions.addCondition(Condition.of("day", "3", Operator.LESS_THAN));
        nodeDefinition = NodeDefinitionBuilder.builderTaskNode(tenantId, workflowDefinition.getId(), 2)
                .name("人事主管审批").desc("任务节点").conditions(conditions)
                .approver(Approver.of("赵六", "赵六名称", "赵六备注"))
                .build();
        nodeDefinition = workflowService.createNode(nodeDefinition);
        System.out.println(nodeDefinition);

        conditions = Conditions.defaultConditions();
        conditions.addCondition(Condition.of("day", "3", Operator.GREATER_THAN_AND_EQUAL));
        nodeDefinition = NodeDefinitionBuilder.builderTaskNode(tenantId, workflowDefinition.getId(), 2)
                .name("人事经理审批").desc("任务节点").conditions(conditions)
                .approver(Approver.of("孙七", "孙七名称", "孙七备注"))
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
        String userName = "老王";
        String userDesc = "这个一个申请人";
        Requester requester = Requester.of(userId, userName, userDesc);
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        DeploymentServiceHelper deploymentServiceHelper = workflowHelper.getDeploymentServiceHelper();
        WorkflowDefinition workflowDefinition = deploymentServiceHelper.getByKey(tenantId, workflowKey);

        RuntimeServiceHelper runtimeServiceHelper = workflowHelper.getRuntimeServiceHelper();
        runtimeServiceHelper.setCurrentUser(userId);
        // runtimeServiceHelper.start(workflowDefinition, userId, "123-123", "project", "申请请假 3 天");

        // 动态审批节点的情况下，预设：动态审批人
        WorkflowServiceHelper workflowServiceHelper = workflowHelper.getWorkflowServiceHelper();
        NodeDefinition firstTaskNode = workflowServiceHelper.getFirstTaskNode(tenantId, workflowDefinition.getId());
        List<Approver> dynamicApprovers = new ArrayList<>();
        if (firstTaskNode.isDynamic()) {
            Integer dynamicAssignmentNum = firstTaskNode.getDynamicAssignmentNum();
            System.out.println("需要指定审批人数量：" + dynamicAssignmentNum);
            // 未指定动态审批人数量
            if (dynamicAssignmentNum == -1) {
                dynamicApprovers.add(Approver.of("张三", "张三姓名", "张三备注"));
                dynamicApprovers.add(Approver.of("李四", "李四姓名", "李四备注"));
            }
            // 指定了动态审批人数量
            else {
                for (int i = 1; i <= dynamicAssignmentNum; i++) {
                    dynamicApprovers.add(Approver.of("张三-" + i, "张三姓名-" + i, "张三备注-" + i));
                }
            }
            System.out.println("审批人设置完毕：" + dynamicApprovers);
        }

        RequestConditions requestConditions = RequestConditions.newInstance();
        requestConditions.addRequestCondition(RequestCondition.of("day", "2"));

        BusinessInfo businessInfo = BusinessInfo.of(MapWrapper.create().put("business_id", 123).put("business_type", "项目资料").build());
        WorkflowInstance workflowInstance = runtimeServiceHelper.start(workflowDefinition, requester, businessInfo, "申请请假 3 天", requestConditions, dynamicApprovers);

        // 添加附件
        RepositoryServiceHelper repositoryServiceHelper = workflowHelper.getRepositoryServiceHelper();
        repositoryServiceHelper.addAttachment(workflowInstance, "文本", "123456", "test.txt", "txt", "/attachment/test.txt");
    }

    @Test
    void combo() {
        deploy();
        createNode();
        publish();
        start();
    }

    /**
     * 审批-强制中断
     */
    @Test
    void interrupt() {
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        RuntimeServiceHelper runtimeServiceHelper = workflowHelper.getRuntimeServiceHelper();
        runtimeServiceHelper.setCurrentUser("system-user");

        runtimeServiceHelper.interrupt(tenantId, 1, "强制中断");
    }

    /**
     * 审批-同意
     */
    @Test
    void approve() {
        // String roleId = "xxx";
        // String approverId = "张三";
        // String approverId = "张三-1";
        // String approverId = "李四";
        // String approverId = "小明";
        // String approverId = "王五";
        // String approverId = "王五-1";
        // String approverId = "哈哈";
        // String approverId = "赵六";
        // String approverId = "孙七";
        // String approverId = "嘿嘿";
        // String approverId = "admin";
        // String approverId = "admin-1";

        // String roleId = "经理";
        // String approverId = "manager-1";
        // String approverId = "manager-2";

        String roleId = "组长";
        // String approverId = "grouper-1";
        String approverId = "grouper-2";
        // String approverId = "grouper-3";

        // String roleId = "负责人";
        // String approverId = "major-1";
        // String approverId = "major-2";

        // String roleId = "总监";
        // String approverId = "major-1";
        // String approverId = "major-2";

        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        RuntimeServiceHelper runtimeServiceHelper = workflowHelper.getRuntimeServiceHelper();
        runtimeServiceHelper.setCurrentUser(approverId);

        // ApproveDatesSegmentCondition approveDatesSegmentCondition = new ApproveDatesSegmentCondition()
        //         .setSubmitStartDate("2024-10-09 14:59:33")
        //         .setSubmitEndDate("2024-10-09 15:59:33")
        //         .setApproveStartDate("2024-10-09 14:59:34")
        //         .setApproveEndDate("2024-10-09 14:59:34");

        // List<NodeStatus> nodeStatuses = List.of(NodeStatus.IN_PROGRESS, NodeStatus.COMPLETED, NodeStatus.CANCELED, NodeStatus.REJECTED);
        // List<ApproveStatus> approveStatuses = List.of(ApproveStatus.IN_PROGRESS, ApproveStatus.REJECTED);

        List<NodeStatus> nodeStatuses = List.of(NodeStatus.IN_PROGRESS);
        List<ApproveStatus> approveStatuses = List.of(ApproveStatus.IN_PROGRESS);

        // 查找待审批待实例
        Page<TaskInstance> page = runtimeServiceHelper.findTaskInstances(tenantId, null, List.of(roleId), approverId, nodeStatuses, approveStatuses, null, 1, 10);
        List<TaskInstance> taskInstances = page.getRecords();
        taskInstances.forEach(System.out::println);

        // int ex = 1/0;

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
            TaskInstance inCurrentlyEffectTaskInstance = runtimeServiceHelper.getInCurrentlyEffectTaskInstance(tenantId, workflowInstanceId);
            if (inCurrentlyEffectTaskInstance == null) {
                return;
            }
            boolean dynamicNodeAndUnSettingApprovers = runtimeServiceHelper.isDynamicNodeAndUnSettingApprovers(tenantId, inCurrentlyEffectTaskInstance.getId());
            if (dynamicNodeAndUnSettingApprovers) {
                NodeDefinition inCurrentlyEffectNodeDefinition = runtimeServiceHelper.getInCurrentlyEffectNodeDefinition(tenantId, workflowInstanceId);
                List<Approver> actualApprovers = new ArrayList<>();
                Integer dynamicAssignmentNum = inCurrentlyEffectNodeDefinition.getDynamicAssignmentNum();
                if (dynamicAssignmentNum == -1) {
                    actualApprovers.add(Approver.of("王五-1", "王五-1-姓名", "王五-1-备注"));
                    actualApprovers.add(Approver.of("王五-2", "王五-2-姓名", "王五-2-备注"));
                } else {
                    for (int i = 1; i <= dynamicAssignmentNum; i++) {
                        actualApprovers.add(Approver.of("王五-" + i, "王五-" + i + "-姓名", "王五-" + i + "-备注"));
                    }
                }
                runtimeServiceHelper.dynamicAssignmentApprovers(tenantId, inCurrentlyEffectNodeDefinition.getId(), inCurrentlyEffectTaskInstance.getId(), actualApprovers);
            }
        });
    }


    /**
     * 审批-撤回
     */
    @Test
    void redo() {
        String roleId = null;
        // String approverId = "张三";
        String approverId = "李四";
        // String approverId = "王五";
        // String approverId = "赵六";

        // String roleId = "经理";
        // String approverId = "manager-1";
        // String approverId = "manager-2";
        // String roleId = "组长";
        // String approverId = "grouper-1";
        // String approverId = "grouper-2";
        // String approverId = "grouper-3";
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        RuntimeServiceHelper runtimeServiceHelper = workflowHelper.getRuntimeServiceHelper();
        runtimeServiceHelper.setCurrentUser(approverId);

        // 查找待审批待实例
        Page<TaskInstance> page = runtimeServiceHelper.findTaskInstances(tenantId, 1, roleId == null ? null : List.of(roleId), approverId, new ArrayList<>(), List.of(ApproveStatus.APPROVED, ApproveStatus.ABANDONED), null, 1, 10);
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
        String roleId = null;
        // String approverId = "张三";
        String approverId = "李四";
        // String approverId = "王五";
        // String approverId = "赵六";

        // String roleId = "经理";
        // String approverId = "manager-1";
        // String approverId = "manager-2";

        // String roleId = "组长";
        // String approverId = "grouper-1";
        // String approverId = "grouper-2";
        // String approverId = "grouper-3";
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        RuntimeServiceHelper runtimeServiceHelper = workflowHelper.getRuntimeServiceHelper();
        runtimeServiceHelper.setCurrentUser(approverId);

        // 查找待审批待实例
        Page<TaskInstance> page = runtimeServiceHelper.findTaskInstances(tenantId, null, roleId, approverId, NodeStatus.IN_PROGRESS, ApproveStatus.IN_PROGRESS, null, 1, 10);
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
        // String roleId = null;
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
        Page<TaskInstance> page = runtimeServiceHelper.findTaskInstances(tenantId, null, roleId, approverId, NodeStatus.IN_PROGRESS, ApproveStatus.IN_PROGRESS, null, 1, 10);
        List<TaskInstance> taskInstances = page.getRecords();
        taskInstances.forEach(System.out::println);

        taskInstances.forEach(taskInstance -> {
            runtimeServiceHelper.abandon(taskInstance, roleId, approverId, "放弃审批");
        });
    }

    /**
     * 审批-取消提交（发起者在未有节点开始审批的时候）
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

        Page<TaskHistoryResponse> taskHistories = taskHistoryServiceHelper.findTaskHistories(tenantId, 1, 1, 20);
        taskHistories.getRecords().forEach(System.out::println);
    }

    @Test
    void publishAndStart() {
        publish();
        start();
    }

    @Test
    void updateNode() {
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        WorkflowServiceHelper workflowServiceHelper = workflowHelper.getWorkflowServiceHelper();

        NodeDefinition nodeDefinition2 = workflowServiceHelper.getNode(tenantId, 2);
        nodeDefinition2.setName("部门经理审批-1");
        nodeDefinition2.setDesc("任务节点-1");
        nodeDefinition2.removeApprover(Approver.of("张三"));
        nodeDefinition2.addApprover(Approver.of("张三-1"));
        nodeDefinition2.setSequence(3.0);
        workflowServiceHelper.updateNode(nodeDefinition2);

        NodeDefinition nodeDefinition3 = workflowServiceHelper.getNode(tenantId, 4);
        nodeDefinition3.setSequence(1.0);
        workflowServiceHelper.updateNode(nodeDefinition3);

        // NodeDefinition nodeDefinition2 = workflowServiceHelper.getNode(tenantId, 2);
        // nodeDefinition2.setRoleApprove(false);
        // nodeDefinition2.getApprovers().clear();
        // nodeDefinition2.setApprovers(Set.of(Approver.of("小明", "李小明")));
        // nodeDefinition2.setApproveType(ApproveType.ANY);
        // nodeDefinition2.setName("部门主管审批-1");
        // nodeDefinition2.setDesc("任务节点-1");
        // workflowServiceHelper.updateNode(nodeDefinition2);
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

    /**
     * 流程实例审批记录
     *
     * @author wangweijun
     * @since 2024/10/9 14:57
     */
    @Test
    void workflowInstanceApproveRecords() {
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        RuntimeServiceHelper runtimeServiceHelper = workflowHelper.getRuntimeServiceHelper();

        List<WorkflowInstanceApproveRecords> workflowInstanceApproveRecords = runtimeServiceHelper.findWorkflowInstanceApproveRecords(tenantId, 1, List.of("经理"), "manager-1");
        System.out.println("===============================");
        System.out.println(JsonUtils.toJson(workflowInstanceApproveRecords));
        WorkflowInstanceApproveRecords workflowInstanceApproveRecord = runtimeServiceHelper.getWorkflowInstanceApproveRecords(tenantId, 2, List.of("经理", "组长"), "manager-1");
        System.out.println("===============================");
        System.out.println(JsonUtils.toJson(workflowInstanceApproveRecord));
    }

    /**
     * 查看流程定义纲要
     *
     * @author wangweijun
     * @since 2024/10/9 14:53
     */
    @Test
    void workflowDefinitionFlowSchema() {
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        DeploymentServiceHelper deploymentServiceHelper = workflowHelper.getDeploymentServiceHelper();

        WorkflowDefinitionFlowSchema workflowDefinitionFlowSchema = deploymentServiceHelper.schema(tenantId, 1);
        System.out.println("===============================");
        System.out.println(JsonUtils.toJson(workflowDefinitionFlowSchema));

        RuntimeServiceHelper runtimeServiceHelper = workflowHelper.getRuntimeServiceHelper();
        WorkflowDefinitionFlowSchema schema = runtimeServiceHelper.schema(tenantId, 1);
        System.out.println("===============================");
        System.out.println(JsonUtils.toJson(schema));
    }

    /**
     * 获取角色审批用户的角色 ID
     *
     * @author wangweijun
     * @since 2024/10/10 20:11
     */
    @Test
    void roleId() {
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        RuntimeServiceHelper runtimeServiceHelper = workflowHelper.getRuntimeServiceHelper();

        System.out.println(runtimeServiceHelper.getRoleIdByTaskInstanceId(tenantId, 2, "grouper-1"));
        System.out.println(runtimeServiceHelper.getRoleIdByTaskInstanceId(tenantId, 2, "major-1"));
        System.out.println(runtimeServiceHelper.getRoleIdByTaskInstanceId(tenantId, 2, "张三"));
    }

    /**
     * 获取正在生效的任务实例和节点定义
     *
     * @author wangweijun
     * @since 2024/10/11 10:33
     */
    @Test
    void getInCurrentlyEffect() {
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        RuntimeServiceHelper runtimeServiceHelper = workflowHelper.getRuntimeServiceHelper();

        // 获取当前流程实例下正在生效的任务实例
        TaskInstance taskInstance = runtimeServiceHelper.getInCurrentlyEffectTaskInstance(tenantId, 1);
        System.out.println("taskInstance = " + taskInstance);

        // 获取当前流程实例下正在生效的任务实例对应的节点定义
        NodeDefinition nodeDefinition = runtimeServiceHelper.getInCurrentlyEffectNodeDefinition(tenantId, 1);
        System.out.println("nodeDefinition = " + nodeDefinition);
    }

    /**
     * 根据节点定义获取审批人列表
     *
     * @author wangweijun
     * @since 2024/10/11 10:33
     */
    @Test
    void findApprovers() {
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        WorkflowServiceHelper workflowServiceHelper = workflowHelper.getWorkflowServiceHelper();

        List<Approver> approvers = workflowServiceHelper.findApprovers(tenantId, 3);
        System.out.println(JsonUtils.toJson(approvers));

        List<RoleApprover> roleApprovers = workflowServiceHelper.findRoleApprovers(tenantId, 3);
        System.out.println(JsonUtils.toJson(roleApprovers));
    }

    /**
     * 获取当前流程实例下正在生效的审批人列表
     *
     * @author wangweijun
     * @since 2024/10/11 14:34
     */
    @Test
    void findInCurrentlyEffectApprovers() {
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        RuntimeServiceHelper runtimeServiceHelper = workflowHelper.getRuntimeServiceHelper();

        List<Approver> inCurrentlyEffectApprovers = runtimeServiceHelper.findInCurrentlyEffectApprovers(tenantId, 1);
        System.out.println(JsonUtils.toJson(inCurrentlyEffectApprovers));

        List<RoleApprover> inCurrentlyEffectRoleApprovers = runtimeServiceHelper.findInCurrentlyEffectRoleApprovers(tenantId, 1);
        System.out.println(JsonUtils.toJson(inCurrentlyEffectRoleApprovers));
    }

    /**
     * 获取用户审批记录
     *
     * @author wangweijun
     * @since 2024/10/11 18:33
     */
    @Test
    void findTaskApprove() {
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        RuntimeServiceHelper runtimeServiceHelper = workflowHelper.getRuntimeServiceHelper();

        TaskApprove taskApprove = runtimeServiceHelper.getTaskApprove(tenantId, 2, "张三");
        System.out.println(JsonUtils.toJson(taskApprove));

        List<TaskApprove> taskApproves = runtimeServiceHelper.findTaskApproves(tenantId, 1);
        System.out.println(JsonUtils.toJson(taskApproves));
    }

    /**
     * 获取角色用户审批记录
     *
     * @author wangweijun
     * @since 2024/10/11 18:33
     */
    @Test
    void findTaskRoleApprove() {
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        RuntimeServiceHelper runtimeServiceHelper = workflowHelper.getRuntimeServiceHelper();

        TaskRoleApprove taskRoleApprove = runtimeServiceHelper.getTaskRoleApprove(tenantId, 2, "经理", "manager-1");
        System.out.println(JsonUtils.toJson(taskRoleApprove));

        List<TaskRoleApprove> taskRoleApproves = runtimeServiceHelper.findTaskRoleApproves(tenantId, 1);
        System.out.println(JsonUtils.toJson(taskRoleApproves));
    }

    /**
     * 查询流程实例
     *
     * @author wangweijun
     * @since 2024/10/14 10:21
     */
    @Test
    void getWorkflowInstance() {
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        RuntimeServiceHelper runtimeServiceHelper = workflowHelper.getRuntimeServiceHelper();

        System.out.println(runtimeServiceHelper.getWorkflowInstanceById(tenantId, 1));
        System.out.println(runtimeServiceHelper.getWorkflowInstanceByTaskInstanceId(tenantId, 2));

        Page<WorkflowInstance> workflowInstances = runtimeServiceHelper.findWorkflowInstancesByKey(tenantId, workflowKey, List.of(WorkflowStatus.IN_PROGRESS), 1, 10);
        System.out.println(workflowInstances);
    }

    /**
     * 查询流程定下正在进行中的实例
     *
     * @author wangweijun
     * @since 2024/10/22 13:29
     */
    @Test
    void findWorkflowInstanceByTaskInstanceId() {
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        RuntimeServiceHelper runtimeServiceHelper = workflowHelper.getRuntimeServiceHelper();

        System.out.println(runtimeServiceHelper.findWorkflowInstances(tenantId, null, 1));
    }

    @Test
    void findByRequestId() {
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        RuntimeServiceHelper runtimeServiceHelper = workflowHelper.getRuntimeServiceHelper();
        Page<WorkflowInstance> page = runtimeServiceHelper.findWorkflowInstancesByRequestId(tenantId, "eric", WorkflowStatus.IN_PROGRESS, 1, 10);
        List<WorkflowInstance> workflowInstances = page.getRecords();
        workflowInstances.forEach(System.out::println);
    }

    @Test
    void businessInfo() {
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        RuntimeServiceHelper runtimeServiceHelper = workflowHelper.getRuntimeServiceHelper();
        WorkflowInstance workflowInstance = runtimeServiceHelper.getWorkflowInstanceById(tenantId, 1);
        BusinessInfo businessInfo = workflowInstance.getBusinessInfo();
        System.out.println(businessInfo.getObject());
        System.out.println(businessInfo.getObject(Map.class));
        System.out.println(businessInfo.getBusiness());
    }

}
