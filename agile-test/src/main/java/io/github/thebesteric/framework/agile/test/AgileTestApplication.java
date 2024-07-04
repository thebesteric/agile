package io.github.thebesteric.framework.agile.test;

import io.github.thebesteric.framework.agile.starter.annotaion.EnableAgile;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableAgile
@SpringBootApplication
@Configuration
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableTransactionManagement
public class AgileTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgileTestApplication.class, args);
    }

}
