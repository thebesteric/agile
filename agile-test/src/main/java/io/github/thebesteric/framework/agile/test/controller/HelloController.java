package io.github.thebesteric.framework.agile.test.controller;

import io.github.thebesteric.framework.agile.plugins.logger.annotation.AgileLogger;
import io.github.thebesteric.framework.agile.test.service.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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


    @GetMapping("/foo")
    public Map<String, Object> foo(String name) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", helloService.foo(name));
        result.put("message", "success");
        return result;
    }

    @AgileLogger
    @PostMapping("/bar")
    public Map<String, Object> bar(@RequestBody Map<String, Object> body) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", body);
        result.put("message", "success");
        return result;
    }

}
