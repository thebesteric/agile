package io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.ObjectParamRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 设备组添加设备
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-06 20:45:09
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "设备组添加设备")
public class AddIotGroupDeviceRequest extends ObjectParamRequest {
    @JsonProperty("group_id")
    @Schema(description = "设备组的唯一标识")
    private String groupId;

    @JsonProperty("device_list")
    @Schema(description = "设备列表")
    private List<RemoveIotGroupDeviceRequest.Device> deviceList;

    @JsonProperty("force_add")
    @Schema(description = "是否强制更新设备列表，等于 true 时将已存在其它设备组中的设备移除并添加到当前设备组，慎用")
    private Boolean forceAdd = false;

    @Data
    public static class Device {
        @JsonProperty("model_id")
        @Schema(description = "设备型号唯一标识")
        private String modelId;

        @Schema(description = "设备的唯一标识")
        private String sn;
    }
}
