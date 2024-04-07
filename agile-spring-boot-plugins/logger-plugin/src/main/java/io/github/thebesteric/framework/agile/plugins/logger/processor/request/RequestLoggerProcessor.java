package io.github.thebesteric.framework.agile.plugins.logger.processor.request;


import io.github.thebesteric.framework.agile.commons.util.DurationWatcher;
import io.github.thebesteric.framework.agile.plugins.logger.domain.InvokeLog;
import io.github.thebesteric.framework.agile.plugins.logger.domain.RequestLog;
import io.github.thebesteric.framework.agile.plugins.logger.domain.SyntheticAgileLogger;
import io.github.thebesteric.framework.agile.plugins.logger.filter.warpper.AgileLoggerRequestWrapper;
import io.github.thebesteric.framework.agile.plugins.logger.filter.warpper.AgileLoggerResponseWrapper;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * RequestLoggerProcessor
 *
 * @author Eric Joe
 * @version 1.0
 */
public interface RequestLoggerProcessor {

    RequestLog processor(String id, Method method, AgileLoggerRequestWrapper requestWrapper, AgileLoggerResponseWrapper responseWrapper, DurationWatcher.Duration duration) throws IOException;

    default void buildSyntheticAgileLogger(Method method, InvokeLog invokeLog) {
        SyntheticAgileLogger syntheticAgileLogger = SyntheticAgileLogger.buildSyntheticAgileLogger(method);
        invokeLog.setLevel(syntheticAgileLogger.getLevel());
        invokeLog.setExtra(syntheticAgileLogger.getExtra());
        invokeLog.setTag(InvokeLog.DEFAULT_TAG);
    }
}
