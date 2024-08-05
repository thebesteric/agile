package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.third;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 设置第三方平台业务域名
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-01 15:17:32
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ThirdPartyJumpDomainResponse extends WechatResponse {
    /** 目前生效的 “全网发布版”第三方平台“小程序服务器域名”。如果修改失败，该字段不会返回。如果没有已发布的第三方平台，该字段也不会返回 */
    @JsonProperty("published_wxa_jump_h5_domain")
    private String publishedWxaJumpH5Domain;

    /** 目前生效的 “测试版”第三方平台“小程序服务器域名”。如果修改失败，该字段不会返回。如果没有体验版，该字段也不会返回 */
    @JsonProperty("testing_wxa_jump_h5_domain")
    private String testingWxaJumpH5Domain;

    /** 未通过验证的域名。如果不存在未通过验证的域名，该字段不会返回 */
    @JsonProperty("invalid_wxa_jump_h5_domain")
    private String invalidWxaJumpH5Domain;
}
