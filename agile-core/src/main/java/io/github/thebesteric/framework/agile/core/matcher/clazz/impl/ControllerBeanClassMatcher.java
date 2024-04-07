package io.github.thebesteric.framework.agile.core.matcher.clazz.impl;

import io.github.thebesteric.framework.agile.core.matcher.clazz.ClassMatcher;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

/**
 * ControllerBeanClassMatcher
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-03-07 16:33:28
 */
public class ControllerBeanClassMatcher implements ClassMatcher {
    @Override
    public boolean matcher(Class<?> clazz) {
        return clazz.isAnnotationPresent(Controller.class) || clazz.isAnnotationPresent(RestController.class);
    }
}
