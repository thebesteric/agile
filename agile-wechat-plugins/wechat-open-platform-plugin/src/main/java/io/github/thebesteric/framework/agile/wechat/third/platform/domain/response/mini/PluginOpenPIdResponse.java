package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 获取插件用户 openpid
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-02 14:26:04
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "插件用户信息")
public class PluginOpenPIdResponse extends WechatResponse {
    @JsonProperty("openpid")
    @Schema(description = "插件用户的唯一标识")
    private String openPid;
}
