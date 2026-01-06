package io.github.thebesteric.framework.agile.plugins.limiter.redis.config;

import io.github.thebesteric.framework.agile.core.AgileConstants;
import io.github.thebesteric.framework.agile.plugins.limiter.config.AgileRateLimiterProperties;
import io.github.thebesteric.framework.agile.plugins.limiter.processor.RateLimiterProcessor;
import io.github.thebesteric.framework.agile.plugins.limiter.redis.processor.impl.RedisRateLimiterProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * AgileRateLimiterRedisAutoConfiguration
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-05-06 17:21:50
 */
@Configuration
@EnableConfigurationProperties(AgileRateLimiterProperties.class)
@ConditionalOnProperty(prefix = AgileConstants.PROPERTIES_PREFIX + ".limiter", name = "enable", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(RedisTemplate.class)
public class AgileRateLimiterRedisAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(RateLimiterProcessor.class)
    public RateLimiterProcessor redisRateLimiterProcessor(RedisTemplate<String, Object> redisTemplate) {
        return new RedisRateLimiterProcessor(redisTemplate);
    }
}