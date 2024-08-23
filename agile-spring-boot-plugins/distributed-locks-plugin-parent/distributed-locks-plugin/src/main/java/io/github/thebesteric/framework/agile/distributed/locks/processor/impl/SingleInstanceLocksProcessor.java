package io.github.thebesteric.framework.agile.distributed.locks.processor.impl;

import io.github.thebesteric.framework.agile.core.func.SuccessFailureExecutor;
import io.github.thebesteric.framework.agile.distributed.locks.processor.DistributedLocksProcessor;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * SingleInstanceLocksProcessor
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-22 17:08:52
 */
public class SingleInstanceLocksProcessor implements DistributedLocksProcessor {

    private final ReentrantLock lock = new ReentrantLock();

    @Override
    public void execute(String key, long waitTime, long leaseTime, TimeUnit timeUnit, SuccessFailureExecutor<Boolean, Boolean, Exception> executor) {
        boolean isLocked;
        try {
            isLocked = lock.tryLock(waitTime, timeUnit);
            if (!isLocked) {
                executor.failure(false);
                return;
            }
            executor.success(true);
        } catch (Exception ex) {
            Thread.currentThread().interrupt();
            executor.exception(ex);
        } finally {
            lock.unlock();
        }
    }
}
