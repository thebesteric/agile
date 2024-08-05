package io.github.thebesteric.framework.agile.wechat.third.platform.constant.mini;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;

/**
 * 小程序状态版本
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-02 17:00:29
 */
@Getter
public enum MiniProgramState {
    DEVELOPER("developer", "开发版"), TRIAL("trial", "体验版"), FORMAL("formal", "正式版");

    @JsonValue
    private final String code;
    private final String desc;

    MiniProgramState(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static MiniProgramState of(String code) {
        return Arrays.stream(values()).filter(i -> i.getCode().equals(code)).findFirst().orElse(null);
    }
}
