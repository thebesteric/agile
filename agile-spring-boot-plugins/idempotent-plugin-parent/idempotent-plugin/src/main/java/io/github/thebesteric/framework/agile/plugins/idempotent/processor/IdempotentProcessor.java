package io.github.thebesteric.framework.agile.plugins.idempotent.processor;

import io.github.thebesteric.framework.agile.core.func.SuccessFailureExecutor;

import java.util.concurrent.TimeUnit;

public interface IdempotentProcessor {

    /**
     * 尝试枷锁
     *
     * @param key      key
     * @param value    value
     * @param duration 加锁时长
     * @param timeUnit 时间单位
     * @param executor 执行器
     *
     * @author wangweijun
     * @since 2024/5/4 17:28
     */
    void execute(String key, Long value, long duration, TimeUnit timeUnit, SuccessFailureExecutor<Boolean, Boolean, Exception> executor);
}
