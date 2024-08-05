package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response;

import cn.hutool.http.HttpResponse;
import io.github.thebesteric.framework.agile.commons.util.JsonUtils;

import java.util.Date;

/**
 * BaseResponse
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-31 16:28:43
 */
public abstract class BaseResponse {

    protected BaseResponse() {
        super();
    }

    public static BaseResponse of(HttpResponse httpResponse) {
        return JsonUtils.toObject(httpResponse.body(), BaseResponse.class);
    }

    public static <T> T of(HttpResponse httpResponse, Class<T> clazz) {
        return JsonUtils.toObject(httpResponse.body(), clazz);
    }

    public Date secondToDate(Long seconds) {
        return new Date(seconds * 1000);
    }

}
