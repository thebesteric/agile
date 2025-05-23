### v1.0.0

- 初版

### v1.0.1

- 日志插件 (bugfix): 修复 MultipartFile 参数解析问题

### v1.0.2

- 日志插件 (chore): 取消 @AgileLogger 内的忽略方法属性
- 日志插件 (bugfix): 修复 filter 无法获取到 method 到问题

### v1.0.3

- 日志插件 (feat): 增加同类返回类 R 适用于 Web 项目返回 Response
- 日志插件 (bugfix): 修复 trackId 重复的问题
- 日志插件 (feat): 支持自定义 track-id

### v1.0.4

- 日志插件 (feat): 支持忽略或冲泻请求日志中的请求信息（Params、Header、Cookie）

### v1.0.5

- 日志插件 (bugfix): 修复 TransactionUtils.clear 时空指针的问题

### v1.0.6

- 日志插件 (chore): 修改日志配置文件的前缀
- 幂等插件 (feat): 新增接口幂等判断插件

### v1.0.7

- 幂等插件 (build): 幂等插件拆分为 memory 实现和 Redis 实现

### v1.0.8

- 幂等插件 (fix): 修复一些已知问题，增加开关控制
- 限流插件 (feat): 新增限流判断插件，支持接口和 IP 地址限流

### v1.0.9

- 幂等插件 (feat): 支持全局消息提示
- 限流插件 (feat): 支持全局消息提示

### v1.0.10

- 重新构建 (build): 空发布

### v1.0.11

- 数据库插件 (feat): 支持正向注解创建和更新表结构

### v1.0.11.1

- 数据库插件 (bugfix): 修复字段特殊值的问题
- 数据库插件 (feat): 优化已知问题，支持忽略创建指定表和字段的能力

### v1.0.11.2

- 数据库插件 (bugfix): 修复 INNODB_COLUMN 冗余问题
- 数据库插件 (bugfix): 修复部分字段映射失败的问题

### v1.0.11.3

- 数据库插件 (bugfix): 修复处理布尔类型参数问题

### v1.0.11.4

- 数据库插件 (feat): 支持 LocalDateTime 类型

### v1.0.11.5

- 数据库插件 (feat): 支持 SQL 语句格式化

### v1.0.11.6

- 数据库插件 (bugfix): 修复格式化时与 MyBatis-Plus 冲突的问题

### v1.0.11.7

- 数据库插件 (feat): 支持删除字段

### v1.0.11.8

- 数据库插件 (feat): 支持联合唯一索引

### v1.0.11.9

- 数据库插件 (feat): 增加是否删除冗余列的控制
- 数据库插件 (bugfix): 屏蔽 INNODB_COLUMN 冗余列删除的问题

### v1.0.11.10

- 数据库插件 (bugfix): 修改字段长度计算错误的问题

### v1.0.11.11

- 数据库插件 (feat): 支持创建索引与联合索引

### v1.0.11.12

- 数据库插件 (perf): 代码优化
- 数据库插件 (feat): 支持动态修改字段属性
- 数据库插件 (feat): @EntityClass 支持 comment 注释
- 数据库插件 (fix): 调整主键列为第一列

### v1.0.11.13

- 数据库插件 (bugfix): 优化判断元数据表是否存在的逻辑

### v1.0.11.14

- 数据库插件 (feat): 增加实体类上的 @Unique 和 @UniqueGroup 注解
- 数据库插件 (feat): 增加实体类上的 @Index 和 @IndexGroup 注解
- 数据库插件 (feat): 存在多个相同的表名增加错误提示
- 数据库插件 (bugfix): 修复 VARCHAR 没有长度的问题

### v1.0.11.15

- 数据库插件 (bugfix): 修复元数据表会插入重复数据问题

### v1.0.12

- 幂等插件 (bugfix): 修复异常会包装为 RuntimeException 的问题
- 限流插件 (bugfix): 修复异常会包装为 RuntimeException 的问题
- 数据库插件 (refactor): 结构调整，优化部分代码
- 数据库插件 (feat): 支持 BLOB 类型
- 数据库插件 (feat): 支持外键映射
- 工作流插件 (feat): 新增工作流插件

### v1.0.12.1

- 数据库插件 (perf): 增加元数据表的的表名和列名长度
- 工作流插件 (bugfix): 修复已知问题
- 工作流插件 (perf): 优化审批流程创建和启动方式，使用 Helper 类
- 工作流插件 (feat): 增加挂载附件

### v1.0.13

- 微信开发平台 (feat): 增加微信开发平台

