package io.github.thebesteric.framework.agile.wechat.third.platform.listener;

import jakarta.servlet.http.HttpServletRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * AbstractListener
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-06 17:03:41
 */
public abstract class AbstractListener {

    /**
     * 获取请求的 body 字符串
     *
     * @param request HTTP request
     *
     * @return String
     *
     * @author wangweijun
     * @since 2024/8/6 17:04
     */
    public String getRequestBodyStr(HttpServletRequest request) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

}
