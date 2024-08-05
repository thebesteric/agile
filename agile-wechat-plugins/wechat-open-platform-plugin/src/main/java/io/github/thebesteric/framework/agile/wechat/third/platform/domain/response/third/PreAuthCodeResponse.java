package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.third;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatExpireResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * PreAuthCodeResponse
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-31 17:48:45
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PreAuthCodeResponse extends WechatExpireResponse {
    /** 预授权码 */
    @JsonProperty("pre_auth_code")
    private String preAuthCode;
}