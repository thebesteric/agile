package io.github.thebesteric.framework.agile.plugins.logger.processor.ignore.impl;

import io.github.thebesteric.framework.agile.plugins.logger.domain.RequestLog;
import io.github.thebesteric.framework.agile.plugins.logger.processor.ignore.AbstractRequestIgnoreProcessor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * CookieIgnoreProcessor
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-04-25 20:18:27
 */
public abstract class CookieIgnoreProcessor extends AbstractRequestIgnoreProcessor {

    @Override
    public void ignore(RequestLog requestLog) {
        String[] ignores = doIgnore(requestLog);
        if (ignores == null || ignores.length == 0) {
            return;
        }
        Set<RequestLog.Cookie> cookies = requestLog.getCookies();
        Arrays.stream(ignores).forEach(key -> {
            Set<RequestLog.Cookie> ignoreCookies = new HashSet<>();
            cookies.forEach(cookie -> {
                if (cookie.getName().equals(key)) {
                    ignoreCookies.add(cookie);
                }
            });
            cookies.removeAll(ignoreCookies);
        });
    }

    @Override
    public void rewrite(RequestLog requestLog) {
        Map<String, String> ignoreRewrites = doRewrite(requestLog);
        if (ignoreRewrites == null || ignoreRewrites.isEmpty()) {
            return;
        }
        Set<RequestLog.Cookie> cookies = requestLog.getCookies();
        ignoreRewrites.forEach((key, value) -> {
            cookies.forEach(cookie -> {
                if (cookie.getName().equals(key)) {
                    cookie.setValue(value);
                }
            });
        });
    }
}
