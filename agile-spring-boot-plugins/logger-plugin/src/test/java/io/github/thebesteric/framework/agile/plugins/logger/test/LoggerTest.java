package io.github.thebesteric.framework.agile.plugins.logger.test;

import io.github.thebesteric.framework.agile.plugins.logger.annotation.AgileLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * SpringTest
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-03-07 15:18:08
 */
@Configuration
@ComponentScan("io.github.thebesteric.framework.agile.plugins.logger")
public class LoggerTest {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(LoggerTest.class);
        TestController testController = ctx.getBean(TestController.class);
        System.out.println(testController.hello());
    }

    @Service
    @AgileLogger
    // @IgnoreMethods("\\bhel\\w*")
    public static class TestService {

        // @AgileLogger
        // @IgnoreMethod
        public String hello() {
            return generate();
        }

        public String generate() {
            return "Hello";
        }
    }

    @RestController
    public static class TestController {

        @Autowired
        TestService testService;

        @AgileLogger
        @GetMapping
        public String hello() {
            return testService.hello();
        }

    }
}
