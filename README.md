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