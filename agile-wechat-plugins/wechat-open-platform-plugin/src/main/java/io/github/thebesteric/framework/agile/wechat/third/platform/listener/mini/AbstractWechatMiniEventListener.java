package io.github.thebesteric.framework.agile.wechat.third.platform.listener.mini;

import io.github.thebesteric.framework.agile.commons.util.LoggerPrinter;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.event.mini.BaseMiniEvent;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.event.mini.MediaCheckAsyncEvent;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.event.mini.CustomerSessionMessageEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * 小程序事件监听
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-06 14:15:46
 */
@Slf4j
public abstract class AbstractWechatMiniEventListener {

    /**
     * 音视频内容安全识别
     *
     * @param mediaCheckAsyncEvent event
     *
     * @author wangweijun
     * @since 2024/8/6 14:16
     */
    public void onMediaCheckAsyncEvent(MediaCheckAsyncEvent mediaCheckAsyncEvent) {
        LoggerPrinter.info(log, "onMediaCheckAsyncEvent: {}", mediaCheckAsyncEvent);
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
        LoggerPrinter.info(log, "onSessionMessageEvent: {}", sessionMessageEvent);
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
