package io.github.thebesteric.framework.agile.plugins.logger.processor.mapping;

import io.github.thebesteric.framework.agile.plugins.logger.filter.AgileLoggerFilter;

import java.lang.reflect.Method;
import java.util.function.Supplier;

/**
 * Process the URL Mapping
 *
 * @author Eric Joe
 * @version 1.0
 */
public interface MappingProcessor {

    boolean supports(Method method);

    void processor(String[] classRequestMappingUrl);

    default void doProcessor(String[] classRequestMappingUrls, Method method, Supplier<String[]> supplier) {
        if (classRequestMappingUrls != null) {
            for (String classRequestMappingUrl : classRequestMappingUrls) {
                String[] methodRequestMappingUrls = supplier.get();
                classRequestMappingUrl = addUrlSlashPrefix(classRequestMappingUrl);
                handlerMapping(classRequestMappingUrl, methodRequestMappingUrls, method);
            }
        }
    }

    default void handlerMapping(String classRequestMappingUrl, String[] methodRequestMappingUrls, Method method) {
        if (methodRequestMappingUrls.length == 0) {
            AgileLoggerFilter.URL_MAPPING.put(classRequestMappingUrl, method);
            if (!classRequestMappingUrl.endsWith("/")) {
                AgileLoggerFilter.URL_MAPPING.put(classRequestMappingUrl + "/", method);
            }
        } else {
            for (String methodRequestMappingUrl : methodRequestMappingUrls) {
                String url = classRequestMappingUrl + addUrlSlashPrefix(methodRequestMappingUrl);
                AgileLoggerFilter.URL_MAPPING.put(url, method);
            }
        }
    }

    default String addUrlSlashPrefix(String url) {
        if (!url.startsWith("/")) {
            url = "/" + url;
        }
        return url;
    }
}
