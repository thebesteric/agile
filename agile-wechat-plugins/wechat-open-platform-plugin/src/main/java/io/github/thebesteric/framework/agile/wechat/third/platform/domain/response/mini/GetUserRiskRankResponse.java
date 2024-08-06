package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 获取用户安全等级
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-06 14:35:22
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "获取用户安全等级")
public class GetUserRiskRankResponse extends WechatResponse {

    @JsonProperty("risk_rank")
    @Schema(description = "用户风险等级，合法值为 0,1,2,3,4，数字越大风险越高")
    private String riskRank;

    @JsonProperty("unoin_id")
    @Schema(description = "唯一请求标识，标记单次请求")
    private String unionId;

}
