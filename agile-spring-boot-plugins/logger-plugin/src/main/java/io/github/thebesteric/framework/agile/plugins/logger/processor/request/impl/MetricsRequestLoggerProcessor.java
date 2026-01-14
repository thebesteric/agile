package io.github.thebesteric.framework.agile.plugins.logger.processor.request.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.github.thebesteric.framework.agile.commons.util.LoggerPrinter;
import io.github.thebesteric.framework.agile.plugins.logger.domain.MetricsRequestLog;
import io.github.thebesteric.framework.agile.plugins.logger.domain.RequestLog;
import io.github.thebesteric.framework.agile.plugins.logger.processor.request.AbstractRequestLoggerProcessor;
import lombok.extern.slf4j.Slf4j;

/**
 * MetricsRequestLoggerProcessor
 * <p>This command is used to collect statistics about interface access parameters
 *
 * @author Eric Joe
 * @version 1.0
 */
@Slf4j
public class MetricsRequestLoggerProcessor extends AbstractRequestLoggerProcessor {

    private static final LoggerPrinter loggerPrinter = LoggerPrinter.newInstance();

    private final Cache<String, MetricsRequestLog.Metrics> cache;

    public MetricsRequestLoggerProcessor() {
        this(256, 2048);
    }

    public MetricsRequestLoggerProcessor(int initialCapacity, int maximumSize) {
        super();
        cache = CacheBuilder.newBuilder().initialCapacity(initialCapacity).maximumSize(maximumSize)
                .removalListener(notification -> loggerPrinter.info("Metrics Log: {}-{} is removed", notification.getKey(), notification.getValue()))
                .build();
    }

    @Override
    public RequestLog doAfterProcessor(RequestLog requestLog) {
        MetricsRequestLog.Metrics metrics = cache.getIfPresent(requestLog.getUri());
        if (metrics == null) {
            metrics = new MetricsRequestLog.Metrics();
        }
        metrics.calc(requestLog);
        cache.put(requestLog.getUri(), metrics);
        return new MetricsRequestLog(requestLog, metrics);
    }
}
