package io.github.thebesteric.framework.agile.commons.exception;

import java.io.Serial;

/**
 * EncryptException
 *
 * @author wangweijun
 * @version v1.0
 * @since 2026-01-24 11:30:44
 */
public class EncryptException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1055626116045997368L;

    public EncryptException(String message, Throwable cause) {
        super(message, cause);
    }
}
