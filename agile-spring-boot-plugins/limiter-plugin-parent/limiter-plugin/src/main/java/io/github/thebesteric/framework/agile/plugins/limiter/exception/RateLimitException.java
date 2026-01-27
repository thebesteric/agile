package io.github.thebesteric.framework.agile.plugins.limiter.exception;

import io.github.thebesteric.framework.agile.commons.util.MessageFormatUtils;

import java.io.Serial;

/**
 * RateLimitException
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-05-06 19:00:19
 */
public class RateLimitException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 3197486896982887488L;

    public RateLimitException() {
        super("Rate limit exception");
    }

    public RateLimitException(String message, Object... params) {
        super(MessageFormatUtils.format(message, params));
    }
}
