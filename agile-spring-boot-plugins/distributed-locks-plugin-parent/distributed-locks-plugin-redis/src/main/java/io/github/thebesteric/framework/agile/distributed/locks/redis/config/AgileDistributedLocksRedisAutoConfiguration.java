package io.github.thebesteric.framework.agile.distributed.locks.redis.config;

import io.github.thebesteric.framework.agile.core.AgileConstants;
import io.github.thebesteric.framework.agile.distributed.locks.config.AgileDistributedLocksProperties;
import io.github.thebesteric.framework.agile.distributed.locks.processor.DistributedLocksProcessor;
import io.github.thebesteric.framework.agile.distributed.locks.redis.processor.impl.RedisDistributedLocksProcessor;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AgileDistributedLocksRedisAutoConfiguration
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-23 10:25:51
 */
@Configuration
@EnableConfigurationProperties(AgileDistributedLocksProperties.class)
@ConditionalOnProperty(prefix = AgileConstants.PROPERTIES_PREFIX + ".distribute-locks", name = "enable", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(RedissonClient.class)
public class AgileDistributedLocksRedisAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(DistributedLocksProcessor.class)
    public DistributedLocksProcessor redisDistributedLocksProcessor(RedissonClient redissonClient) {
        return new RedisDistributedLocksProcessor(redissonClient);
    }

}