package io.github.thebesteric.framework.agile.plugins.logger.processor.ignore.impl;

import io.github.thebesteric.framework.agile.plugins.logger.domain.RequestLog;
import io.github.thebesteric.framework.agile.plugins.logger.processor.ignore.AbstractRequestIgnoreProcessor;

import java.util.Arrays;
import java.util.Map;

/**
 * HeaderIgnoreProcessor
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-04-25 19:39:21
 */
public abstract class HeaderIgnoreProcessor extends AbstractRequestIgnoreProcessor {
    @Override
    public void ignore(RequestLog requestLog) {
        String[] ignores = doIgnore(requestLog);
        if (ignores == null || ignores.length == 0) {
            return;
        }
        Arrays.stream(ignores).forEach(key -> requestLog.getHeaders().remove(key));
    }

    @Override
    public void rewrite(RequestLog requestLog) {
        Map<String, String> ignoreRewrites = doRewrite(requestLog);
        if (ignoreRewrites == null || ignoreRewrites.isEmpty()) {
            return;
        }
        ignoreRewrites.forEach((key, value) -> requestLog.getHeaders().computeIfPresent(key, (k, v) -> value));
    }
}
