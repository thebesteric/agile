package io.github.thebesteric.framework.agile.wechat.third.platform.constant.third;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum InfoType {

    UNAUTHORIZED("取消授权", "unauthorized"),
    UPDATE_AUTHORIZED("更新授权", "updateauthorized"),
    AUTHORIZED("授权成功", "authorized"),
    COMPONENT_VERIFY_TICKET("验证票据", "component_verify_ticket");

    private final String memo;
    private final String tag;


    InfoType(String memo, String tag) {
        this.memo = memo;
        this.tag = tag;
    }

    public static InfoType of(String tag) {
        return Arrays.stream(values()).filter(i -> i.getTag().equals(tag)).findFirst().orElse(null);
    }
}
