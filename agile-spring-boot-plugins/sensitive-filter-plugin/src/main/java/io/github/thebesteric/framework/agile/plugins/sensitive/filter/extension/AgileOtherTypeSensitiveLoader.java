package io.github.thebesteric.framework.agile.plugins.sensitive.filter.extension;

import java.util.List;

/**
 * 其他方式敏感词加载器（用于 loadType 为 other 的情况）
 *
 * @author wangweijun
 * @since 2025/1/9 15:48
 */
@FunctionalInterface
public interface AgileOtherTypeSensitiveLoader {
    /**
     * 加载敏感词
     *
     * @return List<String>
     *
     * @author wangweijun
     * @since 2025/1/9 15:47
     */
    List<String> load();
}
