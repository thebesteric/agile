package io.github.thebesteric.framework.agile.plugins.limiter.advisor;

import cn.hutool.core.text.CharSequenceUtil;
import io.github.thebesteric.framework.agile.commons.exception.InvalidParamsException;
import io.github.thebesteric.framework.agile.commons.util.NetUtils;
import io.github.thebesteric.framework.agile.commons.util.ReflectUtils;
import io.github.thebesteric.framework.agile.commons.util.StringUtils;
import io.github.thebesteric.framework.agile.core.func.SuccessFailureExecutor;
import io.github.thebesteric.framework.agile.plugins.limiter.RateLimitType;
import io.github.thebesteric.framework.agile.plugins.limiter.annotation.RateLimiter;
import io.github.thebesteric.framework.agile.plugins.limiter.config.AgileRateLimiterContext;
import io.github.thebesteric.framework.agile.plugins.limiter.exception.RateLimitException;
import io.github.thebesteric.framework.agile.plugins.limiter.processor.RateLimiterProcessor;
import io.vavr.control.Try;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

/**
 * AgileRateLimiterAdvice
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-05-06 17:34:46
 */
@RequiredArgsConstructor
public class AgileRateLimiterAdvice implements MethodInterceptor {

    private final AgileRateLimiterContext context;

    @Nullable
    @Override
    public Object invoke(@Nonnull MethodInvocation invocation) throws Throwable {

        if (!context.getProperties().isEnable()) {
            // 调用目标方法
            return invocation.proceed();
        }

        Method method = invocation.getMethod();
        RateLimiter rateLimiter = method.getAnnotation(RateLimiter.class);
        if (StringUtils.isEmpty(rateLimiter.keyPrefix())) {
            throw new InvalidParamsException("限流前缀不能为空");
        }

        AtomicReference<Object> result = new AtomicReference<>();
        // 生成 key
        final String key = generateKey(rateLimiter, method);
        RateLimiterProcessor rateLimiterProcessor = context.getRateLimiterProcessor();
        int timeout = rateLimiter.timeout();
        int count = rateLimiter.count();
        if (timeout <= 0 || count <= 0) {
            throw new InvalidParamsException("timeout and count must be great than 0");
        }

        rateLimiterProcessor.execute(key, timeout, count, rateLimiter.timeUnit(), new SuccessFailureExecutor<>() {
            @Override
            @SneakyThrows
            public void success(Boolean success) {
                Try.run(() -> result.set(invocation.proceed())).getOrElseThrow(ex -> ex);
            }

            @Override
            public void failure(Boolean failure) {
                String message = rateLimiter.message();
                if (CharSequenceUtil.isEmpty(message)) {
                    message = context.getProperties().getMessage();
                }
                exception(new RateLimitException(message));
            }

            @Override
            @SneakyThrows
            public void exception(Exception exception) {
                throw exception;
            }

            @Override
            public void complete() {
                SuccessFailureExecutor.super.complete();
            }
        });

        return result.get();
    }

    private String generateKey(RateLimiter rateLimiter, Method method) {
        StringBuilder key = new StringBuilder(rateLimiter.keyPrefix()).append(rateLimiter.delimiter());
        if (RateLimitType.IP == rateLimiter.type()) {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
                HttpServletRequest request = servletRequestAttributes.getRequest();
                String ip = NetUtils.getRequestIp(request);
                // 拼接 IP 地址
                key.append(ip).append(rateLimiter.delimiter());
            }
        }
        String methodSignature = ReflectUtils.methodSignature(method);
        // 拼接方法签名
        key.append(methodSignature);
        return key.toString();
    }
}
