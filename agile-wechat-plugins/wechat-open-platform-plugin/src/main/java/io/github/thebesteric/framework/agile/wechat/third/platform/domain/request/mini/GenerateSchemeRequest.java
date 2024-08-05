package io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.constant.mini.EnvVersion;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.ObjectParamRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 获取加密 scheme 码
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-05 15:00:21
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "获取加密 scheme 码")
public class GenerateSchemeRequest extends ObjectParamRequest {

    @JsonProperty("is_expire")
    @Schema(description = "是否失效")
    private Boolean isExpire = false;

    @JsonProperty("expire_time")
    @Schema(description = "到期失效的 scheme 码的失效时间，为 Unix 时间戳。生成的到期失效 scheme 码在该时间前有效。最长有效期为30天。is_expire 为 true 且 expire_type 为 0 时必填")
    private Long expireTime;

    @JsonProperty("expire_type")
    @Schema(description = "默认值 0，到期失效的 scheme 码失效类型，失效时间：0，失效间隔天数：1")
    private Integer expireType = 0;

    @JsonProperty("expire_interval")
    @Schema(description = "到期失效的 scheme 码的失效间隔天数。生成的到期失效 scheme 码在该间隔时间到达前有效。最长间隔天数为30天。is_expire 为 true 且 expire_type 为 1 时必填")
    private Integer expireInterval = 30;

    @JsonProperty("jump_wxa")
    @Schema(description = "跳转的小程序信息")
    private JumpWxa jumpWxa;

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
