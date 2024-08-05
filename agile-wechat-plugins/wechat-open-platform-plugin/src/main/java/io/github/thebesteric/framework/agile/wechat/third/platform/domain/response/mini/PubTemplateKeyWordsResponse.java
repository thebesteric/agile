package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini;

import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 关键词列表
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-02 16:29:32
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "关键词列表")
public class PubTemplateKeyWordsResponse extends WechatResponse {
    @Schema(description = "模版标题列表总数")
    private Integer count;

    @Schema(description = "关键词列表")
    private List<Item> data = new ArrayList<>();

    @Data
    public static class Item {
        @Schema(description = "关键词 id")
        private Integer kid;

        @Schema(description = "关键词内容")
        private String name;

        @Schema(description = "关键词内容对应的示例")
        private String example;

        @Schema(description = "参数类型")
        private String rule;
    }
}
