package io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.ObjectParamRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 激活与更新服务卡片
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-02 17:57:44
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "激活与更新服务卡片")
public class UserNotifySetRequest extends ObjectParamRequest {

    @JsonProperty("openid")
    @Schema(description = "用户 openid")
    private String openId;

    @JsonProperty("notify_type")
    @Schema(description = "卡片 id")
    private Integer notifyType;

    @JsonProperty("notify_code")
    @Schema(description = "动态更新令牌")
    private String notifyCode;

    @JsonProperty("content_json")
    @Schema(description = "卡片状态与状态相关字段，不同卡片的定义不同")
    private String contentJson;

    @JsonProperty("check_json")
    @Schema(description = "微信支付订单号验证字段。当将微信支付订单号作为 notify_code 时，在激活时需要传入")
    private CheckJson checkJson;

    @Data
    public static class CheckJson {
        @JsonProperty("pay_amount")
        @Schema(description = "订单支付金额， 单位：分")
        private String payAmount;

        @JsonProperty("pay_time")
        @Schema(description = "订单支付时间，时间戳，单位：秒")
        private Long payTime;

        @JsonProperty("pay_channel")
        @Schema(description = "订单渠道，0：普通微信支付，1001：支付分")
        private Integer payChannel;
    }
}
