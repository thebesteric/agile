package io.github.thebesteric.framework.agile.test.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import io.github.thebesteric.framework.agile.core.matcher.clazz.ClassMatcher;
import io.github.thebesteric.framework.agile.core.matcher.clazz.impl.ControllerBeanClassMatcher;
import io.github.thebesteric.framework.agile.plugins.annotation.scanner.AnnotationRegister;
import io.github.thebesteric.framework.agile.plugins.annotation.scanner.domain.Parasitic;
import io.github.thebesteric.framework.agile.plugins.annotation.scanner.listener.AnnotationParasiticRegisteredListener;
import io.github.thebesteric.framework.agile.plugins.logger.config.AgileLoggerProperties;
import io.github.thebesteric.framework.agile.plugins.logger.domain.InvokeLog;
import io.github.thebesteric.framework.agile.plugins.logger.domain.RequestLog;
import io.github.thebesteric.framework.agile.plugins.logger.processor.ignore.RequestIgnoreProcessor;
import io.github.thebesteric.framework.agile.plugins.logger.processor.ignore.impl.HeaderIgnoreProcessor;
import io.github.thebesteric.framework.agile.plugins.logger.processor.ignore.impl.ParameterIgnoreProcessor;
import io.github.thebesteric.framework.agile.plugins.logger.processor.recorder.Recorder;
import io.github.thebesteric.framework.agile.plugins.logger.processor.recorder.impl.CustomRecorder;
import io.github.thebesteric.framework.agile.plugins.logger.recorder.processor.LocalLogRecordPostProcessor;
import io.github.thebesteric.framework.agile.plugins.sensitive.filter.domain.SensitiveFilterResult;
import io.github.thebesteric.framework.agile.plugins.sensitive.filter.extension.AgileOtherTypeSensitiveLoader;
import io.github.thebesteric.framework.agile.plugins.sensitive.filter.extension.AgileSensitiveResultProcessor;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.ContinuousApproveMode;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.TaskInstance;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowInstance;
import io.github.thebesteric.framework.agile.plugins.workflow.listener.AgileApproveListener;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * AgileLoggerConfig
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-04-06 22:10:49
 */
@Configuration
@EnableConfigurationProperties(AgileLoggerProperties.class)
public class AgileConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisSerializer<Object> serializer = redisSerializer();
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(serializer);
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(serializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    public RedisSerializer<Object> redisSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);

        // 自定义 ObjectMapper 的时间处理模块
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern("HH:mm:ss")));
        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern("HH:mm:ss")));
        objectMapper.registerModule(javaTimeModule);

        // 禁用将日期序列化为时间戳的行为
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);
    }

    @Bean
    public Recorder customRecorder(AgileLoggerProperties properties) {
        return new CustomRecorder(properties) {
            @Override
            protected void doProcess(InvokeLog invokeLog) {
                System.out.println("This is my custom log: " + invokeLog.getLogId());
            }
        };
    }

    @Bean
    public RequestIgnoreProcessor headerIgnoreProcessor() {
        return new HeaderIgnoreProcessor() {
            @Override
            protected String[] doIgnore(RequestLog requestLog) {
                return new String[]{"apple", "banana", "postman-token"};
            }

            @Override
            protected Map<String, String> doRewrite(RequestLog requestLog) {
                return Map.of("test", "test**");
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
                return Map.of("name", "name**");
            }
        };
    }

    // @Bean
    // public IdempotentProcessor redisIdempotentProcessor(RedissonClient redissonClient) {
    //     return new RedisIdempotentProcessor(redissonClient);
    // }

    // @Bean
    // public RateLimiterProcessor redisRateLimiterProcessor(RedisTemplate<String, Object> redisTemplate) {
    //     return new RedisRateLimiterProcessor(redisTemplate);
    // }

    @Bean
    public List<ClassMatcher> idempotentCustomClassMatcher() {
        return List.of(new ControllerBeanClassMatcher());
    }

    @Bean
    public AnnotationRegister annotationRegister() {
        AnnotationRegister annotationRegister = new AnnotationRegister();
        annotationRegister.register(CrossOrigin.class, parasitic -> true);
        annotationRegister.register(RestController.class, parasitic -> true);
        return annotationRegister;
    }

    @Bean
    public AnnotationParasiticRegisteredListener listener() {
        return new AnnotationParasiticRegisteredListener() {
            @Override
            public void onClassParasiticRegistered(Parasitic parasitic) {
                String annotation = parasitic.getAnnotation().annotationType().getName();
                System.out.println("onClassParasiticRegistered: " + annotation + " - " + parasitic.getClazz().getName());
            }

            @Override
            public void onMethodParasiticRegistered(Parasitic parasitic) {
                String annotation = parasitic.getAnnotation().annotationType().getName();
                System.out.println("onMethodParasiticRegistered: " + annotation + " - " + parasitic.getClazz().getName() + " - " + parasitic.getMethod().getName());
            }
        };
    }

    @Bean
    public LocalLogRecordPostProcessor customLocalLogRecordPostProcessor() {
        return new LocalLogRecordPostProcessor() {
            @Override
            public boolean postProcessBeforeRecord(InvokeLog invokeLog) {
                Class<?> exceptionClass = invokeLog.getExceptionClass();
                if (exceptionClass == ArithmeticException.class) {
                    return false;
                }
                return true;
            }
        };
    }

    @Bean
    public AgileSensitiveResultProcessor sensitiveResultProcessor() {
        return new AgileSensitiveResultProcessor() {
            @Override
            public void process(SensitiveFilterResult result) {
                result.setResult(result.getResult() + " => 稍微修改了一下");
            }
        };
    }

    @Bean
    public AgileOtherTypeSensitiveLoader otherTypeSensitiveLoader() {
        return new AgileOtherTypeSensitiveLoader() {
            @Override
            public List<String> load() {
                return List.of("赌博", "嫖娼");
            }
        };
    }

    @Bean
    public AgileApproveListener agileApproveProcessor() {
        return new AgileApproveListener() {
            @Override
            public String preApprove(TaskInstance taskInstance, String roleId, String userId) {
                System.out.println("============== preApprove ============== " + taskInstance);
                return "我同意了";
            }

            @Override
            public String preAutoApprove(ContinuousApproveMode approveMode, TaskInstance taskInstance, String roleId, String userId) {
                System.out.println("============== preAutoApprove ============== " + taskInstance);
                return "我是自动同意的";
            }

            @Override
            public void postAutoApproved(ContinuousApproveMode approveMode, TaskInstance taskInstance, String roleId, String userId, String comment) {
                System.out.println("============== postAutoApproved ============== " + taskInstance);
            }

            @Override
            public void postApproved(TaskInstance taskInstance, String roleId, String userId, String comment) {
                System.out.println("============== postApproved ============== " + taskInstance);
            }

            @Override
            public void approveCompleted(WorkflowInstance workflowInstance, TaskInstance taskInstance, String roleId, String userId, String comment) {
                System.out.println("============== approveCompleted ============== " + workflowInstance);
            }
        };
    }

}
