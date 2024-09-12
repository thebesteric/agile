package io.github.thebesteric.framework.agile.plugins.workflow.constant;

import io.github.thebesteric.framework.agile.commons.util.AbstractUtils;

/**
 * WorkflowConstants
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-09-09 19:24:31
 */
public final class WorkflowConstants extends AbstractUtils {
    /** 动态指定审批人前缀 */
    public static final String DYNAMIC_ASSIGNMENT_APPROVER_VALUE_PREFIX = "{assignment:";
    /** 动态指定审批人后缀 */
    public static final String DYNAMIC_ASSIGNMENT_APPROVER_VALUE_SUFFIX = "}";
    /** 动态指定审批人 */
    public static final String DYNAMIC_ASSIGNMENT_APPROVER_VALUE = DYNAMIC_ASSIGNMENT_APPROVER_VALUE_PREFIX + "%s" + DYNAMIC_ASSIGNMENT_APPROVER_VALUE_SUFFIX;

    /** 系统审批人 */
    public static final String AUTO_APPROVER = "{SYSTEM:AUTO}";
    public static final String AUTO_APPROVER_COMMENT = "系统自动同意";
}
