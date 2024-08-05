package io.github.thebesteric.framework.agile.test.controller;

import io.github.thebesteric.framework.agile.wechat.third.platform.WechatThirdPlatformHelper;
import io.github.thebesteric.framework.agile.wechat.third.platform.api.third.platform_credential.CredentialApi;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.third.ComponentAccessTokenResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.third.PreAuthCodeResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.xml.authroization.AuthorizedEvent;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.xml.authroization.ComponentVerifyTicketEvent;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.xml.authroization.UnAuthorizedEvent;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.xml.authroization.UpdateAuthorizedEvent;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.xml.message.WeAppAuditDelayEvent;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.xml.message.WeAppAuditFailEvent;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.xml.message.WeAppAuditSuccessEvent;
import io.github.thebesteric.framework.agile.wechat.third.platform.listener.AuthorizationEventListener;
import io.github.thebesteric.framework.agile.wechat.third.platform.listener.MessageEventListener;
import io.github.thebesteric.framework.agile.wechat.third.platform.listener.WechatThirdPlatformListener;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * WechatThirdPlatformController
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-29 14:46:59
 */
@RestController
@RequestMapping("/wechat-third-platform")
public class WechatThirdPlatformController {

    /** 第三方组件的 AppId */
    private final static String COMPONENT_APP_ID = "wxa2fb5b84243374b2";
    /** 第三方组件的 AppSecret */
    private final static String COMPONENT_APP_SECRET = "3a8638c3f2460a7e33d66d42e3c2dfb7";
    /** 消息校验 Token */
    private final static String VERIFY_TOKEN = "test";
    /** 消息加解密 Key */
    private final static String ENCRYPT_AES_KEY = "1234567890abcde1234567890abcde1234567890abc";

    private final static WechatThirdPlatformHelper helper = new WechatThirdPlatformHelper(COMPONENT_APP_ID, COMPONENT_APP_SECRET, VERIFY_TOKEN, ENCRYPT_AES_KEY);

    @PostMapping("/wxa2fb5b84243374b2/ticket")
    public String ticket(HttpServletRequest request) throws Exception {
        WechatThirdPlatformListener listener = helper.getListener();
        return listener.authorizationEventListener(request, new AuthorizationEventListenerImpl());
    }

    @PostMapping("/wxa2fb5b84243374b2/callback")
    public String callback(HttpServletRequest request) throws Exception {
        WechatThirdPlatformListener listener = helper.getListener();
        return listener.messageEventListener(request, new MessageEventListenerImpl());
    }

    public static class AuthorizationEventListenerImpl extends AuthorizationEventListener {
        @Override
        public void onComponentVerifyTicket(ComponentVerifyTicketEvent componentVerifyTicketEvent) {
            String componentVerifyTicket = componentVerifyTicketEvent.getComponentVerifyTicket();
            System.out.println("onComponentVerifyTicket = " + componentVerifyTicket);

            CredentialApi credentialApi = helper.getCredentialApi();
            ComponentAccessTokenResponse componentAccessTokenResponse = credentialApi.getComponentAccessToken(componentVerifyTicket);
            String componentAccessToken = componentAccessTokenResponse.getComponentAccessToken();
            System.out.println("componentAccessToken = " + componentAccessToken);

            PreAuthCodeResponse preAuthCodeResponse = credentialApi.getPreAuthCode(componentAccessToken);
            System.out.println("preAuthCode = " + preAuthCodeResponse.getPreAuthCode());

        }

        @Override
        public void onAuthorized(AuthorizedEvent authorizedEvent) {
            System.out.println("onAuthorized" + authorizedEvent);
        }

        @Override
        public void onUnAuthorized(UnAuthorizedEvent unAuthorizedEvent) {
            System.out.println("onUnAuthorized" + unAuthorizedEvent);
        }

        @Override
        public void onUpdateAuthorized(UpdateAuthorizedEvent updateAuthorizedEvent) {
            System.out.println("onUpdateAuthorized" + updateAuthorizedEvent);
        }
    }

    public static class MessageEventListenerImpl extends MessageEventListener {
        @Override
        public void onSubmitAuditDelay(WeAppAuditDelayEvent weAppAuditDelayEvent) {
            System.out.println("onSubmitAuditDelay" + weAppAuditDelayEvent);
        }

        @Override
        public void onSubmitAuditFail(WeAppAuditFailEvent weAppAuditFailEvent) {
            System.out.println("onSubmitAuditFail" + weAppAuditFailEvent);
        }

        @Override
        public void onSubmitAuditSuccess(WeAppAuditSuccessEvent weAppAuditSuccessEvent) {
            super.onSubmitAuditSuccess(weAppAuditSuccessEvent);
        }
    }

}
