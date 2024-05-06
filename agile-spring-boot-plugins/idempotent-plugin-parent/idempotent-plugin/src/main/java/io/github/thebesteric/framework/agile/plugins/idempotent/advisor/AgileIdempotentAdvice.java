package io.github.thebesteric.framework.agile.plugins.idempotent.advisor;

import io.github.thebesteric.framework.agile.commons.exception.InvalidParamsException;
import io.github.thebesteric.framework.agile.commons.util.StringUtils;
import io.github.thebesteric.framework.agile.plugins.idempotent.annotation.Idempotent;
import io.github.thebesteric.framework.agile.plugins.idempotent.config.AgileIdempotentContext;
import io.github.thebesteric.framework.agile.plugins.idempotent.exception.IdempotentException;
import io.github.thebesteric.framework.agile.plugins.idempotent.generator.IdempotentKeyGenerator;
import io.github.thebesteric.framework.agile.plugins.idempotent.processor.IdempotentProcessor;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * AgileIdempotentAdvice
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-04-30 15:55:19
 */
@RequiredArgsConstructor
public class AgileIdempotentAdvice implements MethodInterceptor {

    private final AgileIdempotentContext context;

    private final Set<String> KEYS = new HashSet<>(128);

    @Nullable
    @Override
    public Object invoke(@Nonnull MethodInvocation invocation) throws Throwable {

        if (!context.getProperties().isEnable()) {
            // 调用目标方法
            return invocation.proceed();
        }

        Method method = invocation.getMethod();
        Object[] args = invocation.getArguments();
        Idempotent idempotent = method.getAnnotation(Idempotent.class);
        if (StringUtils.isEmpty(idempotent.keyPrefix())) {
            throw new InvalidParamsException("幂等校验前缀不能为空");
        }

        AtomicReference<Object> result = new AtomicReference<>();
        // 生成 Key
        final String idempotentKey = IdempotentKeyGenerator.generate(method, args);
        IdempotentProcessor idempotentProcessor = context.getIdempotentProcessor();
        if (idempotentProcessor.tryLock(idempotentKey, System.currentTimeMillis(), idempotent.timeout(), idempotent.timeUnit())) {
            Try.run(() -> result.set(invocation.proceed())).getOrElseThrow(ex -> new RuntimeException(ex.getMessage()));
        } else {
            throw new IdempotentException(idempotent.message());
        }

        return result.get();
    }
}
