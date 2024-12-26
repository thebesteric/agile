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
@Getter
public class DataValidator {

    @Setter
    private ExceptionThrowStrategy exceptionThrowStrategy;

    @Setter
    private Class<? extends RuntimeException> defaultExceptionClass;

    private final List<RuntimeException> exceptions = new CopyOnWriteArrayList<>();

    private DataValidator(Class<? extends RuntimeException> exClass, ExceptionThrowStrategy exceptionThrowStrategy) {
        this.defaultExceptionClass = exClass == null ? RuntimeException.class : exClass;
        this.exceptionThrowStrategy = exceptionThrowStrategy;
    }

    public static DataValidator create() {
        return newInstance(null, ExceptionThrowStrategy.IMMEDIATELY);
    }

    public static DataValidator create(ExceptionThrowStrategy exceptionThrowStrategy) {
        return newInstance(null, exceptionThrowStrategy);
    }

    public static DataValidator create(Class<? extends RuntimeException> defaultExceptionClass) {
        return newInstance(defaultExceptionClass, ExceptionThrowStrategy.IMMEDIATELY);
    }

    public static DataValidator create(Class<? extends RuntimeException> defaultExceptionClass, ExceptionThrowStrategy exceptionThrowStrategy) {
        return newInstance(defaultExceptionClass, exceptionThrowStrategy);
    }

    private static DataValidator newInstance(Class<? extends RuntimeException> exClass, ExceptionThrowStrategy exceptionThrowStrategy) {
        return new DataValidator(exClass, exceptionThrowStrategy);
    }

    @SneakyThrows
    public DataValidator validate(boolean condition, String message) {
        for (Constructor<?> declaredConstructor : defaultExceptionClass.getDeclaredConstructors()) {
            if (declaredConstructor.getParameterCount() == 1 && declaredConstructor.getParameterTypes()[0] == String.class) {
                RuntimeException ex = (RuntimeException) declaredConstructor.newInstance(message);
                return validate(condition, ex);
            }
        }
        return validate(condition);
    }

    @SneakyThrows
    public DataValidator validate(boolean condition) {
        RuntimeException ex = defaultExceptionClass.getDeclaredConstructor().newInstance();
        return validate(condition, ex);
    }

    public <E extends RuntimeException> DataValidator validate(Supplier<E> supplier) {
        E ex = supplier.get();
        return validate(ex != null, ex);
    }

    public <E extends RuntimeException> DataValidator validate(boolean condition, E ex) {
        if (condition) {
            if (this.isThrowImmediately()) {
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

    public boolean isThrowImmediately() {
        return exceptionThrowStrategy == null || ExceptionThrowStrategy.IMMEDIATELY == exceptionThrowStrategy;
    }

    public enum ExceptionThrowStrategy {
        IMMEDIATELY, COLLECT
    }

}