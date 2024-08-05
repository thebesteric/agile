package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.third;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatExpireResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AuthorizerAccessTokenResponse
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-31 18:13:11
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AuthorizerAccessTokenResponse extends WechatExpireResponse {
    /** 授权方令牌 */
    @JsonProperty("authorizer_access_token")
    private String authorizerAccessToken;

    /** 刷新令牌 */
    @JsonProperty("authorizer_refresh_token")
    private String authorizerRefreshToken;
}
