package io.github.thebesteric.framework.agile.test.controller;

import io.github.thebesteric.framework.agile.core.domain.R;
import io.github.thebesteric.framework.agile.plugins.logger.annotation.AgileLogger;
import io.github.thebesteric.framework.agile.plugins.mocker.annotation.Mock;
import io.github.thebesteric.framework.agile.plugins.mocker.mocker.MockType;
import io.github.thebesteric.framework.agile.test.mock.MyMocker;
import io.github.thebesteric.framework.agile.test.service.MockService;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

/**
 * MockerController
 *
 * @author wangweijun
 * @version v1.0
 * @since 2025-01-23 18:10:10
 */
@RestController
@RequestMapping("/mocker")
@AgileLogger(tag = "hello")
public class MockerController {

    @Resource
    private MockService mockService;

    @Mock(condition = "(#parent.id == 1 || #parent.sub.name == lisi) && #name == zs", type = MockType.CLASS, targetClass = MyMocker.class)
    @AgileLogger
    @PostMapping("/method1")
    public R<String> method1(@RequestParam(required = false) String name, @RequestBody Parent parent) {
        return R.success(name);
    }

    @Mock(condition = "(#parent.id == 1 || #parent.sub.name == lisi) && #name == zs", type = MockType.FILE, path = "classpath:mock/mock-method2.json")
    @AgileLogger
    @PostMapping("/method2")
    public R<String> method2(@RequestParam(required = false) String name, @RequestBody Parent parent) {
        return R.success(name);
    }

    @Mock(condition = "(#parent.id == 1 || #parent.sub.name == lisi) && #name == zs", type = MockType.FILE, path = "file:/Users/wangweijun/Downloads/mock-file.json")
    @AgileLogger
    @PostMapping("/method3")
    public R<String> method3(@RequestParam(required = false) String name, @RequestBody Parent parent) {
        return R.success(name);
    }

    @Mock(condition = "(#parent.id == 1 || #parent.sub.name == lisi) && #name == zs", type = MockType.URL, path = "http://127.0.0.1:8080/mocker/test")
    @AgileLogger
    @PostMapping("/method4")
    public R<String> method4(@RequestParam(required = false) String name, @RequestBody Parent parent) {
        return R.success(name);
    }

    @AgileLogger
    @PostMapping("/method5")
    public R<String> method5(@RequestParam(required = false) String name, @RequestBody Parent parent) {
        return R.success(mockService.method5(name, parent));
    }

    @AgileLogger
    @GetMapping("/test")
    public R<String> test() {
        return R.success("test");
    }

    @Data
    public static class Parent {

        private Integer id;
        private String name;
        private Sub sub;

        @Data
        public static class Sub {
            private Integer id;
            private String name;
        }
    }


}
