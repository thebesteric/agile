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
    private boolean throwImmediately;

    @Setter
    private Class<? extends RuntimeException> defaultExceptionClass;

    private final List<RuntimeException> exceptions = new CopyOnWriteArrayList<>();

    private DataValidator(Class<? extends RuntimeException> exClass, boolean throwImmediately) {
        this.defaultExceptionClass = exClass == null ? RuntimeException.class : exClass;
        this.throwImmediately = throwImmediately;
    }

    public static DataValidator create() {
        return newInstance(null, true);
    }

    public static DataValidator create(boolean throwImmediately) {
        return newInstance(null, throwImmediately);
    }

    public static DataValidator create(Class<? extends RuntimeException> defaultExceptionClass) {
        return newInstance(defaultExceptionClass, true);
    }

    public static DataValidator create(Class<? extends RuntimeException> defaultExceptionClass, boolean throwImmediately) {
        return newInstance(defaultExceptionClass, throwImmediately);
    }

    private static DataValidator newInstance(Class<? extends RuntimeException> exClass, boolean throwImmediately) {
        return new DataValidator(exClass, throwImmediately);
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