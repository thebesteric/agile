package io.github.thebesteric.framework.agile.plugins.logger.recorder.processor.impl;

import io.github.thebesteric.framework.agile.plugins.logger.domain.InvokeLog;
import io.github.thebesteric.framework.agile.plugins.logger.recorder.processor.LocalLogRecordPostProcessor;

/**
 * 默认日志被记录前的回调
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-11-13 10:29:51
 */
public class DefaultLocalLogRecordPostProcessor implements LocalLogRecordPostProcessor {
    @Override
    public boolean postProcessBeforeRecord(InvokeLog invokeLog) {
        return true;
    }
}
