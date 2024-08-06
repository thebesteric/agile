package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini;

import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * 类目列表
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-02 16:14:53
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "类目列表")
public class CategoryResponse extends WechatResponse {

    @Schema(description = "类目列表")
    private List<Item> data = new ArrayList<>();

    @Data
    public static class Item {
        @Schema(description = "类目 id，查询公共库模版时需要")
        private String id;

        @Schema(description = "类目的中文名")
        private String name;
    }

}
