package io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.constant.mini.EnvVersion;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.ObjectParamRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 获取加密 URLLink
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-05 15:30:12
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "获取加密 URLLink")
public class GenerateUrlLinkRequest extends ObjectParamRequest {
    private String path;
    private String query;

    @JsonProperty("expire_type")
    @Schema(description = "默认值 0.小程序 URL Link 失效类型，失效时间：0，失效间隔天数：1")
    private Integer expireType = 0;

    @JsonProperty("expire_time")
    @Schema(description = "到期失效的 scheme 码的失效时间，为 Unix 时间戳。生成的到期失效 scheme 码在该时间前有效。最长有效期为30天。is_expire 为 true 且 expire_type 为 0 时必填")
    private Long expireTime;

    @JsonProperty("expire_interval")
    @Schema(description = "到期失效的 scheme 码的失效间隔天数。生成的到期失效 scheme 码在该间隔时间到达前有效。最长间隔天数为30天。is_expire 为 true 且 expire_type 为 1 时必填")
    private Integer expireInterval = 30;

    @JsonProperty("env_version")
    @Schema(description = "要打开的小程序版本。正式版为 release，体验版为 trial，开发版为 develop。默认为 release")
    private EnvVersion envVersion = EnvVersion.RELEASE;

    @JsonProperty("cloud_base")
    @Schema(description = "云开发静态网站自定义 H5 配置参数，可配置中转的云开发 H5 页面。不填默认用官方 H5 页面")
    private CloudBase cloudBase;

    @Data
    public static class CloudBase {
        @Schema(description = "云开发环境")
        private String env;

        @Schema(description = "静态网站自定义域名，不填则使用默认域名")
        private String domain;

        @Schema(description = "云开发静态网站 H5 页面路径，不可携带 query")
        private String path;

        @Schema(description = "云开发静态网站 H5 页面 query 参数，最大 1024 个字符，只支持数字，大小写英文以及部分特殊字符")
        private String query;

        @JsonProperty("resource_appid")
        @Schema(description = "第三方批量代云开发时必填，表示创建该 env 的 appid（小程序/第三方平台）")
        private String resourceAppid;

    }
}
