package io.github.thebesteric.framework.agile.commons.util;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 执行流程规范工具
 *
 * @author wangweijun
 * @since 2024/12/26 14:51
 */
@Slf4j
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

    public Processor<T> validate(Consumer<T> consumer) {
        Try.run(() -> consumer.accept(this.object)).onFailure(e -> dataValidator.validate(true, new RuntimeException(e)));
        return this;
    }

    public <R> Processor<R> next(Supplier<R> supplier) {
        return this.next(t -> supplier.get());
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
        List<RuntimeException> exceptions = dataValidator.getExceptions();
        if (!exceptions.isEmpty() && exceptionListeners.isEmpty()) {
            LoggerPrinter.warn(log, "Has some exceptions, but not can not find any exception listener");
        }
        for (Predicate<List<RuntimeException>> exceptionListener : exceptionListeners) {
            if (!exceptionListener.test(exceptions)) {
                break;
            }
        }
    }

}