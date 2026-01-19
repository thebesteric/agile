package io.github.thebesteric.framework.agile.wechat.third.platform.listener.mini;

import io.github.thebesteric.framework.agile.commons.util.LoggerPrinter;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.event.mini.BaseMiniEvent;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.event.mini.CustomerSessionMessageEvent;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.event.mini.MediaCheckAsyncEvent;

/**
 * 小程序事件监听
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-06 14:15:46
 */
public abstract class AbstractWechatMiniEventListener {

    private static final LoggerPrinter loggerPrinter = LoggerPrinter.newInstance();

    /**
     * 音视频内容安全识别
     *
     * @param mediaCheckAsyncEvent event
     *
     * @author wangweijun
     * @since 2024/8/6 14:16
     */
    public void onMediaCheckAsyncEvent(MediaCheckAsyncEvent mediaCheckAsyncEvent) {
        loggerPrinter.info("onMediaCheckAsyncEvent: {}", mediaCheckAsyncEvent);
    }

    /**
     * 进入会话事件
     *
     * @param sessionMessageEvent 进入会话事件
     *
     * @author wangweijun
     * @since 2024/8/7 14:54
     */
    public void onSessionMessageEvent(CustomerSessionMessageEvent sessionMessageEvent) {
        loggerPrinter.info("onSessionMessageEvent: {}", sessionMessageEvent);
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
        loggerPrinter.info("onUnknownEvent: {}", baseMiniEvent);
    }
}
