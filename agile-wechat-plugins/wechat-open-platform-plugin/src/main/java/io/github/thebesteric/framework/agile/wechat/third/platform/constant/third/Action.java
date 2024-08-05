package io.github.thebesteric.framework.agile.wechat.third.platform.constant.third;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 操作类型
 *
 * @author wangweijun
 * @since 2024/8/1 15:23
 */
@Getter
public enum Action {
    ADD("add"), DELETE("delete"), SET("set"), GET("get");

    @JsonValue
    private final String action;

    Action(String action) {
        this.action = action;
    }
}