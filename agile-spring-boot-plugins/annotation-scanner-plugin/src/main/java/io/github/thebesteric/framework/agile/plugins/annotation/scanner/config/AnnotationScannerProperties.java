package io.github.thebesteric.framework.agile.plugins.annotation.scanner.config;

import io.github.thebesteric.framework.agile.core.AgileConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * AnnotationScannerProperties
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-13 13:59:56
 */
@Data
@ConfigurationProperties(prefix = AgileConstants.PROPERTIES_PREFIX + ".annotation-scanner")
public class AnnotationScannerProperties {
    /** 是否启用 */
    private boolean enable = true;
    /** 需要注册的注解类的全限定名 */
    private List<String> annotationClassNames = new ArrayList<>();
}
