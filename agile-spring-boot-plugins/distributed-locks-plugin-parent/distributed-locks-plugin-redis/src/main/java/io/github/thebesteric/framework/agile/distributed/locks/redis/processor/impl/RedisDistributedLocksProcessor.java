package io.github.thebesteric.framework.agile.distributed.locks.redis.processor.impl;

import io.github.thebesteric.framework.agile.core.func.SuccessFailureExecutor;
import io.github.thebesteric.framework.agile.distributed.locks.processor.DistributedLocksProcessor;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

/**
 * RedisDistributedLocksProcessor
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-23 10:29:19
 */
@RequiredArgsConstructor
public class RedisDistributedLocksProcessor implements DistributedLocksProcessor {

    private final RedissonClient redissonClient;

    @Override
    public void execute(String key, long waitTime, long leaseTime, TimeUnit timeUnit, SuccessFailureExecutor<Boolean, Boolean, Exception> executor) {
        RLock lock = redissonClient.getLock(key);
        boolean isLocked = false;
        try {
            isLocked = lock.tryLock(waitTime, leaseTime, timeUnit);
            if (!isLocked) {
                executor.failure(false);
                return;
            }
            executor.success(true);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            executor.exception(ex);
        } finally {
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
