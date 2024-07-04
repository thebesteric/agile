package io.github.thebesteric.framework.agile.commons.exception;

import java.io.Serial;

/**
 * DataExistsException
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-24 13:23:40
 */
public class DataExistsException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 8073234923391481924L;

    public DataExistsException() {
        super("Data exists exception");
    }

    public DataExistsException(String message, Object... params) {
        super(String.format(message, params));
    }

    public DataExistsException(Throwable cause) {
        super(cause);
    }

    public DataExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
