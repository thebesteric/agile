package io.github.thebesteric.framework.agile.commons.exception;

import java.io.Serial;

/**
 * ParseErrorException
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-04-07 11:42:50
 */
public class ParseErrorException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -2992304852821095225L;

    public ParseErrorException() {
        super("Parse error exception");
    }

    public ParseErrorException(String message, Object... params) {
        super(String.format(message, params));
    }

    public ParseErrorException(Throwable cause) {
        super(cause);
    }

    public ParseErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
