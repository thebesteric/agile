package io.github.thebesteric.framework.agile.plugins.idempotent.config;

import io.github.thebesteric.framework.agile.core.AgileConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AgileIdempotentProperties
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-04-30 15:24:51
 */
@Data
@ConfigurationProperties(prefix = AgileConstants.PROPERTIES_PREFIX + ".idempotent")
public class AgileIdempotentProperties {
    private boolean enable = true;
    private String message;
}
