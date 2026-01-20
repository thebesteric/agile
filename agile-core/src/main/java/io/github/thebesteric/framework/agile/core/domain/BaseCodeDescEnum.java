package io.github.thebesteric.framework.agile.core.domain;

import io.github.thebesteric.framework.agile.commons.util.MapWrapper;

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
     *
     * @author wangweijun
     * @since 2024/5/31 15:47
     */
    default Map<String, String> toMap() {
        return MapWrapper.createLambda(BaseEnum.class, String.class, new LinkedHashMap<>())
                .put(BaseEnum::getCode, this.getCode())
                .put(BaseEnum::getDesc, this.getDesc()).build();
    }
}
