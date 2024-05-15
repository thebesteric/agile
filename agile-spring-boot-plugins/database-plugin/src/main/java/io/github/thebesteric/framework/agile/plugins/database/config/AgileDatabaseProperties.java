package io.github.thebesteric.framework.agile.plugins.database.config;

import io.github.thebesteric.framework.agile.core.AgileConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AgileDatabaseProperties
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-05-13 11:03:38
 */
@Data
@ConfigurationProperties(prefix = AgileConstants.PROPERTIES_PREFIX + ".database")
public class AgileDatabaseProperties {
    private boolean enable = true;
    private DDLAuto ddlAuto = DDLAuto.UPDATE;
    private boolean showSql = false;
    private boolean formatSql = true;
    private String tableNamePrefix;

    public enum DDLAuto {
        CREATE, UPDATE, NONE
    }
}
