package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.third;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分阶段发布详情
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-02 10:25:28
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MiniCodeGrayReleasePlanResponse extends WechatResponse {

    @JsonProperty("gray_release_plan")
    @Schema(description = "分阶段发布详情")
    private GrayReleasePlan grayReleasePlan;

    @Data
    public static class GrayReleasePlan {
        @Schema(description = "0-初始状态，1-执行中，2-暂停中，3-执行完毕，4-被删除")
        private Integer status;

        @JsonProperty("create_timestamp")
        @Schema(description = "分阶段发布时间，时间戳")
        private Long createTimestamp;

        @JsonProperty("gray_percentage")
        @Schema(description = "当前的灰度比例")
        private Integer grayPercentage;

        @JsonProperty("support_debuger_first")
        @Schema(description = "是否支持按项目成员灰度")
        private Boolean supportDebugerFirst;

        @JsonProperty("support_experiencer_first")
        @Schema(description = "是否支持按体验成员灰度")
        private Boolean supportExperiencerFirst;
    }
}
