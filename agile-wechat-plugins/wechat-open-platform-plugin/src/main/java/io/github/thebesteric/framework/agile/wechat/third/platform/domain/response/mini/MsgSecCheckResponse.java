package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.constant.mini.ContentLabel;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * 文本内容安全识别
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-06 10:26:08
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "文本内容安全识别")
public class MsgSecCheckResponse extends WechatResponse {
    @JsonProperty("trace_id")
    @Schema(description = "唯一请求标识，标记单次请求")
    private String traceId;

    @Schema(description = "详细检测结果")
    private List<Detail> detail;

    @Schema(description = "综合结果")
    private Result result;

    @Data
    public static class Detail {
        @Schema(description = "策略类型")
        private String strategy;

        @JsonProperty("errcode")
        @Schema(description = "错误码，仅当该值为 0 时，该项结果有效")
        private Integer errCode;

        @Schema(description = "建议，有 risky、pass、review 三种值")
        private String suggest;

        @Schema(description = "命中标签枚举值，100 正常；10001 广告；20001 时政；20002 色情；20003 辱骂；20006 违法犯罪；20008 欺诈；20012 低俗；20013 版权；21000 其他")
        private ContentLabel label;

        @Schema(description = "命中的自定义关键词")
        private String keyword;

        @Schema(description = "0-100，代表置信度，越高代表越有可能属于当前返回的标签（label）")
        private Integer prob;
    }

    @Data
    public static class Result {
        @Schema(description = "建议，有 risky、pass、review 三种值")
        private String suggest;

        @Schema(description = "命中标签枚举值，100 正常；10001 广告；20001 时政；20002 色情；20003 辱骂；20006 违法犯罪；20008 欺诈；20012 低俗；20013 版权；21000 其他")
        private ContentLabel label;
    }
}
