package io.github.thebesteric.framework.agile.wechat.third.platform.listener.mini;

import io.github.thebesteric.framework.agile.commons.util.LoggerPrinter;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.event.mini.BaseMiniEvent;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.event.mini.CustomerImageMessageEvent;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.event.mini.CustomerMiniCardMessageEvent;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.event.mini.CustomerTextMessageEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * 小程序消息监听
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-07 14:33:57
 */
@Slf4j
public abstract class AbstractWechatMiniMessageListener {

    /**
     * 文本消息
     *
     * @param customerTextMessageEvent 文本消息
     *
     * @author wangweijun
     * @since 2024/8/7 14:36
     */
    public void onCustomerTextMessageEvent(CustomerTextMessageEvent customerTextMessageEvent) {
        LoggerPrinter.info(log, "onCustomerTextMessageEvent: {}", customerTextMessageEvent);
    }

    /**
     * 图片消息
     *
     * @param customerImageMessageEvent 图片消息
     *
     * @author wangweijun
     * @since 2024/8/7 14:53
     */
    public void onCustomerImageMessageEvent(CustomerImageMessageEvent customerImageMessageEvent) {
        LoggerPrinter.info(log, "onCustomerImageMessageEvent: {}", customerImageMessageEvent);
    }

    /**
     * 小程序卡片消息
     *
     * @param customerMiniCardMessageEvent 小程序卡片消息
     *
     * @author wangweijun
     * @since 2024/8/7 14:53
     */
    public void onCustomerMiniCardMessageEvent(CustomerMiniCardMessageEvent customerMiniCardMessageEvent) {
        LoggerPrinter.info(log, "onCustomerMiniCardMessageEvent: {}", customerMiniCardMessageEvent);
    }

    /**
     * 未知事件
     *
     * @param baseMiniEvent event
     *
     * @author wangweijun
     * @since 2024/8/6 17:11
     */
    public void onUnknownEvent(BaseMiniEvent baseMiniEvent) {
        LoggerPrinter.info(log, "onUnknownEvent: {}", baseMiniEvent);
    }

}
