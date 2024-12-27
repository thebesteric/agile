package io.github.thebesteric.framework.agile.commons.util;

import io.vavr.control.Try;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.*;

/**
 * 执行流程规范工具
 *
 * @author wangweijun
 * @since 2024/12/26 14:51
 */
public class Processor<T> {

    private T object;
    private final Class<T> objectType;
    private final Class<? extends RuntimeException> defaultExceptionClass;
    private final DataValidator.ExceptionThrowStrategy exceptionThrowStrategy;
    private final DataValidator dataValidator;
    private final List<Predicate<List<RuntimeException>>> exceptionListeners = new ArrayList<>();


    private <E extends RuntimeException> Processor(Class<T> initialObjectType, Class<E> defaultExceptionClass, DataValidator.ExceptionThrowStrategy exceptionThrowStrategy) {
        this.objectType = initialObjectType;
        this.defaultExceptionClass = defaultExceptionClass;
        this.exceptionThrowStrategy = exceptionThrowStrategy;
        this.dataValidator = DataValidator.create(defaultExceptionClass, exceptionThrowStrategy);
    }

    public static <T> Processor<T> prepare(Class<T> initialObjectType) {
        return Processor.prepare(initialObjectType, DataValidator.ExceptionThrowStrategy.IMMEDIATELY);
    }

    public static <T> Processor<T> prepare(Class<T> initialObjectType, DataValidator.ExceptionThrowStrategy exceptionThrowStrategy) {
        return Processor.prepare(initialObjectType, RuntimeException.class, exceptionThrowStrategy);
    }

    public static <T, E extends RuntimeException> Processor<T> prepare(Class<T> initialObjectType, Class<E> defaultExceptionClass, DataValidator.ExceptionThrowStrategy exceptionThrowStrategy) {
        return new Processor<>(initialObjectType, defaultExceptionClass, exceptionThrowStrategy);
    }

    public Processor<T> start(Supplier<T> supplier) {
        this.object = supplier.get();
        return this;
    }

    public <E extends RuntimeException> Processor<T> validate(boolean condition, E ex) {
        dataValidator.validate(condition, ex);
        return this;
    }

    public Processor<T> validate(boolean condition, String errorMessage) {
        dataValidator.validate(condition, errorMessage);
        return this;
    }

    public <E extends RuntimeException> Processor<T> validate(Function<T, E> function) {
        E ex = function.apply(this.object);
        dataValidator.validate(ex != null, ex);
        return this;
    }

    public Processor<T> next(Supplier<T> supplier) {
        return start(supplier);
    }

    @SuppressWarnings("unchecked")
    public <R> Processor<R> next(Function<T, R> function) {
        R r = function.apply(this.object);
        Class<R> changedObjectType = (Class<R>) r.getClass();
        Processor<R> processor = new Processor<>(changedObjectType, this.defaultExceptionClass, this.exceptionThrowStrategy);
        processor.object = r;
        return processor;
    }

    public void complete(Consumer<T> consumer) {
        List<RuntimeException> exceptions = dataValidator.getExceptions();
        if (CollectionUtils.isEmpty(exceptions)) {
            Try.run(() -> consumer.accept(this.object))
                    .onFailure(e -> exceptions.add(new RuntimeException(e)));
        }
        throwExceptionsIfNecessary();
    }

    public <R> R complete(Function<T, R> function) {
        List<RuntimeException> exceptions = dataValidator.getExceptions();
        R result = null;
        if (CollectionUtils.isEmpty(exceptions)) {
            result = Try.of(() -> function.apply(this.object))
                    .onFailure(e -> exceptions.add(new RuntimeException(e)))
                    .get();
        }
        throwExceptionsIfNecessary();
        return result;
    }

    public Processor<T> registerExceptionListener(Predicate<List<RuntimeException>> exceptionListener) {
        exceptionListeners.add(exceptionListener);
        return this;
    }

    public <E extends RuntimeException> Processor<T> setDefaultExceptionClass(Class<E> defaultExceptionClass) {
        dataValidator.setDefaultExceptionClass(defaultExceptionClass);
        return this;
    }

    private void throwExceptionsIfNecessary() {
        if (dataValidator.isThrowImmediately()) {
            dataValidator.throwException();
        }
        for (Predicate<List<RuntimeException>> exceptionListener : exceptionListeners) {
            if (!exceptionListener.test(dataValidator.getExceptions())) {
                break;
            }
        }
    }

}