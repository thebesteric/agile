package io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.mini_code;

import cn.hutool.http.HttpResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini.GenerateUrlLinkRequest;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini.QueryUrlLinkRequest;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.BaseResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini.GenerateUrlLinkResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini.QueryUrlLinkResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.utils.HttpUtils;

/**
 * 小程序码与小程序链接 / URL Link
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-05 15:29:17
 */
public class UrlLinkApi {

    /**
     * 获取加密 URLLink
     *
     * @param accessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 access_token 或者 authorizer_access_token
     * @param request     请求
     *
     * @return GenerateUrlLinkResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/qrcode-link/url-link/generateUrlLink.html">获取加密 URLLink</a>
     * @author wangweijun
     * @since 2024/8/5 15:38
     */
    public GenerateUrlLinkResponse generateUrlLink(String accessToken, GenerateUrlLinkRequest request) {
        String url = String.format("https://api.weixin.qq.com/wxa/generate_urllink?access_token=%s", accessToken);
        HttpResponse response = HttpUtils.post(url, request);
        return BaseResponse.of(response, GenerateUrlLinkResponse.class);
    }

    /**
     * 查询加密 URLLink
     *
     * @param accessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 access_token 或者 authorizer_access_token
     * @param request     请求
     *
     * @return QueryUrlLinkResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/qrcode-link/url-link/queryUrlLink.html">查询加密 URLLink</a>
     * @author wangweijun
     * @since 2024/8/5 15:44
     */
    public QueryUrlLinkResponse queryUrlLink(String accessToken, QueryUrlLinkRequest request) {
        String url = String.format("https://api.weixin.qq.com/wxa/query_urllink?access_token=%s", accessToken);
        HttpResponse response = HttpUtils.post(url, request);
        return BaseResponse.of(response, QueryUrlLinkResponse.class);
    }

}
