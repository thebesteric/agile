package io.github.thebesteric.framework.agile.plugins.limiter.processor.impl;

import com.google.common.util.concurrent.RateLimiter;
import io.github.thebesteric.framework.agile.plugins.limiter.processor.RateLimiterProcessor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * InMemoryRateLimiterProcessor
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-05-06 17:20:53
 */
public class InMemoryRateLimiterProcessor implements RateLimiterProcessor {

    private static final Map<String, RateLimiter> RATE_LIMITERS = new ConcurrentHashMap<>(128);

    @Override
    public boolean tryRateLimit(String key, int timeout, int count, TimeUnit timeUnit) {
        RateLimiter rateLimiter = RATE_LIMITERS.get(key);
        long seconds = TimeUnit.SECONDS.convert(timeout, timeUnit);
        // 计算出每秒的个数
        double perSecondCount = (double) count / seconds;
        BigDecimal bdResult = BigDecimal.valueOf(perSecondCount);
        bdResult = bdResult.setScale(2, RoundingMode.HALF_UP);
        if (rateLimiter == null) {
            rateLimiter = RateLimiter.create(Double.parseDouble(bdResult.toPlainString()));
            RATE_LIMITERS.put(key, rateLimiter);
        }
        if (rateLimiter.tryAcquire()) {
            return true;
        }
        return false;
    }
}
