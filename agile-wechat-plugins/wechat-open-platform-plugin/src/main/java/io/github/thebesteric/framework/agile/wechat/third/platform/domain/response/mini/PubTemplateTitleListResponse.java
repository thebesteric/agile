package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini;

import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * 所属类目下的公共模板
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-02 16:35:20
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "所属类目下的公共模板")
public class PubTemplateTitleListResponse extends WechatResponse {

    @Schema(description = "模板标题列表总数")
    private Integer count;

    @Schema(description = "模板标题列表")
    private List<Item> data = new ArrayList<>();

    @Data
    public static class Item {
        @Schema(description = "模板标题 id")
        private String tid;

        @Schema(description = "模板标题")
        private String title;

        @Schema(description = "模版类型，2: 一次性订阅，3: 长期订阅")
        private Integer type;

        @Schema(description = "模板所属类目 id")
        private Integer categoryId;
    }
}
