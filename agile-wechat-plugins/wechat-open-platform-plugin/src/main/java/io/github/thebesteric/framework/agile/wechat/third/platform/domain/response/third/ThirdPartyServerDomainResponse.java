package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.third;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 设置第三方平台服务器
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-01 14:27:38
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "设置第三方平台服务器")
public class ThirdPartyServerDomainResponse extends WechatResponse {

    @Schema(description = "目前生效的 “全网发布版”第三方平台“小程序服务器域名”。如果修改失败，该字段不会返回。如果没有已发布的第三方平台，该字段也不会返回")
    @JsonProperty("published_wxa_server_domain")
    private String publishedWxaServerDomain;

    @Schema(description = "目前生效的 “测试版”第三方平台“小程序服务器域名”。如果修改失败，该字段不会返回。如果没有体验版，该字段也不会返回")
    @JsonProperty("testing_wxa_server_domain")
    private String testingWxaServerDomain;

    @Schema(description = "未通过验证的域名。如果不存在未通过验证的域名，该字段不会返回")
    @JsonProperty("invalid_wxa_server_domain")
    private String invalidWxaServerDomain;
}
