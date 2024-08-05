package io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.mini_code;

import cn.hutool.http.HttpResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini.GenerateNFCSchemeRequest;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini.GenerateSchemeRequest;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini.QuerySchemeRequest;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.BaseResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini.GenerateNFCSchemeResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini.GenerateSchemeResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini.QuerySchemeResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.utils.HttpUtils;

/**
 * 小程序码与小程序链接 / URL Scheme
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-05 14:53:00
 */
public class UrlSchemaApi {


    /**
     * 获取 NFC 的小程序 scheme
     *
     * @param accessToken accessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 access_token 或者 authorizer_access_token
     * @param request     请求
     *
     * @return GenerateNFCSchemeResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/qrcode-link/url-scheme/generateNFCScheme.html">获取 NFC 的小程序 scheme</a>
     * @author wangweijun
     * @since 2024/8/5 14:58
     */
    public GenerateNFCSchemeResponse generateNFCScheme(String accessToken, GenerateNFCSchemeRequest request) {
        String url = String.format("https://api.weixin.qq.com/wxa/generatenfcscheme?access_token=%s", accessToken);
        HttpResponse response = HttpUtils.post(url, request);
        return BaseResponse.of(response, GenerateNFCSchemeResponse.class);
    }

    /**
     * 获取加密 scheme 码
     *
     * @param accessToken accessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 access_token 或者 authorizer_access_token
     * @param request     请求
     *
     * @return GenerateSchemeResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/qrcode-link/url-scheme/generateScheme.html">获取加密 scheme 码</a>
     * @author wangweijun
     * @since 2024/8/5 15:17
     */
    public GenerateSchemeResponse generateScheme(String accessToken, GenerateSchemeRequest request) {
        String url = String.format("https://api.weixin.qq.com/wxa/generatescheme?access_token=%s", accessToken);
        HttpResponse response = HttpUtils.post(url, request);
        return BaseResponse.of(response, GenerateSchemeResponse.class);
    }


    /**
     * 查询 scheme 码
     *
     * @param accessToken accessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 access_token 或者 authorizer_access_token
     * @param request     请求
     *
     * @return QuerySchemeResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/qrcode-link/url-scheme/queryScheme.html">查询 scheme 码</a>
     * @author wangweijun
     * @since 2024/8/5 15:27
     */
    public QuerySchemeResponse queryScheme(String accessToken, QuerySchemeRequest request) {
        String url = String.format("https://api.weixin.qq.com/wxa/queryscheme?access_token=%s", accessToken);
        HttpResponse response = HttpUtils.post(url, request);
        return BaseResponse.of(response, QuerySchemeResponse.class);
    }

}
