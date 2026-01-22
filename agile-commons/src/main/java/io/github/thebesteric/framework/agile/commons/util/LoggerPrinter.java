package io.github.thebesteric.framework.agile.commons.util;

import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LoggerPrinter - 增强的日志打印工具类
 * <p>
 * 特性:
 * - 支持自动识别调用类
 * - 支持事务ID前缀
 * - 支持异常堆栈信息
 *
 * @author Eric Joe
 * @version 1.0
 * @since 2022-08-12 11:43:51
 */
public class LoggerPrinter {

    /** 日志对象 */
    @Getter
    private final Logger logger;

    /** 日志标签 */
    @Getter
    private final String tag;

    public LoggerPrinter() {
        this(getCallerClass(), null);
    }

    public LoggerPrinter(String prefix) {
        this(getCallerClass(), prefix);
    }

    public LoggerPrinter(Class<?> clazz, String tag) {
        this.logger = LoggerFactory.getLogger(clazz);
        this.tag = tag;
    }

    public static LoggerPrinter newInstance() {
        return newInstance(null, null);
    }

    public static LoggerPrinter newInstance(@Nullable Class<?> clazz) {
        return newInstance(clazz, null);
    }

    public static LoggerPrinter newInstance(@Nullable String tag) {
        return newInstance(null, tag);
    }

    public static LoggerPrinter newInstance(@Nullable Class<?> clazz, @Nullable String tag) {
        clazz = clazz != null ? clazz : getCallerClass();
        return new LoggerPrinter(clazz, tag);
    }

    // ==================== debug ====================

    public void debug(String message, Object... args) {
        if (logger.isDebugEnabled()) {
            logger.debug(getLogPrefix() + message, args);
        }
    }

    public void debug(String message, Throwable throwable) {
        if (logger.isDebugEnabled()) {
            logger.debug(getLogPrefix() + message, throwable);
        }
    }

    // ==================== info ====================

    public void info(String message, Object... args) {
        if (logger.isInfoEnabled()) {
            logger.info(getLogPrefix() + message, args);
        }
    }

    public void info(String message, Throwable throwable) {
        if (logger.isInfoEnabled()) {
            logger.info(getLogPrefix() + message, throwable);
        }
    }

    // ==================== warn ====================

    public void warn(String message, Object... args) {
        if (logger.isWarnEnabled()) {
            logger.warn(getLogPrefix() + message, args);
        }
    }

    public void warn(String message, Throwable throwable) {
        if (logger.isWarnEnabled()) {
            logger.warn(getLogPrefix() + message, throwable);
        }
    }

    // ==================== error ====================

    public void error(String message, Object... args) {
        if (logger.isErrorEnabled()) {
            logger.error(getLogPrefix() + message, args);
        }
    }

    public void error(String message, Throwable throwable) {
        if (logger.isErrorEnabled()) {
            logger.error(getLogPrefix() + message, throwable);
        }
    }

    // ==================== trace ====================

    public void trace(String message, Object... args) {
        if (logger.isTraceEnabled()) {
            logger.trace(getLogPrefix() + message, args);
        }
    }

    public void trace(String message, Throwable throwable) {
        if (logger.isTraceEnabled()) {
            logger.trace(getLogPrefix() + message, throwable);
        }
    }

    private String getLogPrefix() {
        StackTraceElement caller = getCallerLocation();
        int lineNumber = caller.getLineNumber();
        return tag != null ? "[Agile:%d] %s: ".formatted(lineNumber, tag) : "[Agile:%d]: ".formatted(lineNumber);
    }

    private StackTraceElement getCallerLocation() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        // [0] = getStackTrace()
        // [1] = getCallerLocation()
        // [2] = getLogPrefixWithLocation()
        // [3] = debug/info/warn/error/trace 方法
        // [4] = 实际调用者 <--
        return stackTrace[4];
    }

    /**
     * 获取调用者的Class对象
     * 通过解析StackTrace确定实际的调用类
     *
     * @return 调用者的Class对象，如果无法确定则返回LoggerPrinter.class
     */
    private static Class<?> getCallerClass() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        // [0] = Thread.currentThread().getStackTrace()
        // [1] = getCallerClass()
        // [2] = LoggerPrinter() 构造方法
        // [3] = 实际调用者 <--
        for (int i = 3; i < stackTrace.length; i++) {
            String className = stackTrace[i].getClassName();
            // 跳过 LoggerPrinter 类本身
            if (className.equals(LoggerPrinter.class.getName())) {
                continue;
            }
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                // 继续尝试下一个堆栈元素
            }
        }
        return LoggerPrinter.class;
    }

}
