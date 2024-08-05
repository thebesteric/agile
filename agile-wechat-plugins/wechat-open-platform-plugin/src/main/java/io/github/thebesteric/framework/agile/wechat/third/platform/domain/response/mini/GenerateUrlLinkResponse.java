package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 获取加密 URLLink
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-05 15:37:13
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "获取加密 URLLink")
public class GenerateUrlLinkResponse extends WechatResponse {
    @JsonProperty("url_link")
    @Schema(description = "生成的小程序 URL Link")
    private String urlLink;
}
