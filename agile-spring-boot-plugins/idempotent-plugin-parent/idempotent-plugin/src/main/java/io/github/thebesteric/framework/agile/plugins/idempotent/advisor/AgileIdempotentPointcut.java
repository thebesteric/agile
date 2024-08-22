package io.github.thebesteric.framework.agile.plugins.idempotent.advisor;

import io.github.thebesteric.framework.agile.core.pointcut.AbstractSpringComponentAnnotationPointcut;
import io.github.thebesteric.framework.agile.plugins.idempotent.annotation.Idempotent;
import io.github.thebesteric.framework.agile.plugins.idempotent.config.AgileIdempotentContext;

import java.io.Serial;
import java.lang.annotation.Annotation;

/**
 * AgileIdempotentPointcut
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-04-30 15:29:43
 */
public class AgileIdempotentPointcut extends AbstractSpringComponentAnnotationPointcut {
    @Serial
    private static final long serialVersionUID = -7686894236205872162L;

    public AgileIdempotentPointcut(AgileIdempotentContext context) {
        super(context.getClassMatchers());
    }

    @Override
    public Class<? extends Annotation> matchedAnnotation() {
        return Idempotent.class;
    }

}
