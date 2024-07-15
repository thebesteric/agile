package io.github.thebesteric.framework.agile.plugins.database.core.exception;

import java.io.Serial;

/**
 * QueryParseException
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-15 14:49:11
 */
public class QueryParseException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 130502460417670043L;

    public QueryParseException() {
        super("Query parse exception");
    }

    public QueryParseException(String message, Object... params) {
        super(String.format(message, params));
    }

    public QueryParseException(Throwable cause) {
        super(cause);
    }

    public QueryParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
