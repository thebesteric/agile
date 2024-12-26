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
    private final DataValidator dataValidator;
    private final List<Predicate<List<RuntimeException>>> exceptionListeners = new ArrayList<>();


    private <E extends RuntimeException> Processor(Class<E> defaultExceptionClass, DataValidator.ExceptionThrowStrategy exceptionThrowStrategy) {
        dataValidator = DataValidator.create(defaultExceptionClass, exceptionThrowStrategy);
    }

    public static <T> Processor<T> prepare() {
        return io.github.thebesteric.framework.agile.commons.util.Processor.prepare(DataValidator.ExceptionThrowStrategy.IMMEDIATELY);
    }

    public static <T> Processor<T> prepare(DataValidator.ExceptionThrowStrategy exceptionThrowStrategy) {
        return Processor.prepare(RuntimeException.class, exceptionThrowStrategy);
    }

    public static <T, E extends RuntimeException> Processor<T> prepare(Class<E> defaultExceptionClass, DataValidator.ExceptionThrowStrategy exceptionThrowStrategy) {
        return new Processor<>(defaultExceptionClass, exceptionThrowStrategy);
    }

    public Processor<T> registerExceptionListener(Predicate<List<RuntimeException>> exceptionListener) {
        exceptionListeners.add(exceptionListener);
        return this;
    }

    public <E extends RuntimeException> Processor<T> setDefaultExceptionClass(Class<E> defaultExceptionClass) {
        dataValidator.setDefaultExceptionClass(defaultExceptionClass);
        return this;
    }

    public Processor<T> start(Supplier<T> supplier) {
        this.object = supplier.get();
        return this;
    }

    public Processor<T> next(Supplier<T> supplier) {
        return start(supplier);
    }

    public Processor<T> next(UnaryOperator<T> unary) {
        this.object = unary.apply(this.object);
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

    public void complete(Consumer<T> consumer) {
        List<RuntimeException> exceptions = dataValidator.getExceptions();
        if (CollectionUtils.isEmpty(exceptions)) {
            Try.run(() -> consumer.accept(this.object))
                    .onFailure(e -> exceptions.add(new RuntimeException(e)));
        }
        throwExceptionsIfNecessary();
    }

    public T complete(UnaryOperator<T> unary) {
        List<RuntimeException> exceptions = dataValidator.getExceptions();
        T result = null;
        if (CollectionUtils.isEmpty(exceptions)) {
            result = Try.of(() -> unary.apply(this.object))
                    .onFailure(e -> exceptions.add(new RuntimeException(e)))
                    .get();
        }
        throwExceptionsIfNecessary();
        return result;
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