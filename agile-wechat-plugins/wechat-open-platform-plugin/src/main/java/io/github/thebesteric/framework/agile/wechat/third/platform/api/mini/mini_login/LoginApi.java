package io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.mini_login;

import cn.hutool.http.HttpResponse;
import io.github.thebesteric.framework.agile.commons.util.MapWrapper;
import io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.AbstractMiniApi;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.BaseResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini.Code2SessionResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini.ResetUserSessionKeyResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.utils.CryptUtils;
import io.github.thebesteric.framework.agile.wechat.third.platform.utils.HttpUtils;

import java.util.Map;

/**
 * 小程序登录
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-05 09:44:03
 */
public class LoginApi extends AbstractMiniApi {

    public LoginApi(String appId, String appSecret) {
        super(appId, appSecret);
    }

    /**
     * 小程序登录
     *
     * @param jsCode 登录时获取的 code，可通过 wx.login 获取
     *
     * @return Code2SessionResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/user-login/code2Session.html">小程序登录</a>
     * @author wangweijun
     * @since 2024/8/5 09:58
     */
    public Code2SessionResponse code2Session(String jsCode) {
        String url = "https://api.weixin.qq.com/sns/jscode2session";
        Map<String, Object> params = MapWrapper.create()
                .put("appid", appId).put("secret", appSecret).put("js_code", jsCode).put("grant_type", "authorization_code")
                .build();
        HttpResponse response = HttpUtils.get(url, params);
        return BaseResponse.of(response, Code2SessionResponse.class);
    }

    /**
     * 检验登录态
     *
     * @param accessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 access_token 或者 authorizer_access_token
     * @param openId      用户唯一标识符
     * @param sessionKey  sessionKey
     *
     * @return WechatResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/user-login/checkSessionKey.html">检验登录态</a>
     * @author wangweijun
     * @since 2024/8/5 11:54
     */
    public WechatResponse checkSessionKey(String accessToken, String openId, String sessionKey) {
        String url = String.format("https://api.weixin.qq.com/wxa/checksession?access_token=%s", accessToken);
        Map<String, Object> params = MapWrapper.create()
                .put("openid", openId).put("signature", CryptUtils.sha256("", sessionKey)).put("sig_method", "hmac_sha256")
                .build();
        HttpResponse response = HttpUtils.get(url, params);
        return BaseResponse.of(response, WechatResponse.class);
    }

    /**
     * 重置登录态
     *
     * @param accessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 access_token 或者 authorizer_access_token
     * @param openId      用户唯一标识符
     * @param sessionKey  sessionKey
     *
     * @return ResetUserSessionKeyResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/user-login/ResetUserSessionKey.html">重置登录态</a>
     * @author wangweijun
     * @since 2024/8/5 11:59
     */
    public ResetUserSessionKeyResponse resetUserSessionKey(String accessToken, String openId, String sessionKey) {
        String url = String.format("https://api.weixin.qq.com/wxa/resetusersessionkey?access_token=%s", accessToken);
        Map<String, Object> params = MapWrapper.create()
                .put("openid", openId).put("signature", CryptUtils.sha256("", sessionKey)).put("sig_method", "hmac_sha256")
                .build();
        HttpResponse response = HttpUtils.get(url, params);
        return BaseResponse.of(response, ResetUserSessionKeyResponse.class);
    }

}
