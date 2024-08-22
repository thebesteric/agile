package io.github.thebesteric.framework.agile.test.config;

import io.github.thebesteric.framework.agile.plugins.idempotent.exception.IdempotentException;
import io.github.thebesteric.framework.agile.plugins.limiter.exception.RateLimitException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * GlobalExceptionHandler
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-03-20 16:57:04
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler({IdempotentException.class, RateLimitException.class})
    public Map<String, Object> idempotentException(Exception e) {
        return Map.of("code", 400, "message", e.getMessage());
    }

}
