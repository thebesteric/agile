package io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.ObjectParamRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 获取设备票据
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-06 20:29:17
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "获取设备票据")
public class GetSnTicketRequest extends ObjectParamRequest {

    @Schema(description = "sn 设备唯一序列号。由厂商分配，长度不能超过128字节。字符只接受数字，大小写字母，下划线（_）和连字符（-）")
    private String sn;

    @JsonProperty("model_id")
    @Schema(description = "设备型号 id ，通过注册设备获得")
    private String modelId;
}
