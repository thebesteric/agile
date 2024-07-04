package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.relation;

import io.github.thebesteric.framework.agile.commons.exception.InvalidParamsException;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.NodeDefinition;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.NodeRelation;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.WorkflowDefinition;

/**
 * NodeRelationBuilder
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-18 15:38:59
 */
public class NodeRelationBuilder extends AbstractBuilder<NodeRelation> {

    private final NodeRelation nodeRelation;

    private NodeRelationBuilder(NodeRelation nodeRelation) {
        this.nodeRelation = nodeRelation;
    }

    public static NodeRelationBuilder builder(String tenantId, Integer workflowDefinitionId) {
        NodeRelationBuilder builder = new NodeRelationBuilder(new NodeRelation());
        builder.tenantId(tenantId);
        builder.workflowDefinitionId(workflowDefinitionId);
        return builder;
    }

    public static NodeRelationBuilder builder(String tenantId, WorkflowDefinition workflowDefinition) {
        return builder(tenantId, workflowDefinition.getId());
    }

    public NodeRelationBuilder workflowDefinition(WorkflowDefinition workflowDefinition) {
        return workflowDefinitionId(workflowDefinition.getId());
    }

    public NodeRelationBuilder workflowDefinitionId(Integer workflowDefinitionId) {
        this.nodeRelation.setWorkflowDefinitionId(workflowDefinitionId);
        return this;
    }

    public NodeRelationBuilder tenantId(String tenantId) {
        this.nodeRelation.setTenantId(tenantId);
        return this;
    }

    public NodeRelationBuilder fromNode(NodeDefinition fromNode) {
        return fromNodeId(fromNode.getId());
    }

    public NodeRelationBuilder fromNodeId(Integer fromNodeId) {
        this.nodeRelation.setFromNodeId(fromNodeId);
        return this;
    }

    public NodeRelationBuilder toNode(NodeDefinition toNode) {
        return toNodeId(toNode.getId());
    }

    public NodeRelationBuilder toNodeId(Integer toNodeId) {
        this.nodeRelation.setToNodeId(toNodeId);
        return this;
    }

    public NodeRelationBuilder sequence(Integer sequence) {
        this.nodeRelation.setSequence(sequence);
        return this;
    }

    public NodeRelationBuilder createdBy(String user) {
        this.nodeRelation.setCreatedBy(user);
        return this;
    }

    public NodeRelationBuilder updateBy(String user) {
        this.nodeRelation.setUpdatedBy(user);
        return this;
    }

    public NodeRelation build() {
        Integer workflowDefinitionId = nodeRelation.getWorkflowDefinitionId();
        String tenantId = nodeRelation.getTenantId();
        Integer fromNodeId = nodeRelation.getFromNodeId();
        Integer toNodeId = nodeRelation.getToNodeId();
        Integer sequence = nodeRelation.getSequence();
        if (tenantId ==null || workflowDefinitionId == null || fromNodeId == null || toNodeId == null || sequence == null) {
            throw new InvalidParamsException("tenantId，workflowDefinitionId, fromNodeId, toNodeId, sequence cannot be null");
        }
        return super.build(this.nodeRelation);
    }
}
