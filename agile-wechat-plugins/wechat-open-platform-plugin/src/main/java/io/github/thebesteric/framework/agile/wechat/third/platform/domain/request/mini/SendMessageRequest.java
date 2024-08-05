package io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.core.domain.SingleValue;
import io.github.thebesteric.framework.agile.wechat.third.platform.constant.mini.MiniProgramState;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.ObjectParamRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 发送订阅消息
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-02 16:55:59
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "发送订阅消息")
@NoArgsConstructor
public class SendMessageRequest extends ObjectParamRequest {
    @JsonProperty("template_id")
    @Schema(description = "所需下发的订阅模板 id")
    private String templateId;

    @Schema(description = "点击模板卡片后的跳转页面，仅限本小程序内的页面。支持带参数,（示例index?foo=bar）。该字段不填则模板无跳转")
    private String page;

    @JsonProperty("touser")
    @Schema(description = "接收者（用户）的 openid")
    private String toUser;

    @JsonProperty("miniprogram_state")
    @Schema(description = "跳转小程序类型：developer 为开发版；trial 为体验版；formal 为正式版；默认为正式版")
    private MiniProgramState miniprogramState = MiniProgramState.FORMAL;

    @Schema(description = "进入小程序查看”的语言类型，支持zh_CN (简体中文)、en_US (英文)、zh_HK (繁体中文)、zh_TW (繁体中文)，默认为 zh_CN")
    private String lang = "zh_CN";

    @Schema(description = "模板内容，格式形如: {\"key1\": {\"value\": any}, \"key2\": {\"value\": any}}")
    private Map<String, SingleValue> data;

    public static SendMessageRequest of(String templateId, String toUser, String page, MiniProgramState miniprogramState, Map<String, SingleValue> data) {
        SendMessageRequest sendMessageRequest = new SendMessageRequest();
        sendMessageRequest.templateId = templateId;
        sendMessageRequest.toUser = toUser;
        sendMessageRequest.page = page;
        sendMessageRequest.miniprogramState = miniprogramState;
        sendMessageRequest.data = data;
        return sendMessageRequest;
    }

    public static SendMessageRequest of(String templateId, String toUser, MiniProgramState miniprogramState, Map<String, SingleValue> data) {
        return of(templateId, toUser, null, miniprogramState, data);
    }

    public static SendMessageRequest of(String templateId, String toUser, Map<String, SingleValue> data) {
        return of(templateId, toUser, null, MiniProgramState.TRIAL, data);
    }
}
