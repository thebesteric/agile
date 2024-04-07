package io.github.thebesteric.framework.agile.plugins.logger.processor.request.impl;

import io.github.thebesteric.framework.agile.plugins.logger.domain.RequestLog;
import io.github.thebesteric.framework.agile.plugins.logger.processor.request.AbstractRequestLoggerProcessor;
import lombok.extern.slf4j.Slf4j;

/**
 * DefaultRequestLoggerProcessor
 *
 * @author Eric Joe
 * @since 1.0
 */
@Slf4j
public class DefaultRequestLoggerProcessor extends AbstractRequestLoggerProcessor {

    @Override
    public RequestLog doAfterProcessor(RequestLog requestLog) {
        return requestLog;
    }
}