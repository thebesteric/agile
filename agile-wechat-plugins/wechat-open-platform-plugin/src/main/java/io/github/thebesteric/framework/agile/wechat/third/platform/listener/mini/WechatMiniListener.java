package io.github.thebesteric.framework.agile.wechat.third.platform.listener.mini;

import io.github.thebesteric.framework.agile.commons.util.JsonUtils;
import io.github.thebesteric.framework.agile.commons.util.LoggerPrinter;
import io.github.thebesteric.framework.agile.wechat.third.platform.config.mini.WechatMiniProperties;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.event.mini.*;
import io.github.thebesteric.framework.agile.wechat.third.platform.exception.AesException;
import io.github.thebesteric.framework.agile.wechat.third.platform.listener.AbstractListener;
import io.github.thebesteric.framework.agile.wechat.third.platform.utils.CryptUtils;
import io.github.thebesteric.framework.agile.wechat.third.platform.utils.XmlParser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * WechatMiniListener
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-06 11:55:11
 */
@Slf4j
@RequiredArgsConstructor
public class WechatMiniListener extends AbstractListener {

    private final WechatMiniProperties properties;

    public String listen(HttpServletRequest request, AbstractWechatMiniEventListener eventListener, AbstractWechatMiniMessageListener messageListener) throws Exception {
        String httpMethod = request.getMethod();
        if ("GET".equalsIgnoreCase(httpMethod)) {
            return doGet(request);
        }
        return doPost(request, eventListener, messageListener);
    }

    public String doPost(HttpServletRequest request, AbstractWechatMiniEventListener eventListener, AbstractWechatMiniMessageListener messageListener) throws Exception {
        // 获取请求体
        String requestStr = getRequestBodyStr(request);

        // 获取推送设置
        WechatMiniProperties.MessagePush messagePush = properties.getMessagePush();
        if (messagePush == null) {
            throw new IllegalArgumentException("WechatMiniProperties.MessagePush is null");
        }

        // 加密类型
        WechatMiniProperties.EncryptType encryptType = messagePush.getEncryptType();
        // 消息类型
        WechatMiniProperties.MessageType messageType = messagePush.getMessageType();

        // 解析对象
        BaseMiniEvent baseMiniEvent = parseObject(requestStr, messageType, BaseMiniEvent.class);

        // 加密模式
        if (WechatMiniProperties.EncryptType.CIPHERTEXT == encryptType) {
            CryptUtils cryptUtils = new CryptUtils(properties);
            String encrypt = baseMiniEvent.getEncrypt();
            requestStr = cryptUtils.decrypt(encrypt);
            baseMiniEvent = parseObject(requestStr, messageType, BaseMiniEvent.class);
            baseMiniEvent.setEncrypt(encrypt);
        }

        String msgType = baseMiniEvent.getMsgType();
        if ("event".equalsIgnoreCase(msgType) && eventListener != null) {
            String event = baseMiniEvent.getEvent();
            switch (event) {
                // 音视频内容安全识别
                case "wxa_media_check":
                    MediaCheckAsyncEvent mediaCheckAsyncEvent = parseObject(requestStr, messageType, MediaCheckAsyncEvent.class);
                    eventListener.onMediaCheckAsyncEvent(mediaCheckAsyncEvent);
                    break;
                // 进入会话事件
                case "user_enter_tempsession":
                    CustomerSessionMessageEvent customerSessionMessageEvent = parseObject(requestStr, messageType, CustomerSessionMessageEvent.class);
                    eventListener.onSessionMessageEvent(customerSessionMessageEvent);
                    break;
                default:
                    eventListener.onUnknownEvent(baseMiniEvent);
            }
        } else if (messageListener != null) {
            switch (msgType) {
                // 文本消息
                case "text":
                    CustomerTextMessageEvent customerTextMessageEvent = parseObject(requestStr, messageType, CustomerTextMessageEvent.class);
                    messageListener.onCustomerTextMessageEvent(customerTextMessageEvent);
                    break;
                // 图片消息
                case "image":
                    CustomerImageMessageEvent customerImageMessageEvent = parseObject(requestStr, messageType, CustomerImageMessageEvent.class);
                    messageListener.onCustomerImageMessageEvent(customerImageMessageEvent);
                    break;
                // 卡片消息
                case "miniprogrampage":
                    CustomerMiniCardMessageEvent customerMiniCardMessageEvent = parseObject(requestStr, messageType, CustomerMiniCardMessageEvent.class);
                    messageListener.onCustomerMiniCardMessageEvent(customerMiniCardMessageEvent);
                    break;
                default:
                    messageListener.onUnknownEvent(baseMiniEvent);
            }
        }

        return "success";
    }

    public String doGet(HttpServletRequest request) throws Exception {
        // 校验签名
        return checkSignature(request);
    }

    /**
     * 检查签名
     *
     * @param request 请求
     *
     * @return String
     *
     * @author wangweijun
     * @since 2024/8/6 17:13
     */
    private String checkSignature(HttpServletRequest request) throws Exception {
        // 请求参数
        String signature = request.getParameter("signature");
        String timestamp = request.getParameter("timestamp");
        String nonce = request.getParameter("nonce");
        String echoStr = request.getParameter("echostr");

        LoggerPrinter.info(log, "接收到事件: signature = {}, timestamp = {}, nonce = {}, echoStr = {}",
                signature, timestamp, nonce, echoStr);

        // 将 Token、timestamp、nonce 三个参数进行字典序排序。
        String token = properties.getMessagePush().getToken();
        List<String> params = new ArrayList<>();
        params.add(token);
        params.add(timestamp);
        params.add(nonce);
        Collections.sort(params);
        // 组合成一个字符串
        String param = String.join("", params);
        // 进行 sha1 计算签名，获得 signature
        if (!Objects.equals(CryptUtils.sha1(param), signature)) {
            throw new AesException(AesException.ValidateSignatureError);
        }
        return echoStr;
    }

    /**
     * 解析对象
     *
     * @param requestStr  请求体
     * @param messageType 消息类型
     * @param clazz       转换类
     *
     * @return T
     *
     * @author wangweijun
     * @since 2024/8/6 17:13
     */
    private <T extends AbstractMiniEvent> T parseObject(String requestStr, WechatMiniProperties.MessageType messageType, Class<T> clazz) throws Exception {
        if (WechatMiniProperties.MessageType.JSON == messageType) {
            return JsonUtils.toObject(requestStr, clazz);
        }
        return XmlParser.parse(requestStr, clazz);
    }

}
