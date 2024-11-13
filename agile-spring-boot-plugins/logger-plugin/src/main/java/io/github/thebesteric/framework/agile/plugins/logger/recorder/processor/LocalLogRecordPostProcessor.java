package io.github.thebesteric.framework.agile.plugins.logger.recorder.processor;

import io.github.thebesteric.framework.agile.plugins.logger.domain.InvokeLog;

public interface LocalLogRecordPostProcessor {
    /**
     * 日志被记录前的回调
     * <p>如果返回 true 则表示需要记录，否则不记录</p>
     *
     * @param invokeLog 日志
     *
     * @return boolean
     *
     * @author wangweijun
     * @since 2024/11/13 10:26
     */
    boolean postProcessBeforeRecord(InvokeLog invokeLog);
}
