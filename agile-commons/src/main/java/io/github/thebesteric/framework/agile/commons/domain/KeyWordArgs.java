package io.github.thebesteric.framework.agile.commons.domain;

import lombok.Builder;
import lombok.Singular;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * KeyWordArgs - 类似 Python kwargs 的参数构建器
 * <p>
 * 使用示例:
 * <pre>
 * KeyWordArgs args = KeyWordArgs.builder()
 *     .kw("name", "张三")
 *     .kw("age", 25)
 *     .kw("email", "zhangsan@example.com")
 *     .build();
 *
 * Map&lt;String, Object&gt; map = args.toMap();
 * </pre>
 *
 * @author wangweijun
 * @version v1.0
 * @since 2026-01-15 13:58:47
 */
@Builder(builderClassName = "KeyWordArgsBuilder", buildMethodName = "build")
public class KeyWordArgs {

    @Singular("kw")
    private final Map<String, Object> kwargs;

    public Map<String, Object> toMap() {
        return new LinkedHashMap<>(kwargs);
    }

    public Object get(String key) {
        return kwargs.get(key);
    }

    public boolean containsKey(String key) {
        return kwargs.containsKey(key);
    }

    public int size() {
        return kwargs.size();
    }

    public boolean isEmpty() {
        return kwargs.isEmpty();
    }

    @Override
    public String toString() {
        return kwargs.toString();
    }
}
