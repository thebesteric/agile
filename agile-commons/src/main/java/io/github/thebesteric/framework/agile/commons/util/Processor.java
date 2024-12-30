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
    private final Class<? extends Throwable> defaultExceptionClass;
    private final DataValidator.ExceptionThrowStrategy exceptionThrowStrategy;
    private final DataValidator dataValidator;
    private final List<Predicate<List<Throwable>>> exceptionListeners;


    private <E extends Throwable> Processor(Class<E> defaultExceptionClass, DataValidator.ExceptionThrowStrategy exceptionThrowStrategy, List<Predicate<List<Throwable>>> exceptionListeners) {
        this.defaultExceptionClass = defaultExceptionClass;
        this.exceptionThrowStrategy = exceptionThrowStrategy;
        this.exceptionListeners = exceptionListeners != null ? exceptionListeners : new ArrayList<>();
        this.dataValidator = DataValidator.create(defaultExceptionClass, exceptionThrowStrategy);
    }

    public static <T> Processor<T> prepare() {
        return Processor.prepare(DataValidator.ExceptionThrowStrategy.IMMEDIATELY);
    }

    public static <T> Processor<T> prepare(DataValidator.ExceptionThrowStrategy exceptionThrowStrategy) {
        return Processor.prepare(Throwable.class, exceptionThrowStrategy);
    }

    public static <T, E extends Throwable> Processor<T> prepare(Class<E> defaultExceptionClass, DataValidator.ExceptionThrowStrategy exceptionThrowStrategy) {
        return new Processor<>(defaultExceptionClass, exceptionThrowStrategy, null);
    }

    public <R> Processor<R> start(Supplier<R> supplier) {
        return this.next(supplier);
    }

    public <E extends Throwable> Processor<T> validate(boolean condition, Throwable ex) {
        dataValidator.validate(condition, ex);
        return this;
    }

    public Processor<T> validate(boolean condition, String errorMessage) {
        dataValidator.validate(condition, errorMessage);
        return this;
    }

    public Processor<T> validate(Consumer<T> consumer) {
        Try.run(() -> consumer.accept(this.object)).onFailure(e -> dataValidator.validate(true, e));
        return this;
    }

    public <R> Processor<R> next(Supplier<R> supplier) {
        return this.next(t -> supplier.get());
    }

    public Processor<T> next(Runnable runnable) {
        return this.next(() -> {
            runnable.run();
            return this.object;
        });
    }

    public <R> Processor<R> next(Function<T, R> function) {
        R r = function.apply(this.object);
        Processor<R> processor = new Processor<>(this.defaultExceptionClass, this.exceptionThrowStrategy, this.exceptionListeners);
        processor.object = r;
        return processor;
    }

    public <R> R complete(Supplier<R> supplier) {
        return this.complete(t -> {
            return supplier.get();
        });
    }

    public void complete(Runnable runnable) {
        this.complete(t -> {
            runnable.run();
        });
    }

    public void complete(Consumer<T> consumer) {
        List<Throwable> exceptions = dataValidator.getExceptions();
        if (CollectionUtils.isEmpty(exceptions)) {
            Try.run(() -> consumer.accept(this.object))
                    .onFailure(exceptions::add);
        }
        throwExceptionsIfNecessary();
    }

    public <R> R complete(Function<T, R> function) {
        List<Throwable> exceptions = dataValidator.getExceptions();
        R result = null;
        if (CollectionUtils.isEmpty(exceptions)) {
            result = Try.of(() -> function.apply(this.object))
                    .onFailure(exceptions::add)
                    .get();
        }
        throwExceptionsIfNecessary();
        return result;
    }

    public Processor<T> registerExceptionListener(Predicate<List<Throwable>> exceptionListener) {
        this.exceptionListeners.add(exceptionListener);
        return this;
    }

    public <E extends Throwable> Processor<T> setDefaultExceptionClass(Class<E> defaultExceptionClass) {
        this.dataValidator.setDefaultExceptionClass(defaultExceptionClass);
        return this;
    }

    private void throwExceptionsIfNecessary() {
        if (this.dataValidator.isThrowImmediately()) {
            this.dataValidator.throwException();
        }
        List<Throwable> exceptions = this.dataValidator.getExceptions();
        if (!exceptions.isEmpty() && this.exceptionListeners.isEmpty()) {
            LoggerPrinter.warn(log, "Has some exceptions, but not can not find any exception listener");
        }
        for (Predicate<List<Throwable>> exceptionListener : this.exceptionListeners) {
            if (!exceptionListener.test(exceptions)) {
                break;
            }
        }
    }

}