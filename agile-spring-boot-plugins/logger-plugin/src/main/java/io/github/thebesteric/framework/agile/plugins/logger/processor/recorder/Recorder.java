package io.github.thebesteric.framework.agile.plugins.logger.processor.recorder;

import io.github.thebesteric.framework.agile.plugins.logger.domain.InvokeLog;
import io.github.thebesteric.framework.agile.plugins.logger.constant.LogMode;

/**
 * Recorder
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-03-11 18:04:57
 */
public interface Recorder {

    boolean support(LogMode model);

    void process(InvokeLog invokeLog);
}
