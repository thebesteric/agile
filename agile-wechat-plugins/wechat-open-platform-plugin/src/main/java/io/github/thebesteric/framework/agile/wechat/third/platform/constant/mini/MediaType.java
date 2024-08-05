package io.github.thebesteric.framework.agile.wechat.third.platform.constant.mini;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;

/**
 * 媒体类型
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-05 13:44:29
 */
@Getter
public enum MediaType {
    IMAGE("image", "图片"), VOICE("voice", "语音"), VIDEO("video", "视频"), THUMB("thumb", "缩略图");

    @JsonValue
    private final String code;
    private final String desc;

    MediaType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static MediaType of(String code) {
        return Arrays.stream(values()).filter(i -> i.getCode().equals(code)).findFirst().orElse(null);
    }
}
