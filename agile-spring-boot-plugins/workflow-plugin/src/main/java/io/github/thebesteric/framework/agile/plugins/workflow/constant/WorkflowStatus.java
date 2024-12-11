package io.github.thebesteric.framework.agile.plugins.workflow.constant;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.github.thebesteric.framework.agile.core.domain.BaseEnum;
import lombok.Getter;

import java.util.Arrays;

/**
 * 工作流状态
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-11 21:28:54
 */
@Getter
public enum WorkflowStatus implements BaseEnum {
    WAITING(0, "待审批"),
    IN_PROGRESS(1, "进行中"),
    COMPLETED(2, "已完成"),
    CANCELED(3, "已取消"),
    REJECTED(4, "已驳回"),
    INTERRUPTED(99, "已中断（表示流程被强制结束）");

    @JsonValue
    @EnumValue
    private final Integer code;
    private final String desc;

    WorkflowStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @JsonCreator
    public static WorkflowStatus of(Integer code) {
        return Arrays.stream(WorkflowStatus.values()).filter(i -> ObjectUtil.equals(i.getCode(), code)).findFirst().orElse(null);
    }
}
