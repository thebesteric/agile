package io.github.thebesteric.framework.agile.plugins.workflow.config;

import io.github.thebesteric.framework.agile.core.AgileConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AgileWorkflowProperties
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-11 21:00:30
 */
@Data
@ConfigurationProperties(prefix = AgileConstants.PROPERTIES_PREFIX + ".workflow")
public class AgileWorkflowProperties {
    private boolean enable = true;
    private DDLAuto ddlAuto = DDLAuto.CREATE;

    public enum DDLAuto {
        CREATE, UPDATE, NONE
    }
}
