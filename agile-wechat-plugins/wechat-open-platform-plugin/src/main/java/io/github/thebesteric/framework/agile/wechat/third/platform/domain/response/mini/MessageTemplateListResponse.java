package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini;

import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * 个人模板列表
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-02 16:46:01
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "个人模板列表")
public class MessageTemplateListResponse extends WechatResponse {

    @Schema(description = "模板列表")
    private List<Item> data = new ArrayList<>();

    @Data
    public static class Item {
        @Schema(description = "添加至账号下的模板 id，发送小程序订阅消息时所需")
        private String priTmplId;

        @Schema(description = "模版标题")
        private String title;

        @Schema(description = "模版内容")
        private String content;

        @Schema(description = "模板内容示例")
        private String example;

        @Schema(description = "模版类型，2: 一次性订阅，3: 长期订阅")
        private Integer type;

        @Data
        public static class KeywordEnumValue {
            @Schema(description = "枚举参数的 key")
            private String keywordCode;

            @Schema(description = "枚举参数值范围列表")
            private List<String> enumValueList = new ArrayList<>();
        }
    }

}
