package io.github.thebesteric.framework.agile.wechat.third.platform.constant.third;

import lombok.Getter;

import java.util.Arrays;

/**
 * Event
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-01 18:55:38
 */
@Getter
public enum Event {
    WEAPP_AUDIT_SUCCESS("审核通过", "weapp_audit_success"),
    WEAPP_AUDIT_FAIL("审核不通过", "weapp_audit_fail"),
    WEAPP_AUDIT_DELAY("审核延后", "weapp_audit_delay");

    private final String memo;
    private final String tag;


    Event(String memo, String tag) {
        this.memo = memo;
        this.tag = tag;
    }

    public static Event of(String tag) {
        return Arrays.stream(values()).filter(i -> i.getTag().equals(tag)).findFirst().orElse(null);
    }
}
