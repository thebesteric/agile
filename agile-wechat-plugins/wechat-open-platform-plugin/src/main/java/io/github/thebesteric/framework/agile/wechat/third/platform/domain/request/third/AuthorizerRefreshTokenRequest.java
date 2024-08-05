package io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.third;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.ObjectParamRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AuthorizerRefreshTokenRequest
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-31 16:05:14
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AuthorizerRefreshTokenRequest extends ObjectParamRequest {
    /** 第三方平台 appid */
    @JsonProperty("component_appid")
    public String componentAppId;

    /** 授权码, 会在授权成功时返回给第三方平台 */
    @JsonProperty("authorization_code")
    private String authorizationCode;
}
