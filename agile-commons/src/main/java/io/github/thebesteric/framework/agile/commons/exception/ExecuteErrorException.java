package io.github.thebesteric.framework.agile.commons.exception;

import java.io.Serial;

/**
 * ExecuteErrorException
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-25 11:26:08
 */
public class ExecuteErrorException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -6876529781058325862L;

    public ExecuteErrorException() {
        super("Execute error exception");
    }

    public ExecuteErrorException(String message, Object... params) {
        super(String.format(message, params));
    }

    public ExecuteErrorException(Throwable cause) {
        super(cause);
    }

    public ExecuteErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}