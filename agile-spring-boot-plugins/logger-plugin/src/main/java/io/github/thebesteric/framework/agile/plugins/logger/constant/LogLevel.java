package io.github.thebesteric.framework.agile.plugins.logger.constant;

import lombok.Getter;

@Getter
public enum LogLevel {
    DEBUG, INFO, WARN, ERROR, TRACE;

    public static LogLevel get(String name) {
        try {
            return Enum.valueOf(LogLevel.class, name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return LogLevel.INFO;
        }
    }

    public static LogLevel ofWithException(String name) {
        return Enum.valueOf(LogLevel.class, name.toUpperCase());
    }
}
