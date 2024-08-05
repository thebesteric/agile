package io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.ObjectParamRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 更新服务卡片扩展信息
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-02 18:24:51
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "更新服务卡片扩展信息")
public class UserNotifyExtRequest extends ObjectParamRequest {

    @JsonProperty("openid")
    @Schema(description = "用户身份标识符")
    private String openId;

    @JsonProperty("notify_type")
    @Schema(description = "卡片 id")
    private String notifyType;

    @JsonProperty("notify_code")
    @Schema(description = "动态更新令牌")
    private String notifyCode;

    @JsonProperty("ext_json")
    @Schema(description = "扩展信息")
    private String extJson;
}
