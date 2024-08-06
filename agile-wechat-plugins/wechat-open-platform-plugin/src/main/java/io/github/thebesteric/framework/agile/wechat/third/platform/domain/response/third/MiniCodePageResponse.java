package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.third;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * 获取已上传的代码页面列表
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-01 16:07:51
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "获取已上传的代码页面列表")
public class MiniCodePageResponse extends WechatResponse {

    @Schema(description = "page_list 页面配置列表")
    @JsonProperty("page_list")
    private List<String> pageList = new ArrayList<>();

}
