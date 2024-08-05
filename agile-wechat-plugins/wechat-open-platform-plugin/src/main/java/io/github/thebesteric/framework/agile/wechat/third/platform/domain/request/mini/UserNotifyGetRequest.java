package io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.ObjectParamRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 查询服务卡片状态
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-02 18:16:35
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "查询服务卡片状态")
public class UserNotifyGetRequest extends ObjectParamRequest {
    @JsonProperty("notify_code")
    @Schema(description = "动态更新令牌")
    private String notifyCode;

    @JsonProperty("notify_type")
    @Schema(description = "卡片 id")
    private Integer notifyType;
}
