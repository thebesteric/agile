package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatExpireResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 接口调用凭证
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-02 14:01:36
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "接口调用凭证")
public class AccessTokenResponse extends WechatExpireResponse {
    @JsonProperty("access_token")
    @Schema(description = "接口调用凭证")
    private String accessToken;
}
