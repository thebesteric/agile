package io.github.thebesteric.framework.agile.plugins.mocker.mocker;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * EmptyMocker
 *
 * @author wangweijun
 * @version v1.0
 * @since 2025-01-24 13:34:11
 */
public class EmptyMocker implements Mocker<Object> {
    @Override
    public Object mock(Method method, Parameter[] parameters, Object[] arguments) {
        return null;
    }
}
