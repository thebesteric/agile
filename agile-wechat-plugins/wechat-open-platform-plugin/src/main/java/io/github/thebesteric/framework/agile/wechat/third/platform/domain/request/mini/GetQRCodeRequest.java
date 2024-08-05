package io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.constant.mini.EnvVersion;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.ObjectParamRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 获取小程序码
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-05 13:37:29
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "获取小程序码")
public class GetQRCodeRequest extends ObjectParamRequest {
    @Schema(description = "扫码进入的小程序页面路径，最大长度 1024 个字符")
    private String path = "pages/index/index";

    @Schema(description = "二维码的宽度，单位 px，最小 280px，最大 1280px")
    private Integer width = 280;

    @JsonProperty("auto_color")
    @Schema(description = "自动配置线条颜色，如果颜色依然是黑色，则说明不建议配置主色调，默认 false")
    private boolean autoColor = false;

    @JsonProperty("is_hyaline")
    @Schema(description = "否需要透明底色，为 true 时，生成透明底色的小程序码，默认 false")
    private boolean isHyaline = false;

    @JsonProperty("env_version")
    @Schema(description = "要打开的小程序版本。正式版为 release，体验版为 trial，开发版为 develop。默认是正式版")
    private EnvVersion envVersion = EnvVersion.RELEASE;

    @JsonProperty("line_color")
    @Schema(description = "设置颜色")
    private LineColor lineColor;

    @Data
    public static class LineColor {
        @Schema(description = "默认值: {\"r\":0,\"g\":0,\"b\":0} ；auto_color 为 false 时生效，使用 rgb 设置颜色 例如: {\"r\":\"xxx\",\"g\":\"xxx\",\"b\":\"xxx\"} 十进制表示")
        private String r;
        @Schema(description = "默认值: {\"r\":0,\"g\":0,\"b\":0} ；auto_color 为 false 时生效，使用 rgb 设置颜色 例如: {\"r\":\"xxx\",\"g\":\"xxx\",\"b\":\"xxx\"} 十进制表示")
        private String g;
        @Schema(description = "默认值: {\"r\":0,\"g\":0,\"b\":0} ；auto_color 为 false 时生效，使用 rgb 设置颜色 例如: {\"r\":\"xxx\",\"g\":\"xxx\",\"b\":\"xxx\"} 十进制表示")
        private String b;
    }
}
