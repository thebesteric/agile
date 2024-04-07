# Agile Framework
> Agile Framework for building web applications easily

## 日志插件
### 相关配置
```yaml
sourceflag:
  agile:
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