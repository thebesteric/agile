package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.third;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatExpireResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 预授权码
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-31 17:48:45
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "预授权码")
public class PreAuthCodeResponse extends WechatExpireResponse {
    @Schema(description = "预授权码")
    @JsonProperty("pre_auth_code")
    private String preAuthCode;
}
