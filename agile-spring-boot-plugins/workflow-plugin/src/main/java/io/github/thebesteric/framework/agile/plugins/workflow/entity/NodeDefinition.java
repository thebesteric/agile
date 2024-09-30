package io.github.thebesteric.framework.agile.plugins.workflow.entity;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityClass;
import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityColumn;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.ApproveType;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.NodeType;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.RoleApproveType;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.RoleUserApproveType;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.Approver;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.Conditions;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.RoleApprover;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.base.BaseEntity;
import io.github.thebesteric.framework.agile.plugins.workflow.exception.WorkflowException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.annotation.Transient;

import java.io.Serial;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 节点定义表
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-11 21:13:45
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@EntityClass(value = "awf_node_definition", comment = "节点定义表")
public class NodeDefinition extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1079297069865230679L;

    @EntityColumn(name = "tenant_id", length = 32, nullable = false, comment = "租户 ID")
    private String tenantId;

    @EntityColumn(name = "wf_def_id", nullable = false, comment = "流程定义 ID")
    private Integer workflowDefinitionId;

    @EntityColumn(length = 32, nullable = false, comment = "流程名称")
    private String name;

    @EntityColumn(type = EntityColumn.Type.TINY_INT, nullable = false, comment = "节点类型")
    private NodeType nodeType;

    @EntityColumn(type = EntityColumn.Type.TINY_INT, nullable = false, comment = "审批类型")
    private ApproveType approveType = ApproveType.ANY;

    @EntityColumn(type = EntityColumn.Type.JSON, comment = "条件定义")
    private Conditions conditions;

    @EntityColumn(nullable = false, length = 12, precision = 2, comment = "排序")
    private Double sequence;

    @EntityColumn(nullable = false, defaultExpression = "0", comment = "是否是动态指定审批人")
    private boolean dynamicAssignment = false;

    @EntityColumn(nullable = false, defaultExpression = "0", comment = "是否是角色审批节点")
    private boolean roleApprove = false;

    @EntityColumn(type = EntityColumn.Type.TINY_INT, nullable = false, comment = "角色用户审批类型")
    private RoleUserApproveType roleUserApproveType = RoleUserApproveType.ANY;

    @EntityColumn(type = EntityColumn.Type.TINY_INT, nullable = false, comment = "角色审批类型")
    private RoleApproveType roleApproveType = RoleApproveType.ANY;

    /** 审批人，存储在 NodeAssignment 表中 */
    @Transient
    private Set<Approver> approvers = new LinkedHashSet<>();

    /** 角色审批人，存储在 NodeRoleUserAssignment 表中 */
    @Transient
    private Set<RoleApprover> roleApprovers = new LinkedHashSet<>();

    /**
     * 是否时动态指定审批人节点，并且没有设置动态审批人
     *
     * @return boolean
     *
     * @author wangweijun
     * @since 2024/9/27 15:13
     */
    public boolean isUnSettingAssignmentApprovers() {
        return this.dynamicAssignment && this.approvers.stream().anyMatch(Approver::isUnSettingAssignmentApprover);
    }

    /**
     * 是否包含审批条件
     *
     * @return boolean
     *
     * @author wangweijun
     * @since 2024/7/4 13:33
     */
    public boolean hasConditions() {
        return this.conditions != null;
    }

    /**
     * 是否是用户审批节点
     *
     * @return boolean
     *
     * @author wangweijun
     * @since 2024/9/27 15:55
     */
    public boolean isUserApprove() {
        return !this.isRoleApprove();
    }

    /**
     * 添加审批人
     *
     * @param approver 审批人
     *
     * @author wangweijun
     * @since 2024/9/29 16:45
     */
    public void addApprover(Approver approver) {
        this.addApprovers(Set.of(approver));
    }


    /**
     * 添加审批人
     *
     * @param approvers 审批人
     *
     * @author wangweijun
     * @since 2024/9/29 16:45
     */
    public void addApprovers(Set<Approver> approvers) {
        if (this.isRoleApprove()) {
            this.throwUserApproveUpdateApproverException();
        }
        this.approvers.addAll(approvers);
    }

    /**
     * 移除审批人
     *
     * @param approver 审批人
     *
     * @author wangweijun
     * @since 2024/7/3 10:44
     */
    public void removeApprover(Approver approver) {
        this.removeApprovers(Set.of(approver));
    }

    /**
     * 移除审批人
     *
     * @param approvers 审批人
     *
     * @author wangweijun
     * @since 2024/7/3 10:44
     */
    public void removeApprovers(Set<Approver> approvers) {
        if (this.isRoleApprove()) {
            this.throwUserApproveUpdateApproverException();
        }
        if (CollectionUtils.isNotEmpty(approvers)) {
            Set<Approver> unContainsApprovers = approvers.stream().filter(approver -> !this.approvers.contains(approver)).collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(unContainsApprovers)) {
                throw new WorkflowException("未查询到审批人: %s", unContainsApprovers);
            }
            approvers.forEach(this.approvers::remove);
        }
    }

    /**
     * 替换审批人
     *
     * @param oldApprover 原审批人
     * @param newApprover 新审批人
     *
     * @author wangweijun
     * @since 2024/7/12 14:10
     */
    public void replaceApprover(Approver oldApprover, Approver newApprover) {
        if (this.isRoleApprove()) {
            this.throwUserApproveUpdateApproverException();
        }
        if (!approvers.contains(oldApprover)) {
            throw new WorkflowException("未查询到审批人: %s", oldApprover);
        }
        this.approvers.remove(oldApprover);
        this.approvers.add(newApprover);
    }

    /**
     * 清空审批人
     *
     * @author wangweijun
     * @since 2024/7/3 13:38
     */
    public void clearApprovers() {
        if (this.isRoleApprove()) {
            this.throwUserApproveUpdateApproverException();
        }
        this.approvers.clear();
    }

    /**
     * 添加角色审批人
     *
     * @param roleApprover 角色审批人
     *
     * @author wangweijun
     * @since 2024/9/29 16:45
     */
    public void addRoleApprover(RoleApprover roleApprover) {
        this.addRoleApprovers(Set.of(roleApprover));
    }

    /**
     * 添加角色审批人
     *
     * @param roleApprovers 角色审批人
     *
     * @author wangweijun
     * @since 2024/9/29 16:45
     */
    public void addRoleApprovers(Set<RoleApprover> roleApprovers) {
        if (this.isUserApprove()) {
            this.throwRoleApproveUpdateApproverException();
        }
        this.roleApprovers.addAll(roleApprovers);
    }

    /**
     * 移除角色审批人
     *
     * @param roleApprovers 角色审批人
     *
     * @author wangweijun
     * @since 2024/9/29 17:04
     */
    public void removeRoleApprovers(Set<RoleApprover> roleApprovers) {
        if (this.isUserApprove()) {
            this.throwRoleApproveUpdateApproverException();
        }
        if (CollectionUtils.isNotEmpty(roleApprovers)) {
            Set<RoleApprover> unContainsRoleApprovers = roleApprovers.stream().filter(approver -> !this.roleApprovers.contains(approver)).collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(unContainsRoleApprovers)) {
                throw new WorkflowException("未查询到角色审批人: %s", unContainsRoleApprovers);
            }
            roleApprovers.forEach(this.roleApprovers::remove);
        }
    }

    /**
     * 移除角色审批人
     *
     * @param roleApprover 角色审批人
     *
     * @author wangweijun
     * @since 2024/9/29 17:04
     */
    public void removeRoleApprover(RoleApprover roleApprover) {
        this.removeRoleApprovers(Set.of(roleApprover));
    }

    /**
     * 替换角色审批人
     *
     * @param oldRoleApprover 原角色审批人
     * @param newRoleApprover 新角色审批人
     *
     * @author wangweijun
     * @since 2024/9/29 17:04
     */
    public void replaceRoleApprover(RoleApprover oldRoleApprover, RoleApprover newRoleApprover) {
        if (this.isUserApprove()) {
            this.throwRoleApproveUpdateApproverException();
        }
        if (!roleApprovers.contains(oldRoleApprover)) {
            throw new WorkflowException("未查询到角色审批人: %s", oldRoleApprover);
        }
        this.roleApprovers.remove(oldRoleApprover);
        this.roleApprovers.add(newRoleApprover);
    }

    /**
     * 清空角色审批人
     *
     * @author wangweijun
     * @since 2024/9/29 17:07
     */
    public void clearRoleApprovers() {
        if (this.isUserApprove()) {
            this.throwRoleApproveUpdateApproverException();
        }
        this.roleApprovers.clear();
    }

    private void throwUserApproveUpdateApproverException() {
        throw new WorkflowException("角色审批节点不允许修改用户审批人");
    }

    private void throwRoleApproveUpdateApproverException() {
        throw new WorkflowException("用户审批节点不允许修改角色审批人");
    }

    public static NodeDefinition of(ResultSet rs) throws SQLException {
        NodeDefinition nodeDefinition = new NodeDefinition();
        nodeDefinition.setTenantId(rs.getString("tenant_id"));
        nodeDefinition.setWorkflowDefinitionId(rs.getInt("wf_def_id"));
        nodeDefinition.setName(rs.getString("name"));
        nodeDefinition.setNodeType(NodeType.of(rs.getInt("node_type")));
        nodeDefinition.setApproveType(ApproveType.of(rs.getInt("approve_type")));
        String conditionStr = rs.getString("conditions");
        if (CharSequenceUtil.isNotEmpty(conditionStr)) {
            nodeDefinition.setConditions(JSONUtil.toBean(conditionStr, Conditions.class));
        }
        nodeDefinition.setSequence(rs.getDouble("sequence"));
        nodeDefinition.setDynamicAssignment(rs.getInt("dynamic_assignment") == 1);
        nodeDefinition.setRoleApprove(rs.getInt("role_approve") == 1);
        nodeDefinition.setRoleUserApproveType(RoleUserApproveType.of(rs.getInt("role_user_approve_type")));
        nodeDefinition.setRoleApproveType(RoleApproveType.of(rs.getInt("role_approve_type")));
        return of(nodeDefinition, rs);
    }
}
