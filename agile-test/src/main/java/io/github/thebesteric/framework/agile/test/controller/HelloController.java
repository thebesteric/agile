package io.github.thebesteric.framework.agile.test.controller;

import io.github.thebesteric.framework.agile.commons.util.LoggerPrinter;
import io.github.thebesteric.framework.agile.core.domain.R;
import io.github.thebesteric.framework.agile.distributed.locks.annotation.DistributedLock;
import io.github.thebesteric.framework.agile.plugins.annotation.scanner.AnnotationParasiticContext;
import io.github.thebesteric.framework.agile.plugins.idempotent.annotation.Idempotent;
import io.github.thebesteric.framework.agile.plugins.idempotent.annotation.IdempotentKey;
import io.github.thebesteric.framework.agile.plugins.idempotent.config.AgileIdempotentContext;
import io.github.thebesteric.framework.agile.plugins.limiter.annotation.RateLimiter;
import io.github.thebesteric.framework.agile.plugins.logger.annotation.AgileLogger;
import io.github.thebesteric.framework.agile.plugins.workflow.WorkflowEngine;
import io.github.thebesteric.framework.agile.test.domain.Id2Vo;
import io.github.thebesteric.framework.agile.test.service.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * HelloController
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-03-13 18:23:26
 */
@RestController
@RequestMapping("/hello")
@AgileLogger(tag = "hello")
public class HelloController {

    @Autowired
    HelloService helloService;

    @Autowired
    WorkflowEngine workflowEngine;

    @Autowired
    AnnotationParasiticContext parasiticContext;

    @Autowired
    AgileIdempotentContext idempotentContext;

    @GetMapping("/parasitic")
    public R<Map<String, List<String>>> parasitic() {
        Map<String, List<String>> map = Map.of(
                "@CrossOrigin", parasiticContext.get(CrossOrigin.class).stream().map(p -> p.getClazz().getName() + "#" + p.getMethod().getName()).toList(),
                "@RestController", parasiticContext.get(RestController.class).stream().map(p -> p.getClazz().getName()).toList());
        return R.success(map);
    }

    @GetMapping("/ex")
    public R<Void> ex() {
        int i = 1/0;
        return R.success();
    }

    @GetMapping("/ex2")
    public R<Void> ex2() {
        throw new RuntimeException("ex2");
    }

    @CrossOrigin
    @GetMapping("/foo")
    public R<String> foo(String name) {
        LoggerPrinter.info("entering foo");
        return R.success(helloService.foo(name));
    }

    @AgileLogger
    @PostMapping("/bar")
    public R<Map<String, Object>> bar(@RequestBody Map<String, Object> body) {
        return R.success(body);
    }

    @PostMapping("/upload")
    public String upload(@RequestParam(value = "file") MultipartFile[] files) {
        return "success";
    }

    @GetMapping("/id1")
    @Idempotent(timeout = 15000, message = "id1 idempotent error")
    public R<String> id1(@IdempotentKey String name, @IdempotentKey Integer age) {
        return R.success(name + "-" + age);
    }

    @PostMapping("/id2")
    @Idempotent(timeout = 10000)
    public R<Id2Vo> id2(@RequestBody Id2Vo id2Vo) {
        System.out.println("IdempotentKey = " + idempotentContext.getIdempotentKey());
        return R.success(id2Vo);
    }

    @GetMapping("/save")
    public R<Id2Vo> save() {
        return R.success();
    }

    @GetMapping("/updateUser")
    public R<Id2Vo> updateUser() {
        return R.success();
    }

    @GetMapping("/test")
    public R<Id2Vo> test() {
        return R.success();
    }

    @GetMapping("/limit")
    @RateLimiter(timeout = 10, count = 10)
    public R<Id2Vo> limit() {
        return R.success();
    }

    @PostMapping("/lock")
    @DistributedLock(key = "abc + #params.name + #params.id", waitTime = 5, message = "加锁失败咯")
    public R<String> lock(@RequestBody Map<String, Object> params) throws InterruptedException {
        TimeUnit.SECONDS.sleep(10);
        return R.success();
    }

}
