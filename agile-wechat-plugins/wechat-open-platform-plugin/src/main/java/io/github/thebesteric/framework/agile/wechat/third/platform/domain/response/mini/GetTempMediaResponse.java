package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini;

import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 获取客服消息内的临时素材
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-05 16:03:20
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "获取客服消息内的临时素材")
public class GetTempMediaResponse extends WechatResponse {
    private byte[] buffer;
}
