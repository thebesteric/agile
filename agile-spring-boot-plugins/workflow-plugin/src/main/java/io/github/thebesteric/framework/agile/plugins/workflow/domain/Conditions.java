package io.github.thebesteric.framework.agile.plugins.workflow.domain;

import cn.hutool.core.collection.CollUtil;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.LogicOperator;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 条件组
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-13 11:11:19
 */
@Data
@NoArgsConstructor
public class Conditions implements Serializable {
    @Serial
    private static final long serialVersionUID = -4187125409333209523L;

    @Schema(description = "条件集合")
    private List<Condition> conditions = new ArrayList<>();
    @Schema(description = "逻辑运算符")
    private LogicOperator logicOperator;
    @Schema(description = "优先级")
    private Integer priority;

    private Conditions(LogicOperator logicOperator, Integer priority) {
        this.logicOperator = logicOperator;
        this.priority = priority;
    }

    public static Conditions defaultConditions() {
        return newInstance(LogicOperator.AND, Integer.MAX_VALUE);
    }

    public static Conditions newInstance(LogicOperator logicOperator, Integer priority) {
        return new Conditions(logicOperator, priority);
    }

    /**
     * 添加条件
     *
     * @param condition 条件
     *
     * @author wangweijun
     * @since 2024/6/19 17:36
     */
    public void addCondition(Condition condition) {
        this.conditions.add(condition);
    }

    /**
     * 条件匹配
     *
     * @param requestConditions 请求条件
     *
     * @return boolean
     *
     * @author wangweijun
     * @since 2024/6/24 18:37
     */
    public boolean matchRequestCondition(RequestConditions requestConditions) {
        // 没有审批条件，直接通过
        if (CollUtil.isEmpty(conditions)) {
            return true;
        }
        // 有审批条件、但是请求没有带着条件，直接拒绝
        List<RequestCondition> reqConditions = requestConditions.getRequestConditions();
        if (CollUtil.isEmpty(reqConditions)) {
            return false;
        }

        // 转换为 Map
        Map<String, RequestCondition> requestConditionMap = reqConditions.stream().collect(Collectors.toMap(RequestCondition::getKey, rc -> rc));

        boolean matched = false;
        for (Condition condition : conditions) {
            String conditionKey = condition.getKey();
            RequestCondition requestCondition = requestConditionMap.get(conditionKey);
            if (requestCondition == null) {
                return false;
            }
            switch (condition.getOperator()) {
                case EQUAL:
                    matched = requestCondition.getValue().equalsIgnoreCase(condition.getValue());
                    if (LogicOperator.AND == logicOperator && matched) {
                        continue;
                    }
                    return matched;
                case NOT_EQUAL:
                    matched = !requestCondition.getValue().equalsIgnoreCase(condition.getValue());
                    if (LogicOperator.AND == logicOperator && matched) {
                        continue;
                    }
                    return matched;
                case LESS_THAN:
                    matched = requestCondition.getValue().compareTo(condition.getValue()) < 0;
                    if (LogicOperator.AND == logicOperator && matched) {
                        continue;
                    }
                    return matched;
                case LESS_THAN_AND_EQUAL:
                    matched = requestCondition.getValue().compareTo(condition.getValue()) <= 0;
                    if (LogicOperator.AND == logicOperator && matched) {
                        continue;
                    }
                    return matched;
                case GREATER_THAN:
                    matched = requestCondition.getValue().compareTo(condition.getValue()) > 0;
                    if (LogicOperator.AND == logicOperator && matched) {
                        continue;
                    }
                    return matched;
                case GREATER_THAN_AND_EQUAL:
                    matched = requestCondition.getValue().compareTo(condition.getValue()) >= 0;
                    if (LogicOperator.AND == logicOperator && matched) {
                        continue;
                    }
                    return matched;
                case LIKE:
                    matched = requestCondition.getValue().contains(condition.getValue());
                    if (LogicOperator.AND == logicOperator && matched) {
                        continue;
                    }
                    return matched;
            }
        }
        return matched;
    }
}
