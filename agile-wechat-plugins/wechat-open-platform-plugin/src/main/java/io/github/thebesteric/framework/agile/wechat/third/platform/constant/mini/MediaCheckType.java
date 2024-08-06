package io.github.thebesteric.framework.agile.wechat.third.platform.constant.mini;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;

/**
 * 媒体检查类型
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-05 13:44:29
 */
@Getter
public enum MediaCheckType {
    VOICE("1", "音频"), IMAGE("2", "图片");

    @JsonValue
    private final String code;
    private final String desc;

    MediaCheckType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static MediaCheckType of(String code) {
        return Arrays.stream(values()).filter(i -> i.getCode().equals(code)).findFirst().orElse(null);
    }
}
