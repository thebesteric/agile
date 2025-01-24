package io.github.thebesteric.framework.agile.plugins.mocker.mocker;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Mocker
 *
 * @author wangweijun
 * @version v1.0
 * @since 2025-01-24 10:58:12
 */
@FunctionalInterface
public interface Mocker<T> {

    /**
     * mock
     *
     * @param method     目标方法
     * @param parameters 参数
     * @param arguments  值
     *
     * @return T
     *
     * @author wangweijun
     * @since 2025/1/24 11:02
     */
    T mock(Method method, Parameter[] parameters, Object[] arguments);

}