### v1.0.13.1

- 微信开发平台 (feat): 新增小程序相关 API 接口
- 微信开发平台 (refactor): 优化接口调用方式

### v1.0.14

- 微信开发平台 (bugfix): 修复监听可能出现的空指针问题
- 微信开发平台 (feat): 增加客服消息监听类型
- 微信开发平台 (feat): 增加硬件设备相关 API
- 注解扫描插件 (feat): 新增注解扫描插件

### v1.0.14.1

- 注解扫描插件 (bugfix): 修复注解上下文加载时机问题

### v1.0.14.2

- 注解扫描插件 (bugfix): 修复注解上下文加载时机问题
- 注解扫描插件 (perf): 优化加载逻辑

### v1.0.14.3

- 注解扫描插件 (feat): 增加判断注解所在宿主是否满足的过滤条件

### v1.0.14.4

- 工作流插件 (feat): 增加审批类型：顺序审批

### v1.0.15

- 幂等插件 (bugfix): 修复如果没有 ClassMatcher，幂等插件不生效的问题
- 限流插件 (bugfix): 修复如果没有 ClassMatcher，限流插件不生效的问题
- 限流插件 (perf): 优化限流插件，无需手动创建 redis-plugin 插件
- 限流插件 (perf): 优化幂等插件，无需手动创建 redis-plugin 插件
- 分布式锁插件 (feat): 增加分布式锁插件

### v1.0.15.1

- 幂等插件 (feat): 增加全局幂等拦截，@Idempotent 优先级高于全局幂等拦截

### v1.0.15.2

- 工作流插件 (fix): 修复审批放弃后，无法流转到下一个审批节点的问题

### v1.0.15.3

- 数据库插件 (fix): 修复非注解的实体类没有表前缀的问题
- 数据库插件 (perf): 优化逻辑，增加 EntityUtils 工具类

### v1.0.15.4

- 工作流插件 (fix): 修复审批流程中缺少判断审批节点类型，导致审批人数错误的问题
- 工作流插件 (perf): 增加可为审批人添加备注描述

### v1.0.15.5

- 日志插件 (perf): 取消自动注入 ClassMatcher，避免其他插件可能引起的冲突
- 限流插件 (refactor): 默认只对 Controller 层生效
- 限流插件 (perf): 支持自定义 ClassMatcher

### v1.0.15.6

- 工作流插件 (bugfix): 修复超过三层任务审批节点时，审批报错的问题
- 工作流插件 (bugfix): 修复获取单个实体店时候，ResultSet 报错的问题
- 工作流插件 (feat): 支持按角色审批
- 工作流插件 (feat): 增加是否允许撤回、是否审批意见必填的配置
- 工作流插件 (feat): 在下个审批节点未审批时，支持审批撤回
- 工作流插件 (feat): 支持插入审批节点，插入后需重新发布流程
- 工作流插件 (feat): 支持动态设置审批人，达到指定审批人的目的
- 工作流插件 (feat): 当节点审批成员为空时，支持自动审批，支持指定默认审批人
- 工作流插件 (feat): 增加连续审批类型设置，支持同一流程下，相同审批人自动同意逻辑
- 工作流插件 (feat): 增加流程定义、节点定义日志

### v1.0.15.7

- 工作流插件 (bugfix): 修复获取流程实例时 business_type 为空的问题
- 工作流插件 (perf): 审核记录结构优化
- 工作流插件 (perf): 优化部分查询
- 工作流插件 (feat): 增加查询角色审批用户的当前审核实例的角色信息
- 工作流插件 (feat): 增加按审批时间查询审批记录

### v1.0.15.8

- 工作流插件 (perf): 流程引擎可以获取相关表执行器，可拓展业务查询
- 工作流插件 (feat): 增加获取节点审批用户和运行时审批用户

### v1.0.15.9

- 工作流插件 (bugfix): 修复获取运行时用户错误的问题
- 工作流插件 (feat): 增加获取审批用户的审批详情

### v1.0.15.10

- 工作流插件 (perf): 任务实例增加判断是否是角色实例的字段
- 工作流插件 (perf): 流程定义纲要和流程实例审批记录增加角色名称和用户名称字段
- 工作流插件 (feat): 增加通过流程 ID 或 实例 ID 获取流程实例
- 工作流插件 (feat): 增加根据流程定义 key 获取流程实例

### v1.0.15.11

- 工作流插件 (feat): 支持按用户进行多角色查询审批流程记录

### v1.0.15.12

