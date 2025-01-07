package io.github.thebesteric.framework.agile.plugins.idempotent.redis.processor.impl;

import io.github.thebesteric.framework.agile.core.func.SuccessFailureExecutor;
import io.github.thebesteric.framework.agile.plugins.idempotent.processor.IdempotentProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@RequiredArgsConstructor
public class RedisIdempotentProcessor implements IdempotentProcessor {

    private final RedissonClient redissonClient;

    @Override
    public void execute(String key, Long value, long duration, TimeUnit timeUnit, SuccessFailureExecutor<Boolean, Boolean, Exception> executor) {
        RLock lock = redissonClient.getLock(key);
        boolean isLocked = false;
        try {
            isLocked = lock.tryLock();
            if (!isLocked) {
                executor.failure(false);
                return;
            }
            lock.lock(duration, timeUnit);
            executor.success(true);
        } catch (Exception ex) {
            log.error("RedisIdempotentProcessor execute error", ex);
            executor.exception(ex);
        } finally {
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
            executor.complete();
        }
    }
}
