package io.github.thebesteric.framework.agile.test.mock;

import io.github.thebesteric.framework.agile.core.domain.R;
import io.github.thebesteric.framework.agile.plugins.mocker.mocker.Mocker;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * MyMocker
 *
 * @author wangweijun
 * @version v1.0
 * @since 2025-01-24 16:35:22
 */
@Component
public class MyMocker implements Mocker<Object> {
    @Override
    public Object mock(Method method, Parameter[] parameters, Object[] arguments) {
        if (method.getName().equals("method1")) {
            return mockMethod1();
        }
        if (method.getName().equals("method5")) {
            return mockMethod5();
        }
        return null;
    }

    public R<String> mockMethod1() {
        return R.success("xxxxxxxxxxxx");
    }

    public String mockMethod5() {
        return "hello eric";
    }
}
