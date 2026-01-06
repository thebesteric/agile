package io.github.thebesteric.framework.agile.commons.exception;

import java.io.Serial;

/**
 * DataValidationException
 *
 * @author wangweijun
 * @version v1.0
 * @since 2026-01-06 14:43:33
 */
public class DataValidationException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -7461325630482803625L;

    public DataValidationException() {
        super("Data validation exception");
    }

    public DataValidationException(String message, Object... params) {
        super(String.format(message, params));
    }

    public DataValidationException(Throwable cause) {
        super(cause);
    }

    public DataValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
