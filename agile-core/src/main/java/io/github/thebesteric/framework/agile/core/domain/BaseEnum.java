package io.github.thebesteric.framework.agile.core.domain;

import java.util.Map;
import java.util.TreeMap;

/**
 * BaseEnum
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-25 17:43:28
 */
public interface BaseEnum {

    String CODE_KEY = "code";
    String DESC_KEY = "desc";

    /** 获取枚举编码 */
    Integer getCode();

    /** 获取枚举描述 */
    String getDesc();

    /**
     * 对象转 Map
     *
     * @return Map<String, Object>
     *
     * @author wangweijun
     * @since 2024/5/31 15:47
     */
    default Map<String, String> toMap() {
        Map<String, String> map = new TreeMap<>();
        map.put(CODE_KEY, String.valueOf(this.getCode()));
        map.put(DESC_KEY, this.getDesc());
        return map;
        // return MapWrapper.createLambda(BaseEnum.class)
        //         .put(BaseEnum::getCode, this.getCode())
        //         .put(BaseEnum::getDesc, this.getDesc()).build();
    }
}
