package io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.third;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.ObjectParamRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分阶段发布（灰度发布）
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-02 10:15:54
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "分阶段发布（灰度发布）")
public class MiniCodeGrayReleaseRequest extends ObjectParamRequest {

    @JsonProperty("gray_percentage")
    @Schema(description = "灰度发布百分比，0-100，如果 gray_percentage=0，support_experiencer_first 与 support_debuger_first 二选一必填")
    private Integer grayPercentage;

    @JsonProperty("support_debuger_first")
    @Schema(description = "是否支持按体验成员灰度，默认是 false")
    private Boolean supportDebugerFirst = false;

    @JsonProperty("support_experiencer_first")
    @Schema(description = "是否支持按项目成员灰度，默认为 false")
    private Boolean supportExperiencerFirst = false;
}
