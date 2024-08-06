package io.github.thebesteric.framework.agile.wechat.third.platform;

import io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.credential.CredentialApi;
import io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.credential.CredentialModule;
import io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.message.DynamicMessageApi;
import io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.message.MessageModule;
import io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.message.SubscribeMessageApi;
import io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.mini_code.*;
import io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.mini_customer.MiniCustomerMessageApi;
import io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.mini_customer.MiniCustomerModule;
import io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.mini_login.LoginApi;
import io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.mini_login.MiniProgramLoginModule;
import io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.mini_security.ContentSecurityApi;
import io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.mini_security.MiniSecurityModule;
import io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.mini_security.UserRiskRankApi;
import io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.user_info.MobileApi;
import io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.user_info.NetworkApi;
import io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.user_info.UserInfoApi;
import io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.user_info.UserInfoModule;
import io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.wechat_customer.WechatCustomerApi;
import io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.wechat_customer.WechatCustomerModule;
import io.github.thebesteric.framework.agile.wechat.third.platform.config.mini.WechatMiniProperties;
import io.github.thebesteric.framework.agile.wechat.third.platform.listener.mini.WechatMiniListener;
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

    /** 配置内容 */
    private final WechatMiniProperties properties;

    /** 接口调用凭证 */
    private final CredentialModule credentialModule;
    /** 小程序登录 */
    private final MiniProgramLoginModule miniProgramLoginModule;
    /** 用户信息 */
    private final UserInfoModule userInfoModule;
    /** 消息相关 */
    private final MessageModule messageModule;
    /** 小程序码与小程序链接 */
    private final MiniCodeModule miniCodeModule;
    /** 小程序客服 */
    private final MiniCustomerModule miniCustomerModule;
    /** 小程序安全 */
    private final MiniSecurityModule miniSecurityModule;
    /** 微信客服 */
    private final WechatCustomerModule wechatCustomerModule;

    /** 事件监听 */
    private final WechatMiniListener listener;

    public WechatMiniHelper(final WechatMiniProperties properties) {
        this.properties = properties;
        String appId = properties.getAppId();
        String appSecret = properties.getAppSecret();

        this.credentialModule = new CredentialModule(new CredentialApi(appId, appSecret));
        this.miniProgramLoginModule = new MiniProgramLoginModule(new LoginApi(appId, appSecret));
        this.userInfoModule = new UserInfoModule(new UserInfoApi(), new MobileApi(), new NetworkApi());
        this.messageModule = new MessageModule(new DynamicMessageApi(), new SubscribeMessageApi());
        this.miniCodeModule = new MiniCodeModule(new MiniCodeApi(), new ShortLinkApi(), new UrlLinkApi(), new UrlSchemaApi());
        this.miniCustomerModule = new MiniCustomerModule(new MiniCustomerMessageApi());
        this.miniSecurityModule = new MiniSecurityModule(new ContentSecurityApi(), new UserRiskRankApi());
        this.wechatCustomerModule = new WechatCustomerModule(new WechatCustomerApi());

        this.listener = new WechatMiniListener(properties);
    }

}
