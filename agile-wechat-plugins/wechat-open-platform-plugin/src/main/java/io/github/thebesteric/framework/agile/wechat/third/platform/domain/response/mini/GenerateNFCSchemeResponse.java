package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 获取 NFC 的小程序 scheme
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-05 14:53:44
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "获取 NFC 的小程序 scheme")
public class GenerateNFCSchemeResponse extends WechatResponse {
    @JsonProperty("openlink")
    @Schema(description = "生成的小程序 scheme 码")
    private String openLink;
}
