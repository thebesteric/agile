package io.github.thebesteric.framework.agile.plugins.logger.processor.ignore.impl;

import io.github.thebesteric.framework.agile.commons.util.StringUtils;
import io.github.thebesteric.framework.agile.plugins.logger.domain.RequestLog;
import io.github.thebesteric.framework.agile.plugins.logger.processor.ignore.AbstractRequestIgnoreProcessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * ParametersIgnoreProcessor
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-04-25 20:08:42
 */
public abstract class ParameterIgnoreProcessor extends AbstractRequestIgnoreProcessor {
    @Override
    public void ignore(RequestLog requestLog) {
        String[] ignores = doIgnore(requestLog);
        // 屏蔽 params
        if (ignores == null || ignores.length == 0) {
            return;
        }
        Arrays.stream(ignores).forEach(key -> requestLog.getParams().remove(key));
        // 屏蔽 query
        String query = requestLog.getQuery();
        if (StringUtils.isEmpty(query)) {
            return;
        }
        List<String> queryList = Arrays.asList(query.split("&"));
        List<Integer> ignoreIndexes = new ArrayList<>();
        Arrays.stream(ignores).forEach(key -> {
            for (int i = 0; i < queryList.size(); i++) {
                String[] pair = queryList.get(i).split("=");
                if (pair.length == 2 && key.equals(pair[0]))
                    ignoreIndexes.add(i);
            }
        });
        List<String> currentQueryList = new ArrayList<>(queryList);
        ignoreIndexes.forEach(index -> currentQueryList.remove(queryList.get(index)));
        query = String.join("&", currentQueryList.toArray(new String[0]));
        requestLog.setQuery(query);
    }

    @Override
    public void rewrite(RequestLog requestLog) {
        Map<String, String> ignoreRewrites = doRewrite(requestLog);
        // 重写 params
        if (ignoreRewrites == null || ignoreRewrites.isEmpty()) {
            return;
        }
        ignoreRewrites.forEach((key, value) -> requestLog.getParams().computeIfPresent(key, (k, v) -> value));
        // 重写 query
        String query = requestLog.getQuery();
        if (StringUtils.isEmpty(query)) {
            return;
        }
        String[] queryArr = query.split("&");
        ignoreRewrites.forEach((key, value) -> {
            for (int i = 0; i < queryArr.length; i++) {
                String[] pair = queryArr[i].split("=");
                if (pair.length == 2 && key.equals(pair[0])) {
                    pair[1] = value;
                    queryArr[i] = String.join("=", pair);
                }
            }
        });
        query = String.join("&", queryArr);
        requestLog.setQuery(query);
    }
}
