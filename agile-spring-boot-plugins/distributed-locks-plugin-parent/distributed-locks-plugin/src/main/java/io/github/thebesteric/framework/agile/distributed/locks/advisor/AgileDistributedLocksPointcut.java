package io.github.thebesteric.framework.agile.distributed.locks.advisor;

import io.github.thebesteric.framework.agile.core.pointcut.AbstractSpringComponentAnnotationPointcut;
import io.github.thebesteric.framework.agile.distributed.locks.annotation.DistributedLock;
import io.github.thebesteric.framework.agile.distributed.locks.config.AgileDistributedLocksContext;

import java.io.Serial;
import java.lang.annotation.Annotation;

/**
 * AgileDistributedLocksPointcut
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-22 16:05:08
 */
public class AgileDistributedLocksPointcut extends AbstractSpringComponentAnnotationPointcut {
    @Serial
    private static final long serialVersionUID = 1154920230998150222L;

    public AgileDistributedLocksPointcut(AgileDistributedLocksContext context) {
        super(context.getClassMatchers());
    }

    @Override
    public Class<? extends Annotation> matchedAnnotation() {
        return DistributedLock.class;
    }
}
