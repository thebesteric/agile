package io.github.thebesteric.framework.agile.test.controller;

import io.github.thebesteric.framework.agile.core.domain.R;
import io.github.thebesteric.framework.agile.plugins.idempotent.annotation.Idempotent;
import io.github.thebesteric.framework.agile.plugins.idempotent.annotation.IdempotentKey;
import io.github.thebesteric.framework.agile.plugins.logger.annotation.AgileLogger;
import io.github.thebesteric.framework.agile.test.domain.Id2Vo;
import io.github.thebesteric.framework.agile.test.service.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * HelloController
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-03-13 18:23:26
 */
@RestController
@RequestMapping("/hello")
@AgileLogger
public class HelloController {

    @Autowired
    HelloService helloService;

    @CrossOrigin
    @GetMapping("/foo")
    public R<String> foo(String name) {
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
    @Idempotent(timeout = 10000)
    public R<String> id1(@IdempotentKey String name, @IdempotentKey Integer age) {
        return R.success(name + "-" + age);
    }

    @PostMapping("/id2")
    @Idempotent(timeout = 10000)
    public R<Id2Vo> id2(@RequestBody Id2Vo id2Vo) {
        return R.success(id2Vo);
    }

}
