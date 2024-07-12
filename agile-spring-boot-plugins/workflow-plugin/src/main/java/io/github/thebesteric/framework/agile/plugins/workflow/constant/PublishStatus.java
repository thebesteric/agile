package io.github.thebesteric.framework.agile.plugins.workflow.constant;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.github.thebesteric.framework.agile.core.domain.BaseEnum;
import lombok.Getter;

import java.util.Arrays;

/**
 * 发布状态
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-11 14:08:54
 */
@Getter
public enum PublishStatus implements BaseEnum {
    PUBLISHED(1, "已发布"),
    UNPUBLISHED(2, "未发布");

    @JsonValue
    @EnumValue
    private final Integer code;
    private final String desc;

    PublishStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @JsonCreator
    public static PublishStatus of(Integer code) {
        return Arrays.stream(PublishStatus.values()).filter(i -> ObjectUtil.equals(i.getCode(), code)).findFirst().orElse(null);
    }
}
