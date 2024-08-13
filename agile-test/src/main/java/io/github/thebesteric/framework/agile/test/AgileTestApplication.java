package io.github.thebesteric.framework.agile.test;

import io.github.thebesteric.framework.agile.plugins.annotation.scanner.AnnotationParasiticContext;
import io.github.thebesteric.framework.agile.plugins.annotation.scanner.domain.Parasitic;
import io.github.thebesteric.framework.agile.starter.annotaion.EnableAgile;
import jakarta.annotation.Resource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@EnableAgile
@SpringBootApplication
@Configuration
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableTransactionManagement
public class AgileTestApplication implements CommandLineRunner {

    @Resource
    @Lazy
    private AnnotationParasiticContext annotationParasiticContext;

    public static void main(String[] args) {
        SpringApplication.run(AgileTestApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        List<Parasitic> parasitics = annotationParasiticContext.get(RestController.class);
        System.out.println(parasitics);
    }
}
