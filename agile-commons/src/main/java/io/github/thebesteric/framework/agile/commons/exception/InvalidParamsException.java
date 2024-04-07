package io.github.thebesteric.framework.agile.commons.exception;

/**
 * InvalidDataException
 *
 * @author Eric Joe
 * @version 1.0
 */
public class InvalidParamsException extends RuntimeException {
    public InvalidParamsException() {
        super("Invalid params exception");
    }

    public InvalidParamsException(String message, Object... params) {
        super(String.format(message, params));
    }
}
