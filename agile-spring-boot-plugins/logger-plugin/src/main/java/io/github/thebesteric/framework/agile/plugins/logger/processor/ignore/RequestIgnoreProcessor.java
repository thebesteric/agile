package io.github.thebesteric.framework.agile.plugins.logger.processor.ignore;

import io.github.thebesteric.framework.agile.plugins.logger.domain.RequestLog;

public interface RequestIgnoreProcessor {
    /**
     * 设置需要不写入日志的字段
     *
     * @param requestLog 日志信息
     *
     * @author wangweijun
     * @since 2024/4/25 19:40
     */
    void ignore(RequestLog requestLog);

    /**
     * 设置需要日志中需要改写的字段
     *
     * @param requestLog 日志信息
     *
     * @author wangweijun
     * @since 2024/4/25 19:47
     */
    void rewrite(RequestLog requestLog);
}
