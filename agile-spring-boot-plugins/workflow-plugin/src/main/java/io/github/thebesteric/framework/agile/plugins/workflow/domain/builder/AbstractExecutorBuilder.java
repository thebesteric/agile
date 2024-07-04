package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder;

import io.github.thebesteric.framework.agile.plugins.workflow.config.AgileWorkflowContext;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.base.BaseEntity;

/**
 * AbstractExecutorBuilder
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-21 16:31:54
 */
public abstract class AbstractExecutorBuilder<T extends BaseEntity> {

    public void setDefaultEntityAttrs(T entity) {
        if (entity.getCreatedBy() == null) {
            entity.setCreatedBy(AgileWorkflowContext.getCurrentUser());
        }
        if (entity.getUpdatedBy() == null) {
            entity.setUpdatedBy(AgileWorkflowContext.getCurrentUser());
        }
    }

}
