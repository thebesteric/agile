package io.github.thebesteric.framework.agile.commons.exception;

/**
 * ParseErrorException
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-04-07 11:42:50
 */
public class ParseErrorException extends RuntimeException {
    public ParseErrorException() {
        super("Parse error exception");
    }

    public ParseErrorException(String message, Object... params) {
        super(String.format(message, params));
    }
}
