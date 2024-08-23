package io.github.thebesteric.framework.agile.distributed.locks.processor;

import io.github.thebesteric.framework.agile.core.func.SuccessFailureExecutor;

import java.util.concurrent.TimeUnit;

public interface DistributedLocksProcessor {
    /**
     * 尝试枷锁
     *
     * @param key       key
     * @param waitTime  等待时间
     * @param leaseTime 租约时间
     * @param timeUnit  时间单位
     * @param executor  执行器
     *
     * @author wangweijun
     * @since 2024/8/22 16:59
     */
    void execute(String key, long waitTime, long leaseTime, TimeUnit timeUnit, SuccessFailureExecutor<Boolean, Boolean, Exception> executor);
}
