package io.github.thebesteric.framework.agile.plugins.logger.constant;

import lombok.Getter;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * log mode
 *
 * @author Eric Joe
 * @version 1.0
 * @since 2022/8/11
 */
@Getter
public enum LogMode {

    NONE("NONE"),
    STDOUT("STDOUT"),
    LOG("LOG"),
    CUSTOM("CUSTOM"),;

    private final String name;

    LogMode(String name) {
        this.name = name;
    }

    public static LogMode getLogMode(String name) {
        LogMode logMode = Arrays.stream(LogMode.values()).filter((mode) -> mode.name.equals(name))
                .collect(Collectors.collectingAndThen(Collectors.toList(), list -> list.size() == 1 ? list.get(0) : null));
        return logMode == null ? LOG : logMode;
    }
}
