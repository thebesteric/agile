package io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.ObjectParamRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 修改动态消息
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-05 17:24:58
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "修改动态消息")
public class SetUpdatableMsgRequest extends ObjectParamRequest {

    @JsonProperty("activity_id")
    @Schema(description = "动态消息的 ID，通过 createActivityId 接口获取")
    private String activityId;

    @JsonProperty("target_state")
    @Schema(description = "动态消息修改后的状态, 0-未开始，1-已开始")
    private Integer targetState;

    @JsonProperty("template_info")
    @Schema(description = "动态消息对应的模板信息")
    private TemplateInfo templateInfo;

    @Data
    private static class TemplateInfo {

        @JsonProperty("parameter_list")
        @Schema(description = "要修改的模板信息")
        private List<ParameterItem> parameterList;

        @Data
        public static class ParameterItem {
            @Schema(description = "要修改的参数名")
            private String name;

            @Schema(description = "要修改的参数值")
            private String value;
        }
    }
}
