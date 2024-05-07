package io.github.thebesteric.framework.agile.plugins.limiter.processor;

import java.util.concurrent.TimeUnit;

public interface RateLimiterProcessor {

    boolean tryRateLimit(String key, int timeout, int count, TimeUnit timeUnit);

}
