package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.third;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatExpireResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ComponentAccessToken
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-26 18:04:40
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ComponentAccessTokenResponse extends WechatExpireResponse {
    /** 第三方平台 access_token */
    @JsonProperty("component_access_token")
    private String componentAccessToken;
}
