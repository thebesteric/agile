package io.github.thebesteric.framework.agile.commons.exception;

import java.io.Serial;

/**
 * DuplicateParamsException
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-05-23 09:47:22
 */
public class DuplicateParamsException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -5660802920756506023L;

    public DuplicateParamsException() {
        super("Duplicate params exception");
    }

    public DuplicateParamsException(String message, Object... params) {
        super(String.format(message, params));
    }
}
