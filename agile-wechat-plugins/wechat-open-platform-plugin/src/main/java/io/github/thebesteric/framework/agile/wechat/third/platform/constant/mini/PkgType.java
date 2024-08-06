package io.github.thebesteric.framework.agile.wechat.third.platform.constant.mini;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;

/**
 * 资源包类型
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-06 20:58:29
 */
@Getter
public enum PkgType {
    PKG_TEST("0", "测试体验包"), PKG_A("1", "A 类设备"), PKG_B("2", "B 类设备"),
    PKG_C("3", "缩略图"), PKG_D("4", "D 类设备"), PKG_E("5", "E 类设备");

    @JsonValue
    private final String code;
    private final String desc;

    PkgType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static PkgType of(String code) {
        return Arrays.stream(values()).filter(i -> i.getCode().equals(code)).findFirst().orElse(null);
    }
}
