package io.github.thebesteric.framework.agile.plugins.idempotent.exception;

import io.github.thebesteric.framework.agile.commons.util.MessageFormatUtils;

import java.io.Serial;

/**
 * IdempotentException
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-04-30 17:30:19
 */
public class IdempotentException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -366445751364958694L;

    public IdempotentException() {
        super("Idempotent check exception");
    }

    public IdempotentException(String message, Object... params) {
        super(MessageFormatUtils.format(message, params));
    }
}
