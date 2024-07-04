package io.github.thebesteric.framework.agile.plugins.workflow.exception;

import java.io.Serial;

/**
 * 流程异常
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-25 13:28:37
 */
public class WorkflowException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -3830721673552550883L;

    public WorkflowException() {
        super("流程异常");
    }

    public WorkflowException(String message, Object... params) {
        super(String.format(message, params));
    }

    public WorkflowException(Throwable cause) {
        super(cause);
    }

    public WorkflowException(String message, Throwable cause) {
        super(message, cause);
    }
}
