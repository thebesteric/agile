package io.github.thebesteric.framework.agile.plugins.idempotent.advisor;

import cn.hutool.core.text.CharSequenceUtil;
import io.github.thebesteric.framework.agile.commons.exception.InvalidParamsException;
import io.github.thebesteric.framework.agile.commons.util.StringUtils;
import io.github.thebesteric.framework.agile.core.func.SuccessFailureExecutor;
import io.github.thebesteric.framework.agile.plugins.idempotent.annotation.Idempotent;
import io.github.thebesteric.framework.agile.plugins.idempotent.config.AgileIdempotentContext;
import io.github.thebesteric.framework.agile.plugins.idempotent.exception.IdempotentException;
import io.github.thebesteric.framework.agile.plugins.idempotent.generator.IdempotentKeyGenerator;
import io.github.thebesteric.framework.agile.plugins.idempotent.processor.IdempotentProcessor;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
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
        idempotentProcessor.execute(idempotentKey, System.currentTimeMillis(), idempotent.timeout(), idempotent.timeUnit(), new SuccessFailureExecutor<>() {
            @Override
            @SneakyThrows
            public void success(Boolean success) {
                Try.run(() -> result.set(invocation.proceed())).getOrElseThrow(ex -> ex);
            }

            @Override
            public void failure(Boolean failure) {
                String message = idempotent.message();
                if (CharSequenceUtil.isEmpty(message)) {
                    message = context.getProperties().getMessage();
                }
                exception(new IdempotentException(message));
            }

            @Override
            @SneakyThrows
            public void exception(Exception exception) {
                throw exception;
            }
        });

        return result.get();
    }
}
