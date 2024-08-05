# 控制器入参校验控制
@Required({"name", "age", "price"})
@Required(dependsOn={@Depends(value={"name", "age"}, valid="NotNull"), @Depends({"price"})})

# 控制器权限控制
@Permission(value={"user:add", "user:delete"}, operate="and")
@Permission(value={"user:add", "user:delete"}, operate="any")
@DenyAccess

# 多版本
@Version(XxxVersion.class)

# Mock 数据
@Mock

# 支持启动后导入 sql 脚本
database.executeSqlScript
ResourceDatabasePopulator
https://blog.csdn.net/weixin_43888891/article/details/130305198

# Logger BUGs
- 实体类使用了 typeHandler 的时候，序列化的时候会报错