package io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.mini_code;

import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini.CreateQRCodeRequest;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini.GetQRCodeRequest;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini.GetUnlimitedQRCodeRequest;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini.GetQRCodeResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.utils.HttpUtils;

/**
 * 小程序码与小程序链接 / 小程序码
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-05 13:31:19
 */
public class MiniCodeApi {

    /**
     * 获取小程序码
     *
     * @param accessToken accessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 access_token 或者 authorizer_access_token
     * @param request     请求
     *
     * @return GetQRCodeResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/qrcode-link/qr-code/getQRCode.html">获取小程序码</a>
     * @author wangweijun
     * @since 2024/8/5 13:49
     */
    public GetQRCodeResponse getQRCode(String accessToken, GetQRCodeRequest request) {
        String url = String.format("https://api.weixin.qq.com/wxa/getwxacode?access_token=%s", accessToken);
        byte[] bytes = HttpUtils.postToBytes(url, request);
        GetQRCodeResponse qrCodeResponse = new GetQRCodeResponse();
        qrCodeResponse.setBuffer(bytes);
        return qrCodeResponse;
    }

    /**
     * 获取小程序码
     *
     * @param accessToken accessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 access_token 或者 authorizer_access_token
     * @param request     请求
     *
     * @return GetQRCodeResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/qrcode-link/qr-code/getUnlimitedQRCode.html">获取小程序码</a>
     * @author wangweijun
     * @since 2024/8/5 14:01
     */
    public GetQRCodeResponse getUnlimitedQRCode(String accessToken, GetUnlimitedQRCodeRequest request) {
        String url = String.format("https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=%s", accessToken);
        byte[] bytes = HttpUtils.postToBytes(url, request);
        GetQRCodeResponse qrCodeResponse = new GetQRCodeResponse();
        qrCodeResponse.setBuffer(bytes);
        return qrCodeResponse;
    }

    /**
     * 获取小程序码
     *
     * @param accessToken accessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 access_token 或者 authorizer_access_token
     * @param request     请求
     *
     * @return GetQRCodeResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/qrcode-link/qr-code/createQRCode.html">获取小程序码</a>
     * @author wangweijun
     * @since 2024/8/5 14:05
     */
    public GetQRCodeResponse createQRCode(String accessToken, CreateQRCodeRequest request) {
        String url = String.format("https://api.weixin.qq.com/cgi-bin/wxaapp/createwxaqrcode?access_token=%s", accessToken);
        byte[] bytes = HttpUtils.postToBytes(url, request);
        GetQRCodeResponse qrCodeResponse = new GetQRCodeResponse();
        qrCodeResponse.setBuffer(bytes);
        return qrCodeResponse;
    }

}
