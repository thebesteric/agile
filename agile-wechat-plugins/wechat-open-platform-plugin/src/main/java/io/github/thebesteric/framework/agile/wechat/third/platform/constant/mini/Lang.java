package io.github.thebesteric.framework.agile.wechat.third.platform.constant.mini;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;

/**
 * 语言
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-05 13:44:29
 */
@Getter
public enum Lang {
    ZH_CN("zh_CN", "简体中文"), ZH_HK("zh_HK", "繁体中文"), ZH_TW("zh_TW", "繁体中文"), EN_US("en_US", "英文");

    @JsonValue
    private final String code;
    private final String desc;

    Lang(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static Lang of(String code) {
        return Arrays.stream(values()).filter(i -> i.getCode().equals(code)).findFirst().orElse(null);
    }
}
