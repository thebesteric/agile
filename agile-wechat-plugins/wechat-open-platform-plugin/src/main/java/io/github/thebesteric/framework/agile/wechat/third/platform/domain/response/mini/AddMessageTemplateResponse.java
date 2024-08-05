package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini;

import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AddMessageTemplateResponse
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-02 17:53:37
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "添加模板")
public class AddMessageTemplateResponse extends WechatResponse {
    @Schema(description = "添加至账号下的模板 id，发送小程序订阅消息时所需")
    private String priTmplId;
}
