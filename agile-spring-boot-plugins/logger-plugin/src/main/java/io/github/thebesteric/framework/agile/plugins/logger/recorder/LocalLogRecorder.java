package io.github.thebesteric.framework.agile.plugins.logger.recorder;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.thebesteric.framework.agile.commons.util.AbstractUtils;
import io.github.thebesteric.framework.agile.commons.util.ExceptionUtils;
import io.github.thebesteric.framework.agile.core.domain.page.PagingResponse;
import io.github.thebesteric.framework.agile.plugins.logger.config.AgileLoggerProperties;
import io.github.thebesteric.framework.agile.plugins.logger.constant.LogLevel;
import io.github.thebesteric.framework.agile.plugins.logger.domain.InvokeLog;
import lombok.Data;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * LocalLogRecorder
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-10-31 14:22:39
 */
public class LocalLogRecorder extends AbstractUtils {

    private static final Cache<String, LocalLogRecord> CACHE = Caffeine.newBuilder()
            .maximumSize(1000)
            .build();

    public static void record(AgileLoggerProperties.LocalLogRecorderConfig config, LocalLogRecord localLogRecord) {
        if (config == null || !config.isEnable()) {
            return;
        }
        Set<LogLevel> recordLevels = config.getRecordLevels();
        if (recordLevels == null) {
            recordLevels = new HashSet<>();
        }
        recordLevels.add(LogLevel.ERROR);
        InvokeLog invokeLog = localLogRecord.getInvokeLog();
        LogLevel logLevel = invokeLog.getLevel();
        if (recordLevels.contains(logLevel)) {
            CACHE.put(invokeLog.getLogId(), localLogRecord);
        }
    }

    public static LocalLogRecord logId(String logId) {
        return CACHE.getIfPresent(logId);
    }

    public static List<LocalLogRecord> trackId(String trackId) {
        List<LocalLogRecord> sortedLocalLogRecords = sortedLocalLogRecords();
        return sortedLocalLogRecords.stream().filter(localLogRecord -> trackId.equals(localLogRecord.getInvokeLog().getTrackId())).toList();
    }

    public static PagingResponse<LocalLogRecord> list(LogLevel logLevel, int current, int size) {
        List<LocalLogRecord> sortedLocalLogRecords = sortedLocalLogRecords();
        List<LocalLogRecord> totalRecords = sortedLocalLogRecords.stream()
                .filter(localLogRecord -> {
                    if (logLevel == null) {
                        return true;
                    }
                    return logLevel.equals(localLogRecord.getInvokeLog().getLevel());
                }).toList();
        List<LocalLogRecord> records = totalRecords.stream().skip((long) (current - 1) * size).limit(size).toList();
        return PagingResponse.of(current, size, totalRecords.size(), records);
    }

    private static List<LocalLogRecord> sortedLocalLogRecords() {
        ConcurrentMap<String, LocalLogRecord> map = CACHE.asMap();
        return map.values().stream().sorted((o1, o2) -> o2.getInvokeLog().getCreatedAt().compareTo(o1.getInvokeLog().getCreatedAt())).toList();
    }

    @Data
    public static class LocalLogRecord {
        /** 日志 ID */
        private String logId;
        /** 链路 ID */
        private String trackId;
        /** 级别 */
        private LogLevel logLevel;
        /** 日志 */
        private InvokeLog invokeLog;
        /** 异常信息 */
        private String exception;
        /** 记录时间 */
        @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
        private Date createdAt = new Date();

        public static Builder builder() {
            return new Builder(new LocalLogRecord());
        }

        /**
         * 构建器
         */
        public static class Builder {

            private final LocalLogRecord localLogRecord;

            public Builder(LocalLogRecord localLogRecord) {
                this.localLogRecord = localLogRecord;
            }

            public Builder invokeLog(InvokeLog invokeLog) {
                this.localLogRecord.invokeLog = invokeLog;
                this.localLogRecord.logId = invokeLog.getLogId();
                this.localLogRecord.trackId = invokeLog.getTrackId();
                this.localLogRecord.logLevel = invokeLog.getLevel();
                return this;
            }

            public Builder exception(Throwable ex) {
                this.localLogRecord.exception = ExceptionUtils.getStackTraceStr(ex);
                return this;
            }

            public LocalLogRecord build() {
                return this.localLogRecord;
            }
        }
    }
}
