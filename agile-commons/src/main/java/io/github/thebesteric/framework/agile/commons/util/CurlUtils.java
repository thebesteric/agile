package io.github.thebesteric.framework.agile.commons.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

/**
 * CurlUtils
 *
 * @author Eric Joe
 * @version 1.0
 */
public class CurlUtils  extends AbstractUtils {

    private static final String FORMAT_HEADER = "-H \"%1$s:%2$s\"";
    private static final String FORMAT_METHOD = "-X %1$s";
    private static final String FORMAT_BODY = "-d '%1$s'";
    private static final String FORMAT_URL = "\"%1$s\"";
    private static final String CONTENT_TYPE = "Content-Type";

    public static String curl(HttpServletRequest request) {

        String url = request.getRequestURL().toString();
        String method = request.getMethod();
        String contentType = request.getContentType();
        Map<String, Object> urlQuery = UrlUtils.queryStringToMap(request.getQueryString());
        Map<String, String> fromParams = request.getParameterMap().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, (entry) -> (String) Array.get(entry.getValue(), 0)));

        Map<String, String> headers = new HashMap<>(16);
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            headers.put(key, request.getHeader(key).replaceAll("\"", ""));
        }

        byte[] body = null;
        if (StringUtils.startWith(contentType, ContentType.JSON.toString())) {
            try {
                body = IOUtils.toByteArray(request.getInputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return curl(url, method, contentType, urlQuery, headers, fromParams, body);
    }

    public static String curl(Builder builder) {
        return curl(builder.url, builder.method, builder.contentType, builder.urlQuery, builder.headers, builder.fromParams, builder.body);
    }

    public static String curl(String url, String method, String contentType, Map<String, Object> urlQuery, Map<String, String> headers, Map<String, String> fromParams, byte[] body) {
        List<String> parts = new ArrayList<>();
        parts.add("curl");

        // Method
        parts.add(String.format(FORMAT_METHOD, method.toUpperCase()));

        // Url
        url = UrlUtils.mergeUrlParams(url, urlQuery);
        parts.add(String.format(FORMAT_URL, url));

        // Headers
        headers.forEach((k, v) -> {
            if (!"content-length".equalsIgnoreCase(k)) {
                parts.add(String.format(FORMAT_HEADER, k, UrlUtils.encodeChinese(v.replaceAll("\"", ""))));
            }
        });

        // ContentType
        if (StringUtils.isNotEmpty(contentType) && !headers.containsKey(CONTENT_TYPE)) {
            parts.add(String.format(FORMAT_HEADER, CONTENT_TYPE, contentType));
        }

        // application/x-www-form-urlencoded
        if (StringUtils.startWith(contentType, ContentType.FORM_URLENCODED.toString()) && CollectionUtils.isNotEmpty(fromParams)) {
            fromParams.forEach((k, v) ->
                    parts.add(StringUtils.format("--data-urlencode '{}={}'", k, UrlUtils.encodeChinese(v))));
        }

        // application/json
        if (StringUtils.startWith(contentType, ContentType.JSON.toString())) {
            String jsonStr = StringUtils.toStr(body);
            if (StringUtils.isNotEmpty(jsonStr)) {
                parts.add(String.format(FORMAT_BODY, jsonStr));
            }
        }

        return String.join(" ", parts);
    }

    @Getter
    public enum ContentType {
        FORM_URLENCODED("application/x-www-form-urlencoded"),
        MULTIPART("multipart/form-data"),
        JSON("application/json"),
        XML("application/xml"),
        TEXT_PLAIN("text/plain"),
        TEXT_XML("text/xml"),
        TEXT_HTML("text/html"),
        OCTET_STREAM("application/octet-stream");

        private final String value;

        ContentType(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

        @Override
        public String toString() {
            return this.getValue();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Getter
    public static class Builder {
        private String url;
        private String method;
        private String contentType;
        private Map<String, Object> urlQuery;
        private Map<String, String> headers;
        private Map<String, String> fromParams;
        private byte[] body;

        private Builder() {
            super();
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder contentType(String contentType) {
            this.contentType = StringUtils.blankToNull(contentType);
            return this;
        }

        public Builder urlQuery(Map<String, Object> urlQuery) {
            this.urlQuery = urlQuery;
            return this;
        }

        public Builder fromParams(Map<String, String> fromParams) {
            this.fromParams = fromParams;
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            this.headers = headers;
            if (this.contentType == null && headers != null) {
                for (String key : headers.keySet()) {
                    if ("content-type".equalsIgnoreCase(key)) {
                        this.contentType = headers.get(key);
                        break;
                    }
                }
            }
            return this;
        }

        public Builder body(byte[] body) {
            this.body = body;
            return this;
        }

        public Builder body(InputStream in) {
            this.body = IOUtils.toByteArray(in);
            return this;
        }

        public String curl() {
            if (StringUtils.isEmpty(url) || StringUtils.isEmpty(method)) {
                throw new IllegalArgumentException("url and method cannot be empty");
            }
            return CurlUtils.curl(this);
        }
    }
}
