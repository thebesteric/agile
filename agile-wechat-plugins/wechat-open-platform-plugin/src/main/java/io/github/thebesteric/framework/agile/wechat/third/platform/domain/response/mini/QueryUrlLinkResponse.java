package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.constant.mini.EnvVersion;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 查询加密 URLLink
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-05 15:41:40
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "查询加密 URLLink")
public class QueryUrlLinkResponse extends WechatResponse {

    @JsonProperty("url_link_info")
    @Schema(description = "url_link 配置")
    private UrlLinkInfo schemeInfo;

    @JsonProperty("quota_info")
    @Schema(description = "quota 配置")
    private QuotaInfo quotaInfo;

    @Data
    public static class UrlLinkInfo {
        @JsonProperty("appid")
        @Schema(description = "小程序 appid")
        private String appId;

        @Schema(description = "小程序页面路径")
        private String path;

        @Schema(description = "小程序页面路径参数")
        private String query;

        @JsonProperty("create_time")
        @Schema(description = "scheme 码的创建时间，为 Unix 时间戳")
        private Long createTime;

        @JsonProperty("expire_time")
        @Schema(description = "到期失效时间，为 Unix 时间戳，0 表示永久生效")
        private Long expireTime;

        @JsonProperty("env_version")
        @Schema(description = "scheme 码对应的小程序版本")
        private EnvVersion envVersion;
    }

    @Data
    public static class QuotaInfo {
        @JsonProperty("remain_visit_quota")
        @Schema(description = "URL Scheme（加密+明文）/加密 URL Link 单天剩余访问次数")
        private Integer remainVisitQuota;
    }

}
