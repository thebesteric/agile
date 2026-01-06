package io.github.thebesteric.framework.agile.plugins.idempotent.redis.config;

import io.github.thebesteric.framework.agile.core.AgileConstants;
import io.github.thebesteric.framework.agile.plugins.idempotent.config.AgileIdempotentProperties;
import io.github.thebesteric.framework.agile.plugins.idempotent.processor.IdempotentProcessor;
import io.github.thebesteric.framework.agile.plugins.idempotent.redis.processor.impl.RedisIdempotentProcessor;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AgileIdempotentRedisAutoConfiguration
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-23 10:03:16
 */
@Configuration
@EnableConfigurationProperties(AgileIdempotentProperties.class)
@ConditionalOnProperty(prefix = AgileConstants.PROPERTIES_PREFIX + ".idempotent", name = "enable", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(RedissonClient.class)
public class AgileIdempotentRedisAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(IdempotentProcessor.class)
    public IdempotentProcessor redisIdempotentProcessor(RedissonClient redissonClient) {
        return new RedisIdempotentProcessor(redissonClient);
    }

}