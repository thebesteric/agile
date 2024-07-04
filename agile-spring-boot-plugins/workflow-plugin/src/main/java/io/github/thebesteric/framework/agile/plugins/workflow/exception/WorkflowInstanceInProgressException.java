package io.github.thebesteric.framework.agile.plugins.workflow.exception;

import java.io.Serial;

/**
 * InProgressException
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-02 19:15:20
 */
public class WorkflowInstanceInProgressException extends WorkflowException {
    @Serial
    private static final long serialVersionUID = 4203938964854518740L;

    public WorkflowInstanceInProgressException() {
        super("检测到有正在进行的流程实例，请先完成后再进行操作");
    }
}
