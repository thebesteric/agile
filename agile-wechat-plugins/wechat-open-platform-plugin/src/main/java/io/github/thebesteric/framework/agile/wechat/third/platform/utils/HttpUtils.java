package io.github.thebesteric.framework.agile.wechat.third.platform.utils;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import io.github.thebesteric.framework.agile.commons.util.AbstractUtils;
import io.github.thebesteric.framework.agile.commons.util.JsonUtils;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.ObjectParamRequest;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * HttpUtils
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-26 17:24:46
 */
public class HttpUtils extends AbstractUtils {

    private static final Integer READ_TIMEOUT = 5000;
    private static final Integer CONNECTION_TIMEOUT = 5000;

    public static HttpResponse post(String url) {
        HttpRequest httpRequest = createPost(url);
        return httpRequest.execute();
    }

    public static HttpResponse post(String url, Map<String, Object> body) {
        HttpRequest httpRequest = createPost(url);
        if (body != null) {
            httpRequest.body(JsonUtils.toJson(body));
        }
        return httpRequest.execute();
    }

    public static HttpResponse post(String url, ObjectParamRequest body) {
        HttpRequest httpRequest = createPost(url);
        if (body != null) {
            httpRequest.body(body.toJson());
        }
        return httpRequest.execute();
    }

    public static HttpResponse get(String url) {
        return get(url, null);
    }

    public static HttpResponse get(String url, Map<String, Object> parameters) {
        if (parameters != null && !parameters.isEmpty()) {
            String queryString = parameters.entrySet().stream()
                    .filter(entry -> entry.getValue() != null)
                    .map(entry -> entry.getKey() + "=" + URLEncoder.encode((String) entry.getValue(), StandardCharsets.UTF_8))
                    .collect(Collectors.joining("&"));
            if (url.contains("?")) {
                url += queryString;
            } else {
                url += "?" + queryString;
            }
        }
        return createGet(url).execute();
    }


    public static InputStream getToInputStream(String url) {
        return getToInputStream(url, null);
    }

    public static InputStream getToInputStream(String url, Map<String, Object> parameters) {
        HttpResponse httpResponse = get(url, parameters);
        try (httpResponse) {
            return httpResponse.bodyStream();
        }
    }

    public static byte[] getToBytes(String url) {
        return getToBytes(url, null);
    }

    public static byte[] getToBytes(String url, Map<String, Object> parameters) {
        HttpResponse httpResponse = get(url, parameters);
        try (httpResponse) {
            return httpResponse.bodyBytes();
        }
    }

    public static InputStream postToInputStream(String url) {
        return postToInputStream(url, null);
    }

    public static InputStream postToInputStream(String url, ObjectParamRequest body) {
        HttpResponse httpResponse = post(url, body);
        try (httpResponse) {
            return httpResponse.bodyStream();
        }
    }

    public static byte[] postToBytes(String url) {
        return postToBytes(url, null);
    }

    public static byte[] postToBytes(String url, ObjectParamRequest body) {
        HttpResponse httpResponse = post(url, body);
        try (httpResponse) {
            return httpResponse.bodyBytes();
        }
    }

    public static HttpRequest createPost(String url) {
        return HttpUtil.createPost(url).setReadTimeout(READ_TIMEOUT).setConnectionTimeout(CONNECTION_TIMEOUT);
    }

    public static HttpRequest createGet(String url) {
        return HttpUtil.createGet(url).setReadTimeout(READ_TIMEOUT).setConnectionTimeout(CONNECTION_TIMEOUT);
    }

}
