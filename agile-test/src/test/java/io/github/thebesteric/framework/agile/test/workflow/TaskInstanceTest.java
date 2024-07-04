package io.github.thebesteric.framework.agile.test.workflow;

import io.github.thebesteric.framework.agile.plugins.workflow.WorkflowEngine;
import io.github.thebesteric.framework.agile.plugins.workflow.service.RuntimeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * TaskInstanceTest
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-25 10:30:45
 */
@SpringBootTest
public class TaskInstanceTest {

    @Autowired
    WorkflowEngine workflowEngine;

    @Test
    void findTaskInstances() {
        RuntimeService runtimeService = workflowEngine.getRuntimeService();
        System.out.println(runtimeService.findTaskInstances("8888", "张三", null, null, 1, 10));
    }

}
