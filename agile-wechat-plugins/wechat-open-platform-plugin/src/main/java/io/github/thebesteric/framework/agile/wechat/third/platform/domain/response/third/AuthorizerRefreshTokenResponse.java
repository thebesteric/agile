package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.third;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.AuthorizationInfo;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatExpireResponse;
import lombok.Data;

/**
 * AuthorizerRefreshTokenResponse
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-31 16:02:09
 */
@Data
public class AuthorizerRefreshTokenResponse extends WechatExpireResponse {
    /** 授权信息 */
    @JsonProperty("authorization_info")
    private AuthorizationInfo authorizationInfo;
}
