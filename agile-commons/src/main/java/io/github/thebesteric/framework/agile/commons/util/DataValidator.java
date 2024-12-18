package io.github.thebesteric.framework.agile.commons.util;

import lombok.Getter;
import lombok.SneakyThrows;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 数据校验器
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-12-18 10:38:40
 */
public class DataValidator extends AbstractUtils {

    public static Builder create() {
        return newInstance(null);
    }

    public static Builder create(Class<? extends RuntimeException> defaultExceptionClass) {
        return newInstance(defaultExceptionClass);
    }

    private static Builder newInstance(Class<? extends RuntimeException> exClass) {
        return new Builder(exClass);
    }

    @Getter
    public static class Builder {
        
        private final Class<? extends RuntimeException> defaultExceptionClass;
        private final List<RuntimeException> exceptions = new CopyOnWriteArrayList<>();

        public Builder(Class<? extends RuntimeException> exClass) {
            this.defaultExceptionClass = exClass == null ? RuntimeException.class : exClass;
        }

        @SneakyThrows
        public DataValidator.Builder validate(boolean validated, String message) {
            for (Constructor<?> declaredConstructor : defaultExceptionClass.getDeclaredConstructors()) {
                if (declaredConstructor.getParameterCount() == 1 && declaredConstructor.getParameterTypes()[0] == String.class) {
                    RuntimeException ex = (RuntimeException) declaredConstructor.newInstance(message);
                    return validate(validated, ex);
                }
            }
            return validate(validated);
        }

        @SneakyThrows
        public DataValidator.Builder validate(boolean validated) {
            RuntimeException ex = defaultExceptionClass.getDeclaredConstructor().newInstance();
            return validate(validated, ex);
        }

        public <E extends RuntimeException> DataValidator.Builder validate(boolean validated, E ex) {
            if (!validated) {
                exceptions.add(ex);
            }
            return this;
        }

        public void throwException() {
            Optional<? extends RuntimeException> optional = exceptions.stream().findFirst();
            if (optional.isPresent()) {
                throw optional.get();
            }
        }

        public void throwException(Class<? extends Throwable> exClass) {
            Optional<? extends RuntimeException> optional = exceptions.stream().filter(e -> e.getClass() == exClass).findFirst();
            if (optional.isPresent()) {
                throw optional.get();
            }
        }
    }

}