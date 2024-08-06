package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 创建设备组
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-06 20:36:47
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "创建设备组")
public class CreateIotGroupIdResponse extends WechatResponse {
    @JsonProperty("group_id")
    @Schema(description = "设备组的唯一标识")
    private String groupId;
}
