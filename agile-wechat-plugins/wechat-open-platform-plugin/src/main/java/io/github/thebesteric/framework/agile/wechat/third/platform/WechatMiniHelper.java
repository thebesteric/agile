package io.github.thebesteric.framework.agile.wechat.third.platform;

import io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.credential.CredentialApi;
import io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.login.LoginApi;
import io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.message.DynamicMessageApi;
import io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.message.SubscribeMessageApi;
import io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.mini_code.MiniCodeApi;
import io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.mini_code.ShortLinkApi;
import io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.mini_code.UrlLinkApi;
import io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.mini_code.UrlSchemaApi;
import io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.user.MobileApi;
import io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.user.NetworkApi;
import io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.user.UserInfoApi;
import io.github.thebesteric.framework.agile.wechat.third.platform.config.mini.WechatMiniProperties;
import lombok.Getter;

/**
 * 微信小程序帮助类
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-02 13:55:55
 */
@Getter
public class WechatMiniHelper {

    private final String appId;
    private final String appSecret;

    /** 接口调用凭证 */
    private final CredentialApi credentialApi;

    /** 用户信息-用户信息 */
    private final UserInfoApi userInfoApi;
    /** 用户信息-手机号 */
    private final MobileApi mobileApi;
    /** 用户信息-网络 */
    private final NetworkApi networkApi;

    /** 小程序登录 */
    private final LoginApi loginApi;

    /** 消息相关-动态消息 */
    private final DynamicMessageApi dynamicMessageApi;
    /** 消息相关-订阅消息 */
    private final SubscribeMessageApi subscribeMessageApi;

    /** 小程序码与小程序链接-小程序码 */
    private final MiniCodeApi miniCodeApi;
    /** 小程序码与小程序链接-ShortLink */
    private final ShortLinkApi shortLinkApi;
    /** 小程序码与小程序链接-UrlLink */
    private final UrlLinkApi urlLinkApi;
    /** 小程序码与小程序链接-UrlSchema */
    private final UrlSchemaApi urlSchemaApi;

    public WechatMiniHelper(final String appId, final String appSecret) {
        this.appId = appId;
        this.appSecret = appSecret;
        this.credentialApi = new CredentialApi(appId, appSecret);
        this.userInfoApi = new UserInfoApi();
        this.mobileApi = new MobileApi();
        this.networkApi = new NetworkApi();
        this.loginApi = new LoginApi(appId, appSecret);
        this.dynamicMessageApi = new DynamicMessageApi();
        this.subscribeMessageApi = new SubscribeMessageApi();
        this.miniCodeApi = new MiniCodeApi();
        this.shortLinkApi = new ShortLinkApi();
        this.urlLinkApi = new UrlLinkApi();
        this.urlSchemaApi = new UrlSchemaApi();
    }

    public WechatMiniHelper(final WechatMiniProperties properties) {
        this(properties.getAppId(), properties.getAppSecret());
    }

}
