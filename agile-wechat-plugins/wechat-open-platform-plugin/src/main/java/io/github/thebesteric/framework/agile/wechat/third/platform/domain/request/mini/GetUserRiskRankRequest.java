package io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.ObjectParamRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 获取用户安全等级
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-06 14:30:19
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "获取用户安全等级")
public class GetUserRiskRankRequest extends ObjectParamRequest {
    @JsonProperty("appid")
    @Schema(description = "小程序 AppId")
    private String appId;

    @JsonProperty("openid")
    @Schema(description = "用户的 OpenId")
    private String openId;

    @Schema(description = "场景值，0:注册，1:营销作弊")
    private Integer scene;

    @JsonProperty("mobile_no")
    @Schema(description = "手机号")
    private String mobileNo;

    @JsonProperty("client_ip")
    @Schema(description = "客户端 IP")
    private String clientIp;

    @JsonProperty("email_address")
    @Schema(description = "邮箱地址")
    private String emailAddress;

    @JsonProperty("extended_info")
    @Schema(description = "额外补充信息")
    private String extendedInfo;

    @JsonProperty("is_test")
    @Schema(description = "false：正式调用，true：测试调用")
    private Boolean isTest = false;
}
