package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 支付后获取 UnionId
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-02 15:39:45
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "支付后获取 UnionID")
public class PaidUnionidResponse extends WechatResponse {
    @JsonProperty("unionid")
    @Schema(description = "用户唯一标识，调用成功后返回")
    private String unionid;
}
