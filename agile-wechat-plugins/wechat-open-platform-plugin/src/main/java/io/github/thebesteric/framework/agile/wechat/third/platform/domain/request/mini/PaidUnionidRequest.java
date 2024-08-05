package io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.ObjectParamRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * PaidUnionidRequest
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-02 15:41:13
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "支付后获取 UnionID")
public class PaidUnionidRequest extends ObjectParamRequest {
    @JsonProperty("openid")
    @Schema(description = "支付用户唯一标识")
    private String openId;

    @JsonProperty("transaction_id")
    @Schema(description = "微信支付订单号")
    private String transactionId;

    @JsonProperty("mch_id")
    @Schema(description = "微信支付分配的商户号，和商户订单号配合使用")
    private String mchId;

    @JsonProperty("out_trade_no")
    @Schema(description = "微信支付商户订单号，和商户号配合使用")
    private String outTradeNo;
}
