package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.constant.mini.PkgType;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * 查询 license 资源包列表
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-06 21:08:08
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "查询 license 资源包列表")
public class GetLicensePkgListResponse extends WechatResponse {

    @JsonProperty("pkg_list")
    @Schema(description = "资源包列表")
    private List<Pkg> pkgList;

    @Data
    public static class Pkg {

        @JsonProperty("pkg_id")
        @Schema(description = "资源包 ID")
        private String pkgId;

        @JsonProperty("pkg_type")
        @Schema(description = "资源包类型")
        private PkgType pkgType;

        @JsonProperty("start_time")
        @Schema(description = "资源包下单时间")
        private Long startTime;

        @JsonProperty("end_time")
        @Schema(description = "资源包过期时间")
        private Long endTime;

        @JsonProperty("pkg_status")
        @Schema(description = "资源包状态，1-已生效，2-未生效，3-已过期")
        private Integer pkgStatus;

        @Schema(description = "已使用额度")
        private Integer used;

        @Schema(description = "资源包总量")
        private Integer all;
    }

}
