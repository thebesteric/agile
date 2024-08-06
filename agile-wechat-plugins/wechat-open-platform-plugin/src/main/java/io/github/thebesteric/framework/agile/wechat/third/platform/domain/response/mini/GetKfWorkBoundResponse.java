package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 查询绑定情况
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-06 17:18:44
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "查询绑定情况")
public class GetKfWorkBoundResponse extends WechatResponse {
    @Schema(description = "该小程序的主体名称，未绑定时不返回")
    private String entityName;

    @JsonProperty("corpid")
    @Schema(description = "企业ID，未绑定时不返回")
    private String corpId;

    @Schema(description = "绑定时间戳（单位：秒），未绑定时不返回")
    private Long bindTime;
}
