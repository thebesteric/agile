package io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.ObjectParamRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 获取用户 encryptKey
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-02 16:02:23
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "获取用户 encryptKey")
public class UserEncryptKeyRequest extends ObjectParamRequest {
    @JsonProperty("用户的 openid")
    @Schema(description = "用户唯一标识")
    private String openId;

    @Schema(description = "用 session_key 对空字符串签名得到的结果。session_key 可通过 code2Session 接口获得")
    private String signature;

    @JsonProperty("sig_method")
    @Schema(description = "签名算法，目前支持 hmac_sha256")
    private String sigMethod = "hmac_sha256";
}
