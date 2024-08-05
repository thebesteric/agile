package io.github.thebesteric.framework.agile.wechat.third.platform.constant.mini;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;

/**
 * 消息类型
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-05 13:44:29
 */
@Getter
public enum MessageType {
    TEXT("text", "文本消息"), IMAGE("image", "图片消息"), LINK("link", "图文链接"), MINI_PROGRAM_PAGE("miniprogrampage", "小程序卡片");

    @JsonValue
    private final String code;
    private final String desc;

    MessageType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static MessageType of(String code) {
        return Arrays.stream(values()).filter(i -> i.getCode().equals(code)).findFirst().orElse(null);
    }
}
