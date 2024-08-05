package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 小程序登录
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-05 09:45:28
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "小程序登录")
public class Code2SessionResponse extends WechatResponse {

    @JsonProperty("session_key")
    @Schema(description = "会话密钥")
    private String sessionKey;

    @JsonProperty("openid")
    @Schema(description = "用户唯一标识")
    private String openId;

    @JsonProperty("unionid")
    @Schema(description = "用户在开放平台的唯一标识符，若当前小程序已绑定到微信开放平台账号下会返回")
    private String unionId;
}
