package io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.test;

import io.github.thebesteric.framework.agile.core.domain.SingleValue;
import io.github.thebesteric.framework.agile.wechat.third.platform.WechatMiniHelper;
import io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.message.SubscribeMessageApi;
import io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.mini_code.MiniCodeApi;
import io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.mini_code.UrlSchemaApi;
import io.github.thebesteric.framework.agile.wechat.third.platform.config.mini.WechatMiniProperties;
import io.github.thebesteric.framework.agile.wechat.third.platform.constant.mini.EnvVersion;
import io.github.thebesteric.framework.agile.wechat.third.platform.constant.mini.MiniProgramState;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini.GenerateSchemeRequest;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini.GetQRCodeRequest;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini.SendMessageRequest;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini.AccessTokenResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini.Code2SessionResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini.GenerateSchemeResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini.GetQRCodeResponse;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.Map;

/**
 * Test
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-02 15:15:17
 */
class WechatMiniHelperTest {
    private static WechatMiniHelper wechatMiniHelper;
    private static String appId = "wxdc5814ab8fad0908";
    private static String appSecret = "ee1a1a02cd51887054f1f7f8ec4bd042";

    static {
        WechatMiniProperties properties = new WechatMiniProperties();
        properties.setAppId(appId);
        properties.setAppSecret(appSecret);
        wechatMiniHelper = new WechatMiniHelper(properties);
    }

    @Test
    void sendMessage() {
        AccessTokenResponse accessToken = wechatMiniHelper.getCredentialModule().credentialApi().getAccessToken();
        System.out.println("accessToken = " + accessToken.getAccessToken());

        Code2SessionResponse code2Session = wechatMiniHelper.getMiniProgramLoginModule().loginApi().code2Session("0e3zMJ000cDuBS1EW7400QfJbA2zMJ0Z");
        System.out.println("openId = " + code2Session.getOpenId());
        System.out.println("unionId = " + code2Session.getUnionId());
        System.out.println("sessionKey = " + code2Session.getSessionKey());

        SubscribeMessageApi subscribeMessageApi = wechatMiniHelper.getMessageModule().subscribeMessageApi();
        SendMessageRequest sendMessageRequest = new SendMessageRequest();
        sendMessageRequest.setMiniprogramState(MiniProgramState.DEVELOPER);
        sendMessageRequest.setToUser(code2Session.getOpenId());
        sendMessageRequest.setTemplateId("4Xj5t9ZcNNVr2-U2P8ygjgDfti_HJf1B4TD_tOhK3vk");
        sendMessageRequest.setData(Map.of(
                "thing1", SingleValue.of("测试任务1"),
                "thing2", SingleValue.of("测试门店1"),
                "time3", SingleValue.of("2023-08-05 00:00:00"),
                "time4", SingleValue.of("2023-08-10 23:59:59")
        ));

        WechatResponse sendMessage = subscribeMessageApi.sendMessage(accessToken.getAccessToken(), sendMessageRequest);
        System.out.println("sendMessage = " + sendMessage);
    }

    @Test
    void qrCode() {
        AccessTokenResponse accessToken = wechatMiniHelper.getCredentialModule().credentialApi().getAccessToken();
        System.out.println("accessToken = " + accessToken.getAccessToken());

        MiniCodeApi miniCodeApi = wechatMiniHelper.getMiniCodeModule().miniCodeApi();
        GetQRCodeRequest qrCodeRequest = new GetQRCodeRequest();
        qrCodeRequest.setWidth(280);
        GetQRCodeResponse qrCodeResponse = miniCodeApi.getQRCode(accessToken.getAccessToken(), qrCodeRequest);
        String base64Img = Base64.getEncoder().encodeToString(qrCodeResponse.getBuffer());
        System.out.println(base64Img);
    }

    @Test
    void getScheme() {
        AccessTokenResponse accessToken = wechatMiniHelper.getCredentialModule().credentialApi().getAccessToken();
        System.out.println("accessToken = " + accessToken.getAccessToken());

        UrlSchemaApi urlSchemaApi = wechatMiniHelper.getMiniCodeModule().urlSchemaApi();
        GenerateSchemeRequest request = new GenerateSchemeRequest();
        GenerateSchemeRequest.JumpWxa jumpWxa = new GenerateSchemeRequest.JumpWxa();
        jumpWxa.setPath("pages/index/index");
        jumpWxa.setEnvVersion(EnvVersion.DEVELOP);
        request.setJumpWxa(jumpWxa);
        GenerateSchemeResponse generateSchemeResponse = urlSchemaApi.generateScheme(accessToken.getAccessToken(), request);
        System.out.println("generateSchemeResponse = " + generateSchemeResponse);
    }
}
