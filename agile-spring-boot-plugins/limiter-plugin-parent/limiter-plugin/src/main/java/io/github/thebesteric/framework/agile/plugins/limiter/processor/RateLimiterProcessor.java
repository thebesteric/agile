package io.github.thebesteric.framework.agile.plugins.limiter.processor;

import io.github.thebesteric.framework.agile.core.func.SuccessFailureExecutor;

import java.util.concurrent.TimeUnit;

public interface RateLimiterProcessor {

    void execute(String key, int timeout, int count, TimeUnit timeUnit, SuccessFailureExecutor<Boolean, Boolean, Exception> executor);

}
