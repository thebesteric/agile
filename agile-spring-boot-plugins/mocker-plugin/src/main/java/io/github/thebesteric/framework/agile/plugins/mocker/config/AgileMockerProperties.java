package io.github.thebesteric.framework.agile.plugins.mocker.config;

import io.github.thebesteric.framework.agile.core.AgileConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * AgileMockerProperties
 *
 * @author wangweijun
 * @version v1.0
 * @since 2025-01-23 17:22:54
 */
@Data
@ConfigurationProperties(prefix = AgileConstants.PROPERTIES_PREFIX + ".mocker")
public class AgileMockerProperties {

    /** 是否开启 */
    private boolean enable = true;

    /** 生效环境 */
    private List<String> envs = new ArrayList<>();

}
