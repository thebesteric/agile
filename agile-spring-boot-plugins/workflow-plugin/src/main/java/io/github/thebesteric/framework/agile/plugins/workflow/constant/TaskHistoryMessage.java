package io.github.thebesteric.framework.agile.plugins.workflow.constant;

import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

/**
 * Messages
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-28 13:35:36
 */
@Getter
public enum TaskHistoryMessage {

    INSTANCE_STARTED("审批流程开始"),
    INSTANCE_ENDED("审批流程结束"),
    INSTANCE_APPROVED("审批通过"),
    INSTANCE_REJECTED("审批驳回"),
    INSTANCE_ABANDONED("审批弃权"),
    INSTANCE_CANCELED("审批取消"),
    INSTANCE_SUBMIT_FORM("提交审批"),
    INSTANCE_CUSTOM("%s");

    private final String template;

    @Setter
    private String value;
    

    TaskHistoryMessage(String template) {
        this.template = template;
    }

    TaskHistoryMessage message(String value) {
        this.value = value;
        return this;
    }

    @JsonCreator
    public static TaskHistoryMessage of(String template) {
        return Arrays.stream(TaskHistoryMessage.values()).filter(i -> ObjectUtil.equals(i.getTemplate(), template)).findFirst().orElse(null);
    }

    public static TaskHistoryMessage custom(String message) {
        return TaskHistoryMessage.INSTANCE_CUSTOM.message(message);
    }

    @Override
    public String toString() {
        return this.value == null ? this.template : this.value;
    }
}
