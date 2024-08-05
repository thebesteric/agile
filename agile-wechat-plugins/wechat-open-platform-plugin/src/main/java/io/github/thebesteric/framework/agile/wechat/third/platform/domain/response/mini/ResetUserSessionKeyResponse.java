package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 重置登录态
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-05 11:56:25
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "重置登录态")
public class ResetUserSessionKeyResponse extends WechatResponse {
    @JsonProperty("openid")
    @Schema(description = "用户唯一标识")
    private String openId;

    @JsonProperty("session_key")
    @Schema(description = "重置后的用户登录态")
    private String sessionKey;
}
