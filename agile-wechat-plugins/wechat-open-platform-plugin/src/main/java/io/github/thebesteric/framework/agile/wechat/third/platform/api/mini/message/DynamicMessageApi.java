package io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.message;

import cn.hutool.http.HttpResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini.SetUpdatableMsgRequest;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.BaseResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini.CreateActivityIdResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.utils.HttpUtils;

import java.util.Map;

/**
 * 消息相关 / 动态消息
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-02 13:30:46
 */
public class DynamicMessageApi {

    /**
     * 创建 activity_id
     *
     * @param accessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 access_token 或者 authorizer_access_token
     * @param openId      openId
     * @param unionId     unionId
     *
     * @return CreateActivityIdResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/mp-message-management/updatable-message/createActivityId.html">创建 activity_id</a>
     * @author wangweijun
     * @since 2024/8/5 17:21
     */
    public CreateActivityIdResponse createActivityId(String accessToken, String openId, String unionId) {
        String url = String.format("https://api.weixin.qq.com/cgi-bin/message/wxopen/activityid/create?access_token=%s", accessToken);
        Map<String, Object> params = null;
        if (openId != null && unionId != null) {
            params = Map.of("openid", openId, "unionid", unionId);
        } else if (openId != null) {
            params = Map.of("openid", openId);
        } else if (unionId != null) {
            params = Map.of("unionid", unionId);
        }
        HttpResponse response = HttpUtils.get(url, params);
        return BaseResponse.of(response, CreateActivityIdResponse.class);
    }


    /**
     * 修改动态消息
     *
     * @param accessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 access_token 或者 authorizer_access_token
     * @param request     请求
     *
     * @return WechatResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/mp-message-management/updatable-message/setUpdatableMsg.html">修改动态消息</a>
     * @author wangweijun
     * @since 2024/8/5 17:31
     */
    public WechatResponse setUpdatableMsg(String accessToken, SetUpdatableMsgRequest request) {
        String url = String.format("https://api.weixin.qq.com/cgi-bin/message/wxopen/updatablemsg/send?access_token=%s", accessToken);
        HttpResponse response = HttpUtils.post(url, request);
        return BaseResponse.of(response, WechatResponse.class);
    }

}
