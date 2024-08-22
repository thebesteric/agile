package io.github.thebesteric.framework.agile.plugins.limiter.config;

import io.github.thebesteric.framework.agile.core.AgileConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AgileRateLimiterProperties
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-05-06 17:17:51
 */
@Data
@ConfigurationProperties(prefix = AgileConstants.PROPERTIES_PREFIX + ".limiter")
public class AgileRateLimiterProperties {
    /** 是否启用 */
    private boolean enable = true;
    /** 异常信息 */
    private String message = "Limited request, please try again later";
}
