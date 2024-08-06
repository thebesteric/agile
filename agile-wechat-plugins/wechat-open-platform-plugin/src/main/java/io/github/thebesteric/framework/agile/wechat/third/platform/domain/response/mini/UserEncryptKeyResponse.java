package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * 获取用户 encryptKey
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-02 15:56:41
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "获取用户 encryptKey")
public class UserEncryptKeyResponse extends WechatResponse {

    @JsonProperty("key_info_list")
    @Schema(description = "用户最近三次的加密 key 列表")
    private List<KeyInfo> keyInfoList = new ArrayList<>();

    @Data
    public static class KeyInfo {
        @JsonProperty("encrypt_key")
        @Schema(description = "加密 key")
        private String encryptKey;

        @Schema(description = "key 的版本号")
        private String version;

        @Schema(description = "用户加密算法的初始向量")
        private String iv;

        @JsonProperty("expire_in")
        @Schema(description = "剩余有效时间")
        private String expireIn;

        @JsonProperty("create_time")
        @Schema(description = "创建 key 的时间戳")
        private String createTime;
    }
}
