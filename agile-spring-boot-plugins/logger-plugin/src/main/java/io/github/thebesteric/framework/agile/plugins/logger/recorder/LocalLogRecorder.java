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
import io.github.thebesteric.framework.agile.plugins.logger.domain.RequestLog;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

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

    /**
     * 记录日志
     *
     * @param config         配置信息
     * @param localLogRecord 日志记录对象
     *
     * @since 2024/11/13 16:57
     */
    public static void record(AgileLoggerProperties.LocalLogRecorderConfig config, LocalLogRecord localLogRecord) {
        if (config == null || !config.isEnable()) {
            return;
        }
        // 需要记录的日志级别
        Set<LogLevel> recordLevels = config.getRecordLevels();
        if (recordLevels == null) {
            recordLevels = new HashSet<>();
        }
        recordLevels.add(LogLevel.ERROR);
        InvokeLog invokeLog = localLogRecord.getInvokeLog();
        LogLevel logLevel = invokeLog.getLevel();

        // 需要记录的日志标签
        Set<String> recordTags = config.getRecordTags();
        String logTag = invokeLog.getTag();

        // 符合需要记录的日志级别或标签，则记录
        if (recordLevels.contains(logLevel) || recordTags.contains(logTag)) {
            CACHE.put(invokeLog.getLogId(), localLogRecord);
        }
    }

    /**
     * 根据日志 ID 查询
     *
     * @param logId 日志 ID
     *
     * @return LocalLogRecord
     *
     * @since 2024/11/13 16:57
     */
    public static LocalLogRecord logId(String logId) {
        return CACHE.getIfPresent(logId);
    }

    /**
     * 根据链路 ID 查询
     *
     * @param trackId 链路 ID
     *
     * @return List<LocalLogRecord>
     *
     * @since 2024/11/13 16:57
     */
    public static List<LocalLogRecord> trackId(String trackId) {
        List<LocalLogRecord> sortedLocalLogRecords = sortedLocalLogRecords();
        return sortedLocalLogRecords.stream().filter(localLogRecord -> trackId.equals(localLogRecord.getInvokeLog().getTrackId())).toList();
    }

    /**
     * 根据日志标签查询
     *
     * @param tagName 日志标签
     *
     * @return List<LocalLogRecord>
     *
     * @since 2024/11/13 16:57
     */
    public static List<LocalLogRecord> tagName(String tagName) {
        List<LocalLogRecord> sortedLocalLogRecords = sortedLocalLogRecords();
        return sortedLocalLogRecords.stream().filter(localLogRecord -> tagName.equals(localLogRecord.getInvokeLog().getTag())).toList();
    }

    /**
     * 根据 URI 名称查询
     *
     * @param uriName URI 名称
     *
     * @return List
     *
     * @since 2024/11/13 16:57
     */
    public static List<LocalLogRecord> uriName(String uriName) {
        List<LocalLogRecord> sortedLocalLogRecords = sortedLocalLogRecords();
        return sortedLocalLogRecords.stream().filter(localLogRecord -> {
            InvokeLog invokeLog = localLogRecord.getInvokeLog();
            if (invokeLog instanceof RequestLog requestLog) {
                return requestLog.getUri().contains(uriName);
            }
            return false;
        }).toList();
    }

    /**
     * 按异常类型分类统计
     *
     * @param exceptionClassName 异常名称
     *
     * @return ExceptionLogInfo
     *
     * @since 2024/11/13 17:04
     */
    public static ExceptionLogInfo classifyException(@Nullable String exceptionClassName) {
        ExceptionLogInfo exceptionLogInfo = ExceptionLogInfo.newInstance();
        Map<Class<?>, ExceptionLogItem> exceptionLogItems = exceptionLogInfo.getExceptionLogItems();

        List<LocalLogRecord> sortedLocalLogRecords = sortedLocalLogRecords();
        for (LocalLogRecord localLogRecord : sortedLocalLogRecords) {
            Class<?> exceptionClass = localLogRecord.getInvokeLog().getExceptionClass();
            if (exceptionClass != null) {
                ExceptionLogItem exceptionLogItem = exceptionLogItems.getOrDefault(exceptionClass, ExceptionLogItem.newInstance());
                // 存在指定异常类型
                if (exceptionClassName != null) {
                    if (exceptionClass.getName().contains(exceptionClassName)) {
                        exceptionLogInfo.increment();
                        exceptionLogItem.addRecord(localLogRecord);
                        exceptionLogItems.put(exceptionClass, exceptionLogItem);
                    }
                    continue;
                }
                // 没有指定异常类型
                exceptionLogInfo.increment();
                exceptionLogItem.addRecord(localLogRecord);
                exceptionLogItems.put(exceptionClass, exceptionLogItem);
            }
        }
        return exceptionLogInfo;
    }

    /**
     * 分页查询
     *
     * @param logLevel 日志级别
     * @param tagName  日志标签
     * @param current  当前页
     * @param size     每页大小
     *
     * @return PagingResponse<LocalLogRecord>
     *
     * @since 2024/11/13 16:57
     */
    public static PagingResponse<LocalLogRecord> list(LogLevel logLevel, String tagName, int current, int size) {
        List<LocalLogRecord> sortedLocalLogRecords = sortedLocalLogRecords();
        List<LocalLogRecord> totalRecords = sortedLocalLogRecords.stream()
                .filter(localLogRecord -> {
                    if (logLevel == null) {
                        return true;
                    }
                    return logLevel.equals(localLogRecord.getInvokeLog().getLevel());
                })
                .filter(localLogRecord -> {
                    if (StringUtils.isEmpty(tagName)) {
                        return true;
                    }
                    return tagName.equals(localLogRecord.getInvokeLog().getTag());
                })
                .toList();
        List<LocalLogRecord> records = totalRecords.stream().skip((long) (current - 1) * size).limit(size).toList();
        return PagingResponse.of(current, size, totalRecords.size(), records);
    }

    private static List<LocalLogRecord> sortedLocalLogRecords() {
        ConcurrentMap<String, LocalLogRecord> map = CACHE.asMap();
        return map.values().stream().sorted((o1, o2) -> o2.getInvokeLog().getCreatedAt().compareTo(o1.getInvokeLog().getCreatedAt())).toList();
    }

    /**
     * 清空缓存
     *
     * @since 2024/11/15 15:26
     */
    public static void clear() {
        CACHE.cleanUp();
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

    @Data
    public static class ExceptionLogInfo {
        /** 总数 */
        private AtomicInteger total;
        /** 异常类型统计 */
        private Map<Class<?>, ExceptionLogItem> exceptionLogItems;

        private ExceptionLogInfo() {
            this.total = new AtomicInteger(0);
            this.exceptionLogItems = new LinkedHashMap<>();
        }

        public static ExceptionLogInfo newInstance() {
            return new ExceptionLogInfo();
        }

        public int increment() {
            return this.total.incrementAndGet();
        }
    }

    @Data
    public static class ExceptionLogItem {
        /** 数量 */
        private AtomicInteger count;
        /** 日志记录 */
        private Set<LocalLogRecord> localLogRecords;

        private ExceptionLogItem() {
            this.count = new AtomicInteger(0);
            this.localLogRecords = new LinkedHashSet<>();
        }

        public static ExceptionLogItem newInstance() {
            return new ExceptionLogItem();
        }

        public void addRecord(LocalLogRecord localLogRecord) {
            this.count.incrementAndGet();
            this.localLogRecords.add(localLogRecord);
        }
    }
}
