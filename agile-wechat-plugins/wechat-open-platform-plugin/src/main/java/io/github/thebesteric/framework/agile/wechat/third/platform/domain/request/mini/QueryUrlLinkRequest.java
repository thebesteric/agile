package io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.ObjectParamRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 查询加密 URLLink
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-05 15:40:04
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "查询加密 URLLink")
public class QueryUrlLinkRequest extends ObjectParamRequest {
    @JsonProperty("url_link")
    @Schema(description = "加密 URLLink")
    private String urlLink;

    @JsonProperty("query_type")
    @Schema(description = "查询类型。默认值 0，查询 url_link 信息：0， 查询每天剩余访问次数：1")
    private Integer queryType;
}
