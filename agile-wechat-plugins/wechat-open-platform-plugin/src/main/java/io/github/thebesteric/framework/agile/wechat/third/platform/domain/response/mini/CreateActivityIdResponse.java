package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 创建 activity_id
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-05 17:16:56
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "创建 activity_id")
public class CreateActivityIdResponse extends WechatResponse {
    @JsonProperty("activity_id")
    @Schema(description = "动态消息的 ID")
    private String activityId;

    @JsonProperty("expiration_time")
    @Schema(description = "activity_id 的过期时间戳。默认 24 小时后过期")
    private Long expirationTime;
}
