package io.github.thebesteric.framework.agile.plugins.idempotent.advisor;

import cn.hutool.core.text.CharSequenceUtil;
import io.github.thebesteric.framework.agile.commons.exception.InvalidParamsException;
import io.github.thebesteric.framework.agile.commons.util.StringUtils;
import io.github.thebesteric.framework.agile.core.func.SuccessFailureExecutor;
import io.github.thebesteric.framework.agile.plugins.idempotent.annotation.Idempotent;
import io.github.thebesteric.framework.agile.plugins.idempotent.config.AgileIdempotentContext;
import io.github.thebesteric.framework.agile.plugins.idempotent.config.AgileIdempotentProperties;
import io.github.thebesteric.framework.agile.plugins.idempotent.exception.IdempotentException;
import io.github.thebesteric.framework.agile.plugins.idempotent.generator.IdempotentKeyGenerator;
import io.github.thebesteric.framework.agile.plugins.idempotent.processor.IdempotentProcessor;
import io.vavr.control.Try;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.TimeUnit;
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
        AgileIdempotentProperties properties = context.getProperties();
        if (!properties.isEnable()) {
            // 调用目标方法
            return invocation.proceed();
        }

        Method method = invocation.getMethod();
        Object[] args = invocation.getArguments();

        final IdempotentAnnotationWrapper idempotentAnnotationWrapper;
        if (method.isAnnotationPresent(Idempotent.class)) {
            Idempotent idempotent = method.getAnnotation(Idempotent.class);
            if (StringUtils.isEmpty(idempotent.keyPrefix())) {
                throw new InvalidParamsException("幂等校验前缀不能为空");
            }
            idempotentAnnotationWrapper = IdempotentAnnotationWrapper.of(idempotent);
        } else {
            AgileIdempotentProperties.GlobalSetting globalSetting = properties.getGlobalSetting();
            Set<String> defaultMethodPrefixes = globalSetting.getDefaultMethodPrefixes();
            // 如果全局配置关闭，并且方法名以指定的前缀开头，则不进行拦截
            if (!globalSetting.isEnable() || defaultMethodPrefixes.stream().noneMatch(prefix -> method.getName().startsWith(prefix))) {
                // 调用目标方法
                return invocation.proceed();
            }
            idempotentAnnotationWrapper = IdempotentAnnotationWrapper.of(globalSetting);
        }

        if (CharSequenceUtil.isEmpty(idempotentAnnotationWrapper.getMessage())) {
            idempotentAnnotationWrapper.setMessage(properties.getMessage());
        }

        AtomicReference<Object> result = new AtomicReference<>();
        // 生成 Key
        final String idempotentKey = IdempotentKeyGenerator.generate(idempotentAnnotationWrapper, method, args);
        context.setIdempotentKey(idempotentKey);
        IdempotentProcessor idempotentProcessor = context.getIdempotentProcessor();
        idempotentProcessor.execute(idempotentKey, System.currentTimeMillis(), idempotentAnnotationWrapper.getTimeout(), idempotentAnnotationWrapper.getTimeUnit(), new SuccessFailureExecutor<>() {
            @Override
            @SneakyThrows
            public void success(Boolean success) {
                Try.run(() -> result.set(invocation.proceed())).getOrElseThrow(ex -> ex);
            }

            @Override
            public void failure(Boolean failure) {
                String message = idempotentAnnotationWrapper.getMessage();
                exception(new IdempotentException(message));
            }

            @Override
            @SneakyThrows
            public void exception(Exception exception) {
                throw exception;
            }

            @Override
            public void complete() {
                context.removeIdempotentKey();
            }
        });

        return result.get();
    }

    @Data
    public static class IdempotentAnnotationWrapper {
        /** 幂等的超时时间 */
        private int timeout = 500;
        /** 时间单位，默认为毫秒 */
        private TimeUnit timeUnit = TimeUnit.MILLISECONDS;
        /** 幂等前缀 */
        private String keyPrefix = "idempotent";
        /** key 分隔符 */
        private String delimiter = "|";
        /** 提示信息，正在执行中的提示 */
        private String message = "";

        private IdempotentAnnotationWrapper() {
            super();
        }

        public static IdempotentAnnotationWrapper of(Idempotent idempotent) {
            IdempotentAnnotationWrapper wrapper = new IdempotentAnnotationWrapper();
            wrapper.setTimeout(idempotent.timeout());
            wrapper.setTimeUnit(idempotent.timeUnit());
            wrapper.setKeyPrefix(idempotent.keyPrefix());
            wrapper.setDelimiter(idempotent.delimiter());
            wrapper.setMessage(idempotent.message());
            return wrapper;
        }

        public static IdempotentAnnotationWrapper of(AgileIdempotentProperties.GlobalSetting globalSetting) {
            IdempotentAnnotationWrapper wrapper = new IdempotentAnnotationWrapper();
            wrapper.setTimeout(globalSetting.getTimeout());
            wrapper.setTimeUnit(globalSetting.getTimeUnit());
            wrapper.setKeyPrefix(globalSetting.getKeyPrefix());
            wrapper.setDelimiter(globalSetting.getDelimiter());
            return wrapper;
        }
    }
}
