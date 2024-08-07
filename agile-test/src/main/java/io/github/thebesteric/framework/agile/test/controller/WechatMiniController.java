package io.github.thebesteric.framework.agile.test.controller;

import io.github.thebesteric.framework.agile.wechat.third.platform.WechatMiniHelper;
import io.github.thebesteric.framework.agile.wechat.third.platform.constant.mini.MediaCheckType;
import io.github.thebesteric.framework.agile.wechat.third.platform.constant.mini.Scene;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.event.mini.CustomerTextMessageEvent;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.event.mini.MediaCheckAsyncEvent;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini.MediaCheckAsyncRequest;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini.AccessTokenResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini.MediaCheckSyncResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.listener.mini.AbstractWechatMiniEventListener;
import io.github.thebesteric.framework.agile.wechat.third.platform.listener.mini.AbstractWechatMiniMessageListener;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * WechatMiniController
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-06 11:35:26
 */
@Slf4j
@RestController
@RequestMapping("/wechat-mini")
@RequiredArgsConstructor
public class WechatMiniController {

    private final WechatMiniHelper wechatMiniHelper;

    private final MyWechatMiniEventListener myWechatMiniEventListener = new MyWechatMiniEventListener();
    private final MyWechatMiniMessageListener myWechatMiniMessageListener = new MyWechatMiniMessageListener();

    @GetMapping("/mediaCheckAsyncEvent")
    public MediaCheckSyncResponse mediaCheckAsyncEvent() {
        AccessTokenResponse accessTokenResponse = wechatMiniHelper.getCredentialModule().credentialApi().getAccessToken();
        String accessToken = accessTokenResponse.getAccessToken();
        MediaCheckAsyncRequest request = new MediaCheckAsyncRequest();
        request.setOpenId("ogItP5cWMGU-rJImIHsjZKsJTV68");
        request.setScene(Scene.COMMENT);
        request.setMediaType(MediaCheckType.IMAGE);
        request.setMediaUrl("https://wechatapppro-1252524126.file.myqcloud.com/appgpn9idwb6991/image/b_u_5f9b9eb6dd0cb_CC5WwKI9/p2k6qylosfrlfr.jpg");
        MediaCheckSyncResponse mediaCheckSyncResponse = wechatMiniHelper.getMiniSecurityModule().contentSecurityApi().mediaCheckAsync(accessToken, request);
        return mediaCheckSyncResponse;
    }

    @PostMapping("/test")
    public String test(HttpServletRequest request) throws Exception {
        return wechatMiniHelper.getListener().listen(request, myWechatMiniEventListener, myWechatMiniMessageListener);
    }

    @RequestMapping("/callback")
    public String callback(HttpServletRequest request) throws Exception {
        System.out.println("request.getMethod() = " + request.getMethod());
        return wechatMiniHelper.getListener().listen(request, myWechatMiniEventListener, myWechatMiniMessageListener);
    }

    public static class MyWechatMiniEventListener extends AbstractWechatMiniEventListener {
        @Override
        public void onMediaCheckAsyncEvent(MediaCheckAsyncEvent mediaCheckAsyncEvent) {
            log.info("mediaCheckAsyncEvent = {}", mediaCheckAsyncEvent);
        }
    }

    public static class MyWechatMiniMessageListener extends AbstractWechatMiniMessageListener {
        @Override
        public void onCustomerTextMessageEvent(CustomerTextMessageEvent customerTextMessageEvent) {
            super.onCustomerTextMessageEvent(customerTextMessageEvent);
        }
    }

}
