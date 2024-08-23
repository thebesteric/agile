package io.github.thebesteric.framework.agile.distributed.locks.exeption;

import java.io.Serial;

/**
 * DistributedLocksException
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-04-30 17:30:19
 */
public class DistributedLocksException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -366445751364958694L;

    public DistributedLocksException() {
        super("Distributed locks exception");
    }
    public DistributedLocksException(String message, Object... params) {
        super(String.format(message, params));
    }
}
