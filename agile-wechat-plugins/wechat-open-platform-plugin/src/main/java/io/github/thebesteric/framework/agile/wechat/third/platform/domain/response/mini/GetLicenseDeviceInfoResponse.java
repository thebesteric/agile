package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini.RemoveIotGroupDeviceRequest;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * 查询设备激活详情
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-06 21:04:44
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "查询设备激活详情")
public class GetLicenseDeviceInfoResponse extends WechatResponse {
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

        @JsonProperty("expire_time")
        @Schema(description = "设备的过期时间")
        private Long expireTime;
    }
}
