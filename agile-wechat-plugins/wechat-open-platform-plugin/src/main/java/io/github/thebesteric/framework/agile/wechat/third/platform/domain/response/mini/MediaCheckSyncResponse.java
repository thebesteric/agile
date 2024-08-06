package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 音视频内容安全识别
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-06 10:53:08
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "音视频内容安全识别")
public class MediaCheckSyncResponse extends WechatResponse {
    @JsonProperty("trace_id")
    @Schema(description = "唯一请求标识，标记单次请求")
    private String traceId;
}
