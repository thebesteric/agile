package io.github.thebesteric.framework.agile.plugins.logger.processor.recorder.impl;

import io.github.thebesteric.framework.agile.plugins.logger.config.AgileLoggerProperties;
import io.github.thebesteric.framework.agile.plugins.logger.domain.InvokeLog;
import io.github.thebesteric.framework.agile.plugins.logger.constant.LogMode;
import io.github.thebesteric.framework.agile.plugins.logger.processor.recorder.AbstractThreadPoolRecorder;

/**
 * StdoutRecorder
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-03-11 18:28:13
 */
public class StdoutRecorder extends AbstractThreadPoolRecorder {

    public StdoutRecorder(AgileLoggerProperties properties) {
        super(properties);
    }

    @Override
    public boolean support(LogMode model) {
        return model == null || model.getName().trim().isEmpty() || LogMode.STDOUT == model;
    }

    @Override
    public void doProcess(InvokeLog invokeLog) {
        switch (invokeLog.getLevel()) {
            case WARN:
            case ERROR:
                System.err.println(invokeLog.print());
                break;
            default:
                System.out.println(invokeLog.print());
        }
    }
}
