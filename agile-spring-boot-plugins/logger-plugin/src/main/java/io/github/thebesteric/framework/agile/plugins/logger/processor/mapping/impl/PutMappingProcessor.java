package io.github.thebesteric.framework.agile.plugins.logger.processor.mapping.impl;

import io.github.thebesteric.framework.agile.commons.util.CollectionUtils;
import io.github.thebesteric.framework.agile.plugins.logger.processor.mapping.MappingProcessor;
import org.springframework.web.bind.annotation.PutMapping;

import java.lang.reflect.Method;

/**
 * PutMappingProcessor
 *
 * @author Eric Joe
 * @since 1.0
 */
public class PutMappingProcessor implements MappingProcessor {

    private Method method;

    @Override
    public boolean supports(Method method) {
        this.method = method;
        return method.isAnnotationPresent(PutMapping.class);
    }

    @Override
    public void processor(String[] classRequestMappingUrls) {
        doProcessor(classRequestMappingUrls, method, () -> {
            PutMapping annotation = method.getAnnotation(PutMapping.class);
            String[] value = annotation.value();
            if (CollectionUtils.isEmpty(value)) {
                value = annotation.path();
            }
            return value;
        });
    }
}
