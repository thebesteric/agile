package io.github.thebesteric.framework.agile.commons.util;

import com.google.common.base.CaseFormat;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Objects;

/**
 * StringUtils
 *
 * @author Eric Joe
 * @version 1.0
 * @since 2022-08-06 00:06:10
 */
public class StringUtils extends AbstractUtils {

    public static boolean isEmpty(String str) {
        return org.apache.commons.lang3.StringUtils.isEmpty(str) || Objects.equals(str, "\"\"");
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static String nullToBlank(String str) {
        return str == null ? "" : str;
    }

    public static String blankToNull(String cs) {
        return isEmpty(cs) ? null : cs;
    }

    public static String toLowerFirst(String str) {
        if (isNotEmpty(str)) {
            char[] cs = str.toCharArray();
            if (cs[0] >= 54 && cs[0] <= 90) {
                cs[0] += 32;
                return String.valueOf(cs);
            }
        }
        return str;
    }

    public static String toUpperFirst(String str) {
        if (isNotEmpty(str)) {
            char[] cs = str.toCharArray();
            if (cs[0] >= 97 && cs[0] <= 122) {
                cs[0] -= 32;
                return String.valueOf(cs);
            }
        }
        return str;
    }
    public static String limit(String str, int limit) {
        if (str != null && str.length() > limit) {
            str = str.substring(0, limit);
        }
        return str;
    }

    public static boolean startWith(CharSequence str, CharSequence prefix) {
        return startWith(str, prefix, true);
    }

    public static boolean startWith(CharSequence str, CharSequence prefix, boolean ignoreCase) {
        if (null != str && null != prefix) {
            return str.toString().regionMatches(ignoreCase, 0, prefix.toString(), 0, prefix.length());
        } else {
            return null == str && null == prefix;
        }
    }

    public static boolean equals(CharSequence str1, CharSequence str2) {
        return equals(str1, str2, false);
    }

    public static boolean equals(CharSequence str1, CharSequence str2, boolean ignoreCase) {
        if (null == str1) {
            return str2 == null;
        } else if (null == str2) {
            return false;
        } else {
            return ignoreCase ? str1.toString().equalsIgnoreCase(str2.toString()) : str1.toString().contentEquals(str2);
        }
    }

    public static boolean notEquals(CharSequence str1, CharSequence str2) {
        return !equals(str1, str2, false);
    }

    public static boolean notEquals(CharSequence str1, CharSequence str2, boolean ignoreCase) {
        return !equals(str1, str2, ignoreCase);
    }

    public static String toStr(Object obj) {
        return toStr(obj, CharsetUtils.CHARSET_UTF_8);
    }

    public static String toStr(Object obj, String charsetName) {
        return toStr(obj, Charset.forName(charsetName));
    }

    public static String toStr(Object obj, Charset charset) {
        if (null == obj) {
            return null;
        } else if (obj instanceof String) {
            return (String) obj;
        } else if (obj instanceof byte[]) {
            return toStr((byte[]) obj, charset);
        } else if (obj instanceof Byte[]) {
            return toStr((Byte[]) obj, charset);
        } else if (obj instanceof ByteBuffer) {
            return toStr(obj, charset);
        } else {
            return CollectionUtils.isArray(obj) ? CollectionUtils.toString(obj) : obj.toString();
        }
    }

    public static String toStr(byte[] data) {
        return toStr(data, CharsetUtils.CHARSET_UTF_8);
    }

    public static String toStr(Byte[] data) {
        return toStr(data, CharsetUtils.CHARSET_UTF_8);
    }

    public static String toStr(byte[] bytes, String charset) {
        return toStr(bytes, CharsetUtils.charset(charset));
    }

    public static String toStr(Byte[] bytes, String charset) {
        return toStr(bytes, CharsetUtils.charset(charset));
    }

    public static String toStr(byte[] data, Charset charset) {
        if (data == null) {
            return null;
        } else {
            return null == charset ? new String(data) : new String(data, charset);
        }
    }

    public static String toStr(Byte[] data, Charset charset) {
        if (data == null) {
            return null;
        } else {
            byte[] bytes = new byte[data.length];
            for (int i = 0; i < data.length; ++i) {
                Byte dataByte = data[i];
                bytes[i] = null == dataByte ? -1 : dataByte;
            }
            return toStr(bytes, charset);
        }
    }

    public static String format(String template, Object... params) {
        return formatWith(template, "{}", params);
    }

    public static String formatWith(String strPattern, String placeHolder, Object... argArray) {
        if (StringUtils.isNotEmpty(strPattern) && StringUtils.isNotEmpty(placeHolder) && CollectionUtils.isNotEmpty(argArray)) {
            int strPatternLength = strPattern.length();
            int placeHolderLength = placeHolder.length();
            StringBuilder builder = new StringBuilder(strPatternLength + 50);
            int handledPosition = 0;

            for (int argIndex = 0; argIndex < argArray.length; ++argIndex) {
                int delimIndex = strPattern.indexOf(placeHolder, handledPosition);
                if (delimIndex == -1) {
                    if (handledPosition == 0) {
                        return strPattern;
                    }

                    builder.append(strPattern, handledPosition, strPatternLength);
                    return builder.toString();
                }

                if (delimIndex > 0 && strPattern.charAt(delimIndex - 1) == '\\') {
                    if (delimIndex > 1 && strPattern.charAt(delimIndex - 2) == '\\') {
                        builder.append(strPattern, handledPosition, delimIndex - 1);
                        builder.append(StringUtils.toStr(argArray[argIndex]));
                        handledPosition = delimIndex + placeHolderLength;
                    } else {
                        --argIndex;
                        builder.append(strPattern, handledPosition, delimIndex - 1);
                        builder.append(placeHolder.charAt(0));
                        handledPosition = delimIndex + 1;
                    }
                } else {
                    builder.append(strPattern, handledPosition, delimIndex);
                    builder.append(StringUtils.toStr(argArray[argIndex]));
                    handledPosition = delimIndex + placeHolderLength;
                }
            }

            builder.append(strPattern, handledPosition, strPatternLength);
            return builder.toString();
        } else {
            return strPattern;
        }
    }

    public static String underlineToCamel(String underline) {
        return underlineToCamel(underline, true);
    }

    public static String underlineToCamel(String underline, boolean lowerCase) {
        if (isEmpty(underline)) {
            return underline;
        }
        return CaseFormat.LOWER_UNDERSCORE.to(lowerCase ? CaseFormat.LOWER_CAMEL : CaseFormat.UPPER_CAMEL, underline);
    }

    public static String camelToUnderline(String camel) {
        return camelToUnderline(camel, true);
    }

    public static String camelToUnderline(String camel, boolean lowerCase) {
        if (isEmpty(camel)) {
            return camel;
        }
        return CaseFormat.LOWER_CAMEL.to(lowerCase ? CaseFormat.LOWER_UNDERSCORE : CaseFormat.UPPER_UNDERSCORE, camel);
    }
}
