package io.github.thebesteric.framework.agile.plugins.logger.processor.ignore;

import io.github.thebesteric.framework.agile.plugins.logger.domain.RequestLog;

import java.util.Map;

/**
 * AbstractRequestIgnoreProcessor
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-04-25 20:48:41
 */
public abstract class AbstractRequestIgnoreProcessor implements RequestIgnoreProcessor {
    /**
     * 设置需要忽略的字段
     *
     * @param requestLog 日志
     *
     * @return String[]
     *
     * @author wangweijun
     * @since 2024/4/25 21:03
     */
    protected abstract String[] doIgnore(RequestLog requestLog);

    /**
     * 设置需要重写的字段
     *
     * @param requestLog 日志
     *
     * @return Map<String, String>
     *
     * @author wangweijun
     * @since 2024/4/25 21:03
     */
    protected abstract Map<String, String> doRewrite(RequestLog requestLog);
}