- 工作流插件 (bugfix): 修复判断空节点自动审批的问题
- 工作流插件 (bugfix): 修复条件节点审批时，创建任务实例报错的问题
- 工作流插件 (bugfix): 修复动态审批节点，只有首次提交才能设置审批人的问题
- 工作流插件 (perf): 优化获取流程实例审批记录，支持动态审批节点
- 工作流插件 (feat): 动态审批人节点，支持不预先设置人数

### v1.0.15.13

- 工作流插件 (feat): 优化条件审批，增加优先级字段，保证只有一个条件匹配

### v1.0.15.14

- 工作流插件 (perf): 优化条件审批，当没有任何条件节点符合当前节点时，默认审批通过

### v1.0.15.15

- 工作流插件 (feat): 增加查询正在进行的流程实例 API

### v1.0.15.16

- 工作流插件 (perf): 部分代码优化

### v1.0.15.17

- 工作流插件 (bugfix): 修复已知问题
- 工作流插件 (perf): 增加流程定义、节点定义名称字段长度
- 工作流插件 (perf): 优化日期过滤类，支持 Date 格式
- 工作流插件 (perf): 优化部分 API 查询方式
- 工作流插件 (perf): 优化系统默认审批人判断逻辑
- 工作流插件 (feat): 增加流程禁用、启用 API
- 工作流插件 (feat): 支持当没有任何条件节点符合当前节点时，支持按照策略继续流转

### v1.0.15.18

- 工作流插件 (perf): 优化查询节点的 API 查询条件
- 工作流插件 (perf): 流程审批记录，增加审批实例的状态信息
- 工作流插件 (perf): 优化提交审批时，增加申请人姓名、描述信息
- 工作流插件 (feat): 增加按申请人获取流程实例 API

### v1.0.16

- 日志插件 (perf): 优化 LoggerPrinter 输出日志时携带 trackId
- 日志插件 (feat): 增加本地日志记录器
- 工作流插件 (feat): 已完成审批的流程实例审批记录会保存在流程实例中
- 工作流插件 (feat): 已完成审批的流程定义纲要记录会保存在流程实例中

### v1.0.16.1

- 日志插件 (feat): 本地日志记录支持按 tag 过滤
- 工作流插件 (feat): 支持流程强制中断（未审批用户状态为中断审批）
- 工作流插件 (feat): 流程实例支持提交 businessInfo 对象格式业务信息
- 工作流插件 (chore): 流程实例删除 businessId 和 businessType 字段

### v1.0.16.2

- 工作流插件 (bugfix): 修复提交审批时，创建时间错误的问题

### v1.0.16.3

- 工作流插件 (bugfix): 修复节点审批人更新失败的问题

### v1.0.16.4

- 工作流插件 (bugfix): 修复审批记录缺少审批时间字段的问题

### v1.0.16.5

- 工作流插件 (perf): 结构优化

### v1.0.16.6

- 工作流插件 (bugfix): 修复审批记录用户 ID 取值错误的问题
- 工作流插件 (perf): 优化审批记录，角色审批排序问题
- 工作流插件 (perf): 优化审批记录，添加审批人姓名、备注等字段
- 工作流插件 (feat): 流程实例支持任务转派给其他用户

### v1.0.16.7

- 工作流插件 (perf): 统一异常返回类型，均为流程异常类
- 工作流插件 (perf): 插入节点时，增加相邻节点判断
- 工作流插件 (perf): 增加节点定义转换方法
- 工作流插件 (bugfix): 修复空节点审批时，缺少判断角色审批状态
- 工作流插件 (bugfix): 修复删除节点定义时，最后一个任务节点也可以删除的问题
- 工作流插件 (bugfix): 修复更新节点定义时，未处理角色审批节点的问题
- 工作流插件 (bugfix): 修复更新节点定义时，当是动态审批节点时，用户审批人校验出错的问题
- 数据库插件 (bugfix): 判断表是否存在时，增加 catalog 字段限制
- 日志插件 (feat): 日志增加异常类的字段
- 日志插件 (feat): 增加清空日志接口
- 日志插件 (feat): 本地日志增加回调函数 LocalLogRecordPostProcessor 支持
- 日志插件 (feat): 增加日志异常信息统计接口
- 日志插件 (feat): 增加日志按 URI 查询接口

### v1.0.16.8

