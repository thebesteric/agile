package io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.ObjectParamRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 获取 ShortLink
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-05 15:47:11
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "获取 ShortLink")
public class GenerateShortLinkRequest extends ObjectParamRequest {
    @JsonProperty("page_url")
    @Schema(description = "通过 Short Link 进入的小程序页面路径，必须是已经发布的小程序存在的页面，可携带 query，最大 1024 个字符")
    public String pageUrl;

    @JsonProperty("page_title")
    @Schema(description = "页面标题，不能包含违法信息，超过20字符会用... 截断代替")
    public String pageTitle;

    @JsonProperty("is_permanent")
    @Schema(description = "默认值 false。生成的 Short Link 类型，短期有效：false，永久有效：true")
    public Boolean isPermanent = false;
}
