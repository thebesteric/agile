package io.github.thebesteric.framework.agile.wechat.third.platform.constant.mini;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;

/**
 * 内容标签枚举值
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-06 10:20:29
 */
@Getter
public enum ContentLabel {
    NORMAL("100", "正常"), ADVERTISEMENT("10001", "广告"), POLITICS("20001", "时政"), PORNOGRAPHIC("20002", "色情"),
    ABUSE("20003", "辱骂"), ILLICIT("20006", "违法犯罪"), CHEAT("20008", "欺诈"),  VULGAR("20012", "低俗"),
    COPYRIGHT("20013", "版权"), OTHER("21000", "其他");

    @JsonValue
    private final String code;
    private final String desc;

    ContentLabel(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ContentLabel of(String code) {
        return Arrays.stream(values()).filter(i -> i.getCode().equals(code)).findFirst().orElse(null);
    }
}
