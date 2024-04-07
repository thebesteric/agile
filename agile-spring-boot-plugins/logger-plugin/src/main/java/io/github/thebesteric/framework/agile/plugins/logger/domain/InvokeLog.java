package io.github.thebesteric.framework.agile.plugins.logger.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.thebesteric.framework.agile.commons.util.JsonUtils;
import io.github.thebesteric.framework.agile.commons.util.StringUtils;
import io.github.thebesteric.framework.agile.plugins.logger.config.AgileLoggerContext;
import io.github.thebesteric.framework.agile.plugins.logger.constant.LogLevel;
import lombok.Data;
import lombok.SneakyThrows;

import java.util.Date;

/**
 * InvokeLog
 *
 * @author Eric Joe
 * @version 1.0
 * @since 2022-07-27 12:15:13
 */
@Data
public class InvokeLog {

    public static final String DEFAULT_TAG = "default";
    public static final LogLevel DEFAULT_LOG_LEVEL = LogLevel.INFO;

    /** 日志 ID */
    protected String logId;

    /** 日志 ID */
    protected String logParentId;

    /** 日志标签 */
    protected String tag = DEFAULT_TAG;

    /** 日志级别 */
    protected LogLevel level = DEFAULT_LOG_LEVEL;

    /** 链路 ID */
    protected String trackId;

    /** 创建时间 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    protected Date createdAt = new Date();

    /** 执行信息 */
    protected ExecuteInfo executeInfo;

    /** 执行结果 */
    protected Object result;

    /** 异常信息 */
    protected String exception;

    /** 扩展信息 */
    protected Object extra;

    /** 线程名称 */
    protected String threadName = Thread.currentThread().getName();

    /** 是否 Mock 数据 */
    protected boolean mock = false;

    public InvokeLog() {
        this.logId = AgileLoggerContext.idGenerator.generate();
    }

    public InvokeLog(String logParentId) {
        this();
        this.logParentId = logParentId;
    }

    public InvokeLog(String logId, String logParentId) {
        this(logParentId);
        this.logId = logId;
    }

    public static Builder builder(InvokeLog invokeLog) {
        return new Builder(invokeLog);
    }

    public static Builder builder() {
        return new Builder(new InvokeLog());
    }

    public String print() {
        return this.toString();
    }

    @Override
    @SneakyThrows
    public String toString() {
        return JsonUtils.mapper.writeValueAsString(this);
    }

    public static class Builder {

        private final InvokeLog invokeLog;

        public Builder(InvokeLog invokeLog) {
            this.invokeLog = invokeLog;
        }

        public Builder id(String id) {
            this.invokeLog.logId = id;
            return this;
        }

        public String id() {
            return this.invokeLog.logId;
        }

        public Builder parentId(String parentId) {
            this.invokeLog.logParentId = parentId;
            return this;
        }

        public String parentId() {
            return this.invokeLog.logParentId;
        }

        public Builder tag(String tag) {
            this.invokeLog.tag = tag;
            return this;
        }

        public Builder level(LogLevel level) {
            this.invokeLog.level = level;
            return this;
        }

        public Builder trackId(String trackId) {
            this.invokeLog.trackId = trackId;
            return this;
        }

        public Builder createdAt(Date createdAt) {
            this.invokeLog.createdAt = createdAt;
            return this;
        }

        public Builder createdAt(long timestamp) {
            return this.createdAt(new Date(timestamp));
        }

        public Builder executeInfo(ExecuteInfo executeInfo) {
            this.invokeLog.executeInfo = executeInfo;
            return this;
        }

        public Builder result(Object result) {
            this.invokeLog.result = result;
            return this;
        }

        public Builder exception(String exception) {
            this.invokeLog.exception = exception;
            return this;
        }

        public Builder extra(String extra) {
            this.invokeLog.extra = StringUtils.blankToNull(extra);
            return this;
        }

        public Builder threadName(String threadName) {
            this.invokeLog.threadName = threadName;
            return this;
        }

        public Builder mock(boolean mock) {
            this.invokeLog.mock = mock;
            return this;
        }

        public InvokeLog build() {
            if (this.invokeLog.createdAt == null) {
                this.invokeLog.createdAt = new Date();
            }
            if (this.invokeLog.threadName == null) {
                this.invokeLog.threadName = Thread.currentThread().getName();
            }
            return this.invokeLog;
        }
    }
}
