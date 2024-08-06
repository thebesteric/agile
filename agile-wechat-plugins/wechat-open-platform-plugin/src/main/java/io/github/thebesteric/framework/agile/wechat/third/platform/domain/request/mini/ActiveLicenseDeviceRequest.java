package io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.constant.mini.PkgType;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.ObjectParamRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 激活设备 license
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-06 20:54:06
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "激活设备 license")
public class ActiveLicenseDeviceRequest extends ObjectParamRequest {
    @JsonProperty("pkg_type")
    @Schema(description = "资源包类型，0：测试体验包（默认），1：A 类设备，2：B 类设备，3：C 类设备，4：D 类设备，5：E 类设备")
    private PkgType pkgType;

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

        @JsonProperty("active_number")
        @Schema(description = "激活码序号，任意 uint32 整数（需与之前使用过的不重复）。主要用于防止重复请求导致重复激活")
        private Long activeNumber;
    }
}
