package io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.mini_code;

import cn.hutool.http.HttpResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini.GenerateShortLinkRequest;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.BaseResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini.GenerateShortLinkResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.utils.HttpUtils;

/**
 * 小程序码与小程序链接 / Short Link
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-05 15:46:37
 */
public class ShortLinkApi {

    /**
     * 获取 ShortLink
     *
     * @param accessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 access_token 或者 authorizer_access_token
     * @param request     请求
     *
     * @return GenerateShortLinkResponse
     *
     * @author wangweijun
     * @since 2024/8/5 15:53
     */
    public GenerateShortLinkResponse generateShortLink(String accessToken, GenerateShortLinkRequest request) {
        String url = String.format("https://api.weixin.qq.com/wxa/genwxashortlink?access_token=%s", accessToken);
        HttpResponse response = HttpUtils.post(url, request);
        return BaseResponse.of(response, GenerateShortLinkResponse.class);
    }

}
