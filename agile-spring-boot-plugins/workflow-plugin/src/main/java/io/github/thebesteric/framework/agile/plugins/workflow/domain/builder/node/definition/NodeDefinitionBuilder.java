package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.definition;

import io.github.thebesteric.framework.agile.plugins.workflow.constant.ApproveType;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.NodeType;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.Conditions;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.NodeDefinition;
import io.github.thebesteric.framework.agile.plugins.workflow.exception.WorkflowException;

import java.util.List;

/**
 * NodeDefinitionBuilder
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-18 15:38:59
 */
public class NodeDefinitionBuilder extends AbstractBuilder<NodeDefinition> {

    private final NodeDefinition nodeDefinition;

    private NodeDefinitionBuilder(NodeDefinition nodeDefinition) {
        this.nodeDefinition = nodeDefinition;
    }

    public static NodeDefinitionBuilder builderNode(String tenantId, Integer workflowDefinitionId, NodeType nodeType, Integer sequence) {
        NodeDefinitionBuilder builder = new NodeDefinitionBuilder(new NodeDefinition());
        builder.nodeDefinition.setTenantId(tenantId);
        builder.nodeDefinition.setWorkflowDefinitionId(workflowDefinitionId);
        builder.nodeDefinition.setNodeType(nodeType);
        builder.nodeDefinition.setSequence(sequence);
        return builder;
    }

    public static NodeDefinitionBuilder builderStartNode(String tenantId, Integer workflowDefinitionId) {
        return builderNode(tenantId, workflowDefinitionId, NodeType.START, Integer.MIN_VALUE);
    }

    public static NodeDefinitionBuilder builderEndNode(String tenantId, Integer workflowDefinitionId) {
        return builderNode(tenantId, workflowDefinitionId, NodeType.END, Integer.MAX_VALUE);
    }

    public static NodeDefinitionBuilder builderTaskNode(String tenantId, Integer workflowDefinitionId, Integer sequence) {
        return builderNode(tenantId, workflowDefinitionId, NodeType.TASK, sequence);
    }

    public NodeDefinitionBuilder name(String name) {
        this.nodeDefinition.setName(name);
        return this;
    }

    public NodeDefinitionBuilder nodeType(NodeType nodeType) {
        this.nodeDefinition.setNodeType(nodeType);
        return this;
    }

    public NodeDefinitionBuilder approveType(ApproveType approveType) {
        this.nodeDefinition.setApproveType(approveType);
        return this;
    }

    public NodeDefinitionBuilder desc(String desc) {
        this.nodeDefinition.setDesc(desc);
        return this;
    }

    public NodeDefinitionBuilder conditions(Conditions conditions) {
        this.nodeDefinition.setConditions(conditions);
        return this;
    }

    public NodeDefinitionBuilder approverId(String approverId) {
        this.nodeDefinition.getApproverIds().add(approverId);
        return this;
    }

    public NodeDefinitionBuilder approverIds(List<String> approverIds) {
        approverIds.forEach(this::approverId);
        return this;
    }

    public NodeDefinition build() {
        String tenantId = this.nodeDefinition.getTenantId();
        Integer workflowDefinitionId = this.nodeDefinition.getWorkflowDefinitionId();
        NodeType nodeType = this.nodeDefinition.getNodeType();
        if (tenantId == null || workflowDefinitionId == null || nodeType == null) {
            throw new WorkflowException("workflowDefinitionId, nodeType cannot be null");
        }
        return super.build(this.nodeDefinition);
    }
}
