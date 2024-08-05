package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * PhoneNumberResponse
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-02 15:48:51
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "获取手机号")
public class PhoneNumberResponse extends WechatResponse {

    @JsonProperty("phone_info")
    @Schema(description = "用户手机号信息")
    private PhoneInfo phoneInfo;

    @Data
    public static class PhoneInfo {
        @Schema(description = "用户绑定的手机号（国外手机号会有区号）")
        private String phoneNumber;

        @Schema(description = "没有区号的手机号")
        private String purePhoneNumber;

        @Schema(description = "区号")
        private String countryCode;

        @Schema(description = "数据水印")
        private Watermark watermark;

        @Data
        public static class Watermark {
            @Schema(description = "小程序 appid")
            private String appid;
            @Schema(description = "用户获取手机号操作的时间戳")
            private Long timestamp;
        }
    }
}
