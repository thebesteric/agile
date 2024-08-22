package io.github.thebesteric.framework.agile.plugins.limiter.redis.processor.impl;

import io.github.thebesteric.framework.agile.core.func.SuccessFailureExecutor;
import io.github.thebesteric.framework.agile.plugins.limiter.processor.RateLimiterProcessor;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * RedisRateLimiterProcessor
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-05-06 19:06:10
 */
@RequiredArgsConstructor
public class RedisRateLimiterProcessor implements RateLimiterProcessor {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final DefaultRedisScript<Long> script = limitScript();

    @Override
    public void execute(String key, int timeout, int count, TimeUnit timeUnit, SuccessFailureExecutor<Boolean, Boolean, Exception> executor) {
        // 计算出每秒的个数
        long seconds = timeUnit.toSeconds(timeout);
        double perSecondCount = (double) count / seconds;
        BigDecimal bdResult = BigDecimal.valueOf(perSecondCount);
        bdResult = bdResult.setScale(1, RoundingMode.HALF_UP);
        double finalPerSecondCount = Double.parseDouble(bdResult.toPlainString());
        Try<Long> tryFunc = Try.of(() -> redisTemplate.execute(script, Collections.singletonList(key), 1, finalPerSecondCount));
        // 判断是否超过限流阈值
        if (tryFunc.get() <= perSecondCount) {
            executor.success(true);
            return;
        }
        executor.failure(false);
    }

    private static DefaultRedisScript<Long> limitScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setResultType(Long.class);
        script.setScriptText("""
                local key = KEYS[1]
                local timeout = tonumber(ARGV[1])
                local count = tonumber(ARGV[2])
                if not timeout or not count or timeout <= 0 or count <= 0 then
                    return -1;
                end
                local current = redis.call('get', key)
                if current and tonumber(current) > count then
                    return tonumber(current)
                end
                current = redis.call('incr', key)
                if tonumber(current) == 1 then
                    redis.call('expire', key, timeout)
                end
                return tonumber(current)
                """);
        return script;
    }
}
