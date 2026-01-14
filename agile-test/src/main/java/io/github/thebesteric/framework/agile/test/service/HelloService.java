package io.github.thebesteric.framework.agile.test.service;

import io.github.thebesteric.framework.agile.commons.util.LoggerPrinter;
import io.github.thebesteric.framework.agile.core.AgileContext;
import io.github.thebesteric.framework.agile.plugins.logger.annotation.AgileLogger;
import io.github.thebesteric.framework.agile.test.domain.FileVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * HelloService
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-03-13 18:24:07
 */
@AgileLogger
@Service
public class HelloService {

    private static final LoggerPrinter loggerPrinter = LoggerPrinter.newInstance();

    @Autowired
    @Lazy
    private HelloService proxy;

    public String foo(String name) {
        loggerPrinter.info("executing foo");
        // 方式一：使用 helloService 代理对象进行调用，注意需要懒加载
        // return proxy.sayHello(name);

        // 方式二：使用 AgileContext 获取当前代理对象，进行调用
        HelloService proxy = AgileContext.currentProxy(HelloService.class);
        return proxy.sayHello(name);
    }

    public String sayHello(String name) {
        return "Hello " + name;
    }

    public FileVo parseFile(MultipartFile file) {
        FileVo fileVo = new FileVo();
        fileVo.setFileName(file.getOriginalFilename());
        fileVo.setResource(file.getResource());
        return fileVo;
    }


}
