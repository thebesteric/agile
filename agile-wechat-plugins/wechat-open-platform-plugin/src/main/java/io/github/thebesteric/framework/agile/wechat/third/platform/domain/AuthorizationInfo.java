package io.github.thebesteric.framework.agile.wechat.third.platform.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * AuthorizationInfo
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-31 16:16:47
 */
@Data
public class AuthorizationInfo {

    /** 授权的公众号或者小程序 appid */
    @JsonProperty("authorizer_appid")
    private String authorizerAppId;

    /** 接口调用令牌（在授权的公众号/小程序具备 API 权限时，才有此返回值） */
    @JsonProperty("authorizer_access_token")
    private String authorizerAccessToken;

    /** authorizer_access_token 的有效期（在授权的公众号/小程序具备API权限时，才有此返回值），单位：秒 */
    @JsonProperty("expires_in")
    private Integer expiresIn;

    /** 刷新令牌（在授权的公众号具备API权限时，才有此返回值），刷新令牌主要用于第三方平台获取和刷新已授权用户的 authorizer_access_token。一旦丢失，只能让用户重新授权，才能再次拿到新的刷新令牌。用户重新授权后，之前的刷新令牌会失效 */
    @JsonProperty("authorizer_refresh_token")
    private String authorizerRefreshToken;

    /** 授权给第三方平台的权限集id列表，权限集id代表的含义可查看 */
    @JsonProperty("func_info")
    private List<FuncInfo> funcInfo;

    @Data
    public static class FuncInfo {
        @JsonProperty("funcscope_category")
        private FuncScopeCategory funcScopeCategory;
    }

    @Data
    public static class FuncScopeCategory {
        /** 权限集id */
        private Integer id;
        /** 权限集类型 */
        private Integer type;
        /** 权限集名称 */
        private String name;
        /** 权限集描述 */
        private String desc;
    }

}
