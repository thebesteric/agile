package io.github.thebesteric.framework.agile.core.domain;

import io.github.thebesteric.framework.agile.commons.util.MapWrapper;

import java.util.Map;

/**
 * BaseEnum
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-25 17:43:28
 */
public interface BaseEnum {
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
    default Map<String, Object> toMap() {
        return MapWrapper.createLambda(BaseEnum.class)
                .put(BaseEnum::getCode, this.getCode())
                .put(BaseEnum::getDesc, this.getDesc()).build();
    }
}
