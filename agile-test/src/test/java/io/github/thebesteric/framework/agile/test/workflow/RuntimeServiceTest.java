package io.github.thebesteric.framework.agile.test.workflow;

import io.github.thebesteric.framework.agile.plugins.workflow.WorkflowEngine;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.ApproveStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.NodeStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.WorkflowStatus;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.BusinessInfo;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.RequestCondition;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.RequestConditions;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.Requester;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.TaskInstance;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowInstance;
import io.github.thebesteric.framework.agile.plugins.workflow.service.RuntimeService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * RuntimeServiceTest
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-25 13:34:31
 */
@SpringBootTest
public class RuntimeServiceTest {

    @Resource
    WorkflowEngine workflowEngine;

    @Test
    void start() {
        String tenantId = "8888";
        String requesterId = "eric";
        workflowEngine.setCurrentUser(requesterId);
        RuntimeService runtimeService = workflowEngine.getRuntimeService();
        RequestConditions requestConditions = RequestConditions.newInstance();
        requestConditions.addRequestCondition(RequestCondition.of("day", "2"));

        BusinessInfo businessInfo = BusinessInfo.of(String.class, "hello world");
        runtimeService.start(tenantId, "test-key", Requester.of(requesterId), businessInfo, "请假申请单", requestConditions, null);
    }

    @Test
    void cancel() {
        String tenantId = "8888";
        String requesterId = "eric";
        workflowEngine.setCurrentUser(requesterId);
        RuntimeService runtimeService = workflowEngine.getRuntimeService();
        List<WorkflowInstance> workflowInstances = runtimeService.findWorkflowInstancesByRequestId("1", requesterId, WorkflowStatus.IN_PROGRESS);
        for (WorkflowInstance workflowInstance : workflowInstances) {
            runtimeService.cancel(tenantId, workflowInstance.getId());
        }
    }

    @Test
    void approve() {
        String roleId = null;
        String tenantId = "8888";
        // String approverId = "张三";
        // String approverId = "李四";
        String approverId = "王五";
        // String approverId = "赵六";
        // String approverId = "孙七";
        workflowEngine.setCurrentUser(approverId);
        RuntimeService runtimeService = workflowEngine.getRuntimeService();
        List<TaskInstance> taskInstances = runtimeService.findTaskInstances(tenantId, null, roleId, approverId, NodeStatus.IN_PROGRESS, ApproveStatus.IN_PROGRESS, null);
        if (!taskInstances.isEmpty()) {
            for (TaskInstance taskInstance : taskInstances) {
                runtimeService.approve(tenantId, taskInstance.getId(), roleId, approverId, "同意");
            }
        }
    }

    @Test
    void reject() {
        String roleId = null;
        String tenantId = "8888";
        String approverId = "张三";
        // String approverId = "李四";
        // String approverId = "王五";
        // String approverId = "赵六";
        // String approverId = "孙七";
        workflowEngine.setCurrentUser(approverId);
        RuntimeService runtimeService = workflowEngine.getRuntimeService();
        List<TaskInstance> taskInstances = runtimeService.findTaskInstances(tenantId, null, roleId, approverId, NodeStatus.IN_PROGRESS, ApproveStatus.IN_PROGRESS, null);
        if (!taskInstances.isEmpty()) {
            for (TaskInstance taskInstance : taskInstances) {
                runtimeService.reject(tenantId, taskInstance.getId(), roleId, approverId, "不同意");
            }
        }
    }

    @Test
    void abandon() {
        String roleId = null;
        String tenantId = "8888";
        // String approverId = "张三";
        // String approverId = "李四";
        String approverId = "王五";
        // String approverId = "赵六";
        // String approverId = "孙七";
        workflowEngine.setCurrentUser(approverId);
        RuntimeService runtimeService = workflowEngine.getRuntimeService();
        List<TaskInstance> taskInstances = runtimeService.findTaskInstances(tenantId, null, roleId, approverId, NodeStatus.IN_PROGRESS, ApproveStatus.IN_PROGRESS, null);
        if (!taskInstances.isEmpty()) {
            for (TaskInstance taskInstance : taskInstances) {
                runtimeService.abandon(tenantId, taskInstance.getId(), roleId, approverId, "弃权");
            }
        }
    }

}
