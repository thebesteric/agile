package io.github.thebesteric.framework.agile.wechat.third.platform.api;

import cn.hutool.http.HttpResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.BaseResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.utils.HttpUtils;

import java.util.Map;

/**
 * BindApi
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-01 13:37:03
 */
public class BindApi {

    /**
     * 将公众号/小程序绑定到开放平台账号下
     *
     * @param authorizerAccessToken 第三方平台接口调用令牌 authorizer_access_token
     * @param openAppId             开放平台账号 appid
     *
     * @return BaseResponse
     *
     * @link <a href="https://developers.weixin.qq.com/doc/oplatform/Third-party_Platforms/2.0/api/account/bind.html">将公众号/小程序绑定到开放平台账号下</a>
     * @author wangweijun
     * @since 2024/8/1 13:38
     */
    public BaseResponse bind(String authorizerAccessToken, String openAppId) {
        String url = "https://api.weixin.qq.com/cgi-bin/open/bind?access_token=%s".formatted(authorizerAccessToken);
        HttpResponse response = HttpUtils.post(url, Map.of("open_appid", openAppId));
        return BaseResponse.of(response);

    }

}
