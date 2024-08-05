package io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.third;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.Component;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.ObjectParamRequest;

/**
 * ComponentAccessTokenRequest
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-26 18:08:49
 */
public class ComponentAccessTokenRequest extends ObjectParamRequest {
    /** 第三方平台 appid */
    @JsonProperty("component_appid")
    public String componentAppId;

    /** 第三方平台 appsecret */
    @JsonProperty("component_appsecret")
    public String componentAppSecret;

    /** 微信后台推送的 ticket */
    @JsonProperty("component_verify_ticket")
    public String componentVerifyTicket;

    public static ComponentAccessTokenRequest of(Component component, String componentVerifyTicket) {
        ComponentAccessTokenRequest request = new ComponentAccessTokenRequest();
        request.componentAppId = component.getComponentAppId();
        request.componentAppSecret = component.getComponentSecret();
        request.componentVerifyTicket = componentVerifyTicket;
        return request;
    }
}
