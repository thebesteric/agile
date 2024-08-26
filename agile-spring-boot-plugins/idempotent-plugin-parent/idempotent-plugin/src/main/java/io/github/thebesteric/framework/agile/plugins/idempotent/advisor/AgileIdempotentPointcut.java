package io.github.thebesteric.framework.agile.plugins.idempotent.advisor;

import io.github.thebesteric.framework.agile.commons.util.ReflectUtils;
import io.github.thebesteric.framework.agile.core.pointcut.AbstractSpringComponentMethodPointcut;
import io.github.thebesteric.framework.agile.plugins.idempotent.config.AgileIdempotentContext;
import io.github.thebesteric.framework.agile.plugins.idempotent.config.AgileIdempotentProperties;
import lombok.EqualsAndHashCode;
import org.springframework.lang.NonNull;

import java.io.Serial;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * AgileIdempotentPointcut
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-04-30 15:29:43
 */
@EqualsAndHashCode(callSuper = true)
public class AgileIdempotentPointcut extends AbstractSpringComponentMethodPointcut {
    @Serial
    private static final long serialVersionUID = -7686894236205872162L;

    private Set<String> ignoredPackages;

    public AgileIdempotentPointcut(AgileIdempotentContext context) {
        super(context.getClassMatchers());
        AgileIdempotentProperties.GlobalSetting globalSetting = context.getProperties().getGlobalSetting();
        ignoredPackages = globalSetting.getIgnoredPackages();
        if (ignoredPackages == null) {
            ignoredPackages = new HashSet<>();
        }
        ignoredPackages.add("org.springframework");

    }

    @Override
    protected boolean matchedMethod(@NonNull Method method, @NonNull Class<?> targetClass) {
        // 需要忽略掉包路径

        // 私有方法、静态方法、final 方法不支持
        return (ReflectUtils.isPrivate(method) || !ReflectUtils.isStatic(method) || !ReflectUtils.isFinal(method))
               && ignoredPackages.stream().noneMatch(pkg -> targetClass.getName().startsWith(pkg));
    }
}
