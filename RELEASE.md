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