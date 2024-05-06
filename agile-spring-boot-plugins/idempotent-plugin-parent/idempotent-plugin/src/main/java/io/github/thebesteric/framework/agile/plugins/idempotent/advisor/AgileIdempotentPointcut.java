package io.github.thebesteric.framework.agile.plugins.idempotent.advisor;

import io.github.thebesteric.framework.agile.core.pointcut.AbstractSpringComponentAnnotationPointcut;
import io.github.thebesteric.framework.agile.plugins.idempotent.annotation.Idempotent;
import io.github.thebesteric.framework.agile.plugins.idempotent.config.AgileIdempotentContext;

import java.lang.annotation.Annotation;

/**
 * AgileIdempotentPointcut
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-04-30 15:29:43
 */
public class AgileIdempotentPointcut extends AbstractSpringComponentAnnotationPointcut {

    public AgileIdempotentPointcut(AgileIdempotentContext context) {
        super(context.getClassMatchers());
    }

    @Override
    protected Class<? extends Annotation> matchedAnnotation() {
        return Idempotent.class;
    }

}
