package io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.credential;

import cn.hutool.http.HttpResponse;
import io.github.thebesteric.framework.agile.commons.util.MapWrapper;
import io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.AbstractMiniApi;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.BaseResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini.AccessTokenResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.utils.HttpUtils;

import java.util.Map;

/**
 * 接口调用凭证
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-02 13:56:56
 */
public class CredentialApi extends AbstractMiniApi {

    public CredentialApi(String appId, String appSecret) {
        super(appId, appSecret);
    }

    /**
     * 获取接口调用凭据
     *
     * @return AccessTokenResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/mp-access-token/getAccessToken.html">获取接口调用凭据</a>
     * @author wangweijun
     * @since 2024/8/2 14:12
     */
    public AccessTokenResponse getAccessToken() {
        String url = "https://api.weixin.qq.com/cgi-bin/token";
        Map<String, Object> params = MapWrapper.create()
                .put("grant_type", "client_credential")
                .put("appid", this.appId)
                .put("secret", this.appSecret)
                .build();
        HttpResponse response = HttpUtils.get(url, params);
        return BaseResponse.of(response, AccessTokenResponse.class);
    }

    /**
     * 获取稳定接口调用凭据
     *
     * @param forceRefresh false: access_token 有效期内重复调用该接口不会更新；true: 会导致上次获取的 access_token 失效，并返回新的 access_token
     *
     * @return AccessTokenResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/mp-access-token/getStableAccessToken.html">获取稳定接口调用凭据</a>
     * @author wangweijun
     * @since 2024/8/2 14:12
     */
    public AccessTokenResponse getStableAccessToken(boolean forceRefresh) {
        String url = "https://api.weixin.qq.com/cgi-bin/stable_token";
        Map<String, Object> params = MapWrapper.create()
                .put("grant_type", "client_credential")
                .put("appid", this.appId)
                .put("secret", this.appSecret)
                .put("forceRefresh", forceRefresh)
                .build();
        HttpResponse response = HttpUtils.post(url, params);
        return BaseResponse.of(response, AccessTokenResponse.class);
    }

}
