package io.github.thebesteric.framework.agile.plugins.limiter.exception;

/**
 * RateLimitException
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-05-06 19:00:19
 */
public class RateLimitException extends RuntimeException {
    public RateLimitException() {
        super("Rate limit exception");
    }
    public RateLimitException(String message, Object... params) {
        super(String.format(message, params));
    }
}
