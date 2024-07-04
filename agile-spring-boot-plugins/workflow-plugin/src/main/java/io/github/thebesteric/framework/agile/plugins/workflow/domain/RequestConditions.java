package io.github.thebesteric.framework.agile.plugins.workflow.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 请求条件（申请人条件）
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-13 11:11:19
 */
@Data
public class RequestConditions implements Serializable {
    @Serial
    private static final long serialVersionUID = 6970415603169771186L;

    private List<RequestCondition> requestConditions = new ArrayList<>();

    public static RequestConditions newInstance() {
        return new RequestConditions();
    }

    public void addRequestCondition(RequestCondition requestCondition) {
        this.requestConditions.add(requestCondition);
    }
}
