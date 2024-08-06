package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 查询服务卡片状态
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-02 18:17:27
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "查询服务卡片状态")
public class UserNotifyGetResponse extends WechatResponse {

    @JsonProperty("notify_info")
    @Schema(description = "卡片状态")
    private NotifyInfo notifyInfo;

    @Data
    private static class NotifyInfo {
        @JsonProperty("notify_code")
        @Schema(description = "卡片 id")
        private Integer notifyType;

        @JsonProperty("content_json")
        @Schema(description = "上次有效推送的卡片状态与状态相关字段，没推送过为空字符串")
        private String contentJson;

        @JsonProperty("code_state")
        @Schema(description = "code 状态：0-正常；1-有风险；2-异常；10-用户拒收本次 code")
        private Integer codeState;

        @JsonProperty("code_expire_time")
        @Schema(description = "code 过期时间，单位：秒")
        private Long codeExpireTime;
    }
}
