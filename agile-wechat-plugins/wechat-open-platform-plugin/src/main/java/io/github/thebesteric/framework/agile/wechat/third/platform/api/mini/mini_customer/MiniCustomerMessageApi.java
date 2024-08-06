package io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.mini_customer;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.constant.mini.MediaType;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini.SendCustomMessageRequest;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini.SetTypingRequest;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.BaseResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini.GetTempMediaResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini.UploadTempMediaResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.utils.HttpUtils;

import java.io.File;
import java.util.Map;

/**
 * 小程序客服 / 客服消息
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-05 16:02:08
 */
public class MiniCustomerMessageApi {

    /**
     * 获取客服消息内的临时素材
     *
     * @param accessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 access_token 或者 authorizer_access_token
     * @param mediaId     媒体文件 ID。可通过 uploadTempMedia 接口获得 media_id
     *
     * @return GetTempMediaResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/kf-mgnt/kf-message/getTempMedia.html">获取客服消息内的临时素材</a>
     * @author wangweijun
     * @since 2024/8/5 16:14
     */
    public GetTempMediaResponse getTempMedia(String accessToken, String mediaId) {
        String url = String.format("https://api.weixin.qq.com/cgi-bin/media/get?access_token=%s", accessToken);
        byte[] bytes = HttpUtils.getToBytes(url, Map.of("media_id", mediaId));
        GetTempMediaResponse response = new GetTempMediaResponse();
        response.setBuffer(bytes);
        return response;
    }

    /**
     * 下发客服当前输入状态
     *
     * @param accessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 access_token 或者 authorizer_access_token
     * @param request     请求
     *
     * @return WechatResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/kf-mgnt/kf-message/setTyping.html">下发客服当前输入状态</a>
     * @author wangweijun
     * @since 2024/8/5 16:29
     */
    public WechatResponse setTyping(String accessToken, SetTypingRequest request) {
        String url = String.format("https://api.weixin.qq.com/cgi-bin/message/custom/business/typing?access_token=%s", accessToken);
        HttpResponse response = HttpUtils.post(url, request);
        return BaseResponse.of(response, WechatResponse.class);
    }

    /**
     * 新增图片素材
     *
     * @param accessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 access_token 或者 authorizer_access_token
     * @param mediaType   媒体类型
     * @param media       媒体文件
     *
     * @return UploadTempMediaResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/kf-mgnt/kf-message/uploadTempMedia.html">新增图片素材</a>
     * @author wangweijun
     * @since 2024/8/5 16:44
     */
    public UploadTempMediaResponse uploadTempMedia(String accessToken, MediaType mediaType, File media) {
        String url = String.format("https://api.weixin.qq.com/cgi-bin/message/custom/business/typing?access_token=%s", accessToken);
        HttpRequest httpRequest = HttpUtils.createPost(url);
        httpRequest.form(mediaType.getCode(), media);
        try (HttpResponse response = httpRequest.execute()) {
            return BaseResponse.of(response, UploadTempMediaResponse.class);
        }
    }

    /**
     * 发送客服消息
     *
     * @param accessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 access_token 或者 authorizer_access_token
     * @param request     请求
     *
     * @return WechatResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/kf-mgnt/kf-message/sendCustomMessage.html">发送客服消息</a>
     * @author wangweijun
     * @since 2024/8/5 17:13
     */
    public WechatResponse sendCustomMessage(String accessToken, SendCustomMessageRequest request) {
        String url = String.format("https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=%s", accessToken);
        HttpResponse response = HttpUtils.post(url, request);
        return BaseResponse.of(response, WechatResponse.class);
    }

}
