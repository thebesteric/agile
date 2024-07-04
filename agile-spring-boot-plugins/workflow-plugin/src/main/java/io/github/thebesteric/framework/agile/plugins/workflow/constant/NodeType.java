package io.github.thebesteric.framework.agile.plugins.workflow.constant;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.github.thebesteric.framework.agile.core.domain.BaseEnum;
import lombok.Getter;

import java.util.Arrays;

/**
 * NodeType
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-11 21:16:17
 */
@Getter
public enum NodeType implements BaseEnum {

    START(1, "开始节点"),
    TASK(2, "任务节点"),
    END(9, "结束节点");

    @JsonValue
    @EnumValue
    private final Integer code;
    private final String desc;

    NodeType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @JsonCreator
    public static NodeType of(Integer code) {
        return Arrays.stream(NodeType.values()).filter(i -> ObjectUtil.equals(i.getCode(), code)).findFirst().orElse(null);
    }
}
