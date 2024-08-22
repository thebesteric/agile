package io.github.thebesteric.framework.agile.distributed.locks.advisor;

import cn.hutool.core.text.CharSequenceUtil;
import io.github.thebesteric.framework.agile.core.func.SuccessFailureExecutor;
import io.github.thebesteric.framework.agile.distributed.locks.annotation.DistributedLock;
import io.github.thebesteric.framework.agile.distributed.locks.config.AgileDistributedLocksContext;
import io.github.thebesteric.framework.agile.distributed.locks.exeption.DistributedLocksException;
import io.github.thebesteric.framework.agile.distributed.locks.generator.DistributedLocksKeyGenerator;
import io.github.thebesteric.framework.agile.distributed.locks.processor.DistributedLocksProcessor;
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
 * AgileDistributedLocksAdvice
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-22 17:28:32
 */
@RequiredArgsConstructor
public class AgileDistributedLocksAdvice implements MethodInterceptor {

    private final AgileDistributedLocksContext context;

    @Nullable
    @Override
    public Object invoke(@Nonnull MethodInvocation invocation) throws Throwable {
        if (!context.getProperties().isEnable()) {
            // 调用目标方法
            return invocation.proceed();
        }

        Method method = invocation.getMethod();
        DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);

        AtomicReference<Object> result = new AtomicReference<>();
        // 生成 Key
        final String distributedLocksKey = DistributedLocksKeyGenerator.generate(method);
        DistributedLocksProcessor distributedLocksProcessor = context.getDistributedLocksProcessor();
        distributedLocksProcessor.execute(distributedLocksKey, distributedLock.waitTime(), distributedLock.leaseTime(), distributedLock.timeUnit(), new SuccessFailureExecutor<Boolean, Boolean, Exception>() {
            @Override
            @SneakyThrows
            public void success(Boolean success) {
                Try.run(() -> result.set(invocation.proceed())).getOrElseThrow(ex -> ex);
            }

            @Override
            public void failure(Boolean failure) {
                String message = distributedLock.message();
                if (CharSequenceUtil.isEmpty(message)) {
                    message = context.getProperties().getMessage();
                }
                exception(new DistributedLocksException(message));
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
