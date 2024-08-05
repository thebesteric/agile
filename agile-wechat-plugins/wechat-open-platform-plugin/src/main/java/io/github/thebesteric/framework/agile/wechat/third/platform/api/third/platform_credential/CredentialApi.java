package io.github.thebesteric.framework.agile.wechat.third.platform.api.third.platform_credential;

import cn.hutool.http.HttpResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.Component;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.third.AuthorizerAccessTokenRequest;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.third.AuthorizerRefreshTokenRequest;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.third.ComponentAccessTokenRequest;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.third.AuthorizerAccessTokenResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.third.AuthorizerRefreshTokenResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.third.ComponentAccessTokenResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.third.PreAuthCodeResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.BaseResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.utils.HttpUtils;
import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * 第三方平台调用凭证
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-26 17:04:37
 */
@RequiredArgsConstructor
public class CredentialApi {

    private final Component component;

    /**
     * 启动票据推送服务
     *
     * @return WechatResponse
     *
     * @link <a href="https://developers.weixin.qq.com/doc/oplatform/openApi/OpenApiDoc/ticket-token/startPushTicket.html">启动票据推送服务</a>
     */
    public WechatResponse startPushTicket() {
        HttpResponse response = HttpUtils.post("https://api.weixin.qq.com/cgi-bin/component/api_start_push_ticket", component);
        return BaseResponse.of(response, WechatResponse.class);
    }

    /**
     * 获取令牌
     *
     * @param componentVerifyTicket 验证票据
     *
     * @return ComponentAccessToken
     *
     * @link <a href="https://developers.weixin.qq.com/doc/oplatform/openApi/OpenApiDoc/ticket-token/getComponentAccessToken.html">获取令牌</a>
     */
    public ComponentAccessTokenResponse getComponentAccessToken(String componentVerifyTicket) {
        ComponentAccessTokenRequest accessTokenRequest = ComponentAccessTokenRequest.of(component, componentVerifyTicket);
        HttpResponse response = HttpUtils.post("https://api.weixin.qq.com/cgi-bin/component/api_component_token", accessTokenRequest);
        return BaseResponse.of(response, ComponentAccessTokenResponse.class);
    }

    /**
     * 获取预授权码（每个预授权码有效期为 1800 秒）
     *
     * @return PreAuthCodeResponse
     *
     * @link <a href="https://developers.weixin.qq.com/doc/oplatform/openApi/OpenApiDoc/ticket-token/getPreAuthCode.html">获取预授权码</a>
     * @author wangweijun
     * @since 2024/7/31 17:54
     */
    public PreAuthCodeResponse getPreAuthCode(String componentAccessToken) {
        String url = "https://api.weixin.qq.com/cgi-bin/component/api_create_preauthcode?access_token=%s".formatted(componentAccessToken);
        Map<String, Object> body = Map.of("component_appid", component.getComponentAppId());
        HttpResponse response = HttpUtils.post(url, body);
        return BaseResponse.of(response, PreAuthCodeResponse.class);
    }

    /**
     * 获取授权账号调用令牌
     *
     * @param componentAccessToken   第三方平台接口调用凭证
     * @param authorizerAppId        授权方 appid
     * @param authorizerRefreshToken 刷新令牌
     *
     * @return AuthorizerAccessTokenResponse
     *
     * @link <a href="https://developers.weixin.qq.com/doc/oplatform/openApi/OpenApiDoc/ticket-token/getAuthorizerAccessToken.html">获取授权账号调用令牌</a>
     * @author wangweijun
     * @since 2024/7/31 18:17
     */
    public AuthorizerAccessTokenResponse getAuthorizerAccessToken(String componentAccessToken, String authorizerAppId, String authorizerRefreshToken) {
        String url = "https://api.weixin.qq.com/cgi-bin/component/api_authorizer_token?component_access_token=%s".formatted(componentAccessToken);
        AuthorizerAccessTokenRequest request = new AuthorizerAccessTokenRequest();
        request.setComponentAppId(component.getComponentAppId());
        request.setAuthorizerAppId(authorizerAppId);
        request.setAuthorizerRefreshToken(authorizerRefreshToken);
        HttpResponse response = HttpUtils.post(url, request);
        return BaseResponse.of(response, AuthorizerAccessTokenResponse.class);
    }

    /**
     * 获取刷新令牌
     *
     * @param componentAccessToken 令牌
     * @param authorizationCode    授权码
     *
     * @return AuthorizerRefreshToken
     *
     * @link <a href="https://developers.weixin.qq.com/doc/oplatform/openApi/OpenApiDoc/ticket-token/getAuthorizerRefreshToken.html">获取刷新令牌</a>
     */
    public AuthorizerRefreshTokenResponse getAuthorizerRefreshToken(String componentAccessToken, String authorizationCode) {
        String url = "https://api.weixin.qq.com/cgi-bin/component/api_query_auth?access_token=%s".formatted(componentAccessToken);
        AuthorizerRefreshTokenRequest request = new AuthorizerRefreshTokenRequest();
        request.setComponentAppId(component.getComponentAppId());
        request.setAuthorizationCode(authorizationCode);
        HttpResponse response = HttpUtils.post(url, request);
        return BaseResponse.of(response, AuthorizerRefreshTokenResponse.class);
    }

}
