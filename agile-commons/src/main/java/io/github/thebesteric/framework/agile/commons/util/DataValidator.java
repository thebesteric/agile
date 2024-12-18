package io.github.thebesteric.framework.agile.commons.util;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

/**
 * 数据校验器
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-12-18 10:38:40
 */
public class DataValidator extends AbstractUtils {

    public static Builder create() {
        return newInstance(null, true);
    }

    public static Builder create(boolean throwImmediately) {
        return newInstance(null, throwImmediately);
    }

    public static Builder create(Class<? extends RuntimeException> defaultExceptionClass) {
        return newInstance(defaultExceptionClass, true);
    }

    public static Builder create(Class<? extends RuntimeException> defaultExceptionClass, boolean throwImmediately) {
        return newInstance(defaultExceptionClass, throwImmediately);
    }

    private static Builder newInstance(Class<? extends RuntimeException> exClass, boolean throwImmediately) {
        return new Builder(exClass, throwImmediately);
    }

    @Getter
    public static class Builder {
        @Setter
        private boolean throwImmediately;
        private final Class<? extends RuntimeException> defaultExceptionClass;
        private final List<RuntimeException> exceptions = new CopyOnWriteArrayList<>();

        public Builder(Class<? extends RuntimeException> exClass, boolean throwImmediately) {
            this.defaultExceptionClass = exClass == null ? RuntimeException.class : exClass;
            this.throwImmediately = throwImmediately;
        }

        @SneakyThrows
        public DataValidator.Builder validate(boolean condition, String message) {
            for (Constructor<?> declaredConstructor : defaultExceptionClass.getDeclaredConstructors()) {
                if (declaredConstructor.getParameterCount() == 1 && declaredConstructor.getParameterTypes()[0] == String.class) {
                    RuntimeException ex = (RuntimeException) declaredConstructor.newInstance(message);
                    return validate(condition, ex);
                }
            }
            return validate(condition);
        }

        @SneakyThrows
        public DataValidator.Builder validate(boolean condition) {
            RuntimeException ex = defaultExceptionClass.getDeclaredConstructor().newInstance();
            return validate(condition, ex);
        }

        public <E extends RuntimeException> DataValidator.Builder validate(Supplier<E> supplier) {
            E ex = supplier.get();
            return validate(ex != null, ex);
        }

        public <E extends RuntimeException> DataValidator.Builder validate(boolean condition, E ex) {
            if (condition) {
                if (throwImmediately) {
                    throw ex;
                }
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