package io.github.thebesteric.framework.agile.commons.util;

/**
 * MessageUtils
 *
 * @author wangweijun
 * @version v1.0
 * @since 2025-01-04 17:31:48
 */
public class MessageUtils extends AbstractUtils {

    /**
     * 格式化消息
     *
     * @param message      格式化消息
     * @param replacements 替换参数
     *
     * @return String
     *
     * @author wangweijun
     * @since 2025/1/4 17:42
     */
    public static String format(String message, Object... replacements) {
        String result = message;
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

}
