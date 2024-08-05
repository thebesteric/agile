package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.third;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 各版本用户占比
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-02 11:00:10
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "各版本用户占比")
public class MiniCodeSupportVersionResponse extends WechatResponse {

    @JsonProperty("now_version")
    @Schema(description = "当前版本")
    private String nowVersion;

    @JsonProperty("uv_info")
    @Schema(description = "各版本用户占比")
    private List<UvInfo> uvInfo = new ArrayList<>();

    @Data
    public static class UvInfo {
        @Schema(description = "基础库版本号")
        private String version;
        @Schema(description = "该版本用户占比")
        private Integer percentage;
    }
}
