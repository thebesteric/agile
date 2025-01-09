package io.github.thebesteric.framework.agile.plugins.sensitive.filter.config;

import io.github.thebesteric.framework.agile.core.AgileConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * SensitiveFilterProperties
 *
 * @author wangweijun
 * @version v1.0
 * @since 2025-01-08 13:46:11
 */
@Data
@ConfigurationProperties(prefix = AgileConstants.PROPERTIES_PREFIX + ".sensitive")
public class AgileSensitiveFilterProperties {

    /** 是否启用 */
    private boolean enable = true;

    /** 占位替换符 */
    private String placeholder = "*";

    /** 敏感词文件加载类型 */
    private LoadType loadType = LoadType.TXT;

    /** 敏感词文件地址 */
    private String filePath;

    /** 特殊符号 */
    private List<Character> symbols = new ArrayList<>();

    public enum LoadType {
        JSON, TXT, OTHER
    }

}
