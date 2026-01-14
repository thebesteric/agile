package io.github.thebesteric.framework.agile.wechat.third.platform.listener.third;

import io.github.thebesteric.framework.agile.commons.util.LoggerPrinter;
import io.github.thebesteric.framework.agile.wechat.third.platform.config.third.WechatThirdPlatformProperties;
import io.github.thebesteric.framework.agile.wechat.third.platform.constant.third.Event;
import io.github.thebesteric.framework.agile.wechat.third.platform.constant.third.InfoType;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.event.third.authroization.*;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.event.third.message.AbstractMessageEvent;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.event.third.message.WeAppAuditDelayEvent;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.event.third.message.WeAppAuditFailEvent;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.event.third.message.WeAppAuditSuccessEvent;
import io.github.thebesteric.framework.agile.wechat.third.platform.exception.AesException;
import io.github.thebesteric.framework.agile.wechat.third.platform.listener.AbstractListener;
import io.github.thebesteric.framework.agile.wechat.third.platform.utils.CryptUtils;
import io.github.thebesteric.framework.agile.wechat.third.platform.utils.XmlParser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * WechatThirdPlatformListener
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-26 18:21:18
 */
@Slf4j
@RequiredArgsConstructor
public class WechatThirdPlatformListener extends AbstractListener {

    private static final LoggerPrinter loggerPrinter = LoggerPrinter.newInstance();

    private static final String SUCCESS = "success";

    private final WechatThirdPlatformProperties properties;


    /**
     * 授权事件接收配置：用于接收 component_verify_ticket 以及授权变更通知推送。
     *
     * @param request                    请求
     * @param authorizationEventListener 授权事件接收监听
     *
     * @author wangweijun
     * @since 2024/8/1 18:11
     */
    public String authorizationEventListener(HttpServletRequest request, AuthorizationEventListener authorizationEventListener) throws Exception {
        // 校验并解密请求
        String decrypt = checkAndDecrypt(request);
        try {
            AbstractAuthorizationEvent abstractAuthorizationEvent = XmlParser.parse(decrypt, AbstractAuthorizationEvent.class);
            InfoType infoType = abstractAuthorizationEvent.getInfoType();
            // 推送 component_verify_ticket 消
            if (InfoType.COMPONENT_VERIFY_TICKET == infoType) {
                ComponentVerifyTicketEvent componentVerifyTicketEvent = XmlParser.parse(decrypt, ComponentVerifyTicketEvent.class);
                authorizationEventListener.onComponentVerifyTicket(componentVerifyTicketEvent);
            }
            // 授权成功通知
            else if (InfoType.AUTHORIZED == infoType) {
                AuthorizedEvent authorizedEvent = XmlParser.parse(decrypt, AuthorizedEvent.class);
                authorizationEventListener.onAuthorized(authorizedEvent);
            }
            // 取消授权通知
            else if (InfoType.UNAUTHORIZED == infoType) {
                UnAuthorizedEvent unAuthorizedEvent = XmlParser.parse(decrypt, UnAuthorizedEvent.class);
                authorizationEventListener.onUnAuthorized(unAuthorizedEvent);
            }
            // 授权更新通知
            else if (InfoType.UPDATE_AUTHORIZED == infoType) {
                UpdateAuthorizedEvent updateAuthorizedEvent = XmlParser.parse(decrypt, UpdateAuthorizedEvent.class);
                authorizationEventListener.onUpdateAuthorized(updateAuthorizedEvent);
            }

            // else if (StringUtils.equals("notify_third_fasteregister", infoType)) {
            //     LoggerPrinter.info(log, "快速注册小程序");
            // } else if (StringUtils.equals("notify_third_fastregisterbetaapp", infoType)) {
            //     LoggerPrinter.info(log, "注册试用小程序");
            // } else if (StringUtils.equals("notify_third_fastverifybetaapp", infoType)) {
            //     LoggerPrinter.info(log, "试用小程序快速认证");
            // } else if (StringUtils.equals("notify_icpfiling_verify_result", infoType)) {
            //     LoggerPrinter.info(log, "发起小程序管理员人脸核身");
            // } else if (StringUtils.equals("notify_apply_icpfiling_result", infoType)) {
            //     LoggerPrinter.info(log, "申请小程序备案");
            // }
        } catch (Exception e) {
            loggerPrinter.error("Error processing authorization event: {}", e.getMessage(), e);
        }
        return SUCCESS;
    }

