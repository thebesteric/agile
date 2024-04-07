package io.github.thebesteric.framework.agile.commons.util;

import io.github.thebesteric.framework.agile.commons.exception.InvalidParamsException;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * UrlUtils
 *
 * @author Eric Joe
 * @version 1.0
 */
public class UrlUtils extends AbstractUtils {

    public static String urlDecode(String str, Charset charset) {
        return str == null ? null : URLDecoder.decode(str, charset);
    }

    public static String urlDecode(String str) {
        return urlDecode(str, CharsetUtils.CHARSET_UTF_8);
    }

    public static String urlEncode(String str, Charset charset) {
        return str == null ? null : URLEncoder.encode(str, charset);
    }

    public static String urlEncode(String str) {
        return urlEncode(str, CharsetUtils.CHARSET_UTF_8);
    }

    /**
     * Merge url parameters
     * <p>
     * url: url?a=1&b=2
     * params: {c=3, d=4}
     * return url?a=1&b=2&c=3&d=4
     *
     * @param url    url
     * @param params params
     * @return String
     */
    public static String mergeUrlParams(String url, Map<String, Object> params) {
        if (params == null) {
            params = new HashMap<>();
        }
        if (url.contains("?")) {
            String[] arr = url.split("\\?");
            url = arr[0];
            String[] pairs = arr[1].split("&");
            for (String pair : pairs) {
                String[] urlParam = pair.split("=");
                params.put(urlParam[0], urlEncode(urlParam[1]));
            }
        }
        String queryString = mapToQueryString(params);
        if (StringUtils.isNotEmpty(queryString)) {
            url += "?" + queryString;
        }
        return url;
    }

    /**
     * Map to query string
     * <p>
     * params: {c=3, d=4}
     * return a=1&b=2&c=3&d=4
     *
     * @param params params
     * @return String
     */
    public static String mapToQueryString(Map<String, Object> params) {
        StringBuilder queryString = new StringBuilder();
        if (CollectionUtils.isNotEmpty(params)) {
            params.forEach((k, v) -> queryString.append(k).append("=").append(v).append("&"));
            if (queryString.indexOf("&") != -1) {
                queryString.deleteCharAt(queryString.lastIndexOf("&"));
            }
        }
        return queryString.toString();
    }

    /**
     * Query string to map
     * <p>
     * queryString: a=1&b=2
     * return {a=1, b=2}
     *
     * @param queryString string
     * @return Map
     */
    public static Map<String, Object> queryStringToMap(String queryString) {
        Map<String, Object> params = new HashMap<>();
        if (StringUtils.isNotEmpty(queryString)) {
            if (queryString.startsWith("?")) {
                queryString = queryString.substring(1);
            }
            String[] pairs = queryString.split("&");
            for (String pair : pairs) {
                int index = pair.indexOf("=");
                if (index != -1) {
                    String key = pair.substring(0, index);
                    String value = pair.substring(index + 1);
                    params.put(key, value);
                    continue;
                }
                throw new InvalidParamsException("Invalid query string: %s", queryString);
            }
        }
        return params;
    }

    private static final Pattern CHINESE_PATTERN = Pattern.compile("[\\u4e00-\\u9fa5]");

    public static String encodeChinese(String str) {
        Matcher matcher = CHINESE_PATTERN.matcher(str);
        while (matcher.find()) {
            String c = matcher.group();
            str = str.replaceAll(c, UrlUtils.urlEncode(c));
        }
        return str;
    }

}
