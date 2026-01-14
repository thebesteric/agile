package io.github.thebesteric.framework.agile.wechat.third.platform.listener.third;

import io.github.thebesteric.framework.agile.commons.util.LoggerPrinter;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.event.third.message.WeAppAuditDelayEvent;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.event.third.message.WeAppAuditFailEvent;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.event.third.message.WeAppAuditSuccessEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * 消息与事件接收监听
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-01 18:05:05
 */
@Slf4j
public abstract class MessageEventListener {

    private static final LoggerPrinter loggerPrinter = LoggerPrinter.newInstance();

    /**
     * 代码审核通过
     *
     * @param weAppAuditSuccessEvent 通知
     *
     * @link <a href="https://developers.weixin.qq.com/doc/oplatform/openApi/OpenApiDoc/miniprogram-management/code-management/submitAudit.html#代码审核结果推送">代码审核通过</a>
     * @author wangweijun
     * @since 2024/8/1 19:35
     */
    public void onSubmitAuditSuccess(WeAppAuditSuccessEvent weAppAuditSuccessEvent) {
        loggerPrinter.info("onSubmitAuditSucceed: time = {}", weAppAuditSuccessEvent.getSuccTime());
    }

    /**
     * 代码审核失败
     *
     * @param weAppAuditFailEvent 通知
     *
     * @link <a href="https://developers.weixin.qq.com/doc/oplatform/openApi/OpenApiDoc/miniprogram-management/code-management/submitAudit.html#代码审核结果推送">代码审核失败</a>
     * @author wangweijun
     * @since 2024/8/1 19:35
     */
    public void onSubmitAuditFail(WeAppAuditFailEvent weAppAuditFailEvent) {
        loggerPrinter.info("onSubmitAuditFail: time = {}, reason = {}", weAppAuditFailEvent.getFailTime(), weAppAuditFailEvent.getReason());
    }

    /**
     * 代码审核延后
     *
     * @param weAppAuditDelayEvent 通知
     *
     * @link <a href="https://developers.weixin.qq.com/doc/oplatform/openApi/OpenApiDoc/miniprogram-management/code-management/submitAudit.html#代码审核结果推送">代码审核延后</a>
     * @author wangweijun
     * @since 2024/8/1 19:35
     */
    public void onSubmitAuditDelay(WeAppAuditDelayEvent weAppAuditDelayEvent) {
        loggerPrinter.info("onSubmitAuditDelayed: time = {}, reason = {}", weAppAuditDelayEvent.getDelayTime(), weAppAuditDelayEvent.getReason());
    }

}
