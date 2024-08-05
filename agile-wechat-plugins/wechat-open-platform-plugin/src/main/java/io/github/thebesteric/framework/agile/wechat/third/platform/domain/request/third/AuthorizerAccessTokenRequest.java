package io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.third;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.ObjectParamRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AuthorizerAccessTokenRequest
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-31 18:08:39
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AuthorizerAccessTokenRequest extends ObjectParamRequest {
    /** 第三方平台 appid */
    @JsonProperty("component_appid")
    public String componentAppId;

    /** 授权方 appid */
    @JsonProperty("authorizer_appid")
    public String authorizerAppId;

    /** 授权方 refresh_token */
    @JsonProperty("authorizer_refresh_token")
    public String authorizerRefreshToken;

}
