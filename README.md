# Agile Framework
> Agile Framework for building web applications easily

## 日志插件
### 相关配置
```yaml
sourceflag:
  agile:
    logger:
      enable: true
      logger:
        log-mode: log
        curl-enable: true
        response-success-define:
          code-fields:
            - name: code
              value: 200
            - name: code
              value: 100
          message-fields: message, msg
      async:
        enable: true
        async-params:
          core-pool-size: 1
          maximum-pool-size: 2
          keep-alive-time: 60s
          queue-size: 1024
```
### 自定义日志记录器
自定义一个 bean 实现 CustomRecorder，实现 process 方法
```java
@Configuration
@EnableConfigurationProperties(AgileLoggerProperties.class)
public class AgileLoggerConfig {
    @Bean
    public Recorder customRecorder(AgileLoggerProperties properties) {
        return new CustomRecorder(properties) {
            @Override
            protected void doProcess(InvokeLog invokeLog) {
                System.out.println("This is my custom log: " + invokeLog.getLogId());
            }
        };
    }
}
```
### 自定义请求忽略或重写
```java
@Bean
public RequestIgnoreProcessor headerIgnoreProcessor() {
    return new HeaderIgnoreProcessor() {
        @Override
        protected String[] doIgnore(RequestLog requestLog) {
            return new String[]{"apple", "banana"};
        }

        @Override
        protected Map<String, String> doRewrite(RequestLog requestLog) {
            return Map.of("phone", "*");
        }
    };
}

@Bean
public RequestIgnoreProcessor parameterIgnoreProcessor() {
    return new ParameterIgnoreProcessor() {
        @Override
        protected String[] doIgnore(RequestLog requestLog) {
            return new String[]{"apple", "banana"};
        }

        @Override
        protected Map<String, String> doRewrite(RequestLog requestLog) {
            return Map.of("name", "**");
        }
    };
}
```
## 幂等插件
主要作用：防止接口重复提交，可自定义幂等关键信息
### 使用方式
```xml
<dependency>
    <groupId>io.github.thebesteric.framework.agile.plugins</groupId>
    <artifactId>idempotent-plugin</artifactId>
    <version>${latest.version}</version>
</dependency>
```
1. 使用方法参数的幂等信息
```java
@GetMapping("/id1")
@Idempotent(timeout = 1000)
public R<String> id1(@IdempotentKey String name, @IdempotentKey Integer age) {
    return R.success(name + "-" + age);
}
```
2. 使用对象内的幂等信息
```java
@PostMapping("/id2")
@Idempotent(timeout = 1000)
public R<Id2Vo> id2(@RequestBody Id2Vo id2Vo) {
    return R.success(id2Vo);
}

@Data
public class Id2Vo {
    @IdempotentKey
    private String name;
    @IdempotentKey
    private Integer age;
}
```
### 幂等实现类配置
默认使用内存实现幂等操作，或自定义实现`IdempotentProcessor`接口，如：使用 Redis 实现幂等操作
```xml
<dependency>
    <groupId>io.github.thebesteric.framework.agile.plugins</groupId>
    <artifactId>idempotent-plugin-redis</artifactId>
    <version>${latest.version}</version>
</dependency>
```
代码实现
```java
@Bean
public IdempotentProcessor redisIdempotentProcessor(RedissonClient redissonClient) {
    return new RedisIdempotentProcessor(redissonClient);
}
```
## 限流插件
主要作用：进行接口限流，同时支持 IP 地址限流
### 使用方式
```xml
<dependency>
    <groupId>io.github.thebesteric.framework.agile.plugins</groupId>
    <artifactId>limiter-plugin</artifactId>
    <version>${latest.version}</version>
</dependency>
```
使用方式：
- `timeout`：表示时间窗口
- `count`：表示时间窗口内允许多少个请求
- `type`：限流类型，分为 `RateLimitType.DEFAULT` 和 `RateLimitType.IP`
```java
@PostMapping("/limit")
@RateLimiter(timeout = 10, count = 10)
public R<Id2Vo> limit(@RequestBody Id2Vo id2Vo) {
    return R.success(id2Vo);
}
```
### 使用 Redis 作为限流实现
```xml
<dependency>
    <groupId>io.github.thebesteric.framework.agile.plugins</groupId>
    <artifactId>limiter-plugin-redis</artifactId>
    <version>${latest.version}</version>
</dependency>
```
代码实现
```java
@Bean
public RateLimiterProcessor redisRateLimiterProcessor(RedisTemplate<String, Object> redisTemplate) {
    return new RedisRateLimiterProcessor(redisTemplate);
}
```
## 数据库插件
支持正向创建和修改表结构
### 使用方式
```xml
<dependency>
    <groupId>io.github.thebesteric.framework.agile.plugins</groupId>
    <artifactId>database-plugin</artifactId>
    <version>${latest.version}</version>
</dependency>
```
相关配置项
```yaml
sourceflag:
  agile:
    database:
      enable: true
      show-sql: true
      ddl-auto: update
      format-sql: true
      delete-column: true
```
```java
@TableName("foo")
public class Foo extends BaseEntity {

    @EntityColumn(length = 32, unique = true, nullable = false, forUpdate = "hello", defaultExpression = "'foo'")
    private String name;

    @EntityColumn(name = "t_phone", unique = true, nullable = false, defaultExpression = "18", comment = "电话", unsigned = true)
    private Integer age;

    @EntityColumn(unique = true, defaultExpression = "'test'")
    private String address;

    @EntityColumn(length = 10, precision = 3, unique = true)
    private BigDecimal amount;

    @EntityColumn(nullable = false, type = EntityColumn.Type.SMALL_INT, unsigned = true)
    private Season season;

    @EntityColumn(length = 10, precision = 2)
    private Float state;

    @EntityColumn(type = EntityColumn.Type.DATETIME, defaultExpression = "now()")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;

    @TableField("t_test")
    @EntityColumn(length = 64, nullable = false)
    private String test;
}
```
## 工作流插件
### 相关配置
```yaml
sourceflag:
  agile:
    workflow:
      ddl-auto: update
```
### 使用方式
```java
class DeploymentServiceTest {

    @Autowired
    WorkflowEngine workflowEngine;

    /**
     * 创建一个流程定义
     */
    @Test
    void createWorkflow() {
        workflowEngine.setCurrentUser("admin");
        DeploymentService deploymentService = workflowEngine.getDeploymentService();
        WorkflowDefinition workflowDefinition = WorkflowDefinitionBuilder.builder().tenantId("1")
                .key("test-key").name("测试流程").type("测试").desc("这是一个测试流程").build();
        WorkflowDefinition workflow = deploymentService.create(workflowDefinition);
        System.out.println(workflow);
    }

    /**
     * 定义工作流程节点
     */
    @Test
    void createNodeDefinitions() {
        String tenantId = "8888";
        workflowEngine.setCurrentUser("admin");
        DeploymentService deploymentService = workflowEngine.getDeploymentService();
        WorkflowDefinition workflowDefinition = deploymentService.get(tenantId, "test-key");
        if (workflowDefinition == null) {
            workflowDefinition = WorkflowDefinitionBuilder.builder().tenantId(tenantId).key("test-key").name("测试流程").type("测试").desc("这是一个测试流程").build();
            workflowDefinition = deploymentService.create(workflowDefinition);
        }

        createNodeDefinitions(tenantId, workflowDefinition);

        WorkflowService workflowService = workflowEngine.getWorkflowService();
        workflowService.createRelations(tenantId, workflowDefinition.getId());

    }

    private void createNodeDefinitions(String tenantId, WorkflowDefinition workflowDefinition) {
        WorkflowService workflowService = workflowEngine.getWorkflowService();
        NodeDefinition nodeDefinition = NodeDefinitionBuilder.builderStartNode(tenantId, workflowDefinition.getId())
                .name("请假流程开始").desc("开始节点").build();
        nodeDefinition = workflowService.createNode(nodeDefinition);
        System.out.println(nodeDefinition);

        Conditions conditions = Conditions.defaultConditions();
        conditions.addCondition(Condition.of("day", "3", Operator.LESS_THAN));
        nodeDefinition = NodeDefinitionBuilder.builderTaskNode(tenantId, workflowDefinition.getId(), 1)
                .name("部门主管审批").desc("任务节点").conditions(conditions).approveType(ApproveType.ALL)
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

        nodeDefinition = NodeDefinitionBuilder.builderTaskNode(tenantId, workflowDefinition.getId(), 2)
                .name("人事主管审批").desc("任务节点")
                .approverId("赵六")
                .build();
        nodeDefinition = workflowService.createNode(nodeDefinition);
        System.out.println(nodeDefinition);

        nodeDefinition = NodeDefinitionBuilder.builderEndNode(tenantId, workflowDefinition.getId())
                .name("请假流程结束").desc("结束节点").build();
        nodeDefinition = workflowService.createNode(nodeDefinition);
        System.out.println(nodeDefinition);
    }

    /**
     * 提交流程
     */
    @Test
    void start() {
        String tenantId = "8888";
        String requesterId = "eric";
        workflowEngine.setCurrentUser(requesterId);
        RuntimeService runtimeService = workflowEngine.getRuntimeService();
        RequestConditions requestConditions = RequestConditions.newInstance();
        requestConditions.addRequestCondition(RequestCondition.of("day", "2"));
        runtimeService.start(tenantId, "test-key", requesterId, "123-789-3", "org.agile.workflow.Business.class", "请假申请单", requestConditions);
    }

    /**
     * 取消流程
     */
    @Test
    void cancel() {
        String tenantId = "8888";
        String requesterId = "eric";
        workflowEngine.setCurrentUser(requesterId);
        RuntimeService runtimeService = workflowEngine.getRuntimeService();
        List<WorkflowInstance> workflowInstances = runtimeService.findWorkflowInstancesByRequestId("1", requesterId, WorkflowStatus.IN_PROGRESS);
        for (WorkflowInstance workflowInstance : workflowInstances) {
            runtimeService.cancel(tenantId, workflowInstance.getId());
        }
    }

    /**
     * 同意流程
     */
    @Test
    void approve() {
        String tenantId = "8888";
        String approverId = "张三";
        workflowEngine.setCurrentUser(approverId);
        RuntimeService runtimeService = workflowEngine.getRuntimeService();
        List<TaskInstance> taskInstances = runtimeService.findTaskInstances(tenantId, approverId, NodeStatus.IN_PROGRESS, ApproveStatus.IN_PROGRESS);
        if (!taskInstances.isEmpty()) {
            for (TaskInstance taskInstance : taskInstances) {
                runtimeService.approve(tenantId, taskInstance.getId(), approverId, "同意");
            }
        }
    }

    /**
     * 拒绝流程
     */
    @Test
    void reject() {
        String tenantId = "8888";
        String approverId = "张三";
        workflowEngine.setCurrentUser(approverId);
        RuntimeService runtimeService = workflowEngine.getRuntimeService();
        List<TaskInstance> taskInstances = runtimeService.findTaskInstances(tenantId, approverId, NodeStatus.IN_PROGRESS, ApproveStatus.IN_PROGRESS);
        if (!taskInstances.isEmpty()) {
            for (TaskInstance taskInstance : taskInstances) {
                runtimeService.reject(tenantId, taskInstance.getId(), approverId, "不同意");
            }
        }
    }

    /**
     * 放弃流程
     */
    @Test
    void abandon() {
        String tenantId = "8888";
        String approverId = "张三";
        workflowEngine.setCurrentUser(approverId);
        RuntimeService runtimeService = workflowEngine.getRuntimeService();
        List<TaskInstance> taskInstances = runtimeService.findTaskInstances(tenantId, approverId, NodeStatus.IN_PROGRESS, ApproveStatus.IN_PROGRESS);
        if (!taskInstances.isEmpty()) {
            for (TaskInstance taskInstance : taskInstances) {
                runtimeService.abandon(tenantId, taskInstance.getId(), approverId, "弃权");
            }
        }
    }
}
```