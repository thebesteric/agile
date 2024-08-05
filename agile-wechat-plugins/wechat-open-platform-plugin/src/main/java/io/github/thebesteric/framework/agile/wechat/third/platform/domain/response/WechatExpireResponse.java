package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * WechatExpireResponse
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-01 15:06:21
 */
public class WechatExpireResponse extends WechatResponse{

    /** 有效期，单位：秒 */
    @JsonProperty("expires_in")
    private Integer expiresIn;

}
