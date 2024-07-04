package io.github.thebesteric.framework.agile.commons.exception;

import java.io.Serial;

/**
 * InvalidDataException
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-24 19:14:23
 */
public class InvalidDataException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 3156845484638159224L;

    public InvalidDataException() {
        super("Invalid data exception");
    }

    public InvalidDataException(String message, Object... params) {
        super(String.format(message, params));
    }
}
