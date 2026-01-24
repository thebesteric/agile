package io.github.thebesteric.framework.agile.commons.exception;

import java.io.Serial;

/**
 * DecryptException
 *
 * @author wangweijun
 * @version v1.0
 * @since 2026-01-24 11:32:27
 */
public class DecryptException extends EncryptException {
    @Serial
    private static final long serialVersionUID = -2381641737665890915L;

    public DecryptException(String message, Throwable cause) {
        super(message, cause);
    }
}
