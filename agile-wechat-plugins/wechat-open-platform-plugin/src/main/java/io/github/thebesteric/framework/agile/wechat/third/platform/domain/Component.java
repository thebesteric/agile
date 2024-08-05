package io.github.thebesteric.framework.agile.wechat.third.platform.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.ObjectParamRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Component
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-26 17:06:16
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class Component extends ObjectParamRequest {

    /** 平台型第三方平台的 appId */
    @JsonProperty("component_appid")
    private String componentAppId;

    /** 平台型第三方平台的 appSecret */
    @JsonProperty("component_secret")
    private String componentSecret;

    private Component(String componentAppId, String componentSecret) {
        this.componentAppId = componentAppId;
        this.componentSecret = componentSecret;
    }

    public static Component of(String componentAppId, String componentSecret) {
        return new Component(componentAppId, componentSecret);
    }
}
