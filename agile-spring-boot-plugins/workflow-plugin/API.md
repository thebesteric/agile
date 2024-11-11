# 工作流 API
## 1. 部署相关 - DeploymentServiceHelper
### 1.1 核心 Helper
```java
WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
DeploymentServiceHelper deploymentServiceHelper = workflowHelper.getDeploymentServiceHelper();
WorkflowServiceHelper workflowServiceHelper = workflowHelper.getWorkflowServiceHelper();
```
### 1.2 相关 APIs
#### 1.2.1 创建流程定义 - `deploymentServiceHelper.deploy`
- tenantId: 租户 ID
- key: 流程唯一标识
- name: 流程名称
- continuousApproveMode: 连续审批模式
- conditionNotMatchedAnyStrategy: 没有条件节点符合时的处理策略
- allowEmptyAutoApprove: 是否允许节点审批人为空的时候，自动通过
- whenEmptyApprover: 当节点审批人为空的时候，使用的默认审批人
- allowRedo: 是否允许撤回
- requiredComment: 是否需要审批意见
- type: 流程类型
```java
WorkflowDefinitionBuilder builder = WorkflowDefinitionBuilder.builder()
        .tenantId(tenantId)
        .key(workflowKey)
        .name("请假流程")
        .continuousApproveMode(ContinuousApproveMode.APPROVE_CONTINUOUS)
        .conditionNotMatchedAnyStrategy(ConditionNotMatchedAnyStrategy.PROCESS_CONTINUE_TO_NEXT)
        .allowEmptyAutoApprove(true)
        .whenEmptyApprover(Approver.of("admin", "系统管理员"))
        .allowRedo(true)
        .requiredComment(true)
        .type("日常办公流程");
WorkflowDefinition workflowDefinition = deploymentServiceHelper.deploy(builder);
```
#### 1.2.2 获取流程定义 - `deploymentServiceHelper.getByKey`
```java
DeploymentServiceHelper deploymentServiceHelper = workflowHelper.getDeploymentServiceHelper();
WorkflowDefinition workflowDefinition = deploymentServiceHelper.getByKey(tenantId, workflowKey);
```
#### 1.2.3 创建节点定义
##### 1.2.3.1 创建开始节点 - `workflowServiceHelper.createStartNode`
- name: 节点名称
- desc: 节点描述
```java
NodeDefinitionBuilder nodeDefinitionBuilder = NodeDefinitionBuilder.builderStartNode(tenantId, workflowDefinition.getId())
        .name("请假流程开始")
        .desc("开始节点");
NodeDefinition nodeDefinition = workflowServiceHelper.createStartNode(nodeDefinitionBuilder);
```
##### 1.2.3.2 创建审批节点 - `workflowServiceHelper.createTaskNode`
- name: 节点名称
- desc: 节点描述
- approverType: 审批类型：1-或签，2-会签，3-顺签
- approvers（approve）： 审批人信息
```java
nodeDefinitionBuilder = NodeDefinitionBuilder.builderTaskNode(tenantId, workflowDefinition.getId(), 1)
        .name("部门主管审批")
        .desc("任务节点")
        .approveType(ApproveType.ALL)
        .approvers(Set.of(Approver.of("1", "张三"), Approver.of("2", "李四")));
// 创建节点
workflowServiceHelper.createTaskNode(nodeDefinitionBuilder);

nodeDefinitionBuilder = NodeDefinitionBuilder.builderTaskNode(tenantId, workflowDefinition.getId(), 2)
        .name("部门经理审批")
        .desc("任务节点")
        .approveType(ApproveType.ANY)
        .approver(Approver.of("3", "王五"));
// 创建节点
workflowServiceHelper.createTaskNode(nodeDefinitionBuilder);
```
##### 1.2.3.3 创建结束节点 - `workflowServiceHelper.createEndNode`
- name: 节点名称
- desc: 节点描述
```java
nodeDefinitionBuilder = NodeDefinitionBuilder.builderEndNode(tenantId, workflowDefinition.getId())
        .name("请假流程结束")
        .desc("结束节点");
nodeDefinition = workflowServiceHelper.createEndNode(nodeDefinitionBuilder);
```
#### 1.2.4 发布流程定义 - `workflowServiceHelper.publish`
- workflowDefinition: 流程定义
```java
WorkflowServiceHelper workflowServiceHelper = workflowHelper.getWorkflowServiceHelper();
// 发布流程
workflowServiceHelper.publish(workflowDefinition);
```
## 2. 流程定义相关 - DeploymentServiceHelper
### 2.1 核心 Helper
```java
WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
DeploymentServiceHelper deploymentServiceHelper = workflowHelper.getDeploymentServiceHelper();
WorkflowServiceHelper workflowServiceHelper = workflowHelper.getWorkflowServiceHelper();
```
### 2.2 相关 APIs
#### 2.2.1 获取流程定义-根据 id 获取 - `deploymentServiceHelper.getById`
- tenantId: 租户 ID
- workflowId: 流程定义 ID
```java
WorkflowDefinition workflowDefinition = deploymentServiceHelper.getByKey(tenantId, workflowId);
```
#### 2.2.2 获取流程定义-根据 key 获取 - `deploymentServiceHelper.getByKey`
- tenantId: 租户 ID
- key: 流程定义 Key
```java
WorkflowDefinition workflowDefinition = deploymentServiceHelper.getByKey(tenantId, key);
```
#### 2.2.3 获取流程定义-列表 - `deploymentServiceHelper.list`
- tenantId: 租户 ID
```java
List<WorkflowDefinition> workflowDefinitions = deploymentServiceHelper.list(tenantId);
```
#### 2.2.4 更新流程定义 - `deploymentServiceHelper.update`
```java
WorkflowDefinition workflowDefinition = deploymentServiceHelper.getByKey(tenantId, key);
workflowDefinition.setTenantId("8888");
workflowDefinition.setKey("test-key");
workflowDefinition.setName("测试流程");
workflowDefinition.setDesc("这是一个测试流程");
deploymentServiceHelper.update(workflowDefinition);
```
#### 2.2.5 禁用流程定义 - `deploymentServiceHelper.disable`
- tenantId: 租户 ID
- key: 流程定义 Key
```java
deploymentServiceHelper.disable(tenantId, key);
```
#### 2.2.6 启用流程定义 - `deploymentServiceHelper.enable`
- tenantId: 租户 ID
- key: 流程定义 Key
```java
deploymentServiceHelper.enable(tenantId, key);
```
### 2.2.7 删除流程定义 - `deploymentServiceHelper.delete`
- tenantId: 租户 ID
- key: 流程定义 Key
```java
deploymentServiceHelper.delete(tenantId, key);
```
#### 2.2.8 发布流程定义 - `workflowServiceHelper.publish`
- tenantId: 租户 ID
- key: 流程定义 Key
```java
workflowServiceHelper.publish(tenantId, key);
```
#### 2.2.9 获取流程定义纲要 - `deploymentServiceHelper.schema`
- tenantId: 租户 ID
- key: 流程定义 Key
```java
WorkflowDefinitionFlowSchema schema = deploymentServiceHelper.schema(tenantId, key);
```
## 3. 节点定义相关 - WorkflowServiceHelper
### 3.1 核心 Helper
```java
WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
WorkflowServiceHelper workflowServiceHelper = workflowHelper.getWorkflowServiceHelper();
```
### 3.2 相关 APIs
#### 3.2.1 创建开始节点定义 - `workflowServiceHelper.createStartNode`
> 一个流程有且仅有一个开始节点
- nodeDefinitionBuilder: 节点定义构建器
```java
NodeDefinition nodeDefinition = workflowServiceHelper.createStartNode(nodeDefinitionBuilder);
```
#### 3.2.2 创建任务节点定义 - `workflowServiceHelper.createTaskNode`
> 一个流程至少包含一个任务节点
- nodeDefinitionBuilder: 节点定义构建器
```java
NodeDefinition nodeDefinition = workflowServiceHelper.createTaskNode(nodeDefinitionBuilder);
```
#### 3.2.3 创建结束节点定义 - `workflowServiceHelper.createEndNode`
> 一个流程有且仅有一个结束节点
- nodeDefinitionBuilder: 节点定义构建器
```java
NodeDefinition nodeDefinition = workflowServiceHelper.createEndNode(nodeDefinitionBuilder);
```
#### 3.2.4 获取开始节点定义 - `workflowServiceHelper.getStartNode`
- tenantId: 租户 ID
- workflowDefinitionKey: 流程定义 Key
```java
NodeDefinition nodeDefinition = workflowServiceHelper.getStartNode(tenantId, workflowDefinitionKey);
```
#### 3.2.5 获取结束节点定义 - `workflowServiceHelper.getEndNode`
- tenantId: 租户 ID
- workflowDefinitionKey: 流程定义 Key
```java
NodeDefinition nodeDefinition = workflowServiceHelper.getEndNode(tenantId, workflowDefinitionKey);
```
#### 3.2.6 获取第一个任务节点定义 - `workflowServiceHelper.getFirstTaskNode`
- tenantId: 租户 ID
- workflowDefinitionKey: 流程定义 Key
```java
NodeDefinition nodeDefinition = workflowServiceHelper.getFirstTaskNode(tenantId, workflowDefinitionKey);
```
#### 3.2.7 获取最后一个任务节点定义 - `workflowServiceHelper.getLastTaskNode`
- tenantId: 租户 ID
- workflowDefinitionKey: 流程定义 Key
```java
NodeDefinition nodeDefinition = workflowServiceHelper.getLastTaskNode(tenantId, workflowDefinitionKey);
```
#### 3.2.8 获取节点定义-根据 ID 获取 - `workflowServiceHelper.getNode`
- tenantId: 租户 ID
- nodeDefinitionId: 节点定义 ID
```java
NodeDefinition nodeDefinition = workflowServiceHelper.getNode(tenantId, nodeDefinitionId);
```
#### 3.2.9 获取任务节点定义-列表 - `workflowServiceHelper.findTaskNodes`
- tenantId: 租户 ID
- workflowDefinitionKey: 流程定义 Key
```java
List<NodeDefinition> taskNodes = workflowServiceHelper.findTaskNodes(tenantId, workflowDefinitionKey);
```
#### 3.2.10 获取节点定义-列表 - `workflowServiceHelper.getNodes`
- tenantId: 租户 ID
- workflowDefinitionKey: 流程定义 Key
```java
List<NodeDefinition> nodes = workflowService.getNodes(tenantId, workflowDefinitionKey);
```
#### 3.2.11 更新节点定义 - `workflowServiceHelper.updateNode`
> 节点更新后，需要重新发布流程
- nodeDefinition: 节点定义
```java
NodeDefinition nodeDefinition = workflowServiceHelper.getNode(tenantId, nodeDefinitionId);
nodeDefinition.setName("部门经理审批");
nodeDefinition.setDesc("任务节点");
nodeDefinition.removeApprover(Approver.of("张三"));
nodeDefinition.addApprover(Approver.of("李四"));
workflowServiceHelper.updateNode(nodeDefinition);
```
#### 3.2.12 插入节点定义 - `workflowServiceHelper.insertNode`
> 节点更新后，需要重新发布流程
- nodeDefinition: 节点定义
- prevNodeDefinitionId: 插入到该节点之前的节点定义 ID
- nextNodeDefinitionId: 插入到该节点之后的节点定义 ID
```java
NodeDefinition nodeDefinition = NodeDefinitionBuilder.builderTaskNode(tenantId, workflowDefinitionId)
        .name("新插入的节点")
        .approver(Approver.of("zs", "张三"))
        .build();
workflowServiceHelper.insertNode(nodeDefinition, prevNodeDefinitionId, nextNodeDefinitionId);
```
#### 3.2.13 删除节点定义 - `workflowServiceHelper.deleteNode`
> 节点更新后，需要重新发布流程
- tenantId: 租户 ID
- nodeDefinitionId: 节点定义 ID
```java
workflowServiceHelper.deleteNode(tenantId, nodeDefinitionId);
```
#### 3.2.14 获取节点审批人 - `workflowServiceHelper.findApprovers`
- tenantId: 租户 ID
- nodeDefinitionId: 节点定义 ID
```java
List<Approver> approvers = workflowServiceHelper.findApprovers(tenantId, nodeDefinitionId);
```
#### 3.2.15 获取角色节点审批人 - `workflowServiceHelper.findRoleApprovers`
- tenantId: 租户 ID
- nodeDefinitionId: 节点定义 ID
```java
List<RoleApprover> approvers = workflowServiceHelper.findRoleApprovers(tenantId, nodeDefinitionId);
```
## 4. 审批相关 - RuntimeServiceHelper
### 4.1 核心 Helper
```java
WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
RuntimeServiceHelper runtimeServiceHelper = workflowHelper.getRuntimeServiceHelper();
```
### 4.2 相关 APIs
#### 4.2.1 提交审批 - `runtimeServiceHelper.start`
- workflowDefinition: 流程定义
- userId: 用户 ID
- businessInfo: 业务信息
- comment: 备注
```java
RuntimeServiceHelper runtimeServiceHelper = workflowHelper.getRuntimeServiceHelper();
BusinessInfo businessInfo = BusinessInfo.of(MapWrapper.create().put("business_id", 123).put("business_type", "项目资料").build());
WorkflowInstance workflowInstance = runtimeServiceHelper.start(workflowDefinition, userId, businessInfo, "申请请假 3 天");
```
#### 4.2.2 取消提交 - `runtimeServiceHelper.findTaskInstances`
> 取消提交的前提是，当前审批流程未发生任何审批记录时，发起者可以主动取消提交
- workflowInstance: 流程实例
```java
runtimeServiceHelper.cancel(workflowInstance);
```
#### 4.2.3 获取审批列表 - `runtimeServiceHelper.findTaskInstances`
- tenantId: 租户 ID
- workflowInstanceId: 流程实例 ID
- roleIds: 角色 ID
- approverId: 审批人 ID
- nodeStatuses: 节点状态
- approveStatuses: 审批状态
- approveDatesSegmentCondition: 日期范围
- page: 当前页
- pageSize: 每页大小
```java
Page<TaskInstance> page = runtimeServiceHelper.findTaskInstances(tenantId, null, List.of(roleId), approverId, nodeStatuses, approveStatuses, null, 1, 10);
List<TaskInstance> taskInstances = page.getRecords();
```
#### 4.2.4 审批-同意 - `runtimeServiceHelper.approve`
- taskInstance: 任务实例
- roleId: 角色 ID
- approverId: 审批人 ID
- comment: 审批意见
```java
runtimeServiceHelper.approve(taskInstance, roleId, approverId, comment);
```
#### 4.2.5 审批-拒绝 - `runtimeServiceHelper.reject`
- taskInstance: 任务实例
- roleId: 角色 ID
- approverId: 审批人 ID
- comment: 审批意见
```java
runtimeServiceHelper.reject(taskInstance, roleId, approverId, "不同意");
```
#### 4.2.6 审批-放弃 - `runtimeServiceHelper.abandon`
> 放弃审批的前提是，当前审批节点包含多个审批人，且当前审批人并不是最后一个审批人
- taskInstance: 任务实例
- roleId: 角色 ID
- approverId: 审批人 ID
- comment: 审批意见
```java
runtimeServiceHelper.abandon(taskInstance, roleId, approverId, "放弃审批");
```
#### 4.2.7 审批-转派（用户审批节点） - `runtimeServiceHelper.reassign`
> 审批转派，是指当前审批人将自己的审批权限转给其他人，转派后，被转派人可以继续审批  
> - 约束-1：无法转派已转派的用户，即：张三转给李四，那么李四无法再转给张三
> - 约束-2：被转派人正处于审批或等待审批中，无法转派
- tenantId: 租户 ID
- taskInstanceId: 任务实例 ID
- userId: 用户 ID
- invitee: 被转派人（用户ID 必须填写）
- comment: 审批意见
```java
runtimeServiceHelper.reassign(tenantId, taskInstanceId, userId, invitee, comment);
```
#### 4.2.8 审批-转派（角色审批节点） - `runtimeServiceHelper.reassign`
> 审批转派，是指当前审批人将自己的审批权限转给其他人，转派后，被转派人可以继续审批
> - 约束-1：无法转派已转派的用户，即：张三转给李四，那么李四无法再转给张三
> - 约束-2：角色审批下，同一个审批节点，无法转派给同一个审批人，即：张三转给李四，那么相同审批节点下，任何人都无法再转给李四
> - 约束-3：相同角色下的用户才可以转派
- tenantId: 租户 ID
- taskInstanceId: 任务实例 ID
- roleId: 角色 ID
- userId: 用户 ID
- invitee: 被转派人（角色 ID 、用户ID 必须填写）
- comment: 审批意见
```java
runtimeServiceHelper.reassign(tenantId, taskInstanceId, roleId, userId, invitee, comment);
```
### 4.2.9 获取审批历史记录 - `runtimeServiceHelper.getWorkflowInstanceApproveRecords`
- tenantId: 租户 ID
- workflowInstanceId: 流程实例 ID
- roleIds: 角色 ID 集合
- userId: 用户 ID
```java
WorkflowInstanceApproveRecords workflowInstanceApproveRecord = runtimeServiceHelper.getWorkflowInstanceApproveRecords(tenantId, workflowInstanceId, roleIds, userId);
```
#### 4.2.10 获取当前流程实例下生效的审批任务实例 - `runtimeServiceHelper.getInCurrentlyEffectTaskInstance`
- tenantId: 租户 ID
- workflowInstanceId: 流程实例 ID
```java
TaskInstance taskInstance = runtimeServiceHelper.getInCurrentlyEffectTaskInstance(tenantId, workflowInstanceId);
```
#### 4.2.11 获取当前流程实例下生效的节点定义 - `runtimeServiceHelper.getInCurrentlyEffectNodeDefinition`
- tenantId: 租户 ID
- workflowInstanceId: 流程实例 ID
```java
NodeDefinition nodeDefinition = runtimeServiceHelper.getInCurrentlyEffectNodeDefinition(tenantId, workflowInstanceId);
```
#### 4.2.12 获取当前流程实例下生效的审批人 - `runtimeServiceHelper.findInCurrentlyEffectApprovers`
- tenantId: 租户 ID
- workflowInstanceId: 流程实例 ID
```java
List<Approver> approvers = runtimeServiceHelper.findInCurrentlyEffectApprovers(tenantId, workflowInstanceId);
```
#### 4.2.13 获取当前流程实例下生效的角色审批人 - `runtimeServiceHelper.findInCurrentlyEffectRoleApprovers`
- tenantId: 租户 ID
- workflowInstanceId: 流程实例 ID
```java
List<RoleApprover> roleApprovers = runtimeServiceHelper.findInCurrentlyEffectRoleApprovers(tenantId, workflowInstanceId);
```
#### 4.2.14 获取用户审批记录 - `runtimeServiceHelper.getTaskApprove`
- tenantId: 租户 ID
- taskInstanceId: 任务实例 ID
- userId: 用户 ID
```java
TaskApprove taskApprove = runtimeServiceHelper.getTaskApprove(tenantId, taskInstanceId, userId);
```
#### 4.2.15 获取角色用户审批记录 - `runtimeServiceHelper.getTaskRoleApprove`
- tenantId: 租户 ID
- taskInstanceId: 任务实例 ID
- roleId: 角色 ID
- userId: 用户 ID
```java
TaskRoleApprove taskRoleApprove = runtimeServiceHelper.getTaskRoleApprove(tenantId, taskInstanceId, roleId, userId);
```
#### 4.2.16 获取流程实例下所有审批记录 - `runtimeServiceHelper.findTaskApproves`
- tenantId: 租户 ID
- workflowInstanceId: 流程实例 ID
- userId: 用户 ID
```java
List<TaskApprove> taskApproves = runtimeServiceHelper.findTaskApproves(tenantId, workflowInstanceId);
```
#### 4.2.17 获取流程实例下所有角色审批记录 - `runtimeServiceHelper.findTaskRoleApproves`
- tenantId: 租户 ID
- workflowInstanceId: 流程实例 ID
- userId: 用户 ID
```java
List<TaskRoleApprove> taskRoleApproves = runtimeServiceHelper.findTaskRoleApproves(tenantId, workflowInstanceId);
```
#### 4.2.18 获取流程实例-根据流程实例 ID 获取 - `runtimeServiceHelper.getWorkflowInstanceById`
- tenantId: 租户 ID
- workflowInstanceId: 流程实例 ID
```java
WorkflowInstance workflowInstance = runtimeServiceHelper.getWorkflowInstanceById(tenantId, workflowInstanceId);
```
#### 4.2.19 获取流程实例-根据任务实例 ID 获取 - `runtimeServiceHelper.getWorkflowInstanceByTaskInstanceId`
- tenantId: 租户 ID
- taskInstanceId: 任务实例 ID
```java
WorkflowInstance workflowInstance = runtimeServiceHelper.getWorkflowInstanceByTaskInstanceId(tenantId, taskInstanceId);
```
#### 4.2.20 获取流程实例-根据提交人 ID 获取 - `runtimeServiceHelper.findWorkflowInstancesByRequestId`
- tenantId: 租户 ID
- requesterId: 提交人 ID
- workflowStatus: 流程状态
- page: 当前页
- pageSize: 每页大小
```java
Page<WorkflowInstance> page = runtimeServiceHelper.findWorkflowInstancesByRequestId(tenantId, requesterId, workflowStatus, page, pageSize);
```
#### 4.2.21 获取流程实例 - `runtimeServiceHelper.findWorkflowInstances`
- tenantId: 租户 ID
- workflowStatus: 流程实例状态
- workflowDefinitionId: 流程定义 ID
```java
List<WorkflowInstance> workflowInstances = runtimeServiceHelper.findWorkflowInstances(tenantId, workflowStatus, workflowDefinitionId);
```
#### 4。2.22 获取审核日志 - `taskHistoryServiceHelper.findTaskHistories`
- tenantId: 租户 ID
- workflowInstanceId: 流程实例 ID
- page: 当前页
- pageSize: 每页大小
```java
Page<TaskHistoryResponse> taskHistories = taskHistoryServiceHelper.findTaskHistories(tenantId, workflowInstanceId, page, pageSize);
```
#### 4。2.23 获取流程定义纲要 - `runtimeServiceHelper.schema`
- tenantId: 租户 ID
- workflowInstanceId: 流程实例 ID
```java
WorkflowDefinitionFlowSchema schema = runtimeServiceHelper.schema(tenantId, workflowInstanceId);
```