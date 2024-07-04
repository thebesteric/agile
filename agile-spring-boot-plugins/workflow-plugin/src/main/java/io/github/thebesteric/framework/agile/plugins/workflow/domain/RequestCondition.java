package io.github.thebesteric.framework.agile.plugins.workflow.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 请求条件（申请人条件）
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-13 11:11:19
 */
@Data
public class RequestCondition implements Serializable {
    @Serial
    private static final long serialVersionUID = -1826250458013487985L;

    private String key;
    private String value;

    public static RequestCondition of(String key, String value) {
        RequestCondition condition = new RequestCondition();
        condition.setKey(key);
        condition.setValue(value);
        return condition;
    }
}
