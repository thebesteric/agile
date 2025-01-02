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
    private Class<? extends Throwable> defaultExceptionClass;

    private final List<Throwable> exceptions = new CopyOnWriteArrayList<>();

    private DataValidator(Class<? extends Throwable> exClass, ExceptionThrowStrategy exceptionThrowStrategy) {
        this.defaultExceptionClass = exClass == null ? Throwable.class : exClass;
        this.exceptionThrowStrategy = exceptionThrowStrategy;
    }

    public static DataValidator create() {
        return newInstance(null, ExceptionThrowStrategy.IMMEDIATELY);
    }

    public static DataValidator create(ExceptionThrowStrategy exceptionThrowStrategy) {
        return newInstance(null, exceptionThrowStrategy);
    }

    public static DataValidator create(Class<? extends Throwable> defaultExceptionClass) {
        return newInstance(defaultExceptionClass, ExceptionThrowStrategy.IMMEDIATELY);
    }

    public static DataValidator create(Class<? extends Throwable> defaultExceptionClass, ExceptionThrowStrategy exceptionThrowStrategy) {
        return newInstance(defaultExceptionClass, exceptionThrowStrategy);
    }

    private static DataValidator newInstance(Class<? extends Throwable> exClass, ExceptionThrowStrategy exceptionThrowStrategy) {
        return new DataValidator(exClass, exceptionThrowStrategy);
    }

    @SneakyThrows
    public DataValidator validate(boolean condition, String message) {
        for (Constructor<?> declaredConstructor : this.defaultExceptionClass.getDeclaredConstructors()) {
            if (declaredConstructor.getParameterCount() == 1 && declaredConstructor.getParameterTypes()[0] == String.class) {
                Throwable ex = (Throwable) declaredConstructor.newInstance(message);
                return validate(condition, ex);
            }
        }
        return validate(condition);
    }

    @SneakyThrows
    public DataValidator validate(boolean condition) {
        Throwable ex = this.defaultExceptionClass.getDeclaredConstructor().newInstance();
        return validate(condition, ex);
    }

    public <E extends Throwable> DataValidator validate(Supplier<E> supplier) {
        E ex = supplier.get();
        return validate(ex != null, ex);
    }


    @SneakyThrows
    public <E extends Throwable> DataValidator validate(boolean condition, E ex) {
        if (condition) {
            if (this.isThrowImmediately()) {
                throw ex;
            }
            this.exceptions.add(ex);
        }
        return this;
    }

    public void addException(Throwable throwable) {
        this.exceptions.add(throwable);
    }

    public void clearExceptions() {
        this.exceptions.clear();
    }

    @SneakyThrows
    public void throwException() {
        Optional<? extends Throwable> optional = this.exceptions.stream().findFirst();
        if (optional.isPresent()) {
            throw optional.get();
        }
    }

    @SneakyThrows
    public void throwException(Class<? extends Throwable> exClass) {
        Optional<? extends Throwable> optional = this.exceptions.stream().filter(e -> e.getClass() == exClass).findFirst();
        if (optional.isPresent()) {
            throw optional.get();
        }
    }

    public boolean isThrowImmediately() {
        return this.exceptionThrowStrategy == null || ExceptionThrowStrategy.IMMEDIATELY == this.exceptionThrowStrategy;
    }

    public enum ExceptionThrowStrategy {
        IMMEDIATELY, COLLECT
    }

}