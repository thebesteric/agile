package io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.core.domain.SingleValue;
import io.github.thebesteric.framework.agile.wechat.third.platform.constant.mini.Lang;
import io.github.thebesteric.framework.agile.wechat.third.platform.constant.mini.MiniProgramState;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.ObjectParamRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * 发送设备消息
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-06 19:55:08
 */

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "发送设备消息")
public class SendHardwareDeviceMessageRequest extends ObjectParamRequest {

    @JsonProperty("template_id")
    @Schema(description = "所需下发的订阅模板 id")
    private String templateId;

    @Schema(description = "设备唯一序列号。由厂商分配，长度不能超过128字节。字符只接受数字，大小写字母，下划线（_）和连字符（-）")
    private String sn;

    @Schema(description = "点击模板卡片后的跳转页面，仅限本小程序内的页面。支持带参数，示例 index?foo=bar，该字段不填则模板无跳转")
    private String page;

    @JsonProperty("to_openid_list")
    @Schema(description = "接收者（用户）的 openid 列表")
    private List<String> toOpenidList;

    @JsonProperty("miniprogram_state")
    private MiniProgramState miniprogramState;

    @Schema(description = "设备型号 id ，通过注册设备获得")
    private String modelId;

    @Schema(description = "模板内容，格式形如 { \"key1\": { \"value\": any }, \"key2\": { \"value\": any } }")
    private Map<String, SingleValue> data;

    @Schema(description = "进入小程序查看”的语言类型")
    private Lang lang = Lang.ZH_CN;
}
