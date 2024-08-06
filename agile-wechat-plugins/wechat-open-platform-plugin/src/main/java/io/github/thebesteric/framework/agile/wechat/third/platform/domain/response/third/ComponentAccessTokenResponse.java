package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.third;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatExpireResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 获取令牌
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-26 18:04:40
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "获取令牌")
public class ComponentAccessTokenResponse extends WechatExpireResponse {
    @Schema(description = "第三方平台 access_token")
    @JsonProperty("component_access_token")
    private String componentAccessToken;
}
