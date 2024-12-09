package io.github.thebesteric.framework.agile.test.controller;

import io.github.thebesteric.framework.agile.core.domain.R;
import io.github.thebesteric.framework.agile.plugins.logger.annotation.AgileLogger;
import io.github.thebesteric.framework.agile.plugins.workflow.WorkflowEngine;
import io.github.thebesteric.framework.agile.plugins.workflow.helper.WorkflowHelper;
import io.github.thebesteric.framework.agile.plugins.workflow.helper.service.RuntimeServiceHelper;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TransactionController
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-12-09 10:38:39
 */
@RestController
@RequestMapping("/trans")
@AgileLogger(tag = "trans")
public class TransactionController {

    @Resource
    private WorkflowEngine workflowEngine;

    private static final String TENANT_ID = "8888";

    @GetMapping("/test")
    @Transactional
    public R<Void> test() {
        WorkflowHelper workflowHelper = new WorkflowHelper(workflowEngine);
        RuntimeServiceHelper runtimeServiceHelper = workflowHelper.getRuntimeServiceHelper();
        runtimeServiceHelper.setCurrentUser("system-user");

        runtimeServiceHelper.interrupt(TENANT_ID, 1, "强制中断");

        // int i = 1 / 0;

        return R.success();
    }
}
