package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.node.history;

import cn.hutool.core.text.CharSequenceUtil;
import io.github.thebesteric.framework.agile.commons.exception.InvalidParamsException;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.DMLOperator;
import io.github.thebesteric.framework.agile.plugins.workflow.domain.builder.AbstractBuilder;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.NodeDefinition;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.NodeDefinitionHistory;

/**
 * NodeDefinitionHistoryBuilder
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-10-08 14:19:39
 */
public class NodeDefinitionHistoryBuilder extends AbstractBuilder<NodeDefinitionHistory> {

    private final NodeDefinitionHistory nodeDefinitionHistory;

    private NodeDefinitionHistoryBuilder(NodeDefinitionHistory nodeDefinitionHistory) {
        this.nodeDefinitionHistory = nodeDefinitionHistory;
    }

    public static NodeDefinitionHistoryBuilder builder() {
        return new NodeDefinitionHistoryBuilder(new NodeDefinitionHistory());
    }

    public NodeDefinitionHistoryBuilder tenantId(String tenantId) {
        this.nodeDefinitionHistory.setTenantId(tenantId);
        return this;
    }

    public NodeDefinitionHistoryBuilder nodeDefinitionId(Integer nodeDefinitionId) {
        this.nodeDefinitionHistory.setNodeDefinitionId(nodeDefinitionId);
        return this;
    }

    public NodeDefinitionHistoryBuilder dmlOperator(DMLOperator dmlOperator) {
        this.nodeDefinitionHistory.setDmlOperator(dmlOperator);
        return this;
    }

    public NodeDefinitionHistoryBuilder beforeObj(NodeDefinition beforeObj) {
        this.nodeDefinitionHistory.setBeforeObj(beforeObj);
        return this;
    }

    public NodeDefinitionHistoryBuilder currentObj(NodeDefinition currentObj) {
        this.nodeDefinitionHistory.setCurrentObj(currentObj);
        return this;
    }

    public NodeDefinitionHistoryBuilder desc(String desc) {
        this.nodeDefinitionHistory.setDesc(desc);
        return this;
    }

    public NodeDefinitionHistory build() {
        String tenantId = this.nodeDefinitionHistory.getTenantId();
        Integer nodeDefinitionId = this.nodeDefinitionHistory.getNodeDefinitionId();
        DMLOperator dmlOperator = this.nodeDefinitionHistory.getDmlOperator();
        if (CharSequenceUtil.isEmpty(tenantId) || nodeDefinitionId == null || dmlOperator == null) {
            throw new InvalidParamsException("tenantId, nodeDefinitionId, dmlOperator cannot be empty");
        }
        return super.build(this.nodeDefinitionHistory);
    }
}
