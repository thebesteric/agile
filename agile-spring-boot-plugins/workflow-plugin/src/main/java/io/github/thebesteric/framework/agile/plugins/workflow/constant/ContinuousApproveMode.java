package io.github.thebesteric.framework.agile.plugins.workflow.constant;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.github.thebesteric.framework.agile.core.domain.BaseEnum;
import lombok.Getter;

import java.util.Arrays;

/**
 * 连续审批类型（同一个审批人重复审批同一项目时）
 * <p>
 * APPROVE_ALL: 每个节点都需要审批：A:manual-B:manual-A:manual-A:manual-B:manual-A:manual<br />
 * APPROVE_FIRST：仅首个节点需要审批，其余自动同意：A:manual-B:manual-A:auto-A:auto-B:manual-A:auto<br />
 * APPROVE_SAME：仅连续审批时自动同意：A:manual-B:manual-A:manual-A:auto-B:manual-A:manual<br />
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-11 14:08:54
 */
@Getter
public enum ContinuousApproveMode implements BaseEnum {
    APPROVE_ALL(1, "同一个审批人重复审批同一项目时，每个节点都需要审批"),
    APPROVE_FIRST(2, "同一个审批人重复审批同一项目时，仅首个节点需要审批，其余自动同意"),
    APPROVE_CONTINUOUS(3, "同一个审批人重复审批同一项目时，仅连续审批时自动同意");


    @JsonValue
    @EnumValue
    private final Integer code;
    private final String desc;

    ContinuousApproveMode(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @JsonCreator
    public static ContinuousApproveMode of(Integer code) {
        return Arrays.stream(ContinuousApproveMode.values()).filter(i -> ObjectUtil.equals(i.getCode(), code)).findFirst().orElse(null);
    }
}
