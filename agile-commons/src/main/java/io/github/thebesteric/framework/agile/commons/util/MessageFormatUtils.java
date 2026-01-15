package io.github.thebesteric.framework.agile.commons.util;

import io.github.thebesteric.framework.agile.commons.domain.KeyWordArgs;
import org.apache.commons.text.StringSubstitutor;

import java.util.Map;

/**
 * MessageUtils
 *
 * @author wangweijun
 * @version v1.0
 * @since 2025-01-04 17:31:48
 */
public class MessageFormatUtils extends AbstractUtils {

    /**
     * 格式化消息
     *
     * @param template     格式化消息
     * @param replacements 替换参数
     *
     * @return String
     *
     * @author wangweijun
     * @since 2025/1/4 17:42
     */
    public static String format(String template, Object... replacements) {
        String result = template;
        int index = result.indexOf("{}");
        int replacementIndex = 0;
        if (index == -1 && replacements.length > 0) {
            for (int i = 0; i < replacements.length; i++) {
                String placeholder = "{" + i + "}";
                index = result.indexOf(placeholder);
                if (index != -1) {
                    result = result.substring(0, index) + replacements[i] + result.substring(index + placeholder.length());
                } else {
                    break;
                }
            }
        } else {
            while (index != -1 && replacementIndex < replacements.length) {
                // 将当前的 {} 替换为相应的 replacement
                result = result.substring(0, index) + replacements[replacementIndex] + result.substring(index + 2);
                replacementIndex++;
                index = result.indexOf("{}");
            }
        }
        return result;
    }

    public static String format(String template, KeyWordArgs keyWordArgs) {
        return format(template, keyWordArgs.toMap());
    }

    public static String format(String template, Map<String, Object> replacements) {
        return format(template, replacements, null, null);
    }

    public static String format(String template, Map<String, Object> replacements, String prefix, String suffix) {

        if ((prefix == null && suffix != null) || (prefix != null && suffix == null)) {
            throw new IllegalArgumentException("Prefix and suffix must be both null or not null");
        }

        StringSubstitutor substitutor;
        if (prefix == null) {
            substitutor = new StringSubstitutor(replacements);
        }
        substitutor = new StringSubstitutor(replacements, prefix, suffix);
        // 设置未找到占位符时不抛出异常
        substitutor.setEnableUndefinedVariableException(false);
        return substitutor.replace(template);
    }


}
