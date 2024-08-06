package io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.ObjectParamRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 创建设备组
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-06 20:34:58
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "创建设备组")
public class CreateIotGroupIdRequest extends ObjectParamRequest {
    @JsonProperty("model_id")
    @Schema(description = "设备型号的唯一标识")
    private String modelId;

    @JsonProperty("group_name")
    @Schema(description = "设备组的名称（创建时时决定，无法修改）")
    private String groupName;
}
