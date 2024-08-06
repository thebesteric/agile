package io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.mini_security;

import cn.hutool.http.HttpResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini.MediaCheckAsyncRequest;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini.MsgSecCheckRequest;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.BaseResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini.MediaCheckSyncResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini.MsgSecCheckResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.utils.HttpUtils;

/**
 * 小程序安全 / 内容安全
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-06 10:07:50
 */
public class ContentSecurityApi {


    /**
     * 文本内容安全识别
     *
     * @param accessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 access_token 或者 authorizer_access_token
     * @param request     请求
     *
     * @return MsgSecCheckResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/sec-center/sec-check/msgSecCheck.html">文本内容安全识别</a>
     * @author wangweijun
     * @since 2024/8/6 10:40
     */
    public MsgSecCheckResponse msgSecCheck(String accessToken, MsgSecCheckRequest request) {
        String url = String.format("https://api.weixin.qq.com/wxa/msg_sec_check?access_token=%s", accessToken);
        HttpResponse response = HttpUtils.post(url, request);
        return BaseResponse.of(response, MsgSecCheckResponse.class);
    }

    /**
     * 音视频内容安全识别
     *
     * @param accessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 access_token 或者 authorizer_access_token
     * @param request     请求
     *
     * @return MediaCheckSyncResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/sec-center/sec-check/mediaCheckAsync.html">音视频内容安全识别</a>
     * @author wangweijun
     * @since 2024/8/6 10:55
     */
    public MediaCheckSyncResponse mediaCheckAsync(String accessToken, MediaCheckAsyncRequest request) {
        String url = String.format("https://api.weixin.qq.com/wxa/media_check_async?access_token=%s", accessToken);
        HttpResponse response = HttpUtils.post(url, request);
        return BaseResponse.of(response, MediaCheckSyncResponse.class);
    }

}
