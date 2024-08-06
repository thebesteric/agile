package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.ObjectParamRequest;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini.RemoveIotGroupDeviceRequest;
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
public class AddIotGroupDeviceResponse extends ObjectParamRequest {

    @JsonProperty("device_list")
    @Schema(description = "设备列表")
    private List<RemoveIotGroupDeviceRequest.Device> deviceList;

    @Data
    public static class Device {
        @JsonProperty("model_id")
        @Schema(description = "设备型号唯一标识")
        private String modelId;

        @Schema(description = "设备的唯一标识")
        private String sn;
    }
}
