package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.third;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * 获取隐私接口检测结果
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-01 15:46:00
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "获取隐私接口检测结果")
public class MiniCodePrivacyInfoResponse extends WechatResponse {
    @Schema(description = "没权限的隐私接口的 api 英文名")
    @JsonProperty("without_auth_list")
    private List<String> withoutAuthList;

    @Schema(description = "没配置的隐私接口的 api 英文名")
    @JsonProperty("without_conf_list")
    private List<String> withoutConfList;
}
