package io.github.thebesteric.framework.agile.wechat.third.platform.domain.request;

import io.github.thebesteric.framework.agile.commons.util.JsonUtils;
import lombok.SneakyThrows;

import java.util.Map;

/**
 * Request
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-26 17:29:32
 */
public class ObjectParamRequest {

    /**
     * 对象转 Map
     *
     * @return Map<String, Object>
     *
     * @author wangweijun
     * @since 2024/7/26 17:35
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> toMap() {
        return JsonUtils.MAPPER.convertValue(this, Map.class);
    }

    /**
     * 对象转 Json
     *
     * @return String
     *
     * @author wangweijun
     * @since 2024/7/26 17:38
     */
    @SneakyThrows
    public String toJson() {
        return JsonUtils.MAPPER.writeValueAsString(this);
    }
}
