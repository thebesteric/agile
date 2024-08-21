package io.github.thebesteric.framework.agile.plugins.workflow.constant;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.github.thebesteric.framework.agile.core.domain.BaseEnum;
import lombok.Getter;

import java.util.Arrays;

/**
 * 审批状态
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-11 21:28:54
 */
@Getter
public enum ApproveStatus implements BaseEnum {
    SUSPEND(0, "挂起（等待进入 IN_PROGRESS 状态，只有顺序审批有这个状态）"),
    IN_PROGRESS(1, "进行中"),
    APPROVED(2, "已同意"),
    REJECTED(3, "已驳回"),
    ABANDONED(5, "弃权"),
    SKIPPED(6, "跳过（也就是不需要审批）");

    @JsonValue
    @EnumValue
    private final Integer code;
    private final String desc;

    ApproveStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @JsonCreator
    public static ApproveStatus of(Integer code) {
        return Arrays.stream(ApproveStatus.values()).filter(i -> ObjectUtil.equals(i.getCode(), code)).findFirst().orElse(null);
    }
}
