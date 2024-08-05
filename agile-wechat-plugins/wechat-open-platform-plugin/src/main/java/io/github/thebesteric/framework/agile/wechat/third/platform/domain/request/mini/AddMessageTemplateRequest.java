package io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini;

import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.ObjectParamRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 添加模板
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-02 17:50:36
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "添加模板")
public class AddMessageTemplateRequest extends ObjectParamRequest {
    @Schema(description = "模板标题 id，可通过接口获取，也可登录小程序后台查看获取")
    private String tid;

    @Schema(description = "开发者自行组合好的模板关键词列表，关键词顺序可以自由搭配，最多支持 5 个，最少 2 个关键词组合")
    private List<Integer> kidList = new ArrayList<>();

    @Schema(description = "服务场景描述，15 个字符以内")
    private String sceneDesc;

}
