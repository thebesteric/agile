package io.github.thebesteric.framework.agile.commons.exception;

import java.io.Serial;

/**
 * AccessDeniedException
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-25 13:28:37
 */
public class AccessDeniedException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -3830721673552550883L;

    public AccessDeniedException() {
        super("Illegal access exception");
    }

    public AccessDeniedException(String message, Object... params) {
        super(String.format(message, params));
    }

    public AccessDeniedException(Throwable cause) {
        super(cause);
    }

    public AccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
}
