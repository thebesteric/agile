package io.github.thebesteric.framework.agile.commons.exception;

import java.io.Serial;

/**
 * InvalidDataException
 *
 * @author Eric Joe
 * @version 1.0
 */
public class InvalidParamsException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 4455818024758136958L;

    public InvalidParamsException() {
        super("Invalid params exception");
    }

    public InvalidParamsException(String message, Object... params) {
        super(String.format(message, params));
    }
}
