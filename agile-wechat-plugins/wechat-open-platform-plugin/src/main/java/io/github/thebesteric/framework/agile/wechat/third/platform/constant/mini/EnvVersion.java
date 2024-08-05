package io.github.thebesteric.framework.agile.wechat.third.platform.constant.mini;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;

/**
 * 小程序版本
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-05 13:44:29
 */
@Getter
public enum EnvVersion {
    DEVELOP("develop", "开发版"), TRIAL("trial", "体验版"), RELEASE("release", "正式版");

    @JsonValue
    private final String code;
    private final String desc;

    EnvVersion(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static EnvVersion of(String code) {
        return Arrays.stream(values()).filter(i -> i.getCode().equals(code)).findFirst().orElse(null);
    }
}
