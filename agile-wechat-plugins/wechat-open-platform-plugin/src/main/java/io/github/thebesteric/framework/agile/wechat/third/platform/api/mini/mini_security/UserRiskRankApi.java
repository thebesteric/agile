package io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.mini_security;

import cn.hutool.http.HttpResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini.GetUserRiskRankRequest;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.BaseResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini.GetUserRiskRankResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.utils.HttpUtils;

/**
 * 小程序安全 / 安全风控
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-06 14:29:24
 */
public class UserRiskRankApi {


    /**
     * 获取用户安全等级
     *
     * @param accessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 access_token 或者 authorizer_access_token
     * @param request     请求
     *
     * @return GetUserRiskRankResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/sec-center/safety-control-capability/getUserRiskRank.html">获取用户安全等级</a>
     * @author wangweijun
     * @since 2024/8/6 14:38
     */
    public GetUserRiskRankResponse getUserRiskRank(String accessToken, GetUserRiskRankRequest request) {
        String url = String.format("https://api.weixin.qq.com/wxa/getuserriskrank?access_token=%s", accessToken);
        HttpResponse response = HttpUtils.post(url, request);
        return BaseResponse.of(response, GetUserRiskRankResponse.class);
    }

}
