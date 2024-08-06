package io.github.thebesteric.framework.agile.wechat.third.platform.constant.mini;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;

/**
 * 场景枚举值
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-06 10:20:29
 */
@Getter
public enum Scene {
    MATERIAL("1", "资料"), COMMENT("2", "评论"), FORUM("3", "论坛"), SOCIAL_LOG("4", "社交日志");

    @JsonValue
    private final String code;
    private final String desc;

    Scene(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static Scene of(String code) {
        return Arrays.stream(values()).filter(i -> i.getCode().equals(code)).findFirst().orElse(null);
    }
}