- 工作流插件 (bugfix): 修复 BusinessInfo 获取对象时，基本类型包装类强转出错的问题
- 工作流插件 (bugfix): 修复空节点审批时，缺少判断角色审批状态
- 工作流插件 (perf): 优化删除节点定义时，同时删除对应的审批用户
- 工作流插件 (perf): 优化审批用户修改逻辑
- 工作流插件 (perf): 部分方法，增加事务判断
- 工作流插件 (feat): 审批人更新：支持角色用户修改，支持跨组更新
- 工作流插件 (feat): 增加获取业务信息 API
- 工作流插件 (feat): 增加设置 BusinessInfo 的 API
- 数据库插件 (perf): 包路径扫描时增加对 @ComponentScan 的 value 属性支持
- 数据库插件 (feat): 支持在指定数据库下创建表

### v1.0.16.9

- 数据库插件 (bugfix): 修复获取数据库名称时，包含特殊字符会报错的问题

### v1.0.16.10

- 数据库插件 (bugfix): 修复 @ComponentScan 获取 value 属性时，取值错误的问题

### v1.0.17

- 数据库插件 (bugfix): 修复 @EntityClass 获取 schema 抛出空指针的问题
- 数据库插件 (bugfix): 修复只使用 @EntityClass 注解时，解析表名失败的问题
- 数据库插件 (feat): 增加 EntityClassDomain 中的 schemas 属性
- 数据库插件 (feat): 增加创建表和更新表的监听器
- 数据库插件 (feat): 支持创建表时，进行字段排序
- 工作流插件 (feat): 流程实例增加待审批状态
- 工作流插件 (feat): 流程定义增加 lock 属性，影响是否可以提交新的流程

### v1.0.17.1

- 数据库插件 (perf): 修复当索引名称过长时数据库报错的问题，会取首字母作为索引名称
- 数据库插件 (perf): 创建表和更新表的监听器增加默认方法
- 数据库插件 (feat): 增加全局表监听器 TableCreateListener 和 TableUpdateListener

### v1.0.17.2

- 工作流插件 (bugfix): 修复动态审批节点获取当前审批人时报错的问题

### v1.0.17.3

- 通用 (feat): 增加 DataValidator 数据校验工具类
  通用 (perf): 增加 PagingRequest 和 PagingResponse 分页工具类
- 工作流插件 (perf): 模型增加 @Schema 注释

### v1.0.17.4

- 通用 (perf): 分页工具类 PagingRequest 和 PagingResponse 增加 @Schema 注释
- 通用 (perf): MapWrapper 支持驼峰和下划线字段格式转换
- 日志插件 (perf): 日志查询接口增加 @Schema 注释

### v1.0.18

- 数据库插件 (bugfix): 解决 @EntityColumn 的 sequence 排序错误的问题
- 通用 (perf): MapWrapper 增加条件添加判断逻辑
- 通用 (perf): DataValidator 可在创建后，设置默认异常类型
- 通用 (feat): 增加 Processor 执行流程管理工具类

### v1.0.18.1

- 通用 (perf): DataValidator 增加是否立即抛出异常的枚举
- 通用 (perf): Processor 优化执行流程，增加校验方法

### v1.0.18.2

- 通用 (perf): Processor 优化执行逻辑，减少强转

### v1.0.18.3

- 通用 (perf): Processor 优化 next 方法
- 通用 (perf): Processor 优化 validate 方法

### v1.0.18.4

- 通用 (perf): DataValidator 异常类型修改为 Throwable
- 通用 (perf): Processor 异常类型修改为 Throwable

### v1.0.18.5

- 通用 (feat): Processor 增加 Supplier 和 Runnable 类型的 complete 方法

### v1.0.18.6

- 通用 (perf): Processor 优化 start 方法，避免类型转换

### v1.0.18.7

- 工作流插件 (bugfix): 修复审批记录插入时机错误问题
- 工作流插件 (perf): 优化当节点定义不存在时抛出的异常信息
- 工作流插件 (perf): 审批记录增加用户审批类型字段
- 通用 (perf): Processor 增加 Runnable 类型的 next 方法
- 通用 (perf): Processor 增加 Runnable 类型的 validate 方法
- 通用 (feat): Processor 增加 interim 方法，支持中间状态过渡

### v1.0.18.8

- 通用 (bugfix): 修复 Processor 异常没有传递的问题
- 通用 (feat): Processor 增加 Predicate 类型的 validate 方法
- 通用 (feat): Processor 增加 Consumer 类型的 registerExceptionListener 方法
- 通用 (feat): Processor 增加 BiFunction 和 BiConsumer 类型的 complete 方法

### v1.0.18.9

