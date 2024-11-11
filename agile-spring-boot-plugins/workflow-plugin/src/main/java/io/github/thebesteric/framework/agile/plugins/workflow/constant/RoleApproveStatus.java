package io.github.thebesteric.framework.agile.plugins.workflow.constant;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.github.thebesteric.framework.agile.core.domain.BaseEnum;
import lombok.Getter;

import java.util.Arrays;

/**
 * 角色审批状态
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-11-08 14:30:16
 */
@Getter
public enum RoleApproveStatus implements BaseEnum {
    SUSPEND(0, "挂起（等待进入 IN_PROGRESS 状态，只有顺序审批有这个状态）"),
    IN_PROGRESS(1, "进行中"),
    APPROVED(2, "已同意"),
    REJECTED(3, "已驳回"),
    ABANDONED(5, "弃权"),
    SKIPPED(6, "跳过（也就是不需要审批）"),
    REASSIGNED(7, "已转派"),
    INTERRUPTED(99, "已中断（表示流程被强制结束）");

    @JsonValue
    @EnumValue
    private final Integer code;
    private final String desc;

    RoleApproveStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @JsonCreator
    public static RoleApproveStatus of(Integer code) {
        return Arrays.stream(RoleApproveStatus.values()).filter(i -> ObjectUtil.equals(i.getCode(), code)).findFirst().orElse(null);
    }
}
