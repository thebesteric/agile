package io.github.thebesteric.framework.agile.test.service;

import io.github.thebesteric.framework.agile.commons.util.LoggerPrinter;
import io.github.thebesteric.framework.agile.core.AgileContext;
import io.github.thebesteric.framework.agile.plugins.logger.annotation.AgileLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * HelloService
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-03-13 18:24:07
 */
@Service
public class HelloService {

    @Autowired
    @Lazy
    private HelloService proxy;

    @AgileLogger
    public String foo(String name) {
        LoggerPrinter.info("executing foo");
        // 方式一：使用 helloService 代理对象进行调用，注意需要懒加载
        // return currentProxy.sayHello(name);

        // 方式二：使用 AgileContext 获取当前代理对象，进行调用
        HelloService proxy = AgileContext.currentProxy(HelloService.class);
        return proxy.sayHello(name);
    }

    @AgileLogger
    public String sayHello(String name) {
        return "Hello " + name;
    }

}
