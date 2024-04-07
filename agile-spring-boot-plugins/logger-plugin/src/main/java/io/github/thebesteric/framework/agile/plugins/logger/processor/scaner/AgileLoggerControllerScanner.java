package io.github.thebesteric.framework.agile.plugins.logger.processor.scaner;

import io.github.thebesteric.framework.agile.commons.util.ClassUtils;
import io.github.thebesteric.framework.agile.core.scaner.ClassPathScanner;
import io.github.thebesteric.framework.agile.plugins.logger.processor.mapping.MappingProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.List;

/**
 * AgileLoggerControllerScanner
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-04-07 21:48:35
 */
@RequiredArgsConstructor
public class AgileLoggerControllerScanner implements ClassPathScanner {

    private final List<MappingProcessor> mappingProcessors;

    @Override
    public void processClassFile(String className) {
        Class<?> clazz = ClassUtils.forName(className, false);

        // Collect the urls from the Controller
        if (clazz != null && (clazz.isAnnotationPresent(Controller.class) || clazz.isAnnotationPresent(RestController.class))) {
            RequestMapping classRequestMapping = clazz.getAnnotation(RequestMapping.class);
            String[] classRequestMappingUrls = {""};
            if (classRequestMapping != null) {
                classRequestMappingUrls = classRequestMapping.value();
                if (classRequestMappingUrls.length == 0) {
                    classRequestMappingUrls = classRequestMapping.path();
                }
            }
            for (Method method : clazz.getDeclaredMethods()) {
                for (MappingProcessor mappingProcessor : mappingProcessors) {
                    if (mappingProcessor.supports(method)) {
                        mappingProcessor.processor(classRequestMappingUrls);
                        break;
                    }
                }
            }
        }

    }
}
