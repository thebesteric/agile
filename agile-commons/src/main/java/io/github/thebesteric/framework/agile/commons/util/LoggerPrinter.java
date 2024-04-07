package io.github.thebesteric.framework.agile.commons.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LoggerPrinter
 *
 * @author Eric Joe
 * @version 1.0
 * @since 2022-08-12 11:43:51
 */
public class LoggerPrinter extends AbstractUtils {

    private static final Logger log = LoggerFactory.getLogger(LoggerPrinter.class);

    private static final String LOG_PREFIX = "[Agile]: ";

    // debug
    public static void debug(Logger log, String message, Object... args) {
        if (log.isDebugEnabled()) log.debug(LOG_PREFIX + message, args);
    }

    public static void debug(String message, Object... args) {
        debug(log, message, args);
    }

    // info
    public static void info(Logger log, String message, Object... args) {
        if (log.isInfoEnabled()) log.info(LOG_PREFIX + message, args);
    }

    public static void info(String message, Object... args) {
        info(log, message, args);
    }

    // warn
    public static void warn(Logger log, String message, Object... args) {
        if (log.isWarnEnabled()) log.warn(LOG_PREFIX + message, args);
    }

    public static void warn(String message, Object... args) {
        warn(log, message, args);
    }

    // error
    public static void error(Logger log, String message, Object... args) {
        if (log.isErrorEnabled()) log.error(LOG_PREFIX + message, args);
    }

    public static void error(String message, Object... args) {
        error(log, message, args);
    }

    // trace
    public static void trace(Logger log, String message, Object... args) {
        if (log.isTraceEnabled()) log.trace(LOG_PREFIX + message, args);
    }

    public static void trace(String message, Object... args) {
        trace(log, message, args);
    }

}
