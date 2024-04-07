package io.github.thebesteric.framework.agile.plugins.logger.filter.warpper;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.thebesteric.framework.agile.commons.util.JsonUtils;
import io.github.thebesteric.framework.agile.commons.util.StringUtils;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * AgileLoggerRequestWrapper
 *
 * @author Eric Joe
 * @since 1.0
 */
public class AgileLoggerRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] body;
    public static final String[] IP_HEADERS = {"x-forwarded-for", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"};
    public static final String UNKNOWN = "unknown";

    public AgileLoggerRequestWrapper(HttpServletRequest request) {
        super(request);
        byte[] temp = new byte[0];
        if (canBeConvert(request)) {
            temp = getRequestBody(request);
        }
        body = temp;
    }


    private byte[] getRequestBody(HttpServletRequest request) {
        byte[] buffer = new byte[0];
        InputStream in = null;
        int len = request.getContentLength();
        if (len > 0) {
            try {
                buffer = new byte[len];
                in = request.getInputStream();
                int read = 1;
                int totalRead = 0;
                while (read > 0) {
                    read = in.read(buffer, totalRead, buffer.length - totalRead);
                    if (read > 0)
                        totalRead += read;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return buffer;
    }

    public String getRawBody() {
        if (this.body != null && this.body.length > 0) {
            return new String(this.body, StandardCharsets.UTF_8);
        }
        return null;
    }

    public Object getBody() {
        String rawBody = getRawBody();
        if (rawBody != null) {
            try {
                return JsonUtils.mapper.readValue(rawBody, Map.class);
            } catch (JsonProcessingException e) {
                return rawBody;
            }
        }
        return null;
    }

    public String getUrlWithQuery() {
        String requestURL = this.getRequestURL().toString();
        String queryString = this.getQueryString();
        return URLDecoder.decode(StringUtils.isEmpty(queryString) ? requestURL : requestURL + "?" + queryString, StandardCharsets.UTF_8);
    }

    public String getDomain() {
        String scheme = this.getScheme();
        String serverName = this.getServerName();
        int port = this.getLocalPort();
        return scheme + "://" + serverName + (port == 80 ? "" : ":" + port);
    }

    public String getIpAddress() {
        String ip = null;
        for (String ipHeader : IP_HEADERS) {
            ip = this.getHeader(ipHeader);
            if (StringUtils.isNotEmpty(ip) && UNKNOWN.equalsIgnoreCase(ip)) {
                break;
            }
        }
        return ip == null ? this.getRemoteAddr() : ip;
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    @Override
    public ServletInputStream getInputStream() {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body);
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }

            @Override
            public int read() {
                return byteArrayInputStream.read();
            }
        };
    }

    private boolean canBeConvert(HttpServletRequest request) {
        String contentType = request.getContentType();
        if (contentType != null) {
            contentType = contentType.toLowerCase();
            return !contentType.startsWith("multipart/")
                    && !contentType.startsWith("application/x-www-form-urlencoded")
                    && !contentType.startsWith("application/octet-stream");
        }
        return true;
    }
}
