package io.github.thebesteric.framework.agile.plugins.logger.domain;

import io.github.thebesteric.framework.agile.commons.util.*;
import io.github.thebesteric.framework.agile.plugins.logger.filter.warpper.AgileLoggerRequestWrapper;
import io.github.thebesteric.framework.agile.plugins.logger.filter.warpper.AgileLoggerResponseWrapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * RequestLog
 *
 * @author Eric Joe
 * @since 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RequestLog extends InvokeLog {

    /** Session ID */
    private String sessionId;

    /** URI */
    private String uri;

    /** URL */
    private String url;

    /** 请求方法 */
    private String method;

    /** 内容类型 */
    private String contentType;

    /** 请求协议 */
    private String protocol;

    /** IP */
    private String ip;

    /** 域信息 */
    private String domain;

    /** 服务器名 */
    private String serverName;

    /** 本地地址 */
    private String localAddr;

    /** 本地端口 */
    private Integer localPort;

    /** 远程地址 */
    private String remoteAddr;

    /** 远程端口 */
    private Integer remotePort;

    /** 请求参数 */
    private String query;

    /** Cookies */
    private Set<Cookie> cookies = new HashSet<>();

    /** 请求头信息 */
    private Map<String, String> headers = new HashMap<>();

    /** 请求参数 */
    private Map<String, String> params = new HashMap<>();

    /** 请求体 */
    private Object body;

    /** 原生请求体 */
    private String rawBody;

    /** 运行时长 */
    private Long duration;

    /** 响应信息 */
    private Response response;

    /** Curl */
    private String curl;

    public RequestLog() {
        super();
    }

    public RequestLog(String id, AgileLoggerRequestWrapper requestWrapper, AgileLoggerResponseWrapper responseWrapper, DurationWatcher.Duration duration) throws IOException {
        this.logId = id;
        this.trackId = TransactionUtils.get();
        this.threadName = Thread.currentThread().getName();
        this.createdAt = duration.getStartTimeToDate();
        this.duration = duration.getDurationTime();
        this.body = requestWrapper.getBody();
        this.rawBody = requestWrapper.getRawBody();
        this.result = StringUtils.toStr(responseWrapper.getByteArray());
        this.serverName = requestWrapper.getServerName();
        this.sessionId = requestWrapper.getRequestedSessionId();
        String queryString = requestWrapper.getQueryString();
        if (queryString != null) {
            this.query = URLDecoder.decode(queryString, StandardCharsets.UTF_8);
        }
        this.method = requestWrapper.getMethod();
        this.protocol = requestWrapper.getProtocol();
        this.ip = requestWrapper.getIpAddress();
        this.domain = requestWrapper.getDomain();
        this.localAddr = requestWrapper.getLocalAddr();
        this.localPort = requestWrapper.getLocalPort();
        this.remoteAddr = requestWrapper.getRemoteAddr();
        this.remotePort = requestWrapper.getRemotePort();
        this.url = requestWrapper.getUrlWithQuery();
        this.uri = requestWrapper.getRequestURI();
        this.contentType = requestWrapper.getContentType();
        this.exception = ExceptionUtils.getSimpleMessage(responseWrapper.getException());

        // params
        Enumeration<String> parameterNames = requestWrapper.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String parameterName = parameterNames.nextElement();
            String parameterValue = requestWrapper.getParameter(parameterName);
            this.getParams().put(parameterName, parameterValue);
        }

        // headers
        Enumeration<String> headerNames = requestWrapper.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = requestWrapper.getHeader(headerName);
            this.getHeaders().put(headerName, headerValue);
        }

        // cookies
        jakarta.servlet.http.Cookie[] cookies = requestWrapper.getCookies();
        if (cookies != null) {
            for (jakarta.servlet.http.Cookie cookie : cookies) {
                this.getCookies().add(new Cookie(cookie));
            }
        }

        // response
        this.response = new Response();
        this.response.setStatus(responseWrapper.getStatus());
        this.response.setContentType(responseWrapper.getContentType());
        this.response.setLocale(responseWrapper.getLocale().toString());
        Map<String, String> responseHeaders = responseWrapper.getHeaderNames().stream()
                .collect(Collectors.toMap(key -> key, responseWrapper::getHeader, (v1, v2) -> v2));
        this.response.setHeaders(responseHeaders);
    }

    @Data
    @NoArgsConstructor
    public static class Cookie {
        private String name;
        private String value;
        private String domain;
        private int maxAge = -1;
        private String path;
        private boolean secure;
        private boolean isHttpOnly = false;

        public Cookie(jakarta.servlet.http.Cookie cookie) {
            this.name = cookie.getName();
            this.value = cookie.getValue();
            this.domain = cookie.getDomain();
            this.maxAge = cookie.getMaxAge();
            this.path = cookie.getPath();
            this.secure = cookie.getSecure();
            this.isHttpOnly = cookie.isHttpOnly();
        }

    }

    @Data
    @NoArgsConstructor
    public static class Response {
        private int status;
        private String contentType;
        private String locale;
        private Map<String, String> headers = new HashMap<>();
    }

    @Override
    @SneakyThrows
    public String toString() {
        return super.toString();
    }

}
