package io.github.thebesteric.framework.agile.plugins.idempotent.processor.impl;

import io.github.thebesteric.framework.agile.plugins.idempotent.processor.IdempotentProcessor;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

/**
 * RedisIdempotentProcessor
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-05-04 17:54:28
 */
@RequiredArgsConstructor
public class RedisIdempotentProcessor implements IdempotentProcessor {

    private final RedissonClient redissonClient;

    @Override
    public boolean tryLock(String key, Long value, long duration, TimeUnit timeUnit) {
        RLock lock = redissonClient.getLock(key);
        boolean isLocked = false;
        try {
            isLocked = lock.tryLock();
            if (!isLocked) {
                return false;
            }
            lock.lock(duration, timeUnit);
            return true;
        } finally {
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
