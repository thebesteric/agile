package io.github.thebesteric.framework.agile.test.workflow;

import io.github.thebesteric.framework.agile.plugins.workflow.constant.LogicOperator;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.Operator;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.Condition;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.Conditions;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.RequestCondition;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.RequestConditions;
import org.junit.jupiter.api.Test;

/**
 * CondtionTest
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-24 18:36:08
 */
public class CondtionTest {

    @Test
    void testConditions() {
        Conditions conditions = Conditions.newInstance(LogicOperator.AND, Integer.MAX_VALUE);
        conditions.addCondition(Condition.of("day", "3", Operator.LESS_THAN));
        conditions.addCondition(Condition.of("name", "lisi", Operator.EQUAL));

        RequestConditions requestConditions = RequestConditions.newInstance();
        requestConditions.addRequestCondition(RequestCondition.of("day", "2"));
        requestConditions.addRequestCondition(RequestCondition.of("name", "lisi"));

        System.out.println(conditions.matchRequestCondition(requestConditions));
    }

}
