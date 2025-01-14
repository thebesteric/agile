package io.github.thebesteric.framework.agile.core.domain;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * BaseCodeDescEnum
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-25 17:43:28
 */
public interface BaseCodeDescEnum {

    String CODE_KEY = "code";
    String DESC_KEY = "desc";

    /** 获取枚举编码 */
    String getCode();

    /** 获取枚举描述 */
    String getDesc();

    /**
     * 对象转 Map
     *
     * @return Map<String, String>
     *
     * @author wangweijun
     * @since 2024/5/31 15:47
     */
    default Map<String, String> toMap() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put(CODE_KEY, String.valueOf(this.getCode()));
        map.put(DESC_KEY, this.getDesc());
        return map;
    }
}
