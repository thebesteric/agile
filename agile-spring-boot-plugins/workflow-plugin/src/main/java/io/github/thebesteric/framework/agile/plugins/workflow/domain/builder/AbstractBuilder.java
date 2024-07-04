package io.github.thebesteric.framework.agile.plugins.workflow.domain.builder;

import io.github.thebesteric.framework.agile.plugins.workflow.config.AgileWorkflowContext;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.base.BaseEntity;

/**
 * AbstractBuilder
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-21 16:01:35
 */
public abstract class AbstractBuilder<T extends BaseEntity> {

    public T build(T entity) {
        if (entity.getCreatedBy() == null) {
            entity.setCreatedBy(AgileWorkflowContext.getCurrentUser());
        }
        if (entity.getUpdatedBy() == null) {
            entity.setUpdatedBy(AgileWorkflowContext.getCurrentUser());
        }
        return entity;
    }

}
