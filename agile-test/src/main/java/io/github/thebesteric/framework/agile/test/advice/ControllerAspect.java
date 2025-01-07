package io.github.thebesteric.framework.agile.test.advice;

import io.github.thebesteric.framework.agile.test.domain.Parent;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

/**
 * ControllerInterceptor
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-12-20 13:07:58
 */
@Component
@Aspect
@Order(1)
public class ControllerAspect {

    private static final String CONTROLLER_EXPR = "within(@org.springframework.stereotype.Controller *)";
    private static final String REST_CONTROLLER_EXPR = "within(@org.springframework.web.bind.annotation.RestController *)";
    private static final String OR = " || ";
    private static final String CONTROLLER_OR_REST_CONTROLLER_EXPR = CONTROLLER_EXPR + OR + REST_CONTROLLER_EXPR;

    public static final List<Class<? extends Annotation>> MAPPING_CLASSES = List.of(GetMapping.class, PostMapping.class, RequestMapping.class,
            PutMapping.class, DeleteMapping.class, PatchMapping.class);

    @Before(CONTROLLER_OR_REST_CONTROLLER_EXPR)
    public void beforeExecute(JoinPoint joinPoint) {
        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 获取方法对象
        Method method = signature.getMethod();
        // 校验方法合法性
        if (!hasRequestMappingAnnotation(method)) {
            return;
        }
        // 判断方法的参数是否包含 BaseRequest 的对象
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof Parent parent) {
                // 获取 BaseRequest 对象
                System.out.println("ControllerAspect is beforeExecute...");
                parent.setParentId("parent-id-0");
            }
        }
    }

    public boolean hasRequestMappingAnnotation(Method method) {
        return MAPPING_CLASSES.stream().anyMatch(method::isAnnotationPresent);
    }

}
