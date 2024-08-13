package io.github.thebesteric.framework.agile.plugins.limiter.advisor;

import io.github.thebesteric.framework.agile.core.pointcut.AbstractSpringComponentAnnotationPointcut;
import io.github.thebesteric.framework.agile.plugins.limiter.annotation.RateLimiter;
import io.github.thebesteric.framework.agile.plugins.limiter.config.AgileRateLimiterContext;

import java.io.Serial;
import java.lang.annotation.Annotation;

/**
 * AgileIdempotentPointcut
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-04-30 15:29:43
 */
public class AgileRateLimiterPointcut extends AbstractSpringComponentAnnotationPointcut {
    @Serial
    private static final long serialVersionUID = 7591337085710426535L;

    public AgileRateLimiterPointcut(AgileRateLimiterContext context) {
        super(context.getClassMatchers());
    }

    @Override
    protected Class<? extends Annotation> matchedAnnotation() {
        return RateLimiter.class;
    }
}
