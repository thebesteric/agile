package io.github.thebesteric.framework.agile.distributed.locks.config;

import io.github.thebesteric.framework.agile.core.AgileConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AgileDistributedLocksProperties
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-22 16:12:20
 */
@Data
@ConfigurationProperties(prefix = AgileConstants.PROPERTIES_PREFIX + ".distribute-locks")
public class AgileDistributedLocksProperties {
    /** 是否启用 */
    private boolean enable = true;
    /** 异常信息 */
    private String message;
}
