package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.third;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatExpireResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 获取授权账号调用令牌
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-31 18:13:11
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "获取授权账号调用令牌")
public class AuthorizerAccessTokenResponse extends WechatExpireResponse {
    @Schema(description = "授权方令牌")
    @JsonProperty("authorizer_access_token")
    private String authorizerAccessToken;

    @Schema(description = "刷新令牌")
    @JsonProperty("authorizer_refresh_token")
    private String authorizerRefreshToken;
}
