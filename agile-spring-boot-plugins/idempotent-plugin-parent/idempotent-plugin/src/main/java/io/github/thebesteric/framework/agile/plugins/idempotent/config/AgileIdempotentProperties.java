package io.github.thebesteric.framework.agile.plugins.idempotent.config;

import io.github.thebesteric.framework.agile.core.AgileConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
    /** 是否启用 */
    private boolean enable = true;
    /** 异常信息 */
    private String message = "Repeated request, please try again later";
    /** 全局默认设置 */
    @NestedConfigurationProperty
    private GlobalSetting globalSetting = new GlobalSetting();

    @Data
    public static class GlobalSetting {
        /** 开关 */
        private boolean enable = true;
        /** 全局默认方法名前缀 */
        private Set<String> defaultMethodPrefixes = Set.of("set", "put", "add", "save", "insert", "update", "edit", "modify");
        /** 幂等的超时时间 */
        private int timeout = 500;
        /** 时间单位，默认为毫秒 */
        private TimeUnit timeUnit = TimeUnit.MILLISECONDS;
        /** 幂等前缀 */
        private String keyPrefix = "idempotent";
        /** key 分隔符 */
        private String delimiter = "|";
        /** 需要忽略的包路径 */
        private Set<String> ignoredPackages = new HashSet<>();
    }

}
