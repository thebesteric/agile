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