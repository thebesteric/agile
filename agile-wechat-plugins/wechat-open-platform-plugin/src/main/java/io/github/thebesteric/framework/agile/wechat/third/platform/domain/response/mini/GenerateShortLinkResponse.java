package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini;

import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 获取 ShortLink
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-05 15:51:34
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "获取 ShortLink")
public class GenerateShortLinkResponse extends WechatResponse {
    @Schema(description = "生成的小程序 Short Link")
    private String link;
}
