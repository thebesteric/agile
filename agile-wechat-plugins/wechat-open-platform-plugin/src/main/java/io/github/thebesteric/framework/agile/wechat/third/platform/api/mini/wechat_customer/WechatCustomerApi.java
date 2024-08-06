package io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.wechat_customer;

import cn.hutool.http.HttpResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.BaseResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini.GetKfWorkBoundResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.utils.HttpUtils;

import java.util.Map;

/**
 * 微信客服
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-06 17:18:06
 */
public class WechatCustomerApi {

    /**
     * 查询绑定情况
     *
     * @param accessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 access_token 或者 authorizer_access_token
     *
     * @return GetKfWorkBoundResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/kf-work/getKfWorkBound.html">查询绑定情况</a>
     * @author wangweijun
     * @since 2024/8/6 17:43
     */
    public GetKfWorkBoundResponse getKfWorkBound(String accessToken) {
        String url = String.format("https://api.weixin.qq.com/customservice/work/get?access_token=%s", accessToken);
        HttpResponse response = HttpUtils.get(url);
        return BaseResponse.of(response, GetKfWorkBoundResponse.class);
    }

    /**
     * 解除绑定微信客服
     *
     * @param accessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 access_token 或者 authorizer_access_token
     * @param corpId      企业 ID
     *
     * @return WechatResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/kf-work/unbindKfWork.html">解除绑定微信客服</a>
     * @author wangweijun
     * @since 2024/8/6 17:47
     */
    public WechatResponse unbindKfWork(String accessToken, String corpId) {
        String url = String.format("https://api.weixin.qq.com/customservice/work/unbind?access_token=%s", accessToken);
        HttpResponse response = HttpUtils.post(url, Map.of("corpid", corpId));
        return BaseResponse.of(response, WechatResponse.class);
    }

    /**
     * 绑定微信客服
     *
     * @param accessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 access_token 或者 authorizer_access_token
     * @param corpId      企业 ID
     *
     * @return WechatResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/kf-work/bindKfWork.html">绑定微信客服</a>
     * @author wangweijun
     * @since 2024/8/6 17:49
     */
    public WechatResponse bindKfWork(String accessToken, String corpId) {
        String url = String.format("https://api.weixin.qq.com/customservice/work/bind?access_token=%s", accessToken);
        HttpResponse response = HttpUtils.post(url, Map.of("corpid", corpId));
        return BaseResponse.of(response, WechatResponse.class);
    }


}
