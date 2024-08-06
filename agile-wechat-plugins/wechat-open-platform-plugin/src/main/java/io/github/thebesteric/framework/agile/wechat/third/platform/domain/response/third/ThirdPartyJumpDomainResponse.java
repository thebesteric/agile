package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.third;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 设置第三方平台业务域名
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-01 15:17:32
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "设置第三方平台业务域名")
public class ThirdPartyJumpDomainResponse extends WechatResponse {
    @Schema(description = "目前生效的 “全网发布版”第三方平台“小程序服务器域名”。如果修改失败，该字段不会返回。如果没有已发布的第三方平台，该字段也不会返回")
    @JsonProperty("published_wxa_jump_h5_domain")
    private String publishedWxaJumpH5Domain;

    @Schema(description = "目前生效的 “测试版”第三方平台“小程序服务器域名”。如果修改失败，该字段不会返回。如果没有体验版，该字段也不会返回")
    @JsonProperty("testing_wxa_jump_h5_domain")
    private String testingWxaJumpH5Domain;

    @Schema(description = "未通过验证的域名。如果不存在未通过验证的域名，该字段不会返回")
    @JsonProperty("invalid_wxa_jump_h5_domain")
    private String invalidWxaJumpH5Domain;
}
