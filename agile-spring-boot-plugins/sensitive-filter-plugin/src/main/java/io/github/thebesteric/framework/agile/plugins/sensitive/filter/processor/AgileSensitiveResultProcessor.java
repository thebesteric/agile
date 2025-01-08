package io.github.thebesteric.framework.agile.plugins.sensitive.filter.processor;

import io.github.thebesteric.framework.agile.plugins.sensitive.filter.domain.SensitiveFilterResult;

public interface AgileSensitiveResultProcessor {
    /**
     * 执行结果处理器
     *
     * @param result 执行结果
     *
     * @author wangweijun
     * @since 2025/1/8 16:08
     */
    void process(SensitiveFilterResult result);
}