    /**
     * 消息与事件接收配置：推送给第三方平台或由第三方平台代收的消息与事件
     *
     * @param request              请求
     * @param messageEventListener 消息与事件接收监听
     *
     * @author wangweijun
     * @since 2024/8/1 18:10
     */
    public String messageEventListener(HttpServletRequest request, MessageEventListener messageEventListener) throws Exception {
        // 校验并解密请求
        String decrypt = checkAndDecrypt(request);
        try {
            AbstractMessageEvent abstractMessageEvent = XmlParser.parse(decrypt, AbstractMessageEvent.class);
            Event event = abstractMessageEvent.getEvent();
            // 代码审核成功通知
            if (Event.WEAPP_AUDIT_SUCCESS == event) {
                WeAppAuditSuccessEvent weAppAuditSuccessEvent = XmlParser.parse(decrypt, WeAppAuditSuccessEvent.class);
                messageEventListener.onSubmitAuditSuccess(weAppAuditSuccessEvent);
            }
            // 代码审核失败通知
            else if (Event.WEAPP_AUDIT_FAIL == event) {
                WeAppAuditFailEvent weAppAuditFailEvent = XmlParser.parse(decrypt, WeAppAuditFailEvent.class);
                messageEventListener.onSubmitAuditFail(weAppAuditFailEvent);
            }
            // 代码审核延后通知
            else if (Event.WEAPP_AUDIT_DELAY == event) {
                WeAppAuditDelayEvent weAppAuditDelayEvent = XmlParser.parse(decrypt, WeAppAuditDelayEvent.class);
                messageEventListener.onSubmitAuditDelay(weAppAuditDelayEvent);
            }
        } catch (Exception e) {
            loggerPrinter.error("Error processing message event: {}", e.getMessage(), e);
        }
        return SUCCESS;
    }

    /**
     * 校验请求合法性并返回解密字符串
     *
     * @param request 请求
     *
     * @return String
     *
     * @link <a href="https://developers.weixin.qq.com/doc/oplatform/Third-party_Platforms/2.0/api/Before_Develop/message_push.html">消息推送</a>
     * @author wangweijun
     * @since 2024/8/1 18:14
     */
    private String checkAndDecrypt(HttpServletRequest request) throws Exception {
        // 请求参数
        String signature = request.getParameter("signature");
        String timestamp = request.getParameter("timestamp");
        String nonce = request.getParameter("nonce");
        String encryptType = request.getParameter("encrypt_type");
        String msgSignature = request.getParameter("msg_signature");

        loggerPrinter.info("接收到事件: signature = {}, timestamp = {}, nonce = {}, encrypt_type = {}, msg_signature = {}",
                signature, timestamp, nonce, encryptType, msgSignature);

        // 获取 xml 请求体
        String xmlStr = getRequestBodyStr(request);
        // 解析 xml 数据
        AuthorizationEvent authorizationEvent = XmlParser.parse(xmlStr, AuthorizationEvent.class);
        String encrypt = authorizationEvent.getEncrypt();

        CryptUtils cryptUtils = new CryptUtils(properties);
        // 验证签名
        if (!cryptUtils.verifySignature(msgSignature, timestamp, nonce, encrypt)) {
            throw new AesException(AesException.ValidateSignatureError);
        }
        // 返回解密后的响应
        return cryptUtils.decrypt(encrypt);
    }

}
