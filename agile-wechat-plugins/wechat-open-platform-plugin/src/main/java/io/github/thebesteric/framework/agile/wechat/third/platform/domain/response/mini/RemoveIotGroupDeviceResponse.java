package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 设备组删除设备
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-06 20:40:02
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "设备组删除设备")
public class RemoveIotGroupDeviceResponse extends AddIotGroupDeviceResponse {
}
