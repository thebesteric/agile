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