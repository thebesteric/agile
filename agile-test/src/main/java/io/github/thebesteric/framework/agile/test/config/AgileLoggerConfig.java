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
public class AgileLoggerConfig {

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

    // @Bean
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

}
