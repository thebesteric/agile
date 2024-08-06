package io.github.thebesteric.framework.agile.wechat.third.platform.listener.mini;

import io.github.thebesteric.framework.agile.commons.util.LoggerPrinter;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.event.mini.BaseMiniEvent;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.event.mini.MediaCheckAsyncEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * WechatMiniEventListener
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