- 通用 (perf): Processor 优化异常处理
- 通用 (feat): 优化 R 类，增加 extractData 方法
- 通用 (feat): 增加 MessageUtils 工具类
- 通用 (feat): 增加 IpUtils 工具类
- 工作流插件 (feat): 获取审批记录时，增加自定义审批状态描述功能

### v1.0.18.10

- 工作流插件 (bugfix): 修复否有正在进行的流程实例时缺少待审批条件的问题
- 通用 (feat): 优化 R 类，增加判断响应是否成功 succeed 方法

### v1.0.19

- 幂等插件 (perf): IdempotentAdvisor 设置优先级为最低
- 幂等插件 (feat): @IdempotentKey 支持注解在父类的属性上
- 幂等插件 (feat): AgileIdempotentContext 提供获取 IdempotentKey 的方法
- 敏感词插件 (feat): 新增敏感词插件

### v1.0.19.1

- 敏感词插件 (feat): 新增直接获取结果和获取结果时抛出异常的方法

### v1.0.19.2

- 敏感词插件 (perf): 优化敏感词加载逻辑
- 敏感词插件 (feat): 新增`AgileOtherTypeSensitiveLoader`用于加载其他方式读取敏感字
- 敏感词插件 (feat): 提供`reload`重新加载敏感词库的方法

### v1.0.19.3

- 工作流插件 (bugfix): 修复动态审批节点，审批人名称获取不到的问题
- 工作流插件 (bugfix): 修复动态审批人排序时出现 null 值的问题
- 工作流插件 (bugfix): 修复中断、取消、委派审批时，判断流程是否结束时的出错问题
- 工作流插件 (bugfix): 修复非首节点时动态审批节点时，多动态审批人设置错误的问题
- 工作流插件 (perf): 增加查找流程实例的重载方法
- 工作流插件 (feat): 增加通过流程实例，节点定义获取任务实例的方法

### v1.0.19.4

- 工作流插件 (bugfix): 修复审核时，没有重新设置 TaskApprove 为最新值的问题
- 工作流插件 (bugfix): 修复更新节点时，动态审批节点 NodeAssignment 没有写入的问题
- 工作流插件 (bugfix): 修复撤回审批时时角色审批记录 TaskRoleApproveRecord 没有清除历史数据的问题
- 工作流插件 (bugfix): 修复多角色或签，角色用户会签时，判断是否需要流转到下一节点的问题
- 工作流插件 (feat): 增加查找 taskInstance 的重载方法
- 通用 (feat): feat: 增加 BaseCodeDescEnum 基类

### v1.0.19.5

- 工作流插件 (bugfix): 修复自动审批时，缺少角色的自动审批判断
- 工作流插件 (feat): 增加`AgileAutoApproveProcessor`自动审批回调处理器
- 通用 (perf): Processor 增加 Runnable 类型的 start 方法

### v1.0.19.6

- 工作流插件 (bugfix): 修复动态审批节点，会签时审批人数量设置错误的问题
- 工作流插件 (bugfix): 修复动态审批节点，撤回审批时审批人数量设置错误的问题

### v1.0.19.7

- 数据库插件 (perf): 优化忽略创建实体类时的日志打印
- 工作流插件 (feat): 增加设置动态审批人时，支持自动审批功能

### v1.0.19.8

- 工作流插件 (bugfix): 修复用户节点自动审批时缺少判断角色用户审批记录的问题
- 工作流插件 (perf): 删除`AgileAutoApproveProcessor`自动审批回调处理器
- 工作流插件 (perf): 增加`AgileApproveProcessor`审批回调处理器，增加`preApprove`、`postApproved`、`approveCompleted`方法
- 工作流插件 (perf): 审批意见返回 null 或者时自动审核返回的 comment，则不修改

### v1.0.19.9

- 工作流插件 (perf): 同意将审批监听器命名为 listener 后缀
- 工作流插件 (feat): 新增`AgileAbandonListener`、`AgileRejectListener`、`AgileRedoListener`审批监听器

### v1.0.19.10

- 工作流插件 (bugfix): 修复设置动态审批人数量时，缺少判断节点类型的问题
- 工作流插件 (perf): 优化一些查询条件

### v1.0.20

- Mock 插件 (feat): 新增 Mock 插件

### v1.0.20.1

- 分布式锁插件 (feat): 分布式锁插件支持动态参数表达式

### v1.0.20.2

- 工作流插件 (bugfix): 修复允许自动审批时，任务节点获取默认审批人的时报错的问题

### v1.0.20.3

- 微信开发平台 (feat): GetUnlimitedQRCodeRequest 增加 page 字段