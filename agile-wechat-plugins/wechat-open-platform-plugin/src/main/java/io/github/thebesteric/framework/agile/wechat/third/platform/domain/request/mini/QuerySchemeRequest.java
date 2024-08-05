package io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.ObjectParamRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 查询 scheme 码
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-05 15:18:40
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "查询 scheme 码")
public class QuerySchemeRequest extends ObjectParamRequest {
    @Schema(description = "小程序 scheme 码。支持加密 scheme 和明文 scheme")
    private String scheme;

    @JsonProperty("query_type")
    @Schema(description = "查询类型。默认值 0，查询 scheme 码信息：0， 查询每天剩余访问次数：1")
    private Integer queryType = 0;
}
