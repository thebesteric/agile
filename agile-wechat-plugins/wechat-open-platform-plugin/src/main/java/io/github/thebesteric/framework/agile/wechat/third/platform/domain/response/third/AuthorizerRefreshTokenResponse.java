package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.third;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.AuthorizationInfo;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatExpireResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 获取刷新令牌
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-31 16:02:09
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "获取刷新令牌")
public class AuthorizerRefreshTokenResponse extends WechatExpireResponse {
    @Schema(description = "授权信息")
    @JsonProperty("authorization_info")
    private AuthorizationInfo authorizationInfo;
}
