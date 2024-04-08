package io.github.thebesteric.framework.agile.plugins.logger.processor.request;

import io.github.thebesteric.framework.agile.commons.util.DurationWatcher;
import io.github.thebesteric.framework.agile.commons.util.JsonUtils;
import io.github.thebesteric.framework.agile.commons.util.LoggerPrinter;
import io.github.thebesteric.framework.agile.plugins.logger.constant.LogLevel;
import io.github.thebesteric.framework.agile.plugins.logger.domain.ExecuteInfo;
import io.github.thebesteric.framework.agile.plugins.logger.domain.RequestLog;
import io.github.thebesteric.framework.agile.plugins.logger.filter.warpper.AgileLoggerRequestWrapper;
import io.github.thebesteric.framework.agile.plugins.logger.filter.warpper.AgileLoggerResponseWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * AbstractRequestLoggerProcessor
 *
 * @author Eric Joe
 * @version 1.0
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractRequestLoggerProcessor implements RequestLoggerProcessor {

    @Override
    public RequestLog processor(String id, Method method, AgileLoggerRequestWrapper requestWrapper, AgileLoggerResponseWrapper responseWrapper, DurationWatcher.Duration duration) throws IOException {
        RequestLog requestLog = new RequestLog(id, requestWrapper, responseWrapper, duration);
        try {
            if (requestLog.getResult() != null) {
                requestLog.setResult(JsonUtils.mapper.readTree(requestLog.getResult().toString()));
            }
        } catch (Exception ex) {
            LoggerPrinter.debug(log, "Cannot parse {} to json", requestLog.getResult().toString());
            requestLog.setResult(requestLog.getResult().toString());
        }
        if (method != null) {
            buildSyntheticAgileLogger(method, requestLog);
            if (responseWrapper.getException() != null) {
                requestLog.setLevel(LogLevel.ERROR);
            }
            requestLog.setExecuteInfo(new ExecuteInfo(method, null));
        }

        // 子类扩展机制
        return doAfterProcessor(requestLog);
    }

    /**
     * Executes when processor is processed
     *
     * @param requestLog {@link RequestLog}
     *
     * @return RequestLog
     */
    public abstract RequestLog doAfterProcessor(RequestLog requestLog);
}
