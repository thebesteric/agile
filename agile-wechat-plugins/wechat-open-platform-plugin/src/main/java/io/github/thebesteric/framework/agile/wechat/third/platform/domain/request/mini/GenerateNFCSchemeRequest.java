package io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.constant.mini.EnvVersion;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.ObjectParamRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 获取 NFC 的小程序 scheme
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-05 14:55:01
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "获取 NFC 的小程序 scheme")
public class GenerateNFCSchemeRequest extends ObjectParamRequest {
    @JsonProperty("model_id")
    @Schema(description = "scheme 对应的设备 model_id")
    private String modelId;

    @Schema(description = "scheme 对应的设备 sn，仅一机一码时填写")
    private String sn;

    @JsonProperty("jump_wxa")
    @Schema(description = "跳转的小程序信息")
    private GenerateSchemeRequest.JumpWxa jumpWxa;

    @Data
    public static class JumpWxa {
        @JsonProperty("path")
        @Schema(description = "通过 scheme 码进入的小程序页面路径，必须是已经发布的小程序存在的页面，不可携带 query。path 为空时会跳转小程序主页")
        private String path;

        @JsonProperty("query")
        @Schema(description = "通过 scheme 码进入小程序时的 query，最大1024个字符，只支持数字，大小写英文以及部分特殊字符")
        private String query;

        @JsonProperty("env_version")
        @Schema(description = "要打开的小程序版本。正式版为 release，体验版为 trial，开发版为 develop。默认为 release")
        private EnvVersion envVersion = EnvVersion.RELEASE;
    }
}
