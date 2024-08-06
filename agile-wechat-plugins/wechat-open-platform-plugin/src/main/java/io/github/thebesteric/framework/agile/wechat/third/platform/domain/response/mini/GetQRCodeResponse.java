package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini;

import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 获取小程序码
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-05 13:48:42
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "获取小程序码")
public class GetQRCodeResponse extends WechatResponse {
    private byte[] buffer;
}
