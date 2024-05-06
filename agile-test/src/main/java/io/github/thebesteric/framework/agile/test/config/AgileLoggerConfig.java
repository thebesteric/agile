package io.github.thebesteric.framework.agile.test.config;

import io.github.thebesteric.framework.agile.plugins.idempotent.processor.IdempotentProcessor;
import io.github.thebesteric.framework.agile.plugins.idempotent.redis.processor.impl.RedisIdempotentProcessor;
import io.github.thebesteric.framework.agile.plugins.logger.config.AgileLoggerProperties;
import io.github.thebesteric.framework.agile.plugins.logger.domain.InvokeLog;
import io.github.thebesteric.framework.agile.plugins.logger.domain.RequestLog;
import io.github.thebesteric.framework.agile.plugins.logger.processor.ignore.RequestIgnoreProcessor;
import io.github.thebesteric.framework.agile.plugins.logger.processor.ignore.impl.HeaderIgnoreProcessor;
import io.github.thebesteric.framework.agile.plugins.logger.processor.ignore.impl.ParameterIgnoreProcessor;
import io.github.thebesteric.framework.agile.plugins.logger.processor.recorder.Recorder;
import io.github.thebesteric.framework.agile.plugins.logger.processor.recorder.impl.CustomRecorder;
import org.redisson.api.RedissonClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    @Bean
    public IdempotentProcessor redisIdempotentProcessor(RedissonClient redissonClient) {
        return new RedisIdempotentProcessor(redissonClient);
    }

}
