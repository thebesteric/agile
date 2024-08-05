package io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini;

import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.ObjectParamRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 获取小程序二维码
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-05 14:01:29
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "获取小程序二维码")
public class CreateQRCodeRequest extends ObjectParamRequest {
    @Schema(description = "扫码进入的小程序页面路径，最大长度 1024 个字符")
    private String path = "pages/index/index";

    @Schema(description = "二维码的宽度，单位 px，最小 280px，最大 1280px")
    private Integer width = 280;
}
