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
 * 小程序版本回退
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-01 20:00:49
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "小程序版本回退")
public class MiniCodeRevertCodeReleaseResponse extends WechatResponse {

    @JsonProperty("version_list")
    @Schema(description = "模板信息列表，当 action = get_history_version 才会返回")
    private List<Version> versionList = new ArrayList<>();

    @Data
    public static class Version {
        @JsonProperty("app_version")
        @Schema(description = "小程序版本")
        private Integer appVersion;

        @JsonProperty("user_version")
        @Schema(description = "模板版本号，开发者自定义字段")
        private String userVersion;

        @JsonProperty("user_desc")
        @Schema(description = "模板描述，开发者自定义字段")
        private String userDesc;

        @JsonProperty("commit_time")
        @Schema(description = "更新时间，时间戳")
        private Long commitTime;
    }
}
