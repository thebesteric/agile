package io.github.thebesteric.framework.agile.plugins.logger.processor.recorder.impl;

import io.github.thebesteric.framework.agile.commons.util.LoggerPrinter;
import io.github.thebesteric.framework.agile.plugins.logger.config.AgileLoggerProperties;
import io.github.thebesteric.framework.agile.plugins.logger.constant.LogLevel;
import io.github.thebesteric.framework.agile.plugins.logger.constant.LogMode;
import io.github.thebesteric.framework.agile.plugins.logger.domain.InvokeLog;
import io.github.thebesteric.framework.agile.plugins.logger.processor.recorder.AbstractThreadPoolRecorder;
import lombok.extern.slf4j.Slf4j;

import java.util.EnumMap;
import java.util.function.Consumer;

/**
 * LogRecorder
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-03-11 18:10:45
 */
public class LogRecorder extends AbstractThreadPoolRecorder {

    private static final LoggerPrinter loggerPrinter = LoggerPrinter.newInstance();

    private static final EnumMap<LogLevel, Consumer<String>> LOG_ACTIONS = new EnumMap<>(LogLevel.class);

    static {
        LOG_ACTIONS.put(LogLevel.DEBUG, loggerPrinter::debug);
        LOG_ACTIONS.put(LogLevel.INFO, loggerPrinter::info);
        LOG_ACTIONS.put(LogLevel.WARN, loggerPrinter::warn);
        LOG_ACTIONS.put(LogLevel.ERROR, loggerPrinter::error);
        LOG_ACTIONS.put(LogLevel.TRACE, loggerPrinter::trace);
    }

    public LogRecorder(AgileLoggerProperties properties) {
        super(properties);
    }

    @Override
    public boolean support(LogMode model) {
        return model != null && !model.getName().trim().isEmpty() && LogMode.LOG == model;
    }

    @Override
    protected void doProcess(InvokeLog invokeLog) {
        Consumer<String> logAction = LOG_ACTIONS.getOrDefault(invokeLog.getLevel(), loggerPrinter::debug);
        logAction.accept(invokeLog.print());
    }
}
