package io.github.thebesteric.framework.agile.wechat.third.platform.listener.third;

import io.github.thebesteric.framework.agile.commons.util.LoggerPrinter;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.event.third.authroization.AuthorizedEvent;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.event.third.authroization.ComponentVerifyTicketEvent;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.event.third.authroization.UnAuthorizedEvent;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.event.third.authroization.UpdateAuthorizedEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * 授权事件接收监听
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-29 19:30:17
 */
@Slf4j
public abstract class AuthorizationEventListener {

    private static final LoggerPrinter loggerPrinter = LoggerPrinter.newInstance();

    /**
     * 推送 component_verify_ticket 消息
     *
     * @param componentVerifyTicketEvent 消息内容
     *
     * @author wangweijun
     * @since 2024/7/29 20:16
     */
    public void onComponentVerifyTicket(ComponentVerifyTicketEvent componentVerifyTicketEvent) {
        loggerPrinter.info("onComponentVerifyTicket: {}", componentVerifyTicketEvent);
    }

    /**
     * 授权成功通知
     *
     * @param authorizedEvent 消息内容
     *
     * @author wangweijun
     * @since 2024/7/29 20:23
     */
    public void onAuthorized(AuthorizedEvent authorizedEvent) {
        loggerPrinter.info("onAuthorized: {}", authorizedEvent);
    }

    /**
     * 取消授权通知
     *
     * @param unAuthorizedEvent 消息内容
     *
     * @author wangweijun
     * @since 2024/7/29 20:47
     */
    public void onUnAuthorized(UnAuthorizedEvent unAuthorizedEvent) {
        loggerPrinter.info("onUnAuthorized: {}", unAuthorizedEvent);
    }

    /**
     * 授权更新通知
     *
     * @param updateAuthorizedEvent 消息内容
     *
     * @author wangweijun
     * @since 2024/7/29 20:50
     */
    public void onUpdateAuthorized(UpdateAuthorizedEvent updateAuthorizedEvent) {
        loggerPrinter.info("onUpdateAuthorized: {}", updateAuthorizedEvent);
    }

}
