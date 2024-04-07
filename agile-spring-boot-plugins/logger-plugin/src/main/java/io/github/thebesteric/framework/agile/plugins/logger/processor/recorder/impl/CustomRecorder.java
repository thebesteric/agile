package io.github.thebesteric.framework.agile.plugins.logger.processor.recorder.impl;

import io.github.thebesteric.framework.agile.plugins.logger.config.AgileLoggerProperties;
import io.github.thebesteric.framework.agile.plugins.logger.domain.InvokeLog;
import io.github.thebesteric.framework.agile.plugins.logger.constant.LogMode;
import io.github.thebesteric.framework.agile.plugins.logger.processor.recorder.AbstractThreadPoolRecorder;

/**
 * CustomRecorder
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-03-12 09:35:00
 */
public abstract class CustomRecorder extends AbstractThreadPoolRecorder {

    protected CustomRecorder(AgileLoggerProperties properties) {
        super(properties);
    }

    @Override
    public boolean support(LogMode model) {
        return model != null && !model.getName().trim().isEmpty() && LogMode.CUSTOM == model;
    }

    @Override
    protected abstract void doProcess(InvokeLog invokeLog);


}
