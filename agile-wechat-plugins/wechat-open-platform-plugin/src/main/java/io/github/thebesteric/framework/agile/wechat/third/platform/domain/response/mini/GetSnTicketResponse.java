package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 获取设备票据
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-06 20:30:49
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "获取设备票据")
public class GetSnTicketResponse extends WechatResponse {
    @JsonProperty("sn_ticket")
    @Schema(description = "设备票据，5 分钟内有效")
    private String snTicket;
}
